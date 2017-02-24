/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package weka.classifiers.lazy.AM.lattice;

import weka.classifiers.lazy.AM.data.BasicSupra;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Labeler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This lass manages several smaller, heterogeneous lattices.
 *
 * @author Nathan Glenn
 */
public class DistributedLattice implements Lattice {
    private final Set<Supracontext> supras;

    /**
     * @return the list of homogeneous supracontexts created with this lattice
     */
    @Override
    public Set<Supracontext> getSupracontexts() {
        return supras;
    }

    /**
     * Creates a distributed lattice for creating Supracontexts. The
     * supracontexts of smaller lattices are combined to create the final
     * Supracontexts. The number of lattices is determined by
     * {@link Labeler#numPartitions()}.
     *
     * @param subList list of Subcontexts to add to the lattice
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public DistributedLattice(SubcontextList subList) throws InterruptedException, ExecutionException {
        Labeler labeler = subList.getLabeler();

        ExecutorService executor = Executors.newCachedThreadPool();
        // first, create heterogeneous lattices by splitting the labels
        // contained in the subcontext list
        CompletionService<Set<Supracontext>> taskCompletionService = new ExecutorCompletionService<>(executor);
        int numLattices = labeler.numPartitions();
        for (int i = 0; i < numLattices; i++) {
            // fill each heterogeneous lattice with a given label partition
            taskCompletionService.submit(new LatticeFiller(subList, i));
        }

        // then combine them 2 at a time, consolidating duplicate supracontexts
        if (numLattices > 2) {
            for (int i = 1; i < numLattices - 1; i++) {
                taskCompletionService.submit(new LatticeCombiner(
                    taskCompletionService.take(),
                    taskCompletionService.take()
                ));
            }
        }
        // the final combination creates ClassifiedSupras and ignores the
        // heterogeneous ones.
        supras = combineFinal(taskCompletionService.take().get(), taskCompletionService.take().get());
    }

    class LatticeFiller implements Callable<Set<Supracontext>> {
        private final SubcontextList subList;
        private final int partitionIndex;

        LatticeFiller(SubcontextList subList, int partitionIndex) {
            this.subList = subList;
            this.partitionIndex = partitionIndex;
        }

        @Override
        public Set<Supracontext> call() throws Exception {
            HeterogeneousLattice lattice = new HeterogeneousLattice(subList, partitionIndex);
            return lattice.getSupracontexts();
        }

    }

    /**
     * Combines two sets of supracontexts (heterogeneous lattices) into one new
     * set of supracontexts for a heterogeneous lattice.
     */
    class LatticeCombiner implements Callable<Set<Supracontext>> {
        final Future<Set<Supracontext>> supras1;
        final Future<Set<Supracontext>> supras2;

        LatticeCombiner(Future<Set<Supracontext>> supras1, Future<Set<Supracontext>> supras2) {
            this.supras1 = supras1;
            this.supras2 = supras2;
        }

        @Override
        public Set<Supracontext> call() throws Exception {
            return combine(supras1.get(), supras2.get());
        }
    }

    /**
     * Combines two lists of {@link ClassifiedSupra Supracontexts} to make a new
     * List representing the intersection of two lattices
     *
     * @return
     */
    private Set<Supracontext> combine(Set<Supracontext> supras1, Set<Supracontext> supras2) {
        BasicSupra newSupra;
        Map<Supracontext, Supracontext> combinedSupras = new HashMap<>();
        for (Supracontext supra1 : supras1) {
            for (Supracontext supra2 : supras2) {
                newSupra = combine(supra1, supra2);
                if (newSupra != null) {
                    if (combinedSupras.containsKey(newSupra)) {
                        Supracontext oldSupra = combinedSupras.get(newSupra);
                        oldSupra.setCount(oldSupra.getCount().add(newSupra.getCount()));
                    } else {
                        combinedSupras.put(newSupra, newSupra);
                    }
                }
            }
        }
        return combinedSupras.keySet();
    }

    /**
     * Combine this partial supracontext with another to make a third which
     * contains the subcontexts in common between the two, and a count which is
     * set to the product of the two counts. Return null if the resulting object
     * would have no subcontexts.
     *
     * @param supra1 first partial supracontext to combine
     * @param supra2 second partial supracontext to combine
     * @return A new partial supracontext, or null if it would have been empty.
     */
    private BasicSupra combine(Supracontext supra1, Supracontext supra2) {
        Set<Subcontext> smaller;
        Set<Subcontext> larger;
        if (supra1.getData().size() > supra2.getData().size()) {
            larger = supra1.getData();
            smaller = supra2.getData();
        } else {
            smaller = supra1.getData();
            larger = supra2.getData();
        }
        Set<Subcontext> combinedSubs = new HashSet<>(smaller);
        combinedSubs.retainAll(larger);

        if (combinedSubs.isEmpty()) return null;
        return new BasicSupra(combinedSubs, supra1.getCount().multiply(supra2.getCount()));
    }

    /**
     * Combines two sets of {@link Supracontext Supracontexts} to make a new
     * List representing the intersection of two lattices; heterogeneous
     * Supracontexts will be pruned
     *
     * @return
     */
    private Set<Supracontext> combineFinal(Set<Supracontext> supras1, Set<Supracontext> supras2) {
        ClassifiedSupra supra;
        // the same supracontext may be formed via different combinations, so we
        // use this as a set (Set doesn't provide a get(Object) method);
        Map<Supracontext, Supracontext> finalSupras = new HashMap<>();
        for (Supracontext supra1 : supras1) {
            for (Supracontext supra2 : supras2) {
                supra = combineFinal(supra1, supra2);
                if (supra == null) continue;
                // add to the existing count if the same supra was formed from a
                // previous combination
                if (finalSupras.containsKey(supra)) {
                    Supracontext existing = finalSupras.get(supra);
                    existing.setCount(supra.getCount().add(existing.getCount()));
                } else {
                    finalSupras.put(supra, supra);
                }
            }
        }
        return finalSupras.keySet();
    }

    /**
     * Combine this partial supracontext with another to make a
     * {@link ClassifiedSupra} object. The new one contains the subcontexts
     * found in both, and the pointer count is set to the product of the two
     * pointer counts. If it turns out that the resulting supracontext would be
     * heterogeneous or empty, then return null instead.
     *
     * @param supra1 first partial supracontext to combine
     * @param supra2 second partial supracontext to combine
     * @return a combined supracontext, or null if supra1 and supra2 had no data in common or if the new supracontext is
     * heterogeneous
     */
    private ClassifiedSupra combineFinal(Supracontext supra1, Supracontext supra2) {
        Set<Subcontext> smaller;
        Set<Subcontext> larger;
        if (supra1.getData().size() > supra2.getData().size()) {
            larger = supra1.getData();
            smaller = supra2.getData();
        } else {
            larger = supra2.getData();
            smaller = supra1.getData();
        }

        ClassifiedSupra supra = new ClassifiedSupra();
        for (Subcontext sub : smaller)
            if (larger.contains(sub)) {
                supra.add(sub);
                if (supra.isHeterogeneous()) return null;
            }
        if (supra.isEmpty()) return null;

        supra.setCount(supra1.getCount().multiply(supra2.getCount()));
        return supra;
    }
}
