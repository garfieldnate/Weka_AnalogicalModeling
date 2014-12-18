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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.lattice.LatticeNode;

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
public class ClassifiedSupra extends Supracontext implements
		LatticeNode<ClassifiedSupra> {
	// number representing when this supracontext was created
	private final int index;
	// class attribute value, or nondeterministic, heterogeneous, or
	// undetermined
	private double outcome = Double.NaN;
	// the contained subcontexts
	private Set<Subcontext> data = new HashSet<>();
	// the number of supracontexts sharing this list of subcontexts, or the
	// number of arrows pointing to it from the supracontextual lattice
	private BigInteger count;
	// pointer which makes a circular linked list out of the lists of
	// subcontext. Using a circular linked list allows optimizations that we
	// will see later.
	private ClassifiedSupra next;

	/**
	 * Creates a supracontext with no data and an index of -1; Note that outcome
	 * will be {@link AMUtils#EMPTY} by default
	 */
	public ClassifiedSupra() {
		data = new HashSet<Subcontext>();
		index = -1;
		count = BigInteger.ONE;
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
		this.count = count;
		for (Subcontext sub : data)
			add(sub);
		index = -1;
	}

	/**
	 * Creates a new supracontext from an old one and another exemplar,
	 * inserting the new after the old. Assumes that the addition of the new
	 * subcontext does not make the supracontext heterogeneous.
	 * 
	 * @param other
	 *            Supracontext to place this one after
	 * @param sub
	 *            Subcontext to insert in the new Supracontext
	 * @param ind
	 *            index of new Supracontext
	 */
	public ClassifiedSupra(ClassifiedSupra other, Subcontext sub, int ind) {
		index = ind;
		data = new HashSet<>(other.getData());
		outcome = other.getOutcome();
		count = BigInteger.ONE;
		add(sub);
		setNext(other.getNext());
		other.setNext(this);
	}

	/**
	 * Add a subcontext to the supracontext and determine the outcome.
	 * 
	 * @param sub
	 *            Subcontext to add to the supracontext.
	 */
	public void add(Subcontext sub) {
		if (data.isEmpty())
			outcome = sub.getOutcome();
		else if (!isHeterogeneous() && wouldBeHetero(sub))
			outcome = AMUtils.HETEROGENEOUS;
		data.add(sub);
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

	@Override
	public ClassifiedSupra getNext() {
		return next;
	}

	@Override
	public void setNext(ClassifiedSupra next) {
		this.next = next;
	}

	@Override
	public int getIndex() {
		return index;
	}

	/**
	 * 
	 * @return the number of supracontexts sharing this list of subcontexts, or
	 *         the number of arrows pointing to it from the supracontextual
	 *         lattice
	 */
	@Override
	public BigInteger getCount() {
		return count;
	}

	/**
	 * Set the count of the supra.
	 * 
	 * @param count
	 *            the count
	 * @throws IllegalArgumentException
	 *             if c is null
	 */
	@Override
	public void setCount(BigInteger count) {
		if (count == null)
			throw new IllegalArgumentException("count must not be null");
		if (count.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException(
					"count must not be less than zero");
		this.count = count;
	}

	/**
	 * Increases count by one; uses this when another lattice index is assigned
	 * to this supracontext.
	 */
	@Override
	public void incrementCount() {
		count = count.add(BigInteger.ONE);
	}

	/**
	 * Decreases the count by one; if this reaches 0, then this Supracontext
	 * should be destroyed (by the caller), as nothing in the lattice points to
	 * it anymore.
	 * 
	 * @throws IllegalStateException
	 *             if the count is already zero.
	 */
	@Override
	public void decrementCount() {
		if (count.equals(BigInteger.ZERO))
			throw new IllegalStateException("Count cannot be less than zero");
		count = count.subtract(BigInteger.ONE);
	}

	/**
	 * @return An unmodifiable view of the set of {@link Subcontext Subcontexts}
	 *         contained in this supracontext.
	 */
	@Override
	public Set<Subcontext> getData() {
		return Collections.unmodifiableSet(data);
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
		if (data.size() == 0) {
			return false;
		}
		if (sub.getOutcome() != outcome) {
			return true;
		} else if (sub.getOutcome() == AMUtils.NONDETERMINISTIC) {
			return true;
		}
		return false;
	}

	/**
	 * @return String representation of this supracontext in this form: "["
	 *         count "x" sub1.toString() "," sub2.toString() ... "]"
	 */
	@Override
	public String toString() {
		if (isEmpty())
			return "[EMPTY]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(count);
		sb.append('x');
		for (Subcontext sub : data) {
			sb.append(sub);
			sb.append(',');
		}
		// remove last commas
		sb.deleteCharAt(sb.length() - 1);
		sb.append(']');
		return sb.toString();
	}
}
