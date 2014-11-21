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
				// use length-1-i instead of i so that in binary the labels show
				// left to right, first to last feature.
				label |= (1 << (length - 1 - i));
			}
		}
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
		LabelMask[] masks = new LabelMask[numMasks];

		int latticeSize = (int) Math.ceil((double) cardinality / numMasks);
		int index = 0;
		for (int i = 0; i < cardinality; i += latticeSize) {
			masks[index] = new LabelMask(i, Math.min(i + latticeSize - 1,
					cardinality - 1));
			index++;
		}
		return masks;
	}
}
