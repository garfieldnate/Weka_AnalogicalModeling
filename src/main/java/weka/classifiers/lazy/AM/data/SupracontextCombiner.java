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
	 * Combine two supracontexts into one. The new one contains the subcontexts
	 * found in both, and the pointer count is set to the product of the two
	 * pointer counts.
	 * 
	 * @param supra1
	 * @param supra2
	 * @return A new Supracontext, or null if supra1 and supra2 had no
	 *         subcontexts in common.
	 */
	public static Supracontext combine(Supracontext supra1, Supracontext supra2) {
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
		Supracontext supra = new Supracontext(combinedSubs, supra1.getCount()
				.multiply(supra2.getCount()));
		return supra;
	}

	/**
	 * Combine two supracontexts into one. The new one contains the subcontexts
	 * found in both, and the pointer count is set to the product of the two
	 * pointer counts. This method will not return a heterogeneous supracontext.
	 * 
	 * @param supra1
	 * @param supra2
	 * @return a combined supracontext, or null if supra1 and supra2 had no data
	 *         in common or if the new supracontext is heterogeneous
	 */
	public static Supracontext combineFinal(Supracontext supra1,
			Supracontext supra2) {
		Set<Subcontext> smaller;
		Set<Subcontext> larger;
		if (supra1.getData().size() > supra2.getData().size()) {
			larger = supra1.getData();
			smaller = supra2.getData();
		} else {
			larger = supra2.getData();
			smaller = supra1.getData();
		}

		Supracontext supra = new Supracontext();
		for (Subcontext sub : smaller)
			if (larger.contains(sub)) {
				supra.add(sub);
				if (supra.isHeterogeneous())
					return null;
			}
		if (!supra.hasData())
			return null;

		supra.setCount(supra1.getCount().multiply(supra2.getCount()));
		return supra;
	}
}
