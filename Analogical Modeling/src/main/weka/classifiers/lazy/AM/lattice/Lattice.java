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

import java.util.LinkedList;
import java.util.List;

/**
 * This class holds the supracontextual lattice and does the work of filling
 * itself during classification.
 * 
 * This class represents the supracontextual lattice. The supractontextual
 * lattice is a boolean algebra which models supra- and subcontexts for the AM
 * algorithm. Using boolean algebra allows efficient computation of these as
 * well as traversal of all subcontexts within a supracontext.
 * 
 * @author Nate Glenn
 * 
 */
public class Lattice {

	/**
	 * Lattice is a 2^n array of Supracontexts
	 */
	private Supracontext[] lattice;

	/**
	 * Cardinality of the labels in the lattice
	 */
	private int cardinality;

	// the current number of the subcontext being added
	private int index = -1;

	/**
	 * All points in the lattice point to the empty supracontext by default.
	 */
	private static Supracontext emptySupracontext;
	// static {
	// }

	private static Supracontext heteroSupra;
	static {
		// points to nothing, has no data or outcome.
		heteroSupra = new Supracontext();
	}

	/**
	 * Initializes the empty and the heterogeneous supracontexts as well as the
	 * lattice
	 * 
	 * @param card
	 *            the size of the exemplars
	 */
	private void init(int card) {
		emptySupracontext = new Supracontext();
		emptySupracontext.setNext(emptySupracontext);
		// set count to 1 so that cleanSupra doesn't destroy it
		emptySupracontext.incrementCount();

		// points to nothing
		heteroSupra = new Supracontext();

		lattice = new Supracontext[(int) (Math.pow(2, card))];
		cardinality = card;
	}

	/**
	 * List of homogeneous supracontexts
	 */

	/**
	 * Initializes Supracontextual lattice to a 2^n length array of
	 * Supracontexts and then fills it with the contents of subList
	 * 
	 * @param card
	 *            Size of the feature vectors; lattice will be 2^card - 1 size.
	 * @param subList
	 *            List of subcontexts
	 */
	public Lattice(int card, SubcontextList subList) {

		init(subList.getCardinality());

		// Fill the lattice with all of the subcontexts
		for (Subcontext sub : subList) {
			index++;
			insert(sub);
		}
	}

	/**
	 * Inserts sub into the lattice, into location given by label
	 * 
	 * @param sub
	 *            Subcontext to be inserted
	 */
	public void insert(Subcontext sub) {
		// skip all children if this exemplar is heterogeneous
		if (!addToContext(sub, sub.getLabel()))
			return;
		SubsetIterator si = new SubsetIterator(sub.getLabel(), cardinality);
		while (si.hasNext()) {
			int temp = si.next();
			addToContext(sub, temp);
			// remove supracontexts with count = 0 after every pass
			cleanSupra();
		}
	}

	/**
	 * @return false if the item was added to heteroSupra, true otherwise
	 * @param sub
	 * @param label
	 */
	private boolean addToContext(Subcontext sub, int label) {
		// System.out.println("adding " + sub + " to " +
		// Subcontext.binaryLabel(cardinality, label));
		// the default value is the empty supracontext (leave null until now to
		// save time/space)
		if (lattice[label] == null) {
			lattice[label] = emptySupracontext;
		}

		// if the Supracontext is heterogeneous, ignore it
		if (lattice[label] == heteroSupra) {
			return false;
		}
		// if the following supracontext matches the current index, just repoint
		// to that one.
		else if (lattice[label].getNext().getIndex() == index) {
			// don't decrement count on emptySupracontext!
			if (lattice[label] != emptySupracontext)
				lattice[label].decrementCount();
			lattice[label] = lattice[label].getNext();
			// if the context has been emptied, then it was found to be
			// heterogeneous;
			// mark this as heterogeneous, too
			// [do not worry about this being emptySupracontext; it's index is
			// -1]
			// if(lattice[label].hasData()){
			lattice[label].incrementCount();
			// }
			// else
			// lattice[label] = heteroSupra;
		}
		// we now know that we will have to make a new Supracontext for this
		// item;
		// if outcomes don't match, then this supracontext is now heterogeneous.
		// if lattice[label]'s outcome is nondeterministic, it will be
		// heterogeneous
		else if (!lattice[label].isDeterministic() || lattice[label].hasData()
				&& lattice[label].getOutcome() != sub.getOutcome()) {
			lattice[label].decrementCount();// removePointers();
			lattice[label] = heteroSupra;
			return false;
		}
		// otherwise make a new Supracontext and add it
		else {
			// don't decrement the count for the emptySupracontext!
			if (lattice[label] != emptySupracontext)
				lattice[label].decrementCount();
			lattice[label] = new Supracontext(lattice[label], sub, index);
			lattice[label].incrementCount();
		}
		return true;
	}

	/**
	 * Creates a new Supracontext and places it in the Supracontextual linked
	 * list right after the other one it is created from.
	 * 
	 * @param other
	 * @param sub
	 */
	public Supracontext insertNewAfter(Supracontext other, Subcontext sub) {
		Supracontext newSup = new Supracontext();
		newSup.setOutcome(other.getOutcome());
		newSup.setIndex(index);
		// count will equal 0

		int[] otherData = other.getData();
		int size = otherData.length;
		int[] data = new int[size + 1];
		for (int i = 0; i < size; i++)
			data[i] = otherData[i];
		data[size - 1] = sub.getIndex();
		newSup.setData(data);

		newSup.setNext(other.getNext());
		other.setNext(newSup);

		return newSup;
	}

	/**
	 * Cycles through the the supracontexts and deletes ones with count=0
	 */
	private void cleanSupra() {
		Supracontext supra = emptySupracontext.getNext();
		Supracontext supraPrev = emptySupracontext;
		while (supra != emptySupracontext) {
			if (supra.getCount() == 0) {
				// linking supraPrev and supra.next() removes supra from the
				// linked list
				supraPrev.setNext(supra.getNext());
			}
			supraPrev = supra;
			supra = supra.getNext();

		}
	}

	/**
	 * 
	 * @return The list of supracontexts that were created by filling the
	 *         supracontextual lattice. From this, you can compute the
	 *         analogical set.
	 */
	public List<Supracontext> getSupracontextList() {
		List<Supracontext> supList = new LinkedList<Supracontext>();
		Supracontext supra = emptySupracontext.getNext();
		while (supra != emptySupracontext) {
			supList.add(supra);
			supra = supra.getNext();
		}
		return supList;
	}

	/**
	 * 
	 * @return A string representation of the list of Supracontexts created when
	 *         the Lattice was filled
	 */
	public String supraListToString() {
		StringBuilder sb = new StringBuilder();
		Supracontext supra = emptySupracontext.getNext();
		if (supra == emptySupracontext)
			return "EMPTY";
		while (supra != emptySupracontext) {
			sb.append(supra);
			sb.append("->");
			supra = supra.getNext();
		}
		return sb.toString();
	}
}
