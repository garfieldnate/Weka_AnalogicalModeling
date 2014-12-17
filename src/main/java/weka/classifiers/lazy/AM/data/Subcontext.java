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
import weka.classifiers.lazy.AM.label.Label;
import weka.core.Instance;

/**
 * Represents a subcontext, containing a list of {@link Instance Instances}
 * which belong to it, along with their shared {@link Label} and common outcome.
 * If the contained instances do not have the same outcome, then the outcome is
 * set to {@link AMUtils#NONDETERMINISTIC}.
 * 
 * @author Nathan Glenn
 */
public class Subcontext {
	private Set<Instance> data;
	private double outcome;
	private Label label;

	/**
	 * The location of this instance in {@link #index}
	 */
	private int indexLocation;

	/**
	 * Initializes the subcontext by creating a list to hold the data
	 * 
	 * @param label
	 *            Binary label of the subcontext
	 */
	public Subcontext(Label label) {
		data = new HashSet<>();
		this.label = label;
	}

	/**
	 * Adds an exemplar to the subcontext and sets the outcome accordingly. If
	 * different outcomes are present in the contained exemplars, the outcome is
	 * {@link Index#NONDETERMINISTIC}
	 * 
	 * @param e
	 */
	public void add(Instance e) {
		if (data.size() != 0) {
			if (e.classValue() != data.iterator().next().classValue())
				outcome = AMUtils.NONDETERMINISTIC;
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
	public Set<Instance> getExemplars() {
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
		if (!label.equals(otherSub.label))
			return false;
		boolean ret = data.equals(otherSub.data);
		return ret;
	}

	private final static int SEED = 37;
	private int hash = -1;

	@Override
	public int hashCode() {
		if (hash != -1)
			return hash;
		hash = SEED * label.hashCode() + data.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');

		sb.append(label);
		sb.append('|');

		// we know all of the exemplars must have the same outcome;
		// otherwise the outcome is nondeterministic
		if (outcome == AMUtils.NONDETERMINISTIC)
			sb.append(AMUtils.NONDETERMINISTIC_STRING);
		else
			sb.append(data.iterator().next()
					.stringValue(data.iterator().next().classAttribute()));
		sb.append('|');

		for (Instance instance : data) {
			sb.append(instance);
			// Instance.toString() separates attributes with commas, so we can't
			// use a comma here or it will be difficult to read
			sb.append('/');
		}
		// remove last slash
		sb.deleteCharAt(sb.length() - 1);

		sb.append(')');

		return sb.toString();
	}

	public boolean isNondeterministic() {
		return outcome == AMUtils.NONDETERMINISTIC;
	}
}