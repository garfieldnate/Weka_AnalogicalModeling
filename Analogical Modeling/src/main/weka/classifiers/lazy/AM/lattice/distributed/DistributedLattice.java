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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.lattice.LabelMask;
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
public class DistributedLattice {

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
	 * Supracontexts.
	 * 
	 * @param subList
	 *            list of Subcontexts to add to the lattice
	 */
	public DistributedLattice(SubcontextList subList) {
		LabelMask[] masks = Labeler.getMasks(AMconstants.NUM_LATTICES,
				subList.getCardinality());
		hlattices = new ArrayList<HeterogeneousLattice>(masks.length);

		for (int i = 0; i < masks.length; i++) {
			// TODO: spawn task for simultaneous filling
			hlattices.add(new HeterogeneousLattice(subList, masks[i]));
		}
		supras = hlattices.get(0).getSupracontextList();
		for (int i = 1; i < hlattices.size() - 1; i++) {
			supras = combine(supras, hlattices.get(i).getSupracontextList());
		}
		// TODO: prune out hetergenenous Supracontexts
		supras = combineFinal(supras, hlattices.get(hlattices.size() - 1)
				.getSupracontextList());
		// make them into an AnalogicalSet
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
		Supracontext temp;
		List<Supracontext> combinedList = new LinkedList<Supracontext>();
		for (Supracontext supra1 : supraList1) {
			for (Supracontext supra2 : supraList2) {
				temp = combine(supra1.getData(), supra2.getData());
				if (temp != null)
					combinedList.add(temp);
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
		Supracontext temp;
		List<Supracontext> combinedList = new LinkedList<Supracontext>();
		for (Supracontext supra1 : supraList1) {
			for (Supracontext supra2 : supraList2) {
				temp = combineFinal(supra1.getData(), supra2.getData());
				if (temp != null)
					combinedList.add(temp);
			}
		}
		return combinedList;
	}

	/**
	 * 
	 * @param list1
	 *            indeces of subcontexts
	 * @param list2
	 *            indeces of subcontexts
	 * @return Supracontext containing all of the Subcontexts whose indeces are
	 *         contained in both list1 and list2, or null if the intersection
	 *         was empty
	 */
	private Supracontext combine(int[] list1, int[] list2) {
		int[] subIndeces = intersection(list1, list2);
		if (subIndeces.length == 0)
			return null;
		Supracontext supra = new Supracontext();
		supra.setData(subIndeces);
		return supra;
	}

	/**
	 * 
	 * @param list1
	 *            indeces of subcontexts
	 * @param list2
	 *            indeces of subcontexts
	 * @return Supracontext containing all of the Subcontexts whose indeces are
	 *         contained in both list1 and list2, or null if the intersection
	 *         was empty or the Supracontext was heterogeneous.
	 */
	private Supracontext combineFinal(int[] list1, int[] list2) {
		int[] subIndeces = intersectionFinal(list1, list2);
		if (subIndeces.length == 0)
			return null;
		Supracontext supra = new Supracontext();
		supra.setData(subIndeces);
		return supra;
	}

	/**
	 * 
	 * Computes the intersection of 2 arrays of integers
	 * 
	 * @param list1
	 * @param list2
	 * @return intersection of the two integer arrays
	 */
	private int[] intersection(int[] list1, int[] list2) {
		int[] smaller;
		int[] larger;
		if (list1.length > list2.length) {
			smaller = list2;
			larger = list1;
		} else {
			smaller = list1;
			larger = list2;
		}

		Set<Integer> set = new HashSet<Integer>();
		for (Integer i : smaller)
			set.add(i);

		Set<Integer> intersection = new HashSet<Integer>();
		for (Integer i : larger)
			if (set.contains(i))
				intersection.add(i);
		Integer[] ints = intersection.toArray(new Integer[intersection.size()]);

		int[] returnVal = new int[ints.length];
		for (int i = 0; i < returnVal.length; i++)
			returnVal[i] = ints[i];
		return returnVal;
	}

	/**
	 * 
	 * Computes the intersection of 2 arrays of integers; Since the integers
	 * represent subcontexts, we use method as a final supracontext determiner
	 * which returns nothing as soon as it detects heterogeneity.
	 * 
	 * @param list1
	 * @param list2
	 * @return intersection of the two integer arrays
	 */
	private int[] intersectionFinal(int[] list1, int[] list2) {
		int[] smaller;
		int[] larger;
		if (list1.length > list2.length) {
			smaller = list2;
			larger = list1;
		} else {
			smaller = list1;
			larger = list2;
		}

		Set<Integer> set = new HashSet<Integer>();
		for (Integer i : smaller)
			set.add(i);

		Set<Integer> intersection = new HashSet<Integer>();
		double outcome = 0;
		for (Integer i : larger)
			// determine heterogeneity whenever we add a new subcontext
			if (set.contains(i)) {
				// the first time we add a Subcontext, we set the current
				// outcome to its outcome, and add the Subcontext to the list
				if (intersection.size() == 0) {
					intersection.add(i);
					outcome = Subcontext.getSubcontext(i).getOutcome();
				}
				// subsequent times, we detect heterogeneity through
				// non-determinism and outcome disagreement among Subcontexts
				else if (outcome == AMconstants.NONDETERMINISTIC
						|| outcome != Subcontext.getSubcontext(i).getOutcome()) {
					return null;
				}
			}
		Integer[] ints = intersection.toArray(new Integer[intersection.size()]);

		int[] returnVal = new int[ints.length];
		for (int i = 0; i < returnVal.length; i++)
			returnVal[i] = ints[i];
		return returnVal;
	}

}
