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
	// TODO: store some kind of label?

	// ///DEFINITION ACCORDING TO AM 2.1
	// number representing when this supracontext was created
	private int index = -1;
	// Zero means nondeterministic; any other number is the class attribute index given by Weka
	private double outcome;
	// the contained subcontexts
	private Set<Subcontext> data;
	// the number of supracontexts sharing this list of subcontexts, or the
	// number
	// of arrows pointing to it from the supracontextual lattice
	private int count = 0;
	// pointer which makes a circular linked list out of the lists of
	// subcontext. Using a circular linked list allows optimizations that we
	// will see later.
	private Supracontext next;

	/**
	 * Creates a supracontext with no data and an index of -1; Note that outcome
	 * will be 0 by default
	 */
	public Supracontext() {
		data = new HashSet<Subcontext>();
		outcome = AMUtils.EMPTY;
		index = -1;
	}

	/**
	 * Creates a new supracontext from an old one and another exemplar,
	 * inserting the new after the old
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
		// if we are creating a Supracontext out of an empty one and a
		// subcontext
		if (!other.hasData()) {
			outcome = sub.getOutcome();
			data = new HashSet<Subcontext>(1);
			data.add(sub);
			setNext(other.getNext());
			other.setNext(this);
			return;
		}
		outcome = other.outcome;
		// count will equal 0

		data = new HashSet<Subcontext>(other.getData().size() + 1);
		data.addAll(other.getData());
		data.add(sub);
		setData(data);

		setNext(other.getNext());
		other.setNext(this);
	}

	public double getOutcome() {
		return outcome;
	}

	public void setOutcome(double d) {
		outcome = d;
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

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Increases count by one; uses this when another lattice index is assigned
	 * to this supracontext.
	 */
	public void incrementCount() {
		count++;
	}

	/**
	 * Decreases the count by one; if this reaches 0, then this supracontext
	 * should be destroyed, as nothing in the lattice points to it anymore.
	 */
	public void decrementCount() {
		count--;
	}

	/**
	 * @return an integer array containing the indices of the contained
	 *         subcontexts
	 */
	public Set<Subcontext> getData() {
		return data;
	}

	public void setData(Set<Subcontext> subs) {
		this.data = subs;
	}

	public boolean hasData() {
		return data.size() != 0;
	}

	/**
	 * 
	 * @return the number of supracontexts sharing this list of subcontexts, or
	 *         the number of arrows pointing to it from the supracontextual
	 *         lattice
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Set the number of arrows pointing to this supracontext from the
	 * supracontextual lattice.
	 * 
	 * @param c
	 */
	public void setCount(int c) {
		count = c;
	}

	/**
	 * @return True if the outcome is deterministic (the subcontext consists of
	 *         data with all the same outcome)
	 */
	public boolean isDeterministic() {
		// empty is still deterministic
		if (data == null)
			return true;
		return outcome != AMUtils.NONDETERMINISTIC;
	}

	/**
	 * @return String representation of this supracontext
	 */
	@Override
	public String toString() {
		if (data == null)
			return "[NULL]";
		if (data.isEmpty())
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

	private int hash = -1;
	private static final int SEED = 37;
	@Override
	public int hashCode() {
		if (hash != -1)
			return hash;
		int code = 0;
		for (Subcontext sub : data)
			code += SEED * code + sub.hashCode();
		hash = code;
		return code;
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
