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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import weka.classifiers.lazy.AM.AMUtils;

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
public class BasicLattice implements ILattice {

	/**
	 * Lattice is a 2^n array of Supracontexts
	 */
	private Supracontext[] lattice;
	private int cardinality;

	// the current number of the subcontext being added
	private int index = -1;

	/**
	 * All points in the lattice point to the empty supracontext by default.
	 */
	private Supracontext emptySupracontext;
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
		cardinality = card;
		emptySupracontext = new Supracontext();
		emptySupracontext.setNext(emptySupracontext);
		// set count to 1 so that cleanSupra doesn't destroy it
		emptySupracontext.incrementCount();

		// points to nothing
		heteroSupra = new Supracontext();

		lattice = new Supracontext[(int) (Math.pow(2, card))];
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
	public BasicLattice(SubcontextList subList) {

		init(subList.getCardinality());

		// Fill the lattice with all of the subcontexts
		for (Subcontext sub : subList) {
			index++;
			insert(sub);
		}
	}

	/**
	 * Inserts sub into the lattice.
	 * 
	 * @param sub
	 *            Subcontext to be inserted
	 */
	private void insert(Subcontext sub) {
		// skip this if the supracontext to be added to is already heterogeneous;
		// it would not be possible to make any non-heterogeneous supracontexts.
		if(lattice[sub.getLabel().intLabel()] == heteroSupra)
			return;
		// add the sub to its label position
		addToContext(sub, sub.getLabel());
		// then add the sub to all of the children of its label position
		Iterator<Label> si = sub.getLabel().subsetIterator();
		while (si.hasNext()) {
			addToContext(sub, si.next());
		}
		// remove supracontexts with count = 0 after every pass
		cleanSupra();
	}

	/**
	 * @return false if the item was added to heteroSupra, true otherwise
	 * @param sub
	 * @param label
	 */
	private void addToContext(Subcontext sub, Label label) {
		int labelBits = label.intLabel();
		// the default value is the empty supracontext (leave null until now to
		// save time/space)
		if (lattice[labelBits] == null) {
			lattice[labelBits] = emptySupracontext;
		}

		// if the Supracontext is heterogeneous, ignore it
		if (lattice[labelBits] == heteroSupra) {
			return;
		}
		// if the following supracontext matches the current index, just
		// re-point to that one.
		else if (lattice[labelBits].getNext().getIndex() == index) {
			assert(lattice[labelBits].getNext().getData().containsAll(lattice[labelBits].getData()));
			// don't decrement count on emptySupracontext!
			if (!lattice[labelBits].getCount().equals(BigInteger.ZERO))
				lattice[labelBits].decrementCount();
			lattice[labelBits] = lattice[labelBits].getNext();
			// if the context has been emptied, then it was found to be
			// heterogeneous;
			// mark this as heterogeneous, too
			// [do not worry about this being emptySupracontext; it's index is
			// -1]
			// if(lattice[label].hasData()){
			lattice[labelBits].incrementCount();
			// }
			// else
			// lattice[label] = heteroSupra;
		}
		// we now know that we will have to make a new Supracontext for this
		// item;
		// if outcomes don't match, then this supracontext is now heterogeneous.
		// if lattice[label]'s outcome is nondeterministic, it will be
		// heterogeneous
		else if (!lattice[labelBits].isDeterministic() || lattice[labelBits].hasData()
				&& lattice[labelBits].getOutcome() != sub.getOutcome()) {
			lattice[labelBits].decrementCount();
			lattice[labelBits] = heteroSupra;
			return;
		}
		// otherwise make a new Supracontext and add it
		else {
			// don't decrement the count for the emptySupracontext!
			if (!lattice[labelBits].getCount().equals(BigInteger.ZERO))
				lattice[labelBits].decrementCount();
			lattice[labelBits] = new Supracontext(lattice[labelBits], sub, index);
			lattice[labelBits].incrementCount();
		}
		return;
	}

	/**
	 * Cycles through the the supracontexts and deletes ones with count=0
	 */
	private void cleanSupra() {
		for(Supracontext supra = emptySupracontext; supra.getNext() != emptySupracontext;){
			if(supra.getNext().getCount().equals(BigInteger.ZERO)){
				supra.setNext(supra.getNext().getNext());
			}
			else
				supra = supra.getNext();
		}
		assert(noZeroSupras());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.classifiers.lazy.AM.lattice.LatticeImpl#getSupracontextList()
	 */
	@Override
	public List<Supracontext> getSupracontextList() {
		List<Supracontext> supList = new LinkedList<Supracontext>();
		Supracontext supra = emptySupracontext.getNext();
		while (supra != emptySupracontext) {
			supList.add(supra);
			supra = supra.getNext();
		}
		return supList;
	}
	
	/*
	 * Below methods are for private debugging and asserting
	 */
	
	//useful for private debugging on occasion
	@SuppressWarnings("unused")
	private String dumpLattice(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < Math.pow(2, cardinality); i++){
			sb.append(new Label(i, cardinality));
			sb.append(':');
			if(lattice[i] == heteroSupra)
				sb.append("[hetero]");
			else if(lattice[i] == null)
				sb.append("[empty]");
			else
				sb.append(lattice[i]);
			sb.append(AMUtils.LINE_SEPARATOR);
		}
		return sb.toString();
	}
	
	private boolean noZeroSupras(){
		for(Supracontext supra : getSupracontextList()){
			if(supra.getCount().equals(BigInteger.ZERO))
				return false;
		}
		return true;
	}
}