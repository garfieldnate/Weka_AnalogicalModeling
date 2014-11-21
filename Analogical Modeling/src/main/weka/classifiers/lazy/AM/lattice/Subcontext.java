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
 * Represents a subcontext, containing a list of Exemplars which belong to it.
 * Also keeps track of all instances of Subcontext in a static index.
 * 
 * A subcontext is a list of exemplars with the same assigned label. In
 * analogical modeling, an exemplar is classified using a list of previously
 * classified exemplars. Each previously classified exemplar is given a label by
 * comparing it to the exemplar being classified. The label is actual a vector
 * of boolean values, each representing whether the exemplars have the same
 * value for a given feature. The label is currently assigned by
 * {@link Labeler#getContextLabel}.
 * 
 * For example, if we were classifying an exemplar <a, b, c>, and we had three
 * already classified exemplars, <x, y, c>, <w, m, c> and <a, b, z>, the labels
 * would be <no, no, yes>, <no, no, yes>, and <yes, yes, no>. Two of the
 * exemplars have the same label, and so would be placed into the same
 * subcontext.
 * 
 * Each subcontext is also assigned a class based on the classification of its
 * contained exemplars. The value of this class is either the value of all of
 * its contained exemplars, or {@link AMconstants#NONDETERMINISTIC} if the
 * exemplars do not all have the same classification.
 * 
 * Underlyingly, each label is represented by an integer, with each bit
 * representing one feature. 0 represents a match, and 1 a mismatch. This allows
 * for quick processing later on via a boolean lattice (see {@link Supracontext}
 * ).
 * 
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
	private int label;

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
	public Subcontext(int l) {
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
	public int getLabel() {
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');

		sb.append(Utils.labelToString(data.get(0).numAttributes() - 1, label));
		sb.append('|');

		// we know all of the exemplars must have the same outcome;
		// otherwise the outcome is nondeterministic
		if (outcome == AMconstants.NONDETERMINISTIC)
			sb.append(AMconstants.NONDETERMINISTIC_STRING);
		else
			sb.append(data.get(0).value(data.get(0).classAttribute()));
		sb.append('|');

		for (int i = 0; i < data.size() - 1; i++) {
			sb.append(data.get(i));
			sb.append(',');
		}
		sb.append(data.get(data.size() - 1));

		sb.append(')');

		return sb.toString();
	}
}