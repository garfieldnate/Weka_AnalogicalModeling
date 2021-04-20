/*
 * **************************************************************************
 * Copyright 2021 Nathan Glenn                                              *
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

import weka.classifiers.lazy.AM.data.*;
import weka.classifiers.lazy.AM.label.Labeler;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * This lass manages several smaller, heterogeneous lattices. The
 * supracontexts of smaller lattices are combined to create the final Supracontexts.
 *
 * @author Nathan Glenn
 */
public class DistributedLattice implements Lattice {
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

        ExecutorService executor = Executors.newWorkStealingPool(ForkJoinPool.getCommonPoolParallelism());
        // first, create heterogeneous lattices by splitting the labels contained in the subcontext list
        CompletionService<Set<Supracontext>> taskCompletionService = new ExecutorCompletionService<>(executor);
        int numLattices = labeler.numPartitions();
        for (int i = 0; i < numLattices; i++) {
            // fill each heterogeneous lattice with a given label partition
			final int partitionIndex = i;
			taskCompletionService.submit(() -> fillLatticePartition(subList, partitionIndex));
        }

		// then combine them 2 at a time, consolidating duplicate supracontexts
		if (numLattices > 2) {
			for (int i = 1; i < numLattices - 1; i++) {
				Set<Supracontext> supras1 = taskCompletionService.take().get();
				Set<Supracontext> supras2 = taskCompletionService.take().get();
				taskCompletionService.submit(() -> latticeProduct(
						supras1,
						supras2,
						IntermediateProduct::new));
			}
		}
        // the final combination creates ClassifiedSupras and ignores the heterogeneous ones.
        supras = latticeProduct(taskCompletionService.take().get(),
                                   taskCompletionService.take().get(),
				FinalizingProduct::new
        );
        executor.shutdownNow();
    }

	/**
	 * Fills a heterogeneous lattice with subcontexts using the given label partition index.
	 */
	private static Set<Supracontext> fillLatticePartition(SubcontextList subList, int partitionIndex) {
		HeterogeneousLattice lattice = new HeterogeneousLattice(partitionIndex);
		lattice.fill(subList);
		return lattice.getSupracontexts();
	}

    /**
     * Combines two sets of {@link Supracontext Supracontexts} to make a new
     * List representing the intersection of two lattices. The lattice-combining
     * step is partitioned and run in several threads.
     *
     * @param supraProductConstructor the constructor of the task which will produce the product of one supracontext with a set of supracontexts
     */
	private Set<Supracontext> latticeProduct(Set<Supracontext> supras1, Set<Supracontext> supras2, BiFunction<Supracontext, Set<Supracontext>, RecursiveTask<CanonicalizingSet<Supracontext>>> supraProductConstructor) {
		Collection<RecursiveTask<CanonicalizingSet<Supracontext>>> subTasks =
				supras1.stream().map(supra -> supraProductConstructor.apply(supra, supras2)).
						collect(Collectors.toList());
		Collection<RecursiveTask<CanonicalizingSet<Supracontext>>> combined =
				ForkJoinTask.invokeAll(subTasks);

		// calling join here signals to the ForkJoinPool that this task is blocking and should
		// be rescheduled
		return combined.parallelStream().map(RecursiveTask::join).
				reduce(DistributedLattice::removeDuplicateResults).
				orElse(CanonicalizingSet.emptySet());
	}

	/**
	 * Find duplicate supracontexts in {@code supras1} and {@code supras2} and return a single set of supracontexts
	 * with the combined counts from both sets.
	 */
	private static CanonicalizingSet<Supracontext> removeDuplicateResults(CanonicalizingSet<Supracontext> supras1, CanonicalizingSet<Supracontext> supras2) {
		// make sure supras2 is the smaller set of supracontexts, since we will iterate over it
		if (supras2.size() > supras1.size()) {
			CanonicalizingSet<Supracontext> temp = supras1;
			supras1 = supras2;
			supras2 = temp;
		}
		for (Supracontext supra : supras2) {
			// add to the existing count if the same supra was formed from a
			// previous combination
			supras1.merge(supra, (s1, s2) -> {
				s1.setCount(s1.getCount().add(s2.getCount()));
				return s1;
			});
		}
		return supras1;
	}

	static class IntermediateProduct extends RecursiveTask<CanonicalizingSet<Supracontext>> {
        private final Supracontext supra1;
        private final Set<Supracontext> supras2;

        IntermediateProduct(Supracontext supra1, Set<Supracontext> supras2) {
            this.supra1 = supra1;
            this.supras2 = supras2;
        }

		@Override
		protected CanonicalizingSet<Supracontext> compute() {
			BasicSupra newSupra;
			CanonicalizingSet<Supracontext> combinedSupras = new CanonicalizingSet<>();
			for (Supracontext supra2 : supras2) {
				newSupra = product(supra1, supra2);
				if (newSupra != null) {
					// add to the existing count if the same supra was formed from a
					// previous combination
					combinedSupras.merge(newSupra, (s1, s2) -> {
						s1.setCount(s1.getCount().add(s2.getCount()));
						return s1;
					});
				}
			}
			return combinedSupras;
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
        private BasicSupra product(Supracontext supra1, Supracontext supra2) {
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

    static class FinalizingProduct extends RecursiveTask<CanonicalizingSet<Supracontext>> {
        private final Supracontext supra1;
        private final Set<Supracontext> supras2;

        FinalizingProduct(Supracontext supra1, Set<Supracontext> supras2) {
            this.supra1 = supra1;
            this.supras2 = supras2;
        }

		@Override
		protected CanonicalizingSet<Supracontext> compute() {
			ClassifiedSupra supra;
			CanonicalizingSet<Supracontext> finalSupras = new CanonicalizingSet<>();
			for (Supracontext supra2 : supras2) {
				supra = product(supra1, supra2);
				if (supra == null) continue;
				// add to the existing count if the same supra was formed from a
				// previous combination
				finalSupras.merge(supra, (s1, s2) -> {
					s1.setCount(s1.getCount().add(s2.getCount()));
					return s1;
				});
			}
			return finalSupras;
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
        private ClassifiedSupra product(Supracontext supra1, Supracontext supra2) {
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
}
