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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import weka.classifiers.lazy.AM.data.UnclassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.label.Labeler;

/**
 * This lass manages several smaller, heterogeneous lattices.
 * 
 * @author Nathan Glenn
 * 
 */
public class DistributedLattice implements Lattice {

	private final List<HeterogeneousLattice> hlattices;

	private final List<ClassifiedSupra> supras;

	/**
	 * Get list of Supracontexts that were created with this lattice
	 * 
	 * @return
	 */
	@Override
	public List<ClassifiedSupra> getSupracontextList() {
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
	 */
	public DistributedLattice(SubcontextList subList) {
		Labeler labeler = subList.getLabeler();

		// fill each heterogeneous lattice with a given label partition
		hlattices = new ArrayList<HeterogeneousLattice>(labeler.numPartitions());
		for (int i = 0; i < labeler.numPartitions(); i++) {
			// TODO: spawn task for simultaneous filling
			hlattices.add(new HeterogeneousLattice(subList, i));
		}

		// then combine them into one non-heterogeneous lattice; all but the
		// last combination will create another heterogeneous lattice. The last
		// combination will remove heterogeneous, non-deterministic
		// supracontexts.
		List<UnclassifiedSupra> partialSupras = hlattices.get(0)
				.getSupracontextList();
		for (int i = 1; i < hlattices.size() - 1; i++) {
			partialSupras = combine(partialSupras, hlattices.get(i)
					.getSupracontextList());
		}
		supras = combineFinal(partialSupras, hlattices
				.get(hlattices.size() - 1).getSupracontextList());
	}

	/**
	 * Combines two lists of {@link ClassifiedSupra Supracontexts} to make a new
	 * List representing the intersection of two lattices
	 * 
	 * @param partialSupras
	 *            First list of Supracontexts
	 * @param list
	 * @return
	 */
	private List<UnclassifiedSupra> combine(
			List<UnclassifiedSupra> partialSupras,
			List<UnclassifiedSupra> list) {
		UnclassifiedSupra newSupra;
		List<UnclassifiedSupra> combinedList = new LinkedList<UnclassifiedSupra>();
		for (UnclassifiedSupra supra1 : partialSupras) {
			for (UnclassifiedSupra supra2 : list) {
				newSupra = supra1.combine(supra2);
				if (newSupra != null)
					combinedList.add(newSupra);
			}
		}
		return combinedList;
	}

	/**
	 * Combines two lists of {@link ClassifiedSupra Supracontexts} to make a new
	 * List representing the intersection of two lattices; heterogeneous
	 * Supracontexts will be pruned
	 * 
	 * @param partialSupras
	 *            First list of Supracontexts
	 * @param list
	 * @return
	 */
	private List<ClassifiedSupra> combineFinal(
			List<UnclassifiedSupra> partialSupras,
			List<UnclassifiedSupra> list) {
		ClassifiedSupra supra;
		// the same supracontext may be formed via different combinations, so we
		// use this as a set (Set doesn't provide a get(Object) method);
		Map<ClassifiedSupra, ClassifiedSupra> finalSupras = new HashMap<ClassifiedSupra, ClassifiedSupra>();
		for (UnclassifiedSupra supra1 : partialSupras) {
			for (UnclassifiedSupra supra2 : list) {
				supra = supra1.combineFinalize(supra2);
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
		return new ArrayList<ClassifiedSupra>(finalSupras.values());
	}
}
