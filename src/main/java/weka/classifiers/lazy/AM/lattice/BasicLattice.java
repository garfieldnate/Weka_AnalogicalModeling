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

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
 */
public class BasicLattice implements Lattice {
	/**
	 * points to nothing, has no data or outcome.
	 */
	private static final LinkedLatticeNode<ClassifiedSupra> heteroSupra = new LinkedLatticeNode<>(new ClassifiedSupra());
	/**
	 * All points in the lattice point to the empty supracontext by default.
	 */
	private final LinkedLatticeNode<ClassifiedSupra> emptySupracontext;

    /**
     * Lattice is a 2^n array of Supracontexts
     */
    private final Map<Label, LinkedLatticeNode<ClassifiedSupra>> lattice;
	private boolean filled;
    // the current number of the subcontext being added
    private int index = -1;

    /**
     * Initializes Supracontextual lattice to a 2^n length array of
     * Supracontexts, as well as the empty and heterogeneous supracontexts.
     */
    BasicLattice() {
		// TODO: dangit, now we have to support a blank constructor.
		emptySupracontext = new LinkedLatticeNode<>(new ClassifiedSupra());
		emptySupracontext.setNext(emptySupracontext);

		lattice = new HashMap<>();
    }

    @Override
	public void fill(SubcontextList subList) {
		if (filled) {
			throw new IllegalStateException("Lattice is already filled and cannot be filled again.");
		}
		filled = true;
		// Fill the lattice with all of the subcontexts
		for (Subcontext sub : subList) {
			index++;
			insert(sub);
		}
	}

    /**
     * Inserts sub into the lattice.
     *
     * @param sub Subcontext to be inserted
     */
    private void insert(Subcontext sub) {
        // skip this if the supracontext to be added to is already
        // heterogeneous;
        // it would not be possible to make any non-heterogeneous supracontexts.
        if (lattice.get(sub.getLabel()) == heteroSupra) return;
        // add the sub to its label position
        addToContext(sub, sub.getLabel());
        // then add the sub to all of the children of its label position
        Iterator<Label> si = sub.getLabel().descendantIterator();
        while (si.hasNext()) {
            addToContext(sub, si.next());
        }
        // remove supracontexts with count = 0 after every pass
        cleanSupra();
    }

    /**
     * @param sub subcontext to be added
     * @param label label of supracontext to add the subcontext to
     */
    private void addToContext(Subcontext sub, Label label) {
        // the default value is the empty supracontext (leave null until now to
        // save time/space)
        if (!lattice.containsKey(label)) {
            lattice.put(label, emptySupracontext);
        }

        // if the Supracontext is heterogeneous, ignore it
        if (lattice.get(label) == heteroSupra) {
            return;
        }
        // if the following supracontext matches the current index, just
        // re-point to that one; this is a supracontext that was made in
        // the final else statement below this one.
        else if (lattice.get(label).getNext().getIndex() == index) {
            assert (lattice.get(label).getNext().getData().containsAll(lattice.get(label).getData()));
            // don't decrement count on emptySupracontext!
            if (lattice.get(label) != emptySupracontext) lattice.get(label).decrementCount();
            lattice.put(label, lattice.get(label).getNext());
            lattice.get(label).incrementCount();
        }
        // we now know that we will have to make a new Supracontext to contain
        // this subcontext; don't bother making heterogeneous supracontexts
        else if (lattice.get(label).getSupracontext().wouldBeHetero(sub)) {
            lattice.get(label).decrementCount();
            lattice.put(label, heteroSupra);
            return;
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
        for (LinkedLatticeNode<ClassifiedSupra> supra = emptySupracontext; supra.getNext() != emptySupracontext; ) {
            if (supra.getNext().getCount().equals(BigInteger.ZERO)) {
                supra.setNext(supra.getNext().getNext());
            } else supra = supra.getNext();
        }
        assert (noZeroSupras());
    }

    @Override
    public Set<Supracontext> getSupracontexts() {
        Set<Supracontext> supList = new HashSet<>();
        LinkedLatticeNode<ClassifiedSupra> supra = emptySupracontext.getNext();
        while (supra != emptySupracontext) {
            supList.add(supra);
            supra = supra.getNext();
        }
        return supList;
    }

	/*
     * Below methods are for private debugging and asserting
	 */

    // useful for private debugging on occasion
    @SuppressWarnings("unused")
    private String dumpLattice() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Label, LinkedLatticeNode<ClassifiedSupra>> e : lattice.entrySet()) {
            sb.append(e.getKey());
            sb.append(':');
            if (e.getValue() == heteroSupra) sb.append("[hetero]");
            else sb.append(e.getValue());
            sb.append(AMUtils.LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private boolean noZeroSupras() {
        for (Supracontext supra : getSupracontexts()) {
            if (supra.getCount().equals(BigInteger.ZERO)) return false;
        }
        return true;
    }
}
