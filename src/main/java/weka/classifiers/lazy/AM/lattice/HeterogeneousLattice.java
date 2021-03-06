/*
 * **************************************************************************
 * Copyright 2021 Nathan Glenn                                              *
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

import weka.classifiers.lazy.AM.data.BasicSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Same as a normal lattice, except no supracontext is deemed heterogeneous and
 * hence everything is kept.
 *
 * Represents a lattice which is to be combined with other sublattices to
 * determine predictions later on. When a sublattice is filled, there are two
 * main differences:
 * <ol>
 * <li>Only a part of a an exemplar's features are used to assign lattice
 * locations.</li>
 * <li>No supracontext is ever determined to be heterogeneous. This is, of
 * course, less efficient in some ways.</li>
 * </ol>
 * Inefficiencies brought about by not eliminating heterogeneous supracontexts
 * and by having to combine sublattices are a compromise to the alternative,
 * using a single lattice for any size exemplars. Remember that the underlying
 * structure of a lattice is an array of size 2^n, n being the size of the
 * exemplars contained. So if the exemplars are 20 features long, a single
 * lattice would be 2^20 or 1M elements long. On the other hand, if the
 * exemplars are split in 4, then 4 sublattices of size 2^5, or 32, can be used
 * instead, making for close to 100,000 times less memory used.
 * <p>
 * In terms of processing power, more is required to use sublattices. However,
 * using threads the processing of each can be done in parallel.
 *
 * @author Nate Glenn
 * @author Nathan Glenn
 */
public class HeterogeneousLattice implements Lattice {

	private final int partitionIndex;
	/**
     * Lattice is a 2^n array of Supracontexts
     */
    private final Map<Label, LinkedLatticeNode<BasicSupra>> lattice;

    // the current number of the subcontext being added
    private int index = -1;

    /**
     * All points in the lattice point to the empty supracontext by default.
     */
    private final LinkedLatticeNode<BasicSupra> emptySupracontext;
    private boolean filled;

    /**
     * Initializes Supracontextual lattice to a 2^n length array of
     * Supracontexts, as well as the empty and the heterogeneous supracontexts.
     *
     * @param partitionIndex       which label partition to use in assigning subcontexts to supracontexts
     */
    public HeterogeneousLattice(int partitionIndex) {
		this.partitionIndex = partitionIndex;
		emptySupracontext = new LinkedLatticeNode<>(new BasicSupra());
		emptySupracontext.setNext(emptySupracontext);

		lattice = new HashMap<>();
    }

    @Override
	public void fill(SubcontextList subList) {
    	if (filled) {
    		throw new IllegalStateException("Lattice is already filled and cannot be filled again.");
		}
    	filled = true;
		Labeler labeler = subList.getLabeler();

		// Fill the lattice with all of the subcontexts, masking labels
		for (Subcontext sub : subList) {
			index++;
			insert(sub, labeler.partition(sub.getLabel(), partitionIndex));
		}
	}

    /**
     * Inserts sub into the lattice, into location given by label
     *
     * @param sub   Subcontext to be inserted
     * @param label label to be assigned to the subcontext
     */
    public void insert(Subcontext sub, Label label) {
        addToContext(sub, label);
        Iterator<Label> si = label.descendantIterator();
        while (si.hasNext()) {
            addToContext(sub, si.next());
        }
        // remove supracontexts with count = 0 after every pass
        cleanSupra();
    }

    /**
     * Add the given subcontext to the supracontext with the given label
     */
    private void addToContext(Subcontext sub, Label label) {
        // the default value is the empty supracontext (leave null until now to
        // save time/space)
        if (!lattice.containsKey(label)) {
            lattice.put(label, emptySupracontext);
        }

        // if the following supracontext matches the current index, just repoint
        // to that one; this is a supracontext that was made in the final else
        // statement below this one.
        if (lattice.get(label).getNext().getIndex() == index) {
            // assert
            // (lattice.get(label).getNext().getData().containsAll(lattice
            // .get(label).getData()));
            // don't decrement count on emptySupracontext!
            if (lattice.get(label) != emptySupracontext) lattice.get(label).decrementCount();
            lattice.put(label, lattice.get(label).getNext());
            lattice.get(label).incrementCount();
        }
        // otherwise make a new Supracontext and add it
        else {
            // don't decrement the count for the emptySupracontext!
            if (lattice.get(label) != emptySupracontext) lattice.get(label).decrementCount();
            lattice.put(label, lattice.get(label).insertAfter(sub, index));
        }
    }

    /**
     * Cycles through the the supracontexts and deletes ones with count=0
     */
    private void cleanSupra() {
        for (LinkedLatticeNode<BasicSupra> supra = emptySupracontext; supra.getNext() != emptySupracontext; ) {
            if (supra.getNext().getCount().equals(BigInteger.ZERO)) {
                supra.setNext(supra.getNext().getNext());
            } else supra = supra.getNext();
        }
        assert (noZeroSupras());
    }

    private boolean noZeroSupras() {
        for (Supracontext supra : getSupracontexts()) {
            if (supra.getCount().equals(BigInteger.ZERO)) return false;
        }
        return true;
    }

    /**
     * @return The list of supracontexts that were created by filling the supracontextual lattice. From this, you can
     * compute the analogical set.
     */
    @Override
    public Set<Supracontext> getSupracontexts() {
        Set<Supracontext> supList = new HashSet<>();
        LinkedLatticeNode<BasicSupra> supra = emptySupracontext.getNext();
        while (supra != emptySupracontext) {
            assert (!supra.getCount().equals(BigInteger.ZERO));
            supList.add(supra);
            supra = supra.getNext();
        }
        return supList;
    }

    /**
     * @return A string representation of the list of Supracontexts created when the Lattice was filled
     */
    public String supraListToString() {
        StringBuilder sb = new StringBuilder();
        LinkedLatticeNode<BasicSupra> supra = emptySupracontext.getNext();
        if (supra == emptySupracontext) return "EMPTY";
        while (supra != emptySupracontext) {
            sb.append(supra);
            sb.append("->");
            supra = supra.getNext();
        }
        return sb.toString();
    }
}
