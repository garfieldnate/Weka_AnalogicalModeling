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

package weka.classifiers.lazy.AM.lattice.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.lattice.ILattice;
import weka.classifiers.lazy.AM.lattice.Labeler;
import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.SubcontextList;
import weka.classifiers.lazy.AM.lattice.Supracontext;

/**
 * This lass manages several smaller, heterogeneous lattices.
 * 
 * @author Nathan Glenn
 * 
 */
public class DistributedLattice implements ILattice {

	/**
	 * The default number of lattices to use during distributional processing.
	 */
	private static final int NUM_LATTICES = 4;

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
	 * Supracontexts. The number of lattices used will be {@link NUM_LATTICES}.
	 * 
	 * @param subList
	 *            list of Subcontexts to add to the lattice
	 * @param labeler
	 *            The Labeler object that was used to assign labels to the
	 *            subcontexts in subList (TODO: maybe that could just be
	 *            retrieved from subList instead).
	 */
	public DistributedLattice(SubcontextList subList, Labeler labeler) {
		this(subList, labeler, NUM_LATTICES);
	}

	/**
	 * Creates a distributed lattice for creating Supracontexts. The
	 * supracontexts of smaller lattices are combined to create the final
	 * Supracontexts.
	 * 
	 * @param subList
	 *            list of Subcontexts to add to the lattice
	 * @param labeler
	 *            The Labeler object that was used to assign labels to the
	 *            subcontexts in subList (TODO: maybe that could just be
	 *            retrieved from subList instead).
	 * @numLattices The number of sub-lattices to use.
	 * @throws IllegalArgumentException
	 *             of numLattices is less than 2
	 */
	public DistributedLattice(SubcontextList subList, Labeler labeler,
			int numLattices) {
		// it would be possible to work with 1 lattice, but pointless, since
		// BasicLattice is used for that and would be much more efficient.
		if (numLattices < 2)
			throw new IllegalArgumentException(
					"numLattices should be greater than 1");
		// create masks for splitting labels
		LabelMask[] masks = LabelMask.getMasks(labeler.getCardinality(), numLattices);

		// fill heterogeneous lattices
		hlattices = new ArrayList<HeterogeneousLattice>(masks.length);
		for (int i = 0; i < masks.length; i++) {
			// TODO: spawn task for simultaneous filling
			hlattices.add(new HeterogeneousLattice(subList, masks[i]));
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
		Set<Subcontext> subIndeces;
		List<Supracontext> combinedList = new LinkedList<Supracontext>();
		for (Supracontext supra1 : supraList1) {
			for (Supracontext supra2 : supraList2) {
				subIndeces = intersectionOfSubs(supra1.getData(),
						supra2.getData());
				if (subIndeces.isEmpty())
					continue;
				supra = new Supracontext();
				supra.setData(subIndeces);
				supra.setCount(supra1.getCount() * supra2.getCount());
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
		Set<Subcontext> intersectedSubs;
		// the same supracontext may be formed via different combinations, so we
		// use this as a set (Set doesn't provide a get(Object) method);
		Map<Supracontext, Supracontext> finalSupras = new HashMap<Supracontext, Supracontext>();
		for (Supracontext supra1 : supraList1) {
			for (Supracontext supra2 : supraList2) {
				// find the intersection of subcontexts
				intersectedSubs = intersectionOfSubsRemoveHeterogeneous(
						supra1.getData(), supra2.getData());
				// continue if no non-heterogeneous supracontext could be formed
				if (intersectedSubs.isEmpty())
					continue;
				// make a new supracontext containing the combined data and a
				// combined count
				supra = new Supracontext();
				supra.setData(intersectedSubs);
				supra.setCount(supra1.getCount() * supra2.getCount());
				// add to the existing count if the same supra was formed from a
				// previous combination
				if (finalSupras.containsKey(supra)) {
					Supracontext existing = finalSupras.get(supra);
					int count = supra.getCount() + existing.getCount();
					existing.setCount(count);
				} else {
					supra.setOutcome(intersectedSubs.iterator().next()
							.getOutcome());
					finalSupras.put(supra, supra);
				}
			}
		}
		return new ArrayList<Supracontext>(finalSupras.keySet());
	}

	/**
	 * 
	 * Computes the intersection of 2 arrays of integers (which represent
	 * subcontext indices).
	 * 
	 * @param set1
	 * @param set2
	 * @return intersection of the two integer arrays
	 */
	// TODO: is the smaller/larger optimization really necessary here?
	private Set<Subcontext> intersectionOfSubs(Set<Subcontext> set1,
			Set<Subcontext> set2) {
		Set<Subcontext> smaller;
		Set<Subcontext> larger;
		if (set1.size() > set2.size()) {
			smaller = set2;
			larger = set1;
		} else {
			smaller = set1;
			larger = set2;
		}

		Set<Subcontext> set = new HashSet<>(smaller);
		set.retainAll(larger);
		return set;
	}

	/**
	 * 
	 * Computes the intersection of 2 given arrays of integers representing
	 * subcontext indices. This method returns an empty array if the
	 * intersection contains indices of subcontexts which would create a
	 * heterogeneous supracontext.
	 * 
	 * @param set1
	 * @param set2
	 * @return intersection of the two integer arrays
	 */
	private Set<Subcontext> intersectionOfSubsRemoveHeterogeneous(
			Set<Subcontext> set1, Set<Subcontext> set2) {
		Set<Subcontext> smaller;
		Set<Subcontext> larger;
		if (set1.size() > set2.size()) {
			smaller = set2;
			larger = set1;
		} else {
			smaller = set1;
			larger = set2;
		}
		Set<Subcontext> set = new HashSet<>(smaller);

		Set<Subcontext> intersection = new HashSet<>();
		// TODO: magic number?
		double outcome = 0;
		for (Subcontext sub : larger)
			// determine heterogeneity whenever we add a new subcontext
			if (set.contains(sub)) {
				// the first time we add a Subcontext, we set the current
				// outcome to its outcome, and add the Subcontext to the list
				if (intersection.size() == 0) {
					intersection.add(sub);
					outcome = sub.getOutcome();
				}
				// subsequent times, we detect heterogeneity through
				// non-determinism and outcome disagreement among Subcontexts
				else if (outcome == AMUtils.NONDETERMINISTIC
						|| outcome != sub.getOutcome()) {
					return new HashSet<Subcontext>();
				} else {
					intersection.add(sub);
				}
			}
		return intersection;
	}
}
