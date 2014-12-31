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

import java.math.BigInteger;
import java.util.Set;

import weka.classifiers.lazy.AM.AMUtils;

/**
 * This supracontext is called "classified" because it keeps track of its
 * outcome at all times by inspecting the outcomes of the subcontexts added to
 * it. It also provides special methods for determining it's heterogeneity, and
 * for determining if the addition of certain subcontext would lead to
 * heterogeneity.
 * 
 * @author Nathan Glenn
 * 
 */
public class ClassifiedSupra implements Supracontext {
	Supracontext supra;
	// class attribute value, or nondeterministic, heterogeneous, or
	// undetermined
	private double outcome = Double.NaN;

	/**
	 * Creates a supracontext with no data and an index of -1; Note that outcome
	 * will be {@link AMUtils#EMPTY} by default
	 */
	public ClassifiedSupra() {
		supra = new BasicSupra();
	}

	/**
	 * Creates a new supracontext with the given parameters as the contents.
	 * 
	 * @param data
	 *            The subcontexts contained in the supracontext
	 * @param count
	 *            The count of this supracontext
	 * @throws IllegalArgumentException
	 *             if data or count are null
	 */
	public ClassifiedSupra(Set<Subcontext> data, BigInteger count) {
		if (data == null)
			throw new IllegalArgumentException("data must not be null");
		if (count == null)
			throw new IllegalArgumentException("count must not be null");
		supra = new BasicSupra();
		for (Subcontext sub : data)
			add(sub);
		supra.setCount(count);
	}

	/**
	 * Add a subcontext to the supracontext and determine the outcome.
	 * 
	 * @param sub
	 *            Subcontext to add to the supracontext.
	 */
	@Override
	public void add(Subcontext sub) {
		if (supra.isEmpty())
			outcome = sub.getOutcome();
		else if (!isHeterogeneous() && wouldBeHetero(sub))
			outcome = AMUtils.HETEROGENEOUS;
		supra.add(sub);
	}

	/**
	 * Get the outcome of this supracontext. This is either a double
	 * corresponding to the class value index given by Weka, or it is
	 * {@link AMUtils#HETEROGENEOUS} or {@link AMUtils#NONDETERMINISTIC}
	 * 
	 * @return
	 */
	public double getOutcome() {
		return outcome;
	}

	/**
	 * Determine if the supracontext is heterogeneous
	 * 
	 * @return true if the supracontext is heterogeneous, false if it is
	 *         homogeneous.
	 */
	public boolean isHeterogeneous() {
		return outcome == AMUtils.HETEROGENEOUS;
	}

	/**
	 * Test if adding a subcontext would cause the supracontext to become
	 * heterogeneous.
	 * 
	 * @param sub
	 *            subcontext to hypothetically add
	 * @return true if adding the given subcontext would cause this supracontext
	 *         to become heterogeneous.
	 */
	// TODO: I don't really like this
	public boolean wouldBeHetero(Subcontext sub) {
		// Heterogeneous if:
		// there are subcontexts with different outcomes
		// there are more than one sub which are non-deterministic
		if (supra.isEmpty()) {
			return false;
		}
		if (sub.getOutcome() != outcome) {
			return true;
		} else if (sub.getOutcome() == AMUtils.NONDETERMINISTIC) {
			return true;
		}
		return false;
	}

	@Override
	public ClassifiedSupra copy() {
		ClassifiedSupra newSupra = new ClassifiedSupra();
		newSupra.supra = supra.copy();
		newSupra.outcome = outcome;
		return newSupra;
	}

	// methods below are simply forwarded to the wrapped supracontext

	@Override
	public Set<Subcontext> getData() {
		return supra.getData();
	}

	@Override
	public boolean isEmpty() {
		return supra.isEmpty();
	}

	@Override
	public BigInteger getCount() {
		return supra.getCount();
	}

	@Override
	public void setCount(BigInteger count) {
		supra.setCount(count);
	}

	@Override
	public boolean equals(Object other) {
		return supra.equals(other);
	}

	@Override
	public int hashCode() {
		return supra.hashCode();
	}

	@Override
	public String toString() {
		return supra.toString();
	}
}
