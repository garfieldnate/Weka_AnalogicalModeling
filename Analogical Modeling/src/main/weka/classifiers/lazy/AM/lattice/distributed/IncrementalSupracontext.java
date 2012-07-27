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
import java.util.List;

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.Supracontext;

public class IncrementalSupracontext extends Supracontext{

	/**
	 * Subcontexts contained in this Supracontext
	 */
	List<Subcontext> subs = new ArrayList<Subcontext>();
	
	/**
	 * Outcome of the Exempars contained in this Supracontext
	 */
	int outcome;
	
	/**
	 * 
	 * @param subIndex index of the subcontext being added
	 * @return true if the Supracontext is still considered homogeneous; false otherwise
	 */
	public boolean add(int subIndex){
		Subcontext sub = Subcontext.getSubcontext(subIndex);
		// the first time we add a Subcontext, we set the current
		// outcome to its outcome, and add the Subcontext to the list
		if (subs.size() == 0) {
			subs.add(sub);
			outcome = sub.getOutcome();
			return true;
		}
		// subsequent times, we detect heterogeneity through
		// non-determinism and outcome disagreement among Subcontexts
		else if (outcome == AMconstants.NONDETERMINISTIC
				|| outcome != sub.getOutcome()) {
			return false;
		}
		//if still homogeneous, then add the Subcontext and return true.
		subs.add(sub);
		return true;
	}
}
