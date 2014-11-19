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

package weka.classifiers.lazy.AM.lattice.distributed;

import java.util.LinkedList;
import java.util.List;

import weka.classifiers.lazy.AM.lattice.LabelMask;
import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.SubcontextList;
import weka.classifiers.lazy.AM.lattice.SubsetIterator;
import weka.classifiers.lazy.AM.lattice.Supracontext;

/**
 * Same as a normal lattice, except no supracontext is deemed heterogeneous and
 * hence everything is kept.
 * 
 * Represents a lattice which is to be combined with other sublattices to
 * determine predictions later on. When a sublattice is filled, there are two
 * main differences: <list> <li>Only a part of a an exemplar's features are used
 * to assign lattice locations; this is taken care of by
 * {@link Subsubcontextlist}.</li> <li>No supracontext is ever determined to be
 * heterogeneous. This is, of course, less efficient in some ways.</li> </list>
 * Inefficiencies brought about by not eliminating heterogeneous supracontexts
 * and by having to combine sublattices are a compromise to the alternative,
 * using a single lattice for any size exemplars. Remember that the underlying
 * structure of a lattice is an array of size 2^n, n being the size of the
 * exemplars contained. So if the exemplars are 20 features long, a signle
 * lattice would be 2^20 or 1M elements long. On the other hand, if the
 * exemplars are split in 4, then 4 sublattices of size 2^5, or 32, can be used
 * instead, making for close to 100,000 times less memory used.
 * <p>
 * In terms of processing power, more is required to use sublattices. However,
 * using threads the processing of each can be done in parallel.
 * 
 * @author Nate Glenn
 * @author Nathan Glenn
 * 
 */
public class HeterogeneousLattice {

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
	 * @param subList
	 *            List of subcontexts
	 * 
	 * @param labelMask
	 *            to use in assigning labels
	 */
	public HeterogeneousLattice(SubcontextList subList, LabelMask labelMask) {
		init(labelMask.getLength());

		// Fill the lattice with all of the subcontexts, masking labels
		for (Subcontext sub : subList) {
			index++;
			insert(sub, sub.getLabel() | labelMask.getMask());
		}
	}

	/**
	 * Inserts sub into the lattice, into location given by label
	 * 
	 * @param sub
	 *            Subcontext to be inserted
	 * @param labelMask
	 *            mask to use in assigning labels
	 */
	public void insert(Subcontext sub, int label) {
		// skip all children if this exemplar is heterogeneous
		if (!addToContext(sub, label))
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