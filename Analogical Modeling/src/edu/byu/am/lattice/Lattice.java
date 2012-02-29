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

import java.util.Iterator;
import java.util.List;

import edu.byu.am.data.Exemplar;

/**
 * This class holds the supracontextual lattice and does the work of filling itself during
 * classification. The supractontextual lattice is a boolean algebra which models supra- and
 * subcontexts for the AM algorithm. Using boolean algebra allows efficient computation of these
 * as well as traversal of all subcontexts within a supracontext.
 * @author Nate Glenn
 *
 */
public class Lattice {
	
	//TODO:keep track of supracontexts with the same lists

	/**
	 * Lattice is a 2^n array of Supracontexts
	 */
	private Supracontext[] lattice;
	
	/**
	 * Cardinality of the labels in the lattice
	 */
	private int cardinality;
	
	/**
	 * Exemplars
	 */
	private List<Exemplar> data;
	
	//the current number of the subcontext being added 
	private int index = -1;
	
	/**
	 * The empty supracontext will have an index of -1 so that any attempt to add another
	 * subcontext will result in creating a new one
	 */
	private static Supracontext emptySupracontext;
	static{
		emptySupracontext = new Supracontext();
		emptySupracontext.setNext(emptySupracontext);
		//set count to 1 so that cleanSupra doesn't destroy it
		emptySupracontext.incrementCount();
	}
	private static Supracontext heteroSupra;
	static{
		heteroSupra = new Supracontext();
		//0 is used throughout to represent non-determinism
		heteroSupra.setOutcome(0);
	}
	
	/**
	 * List of homogeneous supracontexts
	 */
	
	/**
	 * Initializes Supracontextual lattice to a 2^n length array of Supracontexts and then fills
	 * it with the contents of subList
	 * @param card Size of the feature vectors; lattice will be 2^card - 1 size.
	 * @param subList List of subcontexts
	 */
	public Lattice(int card, SubcontextList subList) {
		lattice = new Supracontext[(int) (Math.pow(2, card))];
		cardinality = card;
		
		//Fill the lattice with all of the subcontexts
		for(Subcontext sub : subList){
			index++;
			insert(sub);
		}
	}
	
	/**
	 * Inserts sub into the lattice, into location given by label
	 * @param sub Subcontext to be inserted
	 */
	public void insert(Subcontext sub){
		//skip all children if this exemplar is heterogeneous
		if(!addToContext(sub,sub.getLabel()))
			return;
		SubsetIterator si = new SubsetIterator(sub.getLabel(),cardinality);
		while(si.hasNext()){
			int temp = si.next();
			System.out.println("adding " + sub + " to " + temp);
			addToContext(sub,temp);
			//remove supracontexts with count = 0 after every pass
			cleanSupra();
		}
	}
	
	/**
	 * @return false if the item was added to heteroSupra, true otherwise
	 * @param sub
	 * @param label
	 */
	private boolean addToContext(Subcontext sub, int label){
		//the default value is the empty supracontext (leave null until now to save time/space)
		if(lattice[label] == null){
			lattice[label] = emptySupracontext;
		}
		
		//if the Supracontext is heterogeneous, ignore it
		if(lattice[label] == heteroSupra){
			return false;
		}
		//if the following supracontext matches the current index, just repoint to that one.
		else if(lattice[label].getNext().getIndex() == index){
			//don't decrement count on emptySupracontext!
			if(lattice[label] != emptySupracontext)
				lattice[label].decrementCount();
			lattice[label] = lattice[label].getNext();
			//if the context has been emptied, then it was found to be hetergeneous;
			//mark this as heterogeneous, too
			//[do not worry about this being emptySupracontext; it's index is -1]
			if(lattice[label].hasData()){
				lattice[label].incrementCount();
			}
			else
				lattice[label] = heteroSupra;
		}
		//we now know that we will have to make a new Supracontext for this item;
		//if outcomes don't match, then this Supracontext is now heterogeneous.
		//if lattice[label]'s outcome is nondeterministic, it will be heterogeneous
		else if(!lattice[label].isDeterministic() || 
				lattice[label].hasData() && lattice[label].getOutcome() != sub.getOutcome()){
			lattice[label].removePointers();
			lattice[label] = heteroSupra;
			return false;
		}
		//otherwise make a new Supracontext and add it
		else{
			//don't decrement the count for the emptySupracontext!
			if(lattice[label] != emptySupracontext)
				lattice[label].decrementCount();
			lattice[label] = insertNewAfter(lattice[label],sub);
			lattice[label].incrementCount();
		}
		return true;
	}
	
	/**
	 * Creates a new Supracontext and places it in the Supracontextual linked list right after
	 * the other one it is created from.
	 * @param other
	 * @param sub
	 */
	public Supracontext insertNewAfter(Supracontext other, Subcontext sub) {
		Supracontext newSup = new Supracontext();
		newSup.setOutcome(other.getOutcome());
		newSup.setIndex(index);
		//count will equal 0
		
		Subcontext[] otherData = other.getData();
		int size = otherData.length;
		Subcontext[] data = new Subcontext[size+1];
		for(int i = 0; i < size; i++)
			data[i] = otherData[i];
		data[size-1] = sub;
		newSup.setData(data);
		
		newSup.setNext(other.getNext());
		other.setNext(newSup);
		
		return newSup;
	}
	
	/**
	 * Cycles through the the supracontexts and deletes ones with count=0
	 */
	private void cleanSupra(){
		Supracontext supra = emptySupracontext.getNext();
		Supracontext supraPrev = emptySupracontext;
		while(supra != emptySupracontext){
			if(supra.getCount() == 0){
				//linking supraPrev and supra.next() removes supra from the linked list
				supraPrev.setNext(supra.getNext());
			}
		}
	}
}
