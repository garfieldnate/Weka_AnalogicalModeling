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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
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
		List<List<Supracontext>> subSupras = new ArrayList<>();
		for (int i = 0; i < numLattices; i++)
			subSupras.add(taskCompletionService.take().get());
		// then combine the resulting unclassified Supracontexts into
		// classified ones;
		Lattice combiningLattice = new CombiningLattice(subSupras);
		supras = combiningLattice.getSupracontextList();
	}

	/**
	 * This fills a single heterogeneous lattice with a list of subcontexts
	 * using a given label partition.
	 * 
	 */
	class LatticeFiller implements Callable<List<Supracontext>> {
		private final SubcontextList subList;
		private final int partitionIndex;

		/**
		 * 
		 * @param subList
		 *            list of subcontexts to fill the lattice with
		 * @param partitionIndex
		 *            index of the label partition to use.
		 */
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
}
