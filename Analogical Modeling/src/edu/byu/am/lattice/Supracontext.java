/*
 * 	Analogical Modeling Java module
 *  Copyright (C) 2011  Nathan Glenn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.byu.am.lattice;

import edu.byu.am.data.Index;


public class Supracontext{
	/////DEFINITION ACCORDING TO AM 2.1
	//number representing when this supracontext was created
	private int index = -1;
	//Zero means nondeterministic
	private int outcome;
	//an array listing the subcontexts; data[0] is the number of subcontexts, and
	//	data[1]...data[data[0]] contains the indeces of the subcontexts.
	private Subcontext[] data;
	//the number of supracontexts sharing this list of subcontexts, or the number
	//	of arrows pointing to it from the supracontextual lattice
	private int count = 0;
	//pointer which makes a circular linked list out of the lists of subcontext. Using a circular linked list allows optimizations that we will see later.
	private Supracontext next;
	
	/**
	 * Creates a supracontext with no data and an index of -1;
	 * Note that outcome will be 0 by default
	 */
	public Supracontext(){
		data = new Subcontext[0];
		outcome = Index.EMPTY;
		index = -1;
	}
	
	/**
	 * Creates a new supracontext from an old one and another exemplar, inserting the new after the old
	 * @param other Supracontext to place this one after
	 * @param sub Exemplar to insert in the new Supracontext
	 * @param ind index of new Supracontext
	 */
	public Supracontext(Supracontext other, Subcontext sub, int ind){
		index = ind;
		//if we are creating a Supracontext out of an empty one and a subcontext
		if(!other.hasData()){
			outcome = sub.outcome;
			data = new Subcontext[1];
			data[0] = sub;
			setNext(other.getNext());
			other.setNext(this);
			return;
		}
		outcome = other.outcome;
		//count will equal 0
		
		Subcontext[] otherData = other.getData();
		int size = otherData.length;
		data = new Subcontext[size+1];
		for(int i = 0; i < size; i++)
			data[i] = otherData[i];
		data[size] = sub;
		setData(data);
		
		setNext(other.getNext());
		other.setNext(this);
	}

	public int getOutcome() {
		return outcome;
	}

	public void setOutcome(int o) {
		outcome = o;
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
	 * Increases count by one; uses this when another lattice index is assigned to this
	 * supracontext.
	 */
	public void incrementCount(){
		count++;
	}

	/**
	 * Decreases the count by one; if this reaches 0, then this supracontext should be destroyed,
	 * as nothing in the lattice points to it anymore.
	 */
	public void decrementCount() {
		count--;
	}

	public Subcontext[] getData() {
		return data;
	}
	
	public void setData(Subcontext[] data2) {
		data = data2;
	}
	
	/**
	 * Remove all pointers to this Supracontext by setting count to zero and destroying its data.
	 */
	public void removePointers(){
		System.err.println(this + " has been declared heterogeneous!");
		count = 0;
		data = null;
	}
	
	public boolean hasData(){
//		System.err.println(data);
		return data.length != 0;
	}
	public int getCount(){
		return count;
	}

	/**
	 * @return True if the outcome is deterministic (the subcontext consists of data with all
	 * the same outcome)
	 */
	public boolean isDeterministic() {
		//empty is still deterministic
		if(data == null)
			return true;
		return outcome != Index.NONDETERMINISTIC;
	}
	
	/**
	 * @return String representation of this supracontext
	 */
	@Override
	public String toString(){
		if(data == null)
			return "[NULL]";
		if(data.length == 0)
			return "[EMPTY]";
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(count);
		sb.append('x');
		for(Subcontext sub : data){
			sb.append(sub);
			sb.append(',');
		}
		sb.append(']');
		return sb.toString();
	}
	
}
