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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import weka.classifiers.lazy.AM.lattice.Label;
import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.SubcontextList;
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

	// the current number of the subcontext being added
	private int index = -1;

	/**
	 * All points in the lattice point to the empty supracontext by default.
	 */
	private Supracontext emptySupracontext;

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
		init(labelMask.getCardinality());

		// Fill the lattice with all of the subcontexts, masking labels
		for (Subcontext sub : subList) {
			index++;
			insert(sub, labelMask.mask(sub.getLabel()));
		}
	}

	/**
	 * Inserts sub into the lattice, into location given by label
	 * 
	 * @param sub
	 *            Subcontext to be inserted
	 * @param label
	 *            label to be assigned to the subcontext
	 */
	public void insert(Subcontext sub, Label label) {
		addToContext(sub, label.intLabel());
		cleanSupra();
		Iterator<Label> si = label.subsetIterator();
		while (si.hasNext()) {
			addToContext(sub, si.next().intLabel());
			// remove supracontexts with count = 0 after every pass
			cleanSupra();
		}
	}

	/**
	 * Add the given subcontext to the supracontext with the given label
	 * @param sub
	 * @param label
	 */
	private void addToContext(Subcontext sub, int label) {
		// the default value is the empty supracontext (leave null until now to
		// save time/space)
		if (lattice[label] == null) {
			lattice[label] = emptySupracontext;
		}

		// if the following supracontext matches the current index, just repoint
		// to that one.
		if (lattice[label].getNext().getIndex() == index) {
			// don't decrement count on emptySupracontext!
			if (lattice[label] != emptySupracontext)
				lattice[label].decrementCount();
			lattice[label] = lattice[label].getNext();
			lattice[label].incrementCount();
		}
		// otherwise make a new Supracontext and add it
		else {
			// don't decrement the count for the emptySupracontext!
			if (lattice[label] != emptySupracontext)
				lattice[label].decrementCount();
			lattice[label] = new Supracontext(lattice[label], sub, index);
			lattice[label].incrementCount();
		}
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
			assert(supra.getCount() != 0);
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
