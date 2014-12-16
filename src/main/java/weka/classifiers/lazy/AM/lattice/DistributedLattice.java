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

import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.data.SupracontextCombiner;
import weka.classifiers.lazy.AM.label.Labeler;

/**
 * This lass manages several smaller, heterogeneous lattices.
 * 
 * @author Nathan Glenn
 * 
 */
public class DistributedLattice implements Lattice {

	private List<HeterogeneousLattice> hlattices;

	private List<Supracontext> supras;

	/**
	 * Get list of Supracontexts that were created with this lattice
	 * 
	 * @return
	 */
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
		supras = hlattices.get(0).getSupracontextList();
		for (int i = 1; i < hlattices.size() - 1; i++) {
			supras = combine(supras, hlattices.get(i).getSupracontextList());
		}
		supras = combineFinal(supras, hlattices.get(hlattices.size() - 1)
				.getSupracontextList());
	}

	/**
	 * Combines two lists of {@link Supracontext Supracontexts} to make a new
	 * List representing the intersection of two lattices
	 * 
	 * @param supraList1
	 *            First list of Supracontexts
	 * @param supraList2
	 * @return
	 */
	private List<Supracontext> combine(List<Supracontext> supraList1,
			List<Supracontext> supraList2) {
		Supracontext supra;
		List<Supracontext> combinedList = new LinkedList<Supracontext>();
		for (Supracontext supra1 : supraList1) {
			for (Supracontext supra2 : supraList2) {
				supra = SupracontextCombiner.combine(supra1, supra2);
				if (supra != null)
					combinedList.add(supra);
			}
		}
		return combinedList;
	}

	/**
	 * Combines two lists of {@link Supracontext Supracontexts} to make a new
	 * List representing the intersection of two lattices; heterogeneous
	 * Supracontexts will be pruned
	 * 
	 * @param supraList1
	 *            First list of Supracontexts
	 * @param supraList2
	 * @return
	 */
	private List<Supracontext> combineFinal(List<Supracontext> supraList1,
			List<Supracontext> supraList2) {
		Supracontext supra;
		// the same supracontext may be formed via different combinations, so we
		// use this as a set (Set doesn't provide a get(Object) method);
		Map<Supracontext, Supracontext> finalSupras = new HashMap<Supracontext, Supracontext>();
		for (Supracontext supra1 : supraList1) {
			for (Supracontext supra2 : supraList2) {
				supra = SupracontextCombiner.combineFinal(supra1, supra2);
				if (supra == null)
					continue;
				// add to the existing count if the same supra was formed from a
				// previous combination
				if (finalSupras.containsKey(supra)) {
					Supracontext existing = finalSupras.get(supra);
					supra = new Supracontext(existing.getData(), supra
							.getCount().add(existing.getCount()),
							supra.getOutcome());
					finalSupras.put(supra, supra);
				} else {
					finalSupras.put(supra, supra);
				}
			}
		}
		return new ArrayList<Supracontext>(finalSupras.values());
	}
}
