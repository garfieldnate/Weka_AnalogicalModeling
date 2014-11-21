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

import weka.classifiers.lazy.AM.AMconstants;

public class Supracontext {
	// ///DEFINITION ACCORDING TO AM 2.1
	// number representing when this supracontext was created
	private int index = -1;
	// Zero means nondeterministic
	private double outcome;
	// an array listing the indeces of the contained subcontexts
	private int[] data;
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
		data = new int[0];
		outcome = AMconstants.EMPTY;
		index = -1;
	}

	/**
	 * Creates a new supracontext from an old one and another exemplar,
	 * inserting the new after the old
	 * 
	 * @param other
	 *            Supracontext to place this one after
	 * @param sub
	 *            Exemplar to insert in the new Supracontext
	 * @param ind
	 *            index of new Supracontext
	 */
	public Supracontext(Supracontext other, Subcontext sub, int ind) {
		index = ind;
		// if we are creating a Supracontext out of an empty one and a
		// subcontext
		if (!other.hasData()) {
			outcome = sub.getOutcome();
			data = new int[1];
			data[0] = sub.getIndex();
			setNext(other.getNext());
			other.setNext(this);
			return;
		}
		outcome = other.outcome;
		// count will equal 0

		int[] otherData = other.getData();
		int size = otherData.length;
		data = new int[size + 1];
		for (int i = 0; i < size; i++)
			data[i] = otherData[i];
		data[size] = sub.getIndex();
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

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

	/**
	 * Remove all pointers to this Supracontext by setting count to zero and
	 * destroying its data.
	 */
	public void removePointers() {
		System.err.println(this + " has been declared heterogeneous!");
		count = 0;
		data = null;
	}

	public boolean hasData() {
		// System.err.println(data);
		return data.length != 0;
	}

	/**
	 * 
	 * @return the number of supracontexts sharing this list of subcontexts, or the number
		of arrows pointing to it from the supracontextual lattice
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return True if the outcome is deterministic (the subcontext consists of
	 *         data with all the same outcome)
	 */
	public boolean isDeterministic() {
		// empty is still deterministic
		if (data == null)
			return true;
		return outcome != AMconstants.NONDETERMINISTIC;
	}

	/**
	 * @return String representation of this supracontext
	 */
	@Override
	public String toString() {
		if (data == null)
			return "[NULL]";
		if (data.length == 0)
			return "[EMPTY]";
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(count);
		sb.append('x');
		for (int index : data) {
			sb.append(Subcontext.getSubcontext(index));
			sb.append(',');
		}
		sb.append(']');
		return sb.toString();
	}

}
