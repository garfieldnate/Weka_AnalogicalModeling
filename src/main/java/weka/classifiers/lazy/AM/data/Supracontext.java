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

/**
 * A supracontext is a list of subcontexts with certain commonalities in their
 * labels
 * 
 * @author Nathan Glenn
 * 
 */
public class Supracontext {
	// number representing when this supracontext was created
	private int index = -1;
	// class attribute value, or nondeterministic, heterogeneous, or
	// undetermined
	private double outcome = Double.NaN;
	// the contained subcontexts
	private Set<Subcontext> data = new HashSet<>();
	// the number of supracontexts sharing this list of subcontexts, or the
	// number of arrows pointing to it from the supracontextual lattice
	private BigInteger count = BigInteger.ZERO;
	// pointer which makes a circular linked list out of the lists of
	// subcontext. Using a circular linked list allows optimizations that we
	// will see later.
	private Supracontext next;

	/**
	 * Creates a supracontext with no data and an index of -1; Note that outcome
	 * will be {@link AMUtils#EMPTY} by default
	 */
	public Supracontext() {
		data = new HashSet<Subcontext>();
		index = -1;
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
	public Supracontext(Set<Subcontext> data, BigInteger count) {
		if (data == null)
			throw new IllegalArgumentException("data must not be null");
		if (count == null)
			throw new IllegalArgumentException("count must not be null");
		this.count = count;
		for (Subcontext sub : data)
			add(sub);
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
	public Supracontext(Supracontext other, Subcontext sub, int ind) {
		index = ind;
		data = new HashSet<>(other.getData());
		outcome = other.getOutcome();
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

	public Supracontext getNext() {
		return next;
	}

	public void setNext(Supracontext next) {
		this.next = next;
	}

	public int getIndex() {
		return index;
	}

	/**
	 * 
	 * @return the number of supracontexts sharing this list of subcontexts, or
	 *         the number of arrows pointing to it from the supracontextual
	 *         lattice
	 */
	public BigInteger getCount() {
		return count;
	}

	/**
	 * Set the count of the supra.
	 * 
	 * @param c
	 *            the count
	 * @throws IllegalArgumentException
	 *             if c is null
	 */
	public void setCount(BigInteger c) {
		if (c == null)
			throw new IllegalArgumentException("c must not be null");
		count = c;
	}

	/**
	 * Increases count by one; uses this when another lattice index is assigned
	 * to this supracontext.
	 */
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
	public void decrementCount() {
		if (count.equals(BigInteger.ZERO))
			throw new IllegalStateException(
					"Can't decrement the count of a Supracontext with zero pointers");
		count = count.subtract(BigInteger.ONE);
	}

	/**
	 * @return An unmodifiable view of the set of {@link Subcontext Subcontexts}
	 *         contained in this supracontext.
	 */
	public Set<Subcontext> getData() {
		return Collections.unmodifiableSet(data);
	}

	public boolean hasData() {
		return !data.isEmpty();
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
	 * @return String representation of this supracontext
	 */
	@Override
	public String toString() {
		if (!hasData())
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

	@Override
	public int hashCode() {
		return data.hashCode();
	}

	/**
	 * Two supracontexts are the same if they contain the exact same subcontext
	 * objects (no deep comparison of subcontexts is performed). Outcome and
	 * count are not compared.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Supracontext))
			return false;
		Supracontext otherSupra = (Supracontext) other;
		return data.equals(otherSupra.data);
	}
}
