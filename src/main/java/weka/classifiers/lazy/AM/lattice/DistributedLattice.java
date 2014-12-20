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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.data.UnclassifiedSupra;
import weka.classifiers.lazy.AM.label.Labeler;

/**
 * This lass manages several smaller, heterogeneous lattices.
 * 
 * @author Nathan Glenn
 * 
 */
public class DistributedLattice implements Lattice {
	private final List<Supracontext> supras;

	/**
	 * Get list of Supracontexts that were created with this lattice
	 * 
	 * @return
	 */
	@Override
	public List<Supracontext> getSupracontextList() {
		return supras;
	}

	/**
	 * Creates a distributed lattice for creating Supracontexts. The
	 * supracontexts of smaller lattices are combined to create the final
	 * Supracontexts. The number of lattices is determined by
	 * {@link Labeler#numPartitions()}.
	 * 
	 * @param subList
	 *            list of Subcontexts to add to the lattice
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public DistributedLattice(SubcontextList subList)
			throws InterruptedException, ExecutionException {
		Labeler labeler = subList.getLabeler();

		ExecutorService executor = Executors.newCachedThreadPool();
		// first, create heterogeneous lattices by splitting the labels
		// contained in the subcontext list
		CompletionService<List<Supracontext>> taskCompletionService = new ExecutorCompletionService<>(
				executor);
		int numLattices = labeler.numPartitions();
		for (int i = 0; i < numLattices; i++) {
			// fill each heterogeneous lattice with a given label partition
			taskCompletionService.submit(new LatticeFiller(subList, i));
		}

		// then combine the resulting unclassified Supracontexts into classified
		// ones;
		List<Supracontext> unclassifiedSupras = taskCompletionService.take()
				.get();
		// the first combinations create more unclassified supras
		for (int i = 1; i < numLattices - 1; i++) {
			unclassifiedSupras = combine(unclassifiedSupras,
					taskCompletionService.take().get());
		}
		// the final combination creates classified supras
		supras = combineFinal(unclassifiedSupras, taskCompletionService.take()
				.get());
	}

	class LatticeFiller implements Callable<List<Supracontext>> {
		private final SubcontextList subList;
		private final int partitionIndex;

		LatticeFiller(SubcontextList subList, int partitionIndex) {
			this.subList = subList;
			this.partitionIndex = partitionIndex;
		}

		@Override
		public List<Supracontext> call() throws Exception {
			HeterogeneousLattice lattice = new HeterogeneousLattice(subList,
					partitionIndex);
			return lattice.getSupracontextList();
		}

	}

	/**
	 * Combines two lists of {@link ClassifiedSupra Supracontexts} to make a new
	 * List representing the intersection of two lattices
	 * 
	 * @param unclassifiedSupras
	 *            First list of Supracontexts
	 * @param list
	 * @return
	 */
	private List<Supracontext> combine(List<Supracontext> unclassifiedSupras,
			List<Supracontext> list) {
		UnclassifiedSupra newSupra;
		List<Supracontext> combinedList = new LinkedList<Supracontext>();
		for (Supracontext supra1 : unclassifiedSupras) {
			for (Supracontext supra2 : list) {
				newSupra = combine(supra1, supra2);
				if (newSupra != null)
					combinedList.add(newSupra);
			}
		}
		return combinedList;
	}

	/**
	 * Combine this partial supracontext with another to make a third which
	 * contains the subcontexts in common between the two, and a count which is
	 * set to the product of the two counts. Return null if the resulting object
	 * would have no subcontexts.
	 * 
	 * @param supra2
	 *            other partial supracontext to combine with
	 * @return A new partial supracontext, or null if it would have been empty.
	 */
	public UnclassifiedSupra combine(Supracontext supra1, Supracontext supra2) {
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

		if (combinedSubs.isEmpty())
			return null;
		UnclassifiedSupra supra = new UnclassifiedSupra(combinedSubs, supra1
				.getCount().multiply(supra2.getCount()));
		return supra;
	}

	/**
	 * Combines two lists of {@link ClassifiedSupra Supracontexts} to make a new
	 * List representing the intersection of two lattices; heterogeneous
	 * Supracontexts will be pruned
	 * 
	 * @param unclassifiedSupras
	 *            First list of Supracontexts
	 * @param list
	 * @return
	 */
	private List<Supracontext> combineFinal(
			List<Supracontext> unclassifiedSupras, List<Supracontext> list) {
		ClassifiedSupra supra;
		// the same supracontext may be formed via different combinations, so we
		// use this as a set (Set doesn't provide a get(Object) method);
		Map<ClassifiedSupra, ClassifiedSupra> finalSupras = new HashMap<ClassifiedSupra, ClassifiedSupra>();
		for (Supracontext supra1 : unclassifiedSupras) {
			for (Supracontext supra2 : list) {
				supra = combineFinal(supra1, supra2);
				if (supra == null)
					continue;
				// add to the existing count if the same supra was formed from a
				// previous combination
				if (finalSupras.containsKey(supra)) {
					ClassifiedSupra existing = finalSupras.get(supra);
					existing.setCount(supra.getCount().add(existing.getCount()));
				} else {
					finalSupras.put(supra, supra);
				}
			}
		}
		return new ArrayList<Supracontext>(finalSupras.values());
	}

	/**
	 * Combine this partial supracontext with another to make a
	 * {@link ClassifiedSupra} object. The new one contains the subcontexts
	 * found in both, and the pointer count is set to the product of the two
	 * pointer counts. If it turns out that the resulting supracontext would be
	 * heterogeneous or empty, then return null instead.
	 * 
	 * @param supra2
	 *            other partial supracontext to combine with
	 * @return a combined supracontext, or null if supra1 and supra2 had no data
	 *         in common or if the new supracontext is heterogeneous
	 */
	public ClassifiedSupra combineFinal(Supracontext supra1, Supracontext supra2) {
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
				if (supra.isHeterogeneous())
					return null;
			}
		if (supra.isEmpty())
			return null;

		supra.setCount(supra1.getCount().multiply(supra2.getCount()));
		return supra;
	}
}
