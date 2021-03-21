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

import com.google.common.collect.Iterables;
import weka.classifiers.lazy.AM.data.*;
import weka.classifiers.lazy.AM.label.Labeler;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

import static weka.classifiers.lazy.AM.AMUtils.NUM_CORES;

/**
 * This lass manages several smaller, heterogeneous lattices. The
 * supracontexts of smaller lattices are combined to create the final Supracontexts.
 *
 * @author Nathan Glenn
 */
public class DistributedLattice implements Lattice {
	// Less than 3 threads and this implementation will hang forever!
	// TODO: introduce task framework that accounts for tasks adding subtasks
	private static final int MIN_THREADS = 3;
	private Set<Supracontext> supras;
	private boolean filled;

	/**
     * @return the list of homogeneous supracontexts created with this lattice
     */
    @Override
    public Set<Supracontext> getSupracontexts() {
        return supras;
    }

	public DistributedLattice(){}

    /**
	 * {@inheritDoc}
     * The number of sub-lattices is determined via {@link Labeler#numPartitions() subList.getLabeler().numPartitions()}.
     *
     * @param subList list of Subcontexts to add to the lattice
     * @throws ExecutionException If execution is rejected for some reason
     * @throws InterruptedException If any thread is interrupted for any reason (user presses ctrl-C, etc.)
     */
    @Override
	public void fill(SubcontextList subList) throws InterruptedException, ExecutionException {
		if (filled) {
			throw new IllegalStateException("Lattice is already filled and cannot be filled again.");
		}
		filled = true;
		if (subList.size() == 0) {
			return;
		}
        Labeler labeler = subList.getLabeler();

        ExecutorService executor = Executors.newWorkStealingPool(Math.max(MIN_THREADS, NUM_CORES));
        // first, create heterogeneous lattices by splitting the labels contained in the subcontext list
        CompletionService<Set<Supracontext>> taskCompletionService = new ExecutorCompletionService<>(executor);
        int numLattices = labeler.numPartitions();
        for (int i = 0; i < numLattices; i++) {
            // fill each heterogeneous lattice with a given label partition
            taskCompletionService.submit(new LatticeFiller(subList, i));
        }

        // then combine them 2 at a time, consolidating duplicate supracontexts
        if (numLattices > 2) {
            for (int i = 1; i < numLattices - 1; i++) {
                taskCompletionService.submit(new LatticeCombiner(taskCompletionService.take().get(),
                                                                 taskCompletionService.take().get(),
                                                                 executor
                ));
            }
        }
        // the final combination creates ClassifiedSupras and ignores the heterogeneous ones.
        supras = combineInParallel(taskCompletionService.take().get(),
                                   taskCompletionService.take().get(),
                                   executor,
				FinalCombiner::new
        );
        executor.shutdownNow();
    }

    /**
     * Fills a heterogeneous lattice with subcontexts.
     */
	static class LatticeFiller implements Callable<Set<Supracontext>> {
        private final SubcontextList subList;
        private final int partitionIndex;

        LatticeFiller(SubcontextList subList, int partitionIndex) {
            this.subList = subList;
            this.partitionIndex = partitionIndex;
        }

        @Override
        public Set<Supracontext> call() {
            HeterogeneousLattice lattice = new HeterogeneousLattice(partitionIndex);
            lattice.fill(subList);
            return lattice.getSupracontexts();
        }

    }

	/**
     * Combines two sets of supracontexts (heterogeneous lattices) into one new
     * set of supracontexts for a heterogeneous lattice.
     */
    class LatticeCombiner implements Callable<Set<Supracontext>> {
        final Set<Supracontext> supras1;
        final Set<Supracontext> supras2;
        final Executor executor;

        LatticeCombiner(Set<Supracontext> supras1, Set<Supracontext> supras2, Executor executor) {
            this.supras1 = supras1;
            this.supras2 = supras2;
            this.executor = executor;
        }

        @Override
        public Set<Supracontext> call() throws Exception {
            return combineInParallel(supras1,
                                     supras2,
                                     executor,
					IntermediateCombiner::new
            );
        }
    }

    /**
     * Combines two sets of {@link Supracontext Supracontexts} to make a new
     * List representing the intersection of two lattices. The lattice-combining
     * step is partitioned and run in several threads.
     *
     * @param combinerConstructor the constructor of the Callable which will combine (one partition of) the sets of
     *                            supracontexts
     */
    private Set<Supracontext> combineInParallel(Set<Supracontext> supras1, Set<Supracontext> supras2, Executor executor, BiFunction<Iterable<Supracontext>, Set<Supracontext>, Callable<Set<Supracontext>>> combinerConstructor) throws ExecutionException, InterruptedException {
        CompletionService<Set<Supracontext>> taskCompletionService = new ExecutorCompletionService<>(executor);
        Iterable<List<Supracontext>> suprasPartition = Iterables.partition(supras1, getPartitionSize(supras1));
        int numSubmitted = 0;
        for (Iterable<Supracontext> supraIter : suprasPartition) {
            taskCompletionService.submit(combinerConstructor.apply(supraIter, supras2));
            numSubmitted++;
        }
        return reduceSupraCombinations(taskCompletionService, numSubmitted);
    }

