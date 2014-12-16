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

package weka.classifiers.lazy.AM.data;

import java.util.HashSet;
import java.util.Set;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.lattice.BasicLattice;

/**
 * The methods in this class are designed to combine supracontexts created by
 * {@Link HeterogeneousLattice} into Supracontexts equivalent to those
 * created by {@link BasicLattice}.
 * 
 * @author Nate
 * 
 */
public class SupracontextCombiner {
	/**
	 * Combine two incremental supracontexts into one. The new one contains the
	 * subcontexts found in both, and the pointer count is set to the product of
	 * the two pointer counts.
	 * 
	 * 
	 * @param supra1
	 * @param supra2
	 * @return A new Supracontext, or null if supra1 and supra2 had no
	 *         subcontexts in common.
	 */
	public static Supracontext combine(Supracontext supra1, Supracontext supra2) {
		Set<Subcontext> subIndeces;
		subIndeces = intersectionOfSubs(supra1.getData(), supra2.getData());
		if (subIndeces.isEmpty())
			return null;
		Supracontext supra = new Supracontext(subIndeces, supra1.getCount()
				.multiply(supra2.getCount()), 0);
		return supra;
	}

	/**
	 * Computes the intersection of 2 arrays of integers (which represent
	 * subcontext indices).
	 * 
	 * @param set1
	 * @param set2
	 * @return intersection of the two integer arrays
	 */
	// TODO: is the smaller/larger optimization really necessary here?
	private static Set<Subcontext> intersectionOfSubs(Set<Subcontext> set1,
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

	public static Supracontext combineFinal(Supracontext supra1,
			Supracontext supra2) {
		Set<Subcontext> intersectedSubs;
		// find the intersection of subcontexts
		intersectedSubs = intersectionOfSubsRemoveHeterogeneous(
				supra1.getData(), supra2.getData());
		// continue if no non-heterogeneous supracontext could be formed
		if (intersectedSubs.isEmpty())
			return null;
		double outcome = intersectedSubs.iterator().next().getOutcome();
		// make a new supracontext containing the combined data and a
		// combined count
		Supracontext supra = new Supracontext(intersectedSubs, supra1
				.getCount().multiply(supra2.getCount()), outcome);
		return supra;
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
	private static Set<Subcontext> intersectionOfSubsRemoveHeterogeneous(
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
