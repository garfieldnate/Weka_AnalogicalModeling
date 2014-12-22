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

import java.util.Collection;
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
 * This class is immutable and does not override {@link Object#equals(Object)}
 * or {@link Object#hashCode()} to help hashSets be faster. For a test of deep
 * equality, use {@link #deepEquals(Subcontext)}.
 * 
 * @author Nathan Glenn
 */
public class Subcontext {
	private final Set<Instance> data;
	private double outcome;
	private final Label label;

	/**
	 * Initializes the subcontext with the given label and
	 * 
	 * @param label
	 *            Label shared by the instances in the subcontext.
	 * @param data
	 *            Instances in the subcontext.
	 */
	public Subcontext(Label label, Collection<Instance> data) {
		this.data = new HashSet<>();
		for (Instance instance : data)
			add(instance);
		this.label = label;
	}

	private void add(Instance e) {
		if (data.size() != 0) {
			if (e.classValue() != data.iterator().next().classValue())
				outcome = AMUtils.NONDETERMINISTIC;
		} else {
			outcome = e.classValue();
		}
		data.add(e);
	}

	/**
	 * @return The index for the outcome shared by the instances in this
	 *         subcontext, or {@link AMUtils#NONDETERMINISTIC} if they do not
	 *         share a common outcome.
	 */
	public double getOutcome() {
		return outcome;
	}

	/**
	 * @return True if the the subcontext is nondeterministic (has more than one
	 *         outcome), false otherwise.
	 */
	public boolean isNondeterministic() {
		return outcome == AMUtils.NONDETERMINISTIC;
	}

	/**
	 * @return Label of this subcontext
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * @return list of Exemplars contained in this subcontext
	 */
	public Set<Instance> getExemplars() {
		return data;
	}

	/**
	 * Performs a deep equality test with another subcontext. This is meant
	 * primarily for testing purposes.
	 * 
	 * @return True if both subcontexts have an identical label and contain the
	 *         same instances. Note that Weka Instance objects do not override
	 *         {@link Object#equals(Object)}, so the subs need to contain the
	 *         exact same instance objects.
	 */
	public boolean deepEquals(Subcontext otherSub) {
		if (!label.equals(otherSub.label))
			return false;
		boolean ret = data.equals(otherSub.data);
		return ret;
	}

	@Override
	public boolean equals(Object other) {
		return this == other;
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
}