    private static int getPartitionSize(Collection<?> coll) {
        return (int) Math.ceil(coll.size() / (double) NUM_CORES);
    }

    // combine supracontext sets generated in separate threads into one set
    private Set<Supracontext> reduceSupraCombinations(CompletionService<Set<Supracontext>> taskCompletionService, int numSubmitted) throws InterruptedException, ExecutionException {
        GettableSet<Supracontext> finalSupras = new GettableSet<>();
        for (int i = 0; i < numSubmitted; i++) {
            Set<Supracontext> partialCountSupras = taskCompletionService.take().get();
            for (Supracontext supra : partialCountSupras) {
                // add to the existing count if the same supra was formed from a
                // previous combination
                if (finalSupras.contains(supra)) {
                    Supracontext existing = finalSupras.get(supra);
                    existing.setCount(supra.getCount().add(existing.getCount()));
                } else {
                    finalSupras.add(supra);
                }
            }
        }
        return finalSupras.unwrap();
    }

    static class IntermediateCombiner implements Callable<Set<Supracontext>> {
        private final Iterable<Supracontext> supras1;
        private final Set<Supracontext> supras2;

        IntermediateCombiner(Iterable<Supracontext> supras1, Set<Supracontext> supras2) {
            this.supras1 = supras1;
            this.supras2 = supras2;
        }

        @Override
        public Set<Supracontext> call() {
            BasicSupra newSupra;
            GettableSet<Supracontext> combinedSupras = new GettableSet<>();
            for (Supracontext supra1 : supras1) {
                for (Supracontext supra2 : supras2) {
                    newSupra = combine(supra1, supra2);
                    if (newSupra != null) {
                        if (combinedSupras.contains(newSupra)) {
                            Supracontext oldSupra = combinedSupras.get(newSupra);
                            oldSupra.setCount(oldSupra.getCount().add(newSupra.getCount()));
                        } else {
                            combinedSupras.add(newSupra);
                        }
                    }
                }
            }
            return combinedSupras.unwrap();
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
    }

    static class FinalCombiner implements Callable<Set<Supracontext>> {
        private final Iterable<Supracontext> supras1;
        private final Set<Supracontext> supras2;

        FinalCombiner(Iterable<Supracontext> supras1, Set<Supracontext> supras2) {
            this.supras1 = supras1;
            this.supras2 = supras2;
        }

        @Override
        public Set<Supracontext> call() {
            ClassifiedSupra supra;
            GettableSet<Supracontext> finalSupras = new GettableSet<>();
            for (Supracontext supra1 : supras1) {
                for (Supracontext supra2 : supras2) {
                    supra = combine(supra1, supra2);
                    if (supra == null) continue;
                    // add to the existing count if the same supra was formed from a
                    // previous combination
                    if (finalSupras.contains(supra)) {
                        Supracontext existing = finalSupras.get(supra);
                        existing.setCount(supra.getCount().add(existing.getCount()));
                    } else {
                        finalSupras.add(supra);
                    }
                }
            }
            return finalSupras.unwrap();
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
         * @return a combined supracontext, or null if supra1 and supra2 had no data in common or if the new
         * supracontext is heterogeneous
         */
        private ClassifiedSupra combine(Supracontext supra1, Supracontext supra2) {
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
                    if (supra.isHeterogeneous()) {
                        return null;
                    }
                }
            if (supra.isEmpty()) {
                return null;
            }
            supra.setCount(supra1.getCount().multiply(supra2.getCount()));
            return supra;
        }
    }

    /**
     * A set implementation that adds a get method (not present in Java's Set interface).
     * This is required for combining sets of supracontexts, since supracontexts
     * are equal even if their counts are different.
     */
    private static class GettableSet<T> implements Set<T> {
        private final Map<T, T> backingMap = new HashMap<>();

        /**
         * @return null if {@code t} is not contained in the set; otherwise the object contained in the set for which
         * {@code t.equals(theObject} is true.
         */
        public T get(T t) {
            return backingMap.get(t);
        }

        /**
         * @return the underlying set
         */
        public Set<T> unwrap() {
            return backingMap.keySet();
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean isEmpty() {
            return backingMap.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return backingMap.containsKey(o);
        }

        @Override
        public Iterator<T> iterator() {
            return backingMap.keySet().iterator();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(T t) {
            backingMap.put(t, t);
            return true;
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
