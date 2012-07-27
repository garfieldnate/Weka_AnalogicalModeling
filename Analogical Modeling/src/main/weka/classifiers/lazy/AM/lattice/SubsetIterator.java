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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class for iterating over all of the binary labels of a supracontext's
 * subsets.
 * 
 * @author Nate Glenn
 * 
 */
public class SubsetIterator implements Iterator<Integer> {

	// each will be all zeros except where one of the zeros in the tested item
	// is.
	private int[] gaps;

	// number of gaps

	private boolean hasNext = true;

	int current;
	int binCounter;

	/**
	 * @param supracontext
	 *            integer representing a label for a supracontext
	 * @param card
	 *            number of bits needed to represent the vector
	 * @return Iterator over all subsets of the given label
	 */
	public SubsetIterator(int supracontext, int card) {
		current = supracontext;
		gaps = new int[card];
		List<Integer> gapsTemp = new ArrayList<Integer>();

		// iterate over the clear bits and create a list of gaps;
		// each gap in the list is an int with all 0s except in the index where
		// the gap was found
		// in the supracontext
		// int gapIndex = 0;
		for (int i = 0; i < card; i++) {
			if (((1 << i) & supracontext) == 0) {
				// create an int with only bit i set to 1
				gapsTemp.add(1 << i);
			}
			// gapIndex++;
		}
		int size = gapsTemp.size();
		// if there were no gaps, then there is nothing to iterate over
		if (size == 0) {// gapIndex == 0) {
			hasNext = false;
			return;
		}
		// binCounter needs to be all ones for the last n bits, where n is
		// numGaps;
		binCounter = 0;
		for (int i = 0; i < size; i++)
			binCounter |= 1 << i;
		hasNext = true;

		gaps = new int[size];
		for (int i = 0; i < size; i++)
			gaps[i] = gapsTemp.get(i);
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public Integer next() {
		// choose gap to choose bit to flip; it's whichever is the rightmost 1
		// in binCounter
		// find the rightmost 1 in t; from HAKMEM, I believe
		int i, tt;
		for (i = 0, tt = ~binCounter & (binCounter - 1); tt > 0; tt >>= 1, ++i)
			;
		// System.out.println("Using " + Integer.toBinaryString(binCounter) +
		// ", Rightmost 1 is " + i +
		// "; gaps[" + i + "] is " + Integer.toBinaryString(gaps[i]));
		current ^= gaps[i];
		binCounter--;
		if (binCounter == 0)
			hasNext = false;
		// System.out.println("printing " + current);
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public static void main(String[] args) {
		System.out.println("Iterating over: " + Subcontext.binaryLabel(3, 4));
		SubsetIterator si = new SubsetIterator(4, 3);
		while (si.hasNext()) {
			System.out.println(Integer.toBinaryString(si.next()));
		}
		System.out.println("Iterating over: " + Subcontext.binaryLabel(7, 84));
		si = new SubsetIterator(84, 7);
		while (si.hasNext()) {
			System.out.println(Integer.toBinaryString(si.next()));
		}
	}
}
