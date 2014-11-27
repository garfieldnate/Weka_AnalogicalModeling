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
import java.util.LinkedList;
import java.util.List;

import weka.classifiers.lazy.AM.AMconstants;
import weka.core.Instance;

/**
 * Represents a subcontext, containing a list of {@link Instance Instances}
 * which belong to it, along with their shared {@link Label} and common outcome.
 * If the contained instances do not have the same outcome, then the outcome is
 * set to {@link AMconstants#NONDETERMINISTIC}.
 * 
 * This class also keeps track of all instances of Subcontext in a static index.
 * 
 * @author Nathan Glenn
 */
public class Subcontext {
	// store an index of all existing instances of Subcontext
	private static List<Subcontext> index = new ArrayList<Subcontext>();

	/**
	 * 
	 * @param indexLocation
	 *            Index of the desired Subcontext in the Subcontext index
	 * @return Subcontext contained in the index
	 */
	public static Subcontext getSubcontext(int indexLocation) {
		assert (indexLocation < index.size());
		return index.get(indexLocation);
	}

	private List<Instance> data;
	private double outcome;
	private Label label;

	/**
	 * The location of this instance in {@link #index}
	 */
	private int indexLocation;

	/**
	 * Initializes the subcontext by creating a list to hold the data
	 * 
	 * @param l
	 *            Binary label of the subcontext
	 */
	public Subcontext(Label l) {
		data = new LinkedList<>();
		label = l;

		index.add(this);
		indexLocation = index.size() - 1;
	}

	/**
	 * Adds an exemplar to the subcontext and sets the outcome accordingly. If
	 * different outcomes are present in the contained exemplars, the outcome is
	 * {@link Index#NONDETERMINISTIC}
	 * 
	 * @param e
	 */
	void add(Instance e) {
		if (data.size() != 0) {
			if (e.classValue() != data.get(0).classValue())
				outcome = AMconstants.NONDETERMINISTIC;
		} else {
			outcome = e.classValue();
		}
		data.add(e);
	}

	public double getOutcome() {
		return outcome;
	}

	/**
	 * @return Binary label of of this subcontext
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * 
	 * @return the location of this Subcontext in the static Subcontext index
	 */
	public int getIndex() {
		return indexLocation;
	}

	/**
	 * @return list of Exemplars contained in this subcontext
	 */
	public List<Instance> getExemplars() {
		return data;
	}

	/**
	 * Two Subcontexts are considered equal if they have the same label and
	 * contain the same instances.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Subcontext))
			return false;
		Subcontext otherSub = (Subcontext) other;
		if (label != otherSub.label)
			return false;
		return data.equals(otherSub.data);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');

		sb.append(label);
		sb.append('|');

		// we know all of the exemplars must have the same outcome;
		// otherwise the outcome is nondeterministic
		if (outcome == AMconstants.NONDETERMINISTIC)
			sb.append(AMconstants.NONDETERMINISTIC_STRING);
		else
			sb.append(data.get(0).stringValue(data.get(0).classAttribute()));
		sb.append('|');

		// TODO: won't work if class isn't last item
		for (int i = 0; i < data.size() - 1; i++) {
			sb.append(data.get(i));
			// Instance.toString() separates attributes with commas, so we can't
			// use a comma here or it will be difficult to read
			sb.append('/');
		}
		sb.append(data.get(data.size() - 1));

		sb.append(')');

		return sb.toString();
	}
}