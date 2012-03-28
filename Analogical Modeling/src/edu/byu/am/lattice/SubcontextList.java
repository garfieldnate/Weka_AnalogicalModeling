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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.byu.am.data.Exemplar;
import edu.byu.am.data.Index;

/**
 * Manages the list of subcontexts
 * 
 * @author Nate Glenn
 * 
 */
public class SubcontextList implements Iterable<Subcontext> {

	private HashMap<Integer, Subcontext> labelToSubcontext = new HashMap<Integer, Subcontext>();

	/**
	 * Exemplar which is being classified and assigns contexts
	 */
	Exemplar test;

	/**
	 * Defines how missing data will be treated. TODO: let the user set this
	 * somehow.
	 */
	MissingDataCompare mdc = MissingDataCompare.MATCH;

	/**
	 * 
	 * @param testEx
	 *            Exemplar which is being classified and assigns contexts
	 * @param data
	 *            to add to subcontexts
	 */
	public SubcontextList(Exemplar testEx, List<Exemplar> data) {
		test = testEx;
		for (Exemplar e : data)
			add(e);
	}

	/**
	 * Adds the exemplar to the correct subcontext
	 * 
	 * @param data
	 */
	public void add(Exemplar data) {
		int label = getContextLabel(data);
		if (!labelToSubcontext.containsKey(label))
			labelToSubcontext.put(label, new Subcontext(label));
		labelToSubcontext.get(label).add(data);
	}

	/**
	 * @param data
	 *            Exemplar to be added to a subcontext
	 * @return binary label of length n, where n is the length of the feature
	 *         vectors. If the features of the test exemplar and the data
	 *         exemplar are the same at index i, then the i'th bit will be 1;
	 *         otherwise it will be 0.
	 */
	public int getContextLabel(Exemplar data) {
		int label = 0;
		// System.out.println("Data: " + data + "\nWith: " + test);
		int length = test.getFeatures().length;
		int[] testFeats = test.getFeatures();
		int[] dataFeats = data.getFeatures();
		for (int i = 0; i < length; i++) {
			if (testFeats[i] == Index.MISSING || dataFeats[i] == Index.MISSING)
				label |= (mdc.outcome(testFeats[i], dataFeats[i]));
			else if (testFeats[i] != dataFeats[i]) {
				// use label-1-i instead of i so that it's easier to understand
				// how to match a
				// binary label to an exemplar (using just i produces mirror
				// images)
				label |= (1 << length - 1 - i);
			}
		}
		// System.out.println(Integer.toBinaryString(label));

		return label;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		Iterator<Subcontext> iter = iterator();
		while (iter.hasNext()) {
			s.append(iter.next());
			s.append(',');
		}
		return s.toString();
	}

	/**
	 * 
	 * @return An iterator which returns each of the existing subcontexts.
	 */
	@Override
	public Iterator<Subcontext> iterator() {

		return new Iterator<Subcontext>() {

			Iterator<Integer> keyIterator = labelToSubcontext.keySet()
					.iterator();

			@Override
			public boolean hasNext() {
				return keyIterator.hasNext();
			}

			@Override
			public Subcontext next() {
				return labelToSubcontext.get(keyIterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
