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

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.data.Exemplar;

/**
 * This class contains functions for assigning binary labels.
 * 
 * @author Nathan Glenn
 * 
 */
public class Labeler {

	private final MissingDataCompare mdc;
	private final Exemplar testItem;
	private int cardinality;

	public Labeler(MissingDataCompare mdc, Exemplar testItem, int card) {
		this.mdc = mdc;
		this.testItem = testItem;
		cardinality = card;
	}

	public int getCardinality() {
		return cardinality;
	}

	/**
	 * @param data
	 *            Exemplar to be added to a subcontext
	 * @param test
	 *            Exemplar being classified
	 * @return binary label of length n, where n is the length of the feature
	 *         vectors. If the features of the test exemplar and the data
	 *         exemplar are the same at index i, then the i'th bit will be 1;
	 *         otherwise it will be 0.
	 */
	public int getContextLabel(Exemplar data) {
		int label = 0;
		// System.out.println("Data: " + data + "\nWith: " + test);
		int length = testItem.getFeatures().length;
		int[] testFeats = testItem.getFeatures();
		int[] dataFeats = data.getFeatures();
		for (int i = 0; i < length; i++) {
			if (testFeats[i] == AMconstants.MISSING
					|| dataFeats[i] == AMconstants.MISSING)
				label |= (mdc.outcome(testFeats[i], dataFeats[i]));
			else if (testFeats[i] != dataFeats[i]) {
				// use length-1-i instead of i so that it's easier to understand
				// how to match a
				// binary label to an exemplar (using just i produces mirror
				// images)
				label |= (1 << i);// length - 1 - i
			}
		}
		// System.out.println(Integer.toBinaryString(label));
		return label;
	}

	/**
	 * Create and return a set of masks that can be used to split sublattice
	 * labels for distributed processing.
	 * 
	 * @param numMasks
	 *            the number of masks to be created, or the number of separate
	 *            labels that a given label will be separated into.
	 * @param cardinality
	 *            the number of features in the exemplar
	 * @return A set of masks for splitting labels
	 */
	public static LabelMask[] getMasks(int numMasks, int cardinality) {
		int[] splitPoints = getSplitPoints(numMasks, cardinality);
		// System.out.println("split into: " + Arrays.toString(splitPoints));
		LabelMask[] masks = new LabelMask[splitPoints.length - 1];
		masks[0] = new LabelMask(0, splitPoints[1]);
		for (int i = 1; i < splitPoints.length - 1; i++)
			masks[i] = new LabelMask(splitPoints[i] + 1, splitPoints[i + 1]);
		return masks;
	}

	// /////////////////
	// PRIVATE METHODS//
	// /////////////////

	/**
	 * Sets the boundaries for splitting the exemplars (and then the lattices).
	 * It does it based on the SPLIT_NUM, which defines the number of lattices
	 * to use The length of the array will be SPLIT_NUM-1 unlles the cardinality
	 * of the exemplars is smaller than or equal to SPLIT_NUM, in which case it
	 * will be 2. If splitPoints = {0,3,7,11}, then that means there will be 3
	 * lattices, the first using features 0-3, and the last using features 7-11,
	 * etc. The if the cardinality of the exemplars does not divide evenly by
	 * SPLIT_NUM, then the last lattice will always be the one with less
	 * exemplars.
	 * 
	 * TODO: parameterize splitting strategy for experimentation
	 * 
	 * @param cardinality
	 *            the number of features in the exemplar
	 */
	private static int[] getSplitPoints(int numLattices, int cardinality) {
		int[] splitPoints;
		if (numLattices >= cardinality) {
			System.out.println("No split");
			splitPoints = new int[] { cardinality - 1 };
			// return splitPoints;
		}
		int splitTimes = (int) Math.ceil(cardinality / numLattices) + 1;
		// need extra space for ending index
		splitPoints = new int[splitTimes + 1];
		// the first index is 0 by default
		splitPoints[0] = 0;
		for (int i = 1, splitPoint = numLattices - 1; i <= splitTimes; i++, splitPoint += numLattices) {
			if (splitPoint > cardinality - 1) {
				splitPoints[i] = cardinality - 1;
			} else
				splitPoints[i] = splitPoint;
		}
		return splitPoints;
	}

}
