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

import static weka.classifiers.lazy.AM.AMconstants.LATTICE_NUM;
import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.data.Exemplar;

/**
 * This class contains functions for assigning binary labels.
 * 
 * @author Nathan Glenn
 * 
 */
public class Utils {

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
	public static int getContextLabel(Exemplar data, Exemplar test) {
		int label = 0;
		// System.out.println("Data: " + data + "\nWith: " + test);
		int length = test.getFeatures().length;
		int[] testFeats = test.getFeatures();
		int[] dataFeats = data.getFeatures();
		for (int i = 0; i < length; i++) {
			if (testFeats[i] == AMconstants.MISSING
					|| dataFeats[i] == AMconstants.MISSING)
				label |= (AMconstants.missingDataCompare.outcome(testFeats[i],
						dataFeats[i]));
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
	 * Sets the masks to use in assigning sublattice labels. The number of masks
	 * will determine how many piece each subcontext will be split into.
	 * 
	 * @param cardinality
	 *            the number of features in the exemplar
	 * @return A set of masks for splitting labels
	 */
	public static Mask[] getMasks(int cardinality) {
		int[] splitPoints = getSplitPoints(cardinality);
		// System.out.println("split into: " + Arrays.toString(splitPoints));
		Mask[] masks = new Mask[splitPoints.length - 1];
		masks[0] = new Mask(0, splitPoints[1]);
		for (int i = 1; i < splitPoints.length - 1; i++)
			masks[i] = new Mask(splitPoints[i] + 1, splitPoints[i + 1]);
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
	private static int[] getSplitPoints(int cardinality) {
		int[] splitPoints;
		if (LATTICE_NUM >= cardinality) {
			System.out.println("No split");
			splitPoints = new int[] { cardinality - 1 };
			// return splitPoints;
		}
		int splitTimes = (int) Math.ceil(cardinality / LATTICE_NUM) + 1;
		// need extra space for ending index
		splitPoints = new int[splitTimes + 1];
		// the first index is 0 by default
		splitPoints[0] = 0;
		for (int i = 1, splitPoint = LATTICE_NUM - 1; i <= splitTimes; i++, splitPoint += LATTICE_NUM) {
			if (splitPoint > cardinality - 1) {
				splitPoints[i] = cardinality - 1;
			} else
				splitPoints[i] = splitPoint;
		}
		// System.out.println(Arrays.toString(splitPoints));
		return splitPoints;
	}

	/**
	 * A class for masking binary labels.
	 * 
	 */
	public static class Mask {

		private int mask;

		/**
		 * @return the integer mask
		 */
		public int getMask() {
			return mask;
		}

		/**
		 * The number of attributes to be compared
		 */
		private int length;

		/**
		 * @return The cardinality of the integer mask
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @param start
		 *            The first feature index to be considered
		 * @param end
		 *            The last feature index to be considered
		 */
		Mask(int start, int end) {
			length = end - start + 1;
			mask = 0;
			for (int i = start; i <= end; i++)
				mask |= 1 << i;
			// TODO: test with print statement
		}

	}

	/**
	 * @param card
	 *            Number of features in the subcontext
	 * @param label
	 *            Integer label for the subcontext
	 * @return String representation of binary label, with zeros padded in the
	 *         front
	 */
	public static String binaryLabel(int card, int label) {
		StringBuilder sb = new StringBuilder();
		String binary = Integer.toBinaryString(label);
	
		int diff = card - binary.length();
		for (int i = 0; i < diff; i++)
			sb.append('0');
	
		sb.append(binary);
		return sb.toString();
	}
}
