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

import weka.core.Attribute;
import weka.core.Instance;

/**
 * This class contains functions for assigning binary labels.
 * 
 * @author Nathan Glenn
 * 
 */
public class Labeler {

	private final MissingDataCompare mdc;
	private final Instance testItem;

	public Labeler(MissingDataCompare mdc, Instance instance) {
		this.mdc = mdc;
		this.testItem = instance;
	}

	public int getCardinality() {
		return testItem.numAttributes() - 1;
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
	public int getContextLabel(Instance data) {
		int label = 0;
		int length = getCardinality();
		Attribute att;
		for (int i = 0; i < length; i++) {
			att = testItem.attribute(i);
			if (testItem.isMissing(i)
					|| data.isMissing(i))
				label |= (mdc.outcome(testItem, data, att));
			else if (testItem.value(att) != data.value(att)) {
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
