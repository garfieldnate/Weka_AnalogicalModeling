package weka.classifiers.lazy.AM.lattice.distributed;

import weka.classifiers.lazy.AM.lattice.IntLabel;

/**
 * A class for masking binary labels.
 * 
 */
public class LabelMask {

	private int mask;
	private int start;

	/**
	 * The number of attributes to be compared
	 */
	private int cardinality;

	/**
	 * @return The cardinality of the integer mask
	 */
	public int getCardinality() {
		return cardinality;
	}

	/**
	 * @param start
	 *            The first feature index to be considered
	 * @param card
	 *            The length of the mask
	 */
	LabelMask(int start, int card) {
		if (start < 0)
			throw new IllegalArgumentException("start should be non-negative");
		if (card < 1)
			throw new IllegalArgumentException(
					"card should be greater than or equal to one");

		this.start = start;
		cardinality = card;
		mask = 0;
		for (int i = start; i < start + card; i++)
			mask |= (1 << i);
	}

	public IntLabel mask(IntLabel label) {
		return new IntLabel((mask & label.intLabel()) >> start, getCardinality());
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LabelMask))
			return false;
		LabelMask otherMask = (LabelMask) other;
		return getCardinality() == otherMask.getCardinality()
				&& start == otherMask.start;
	}

	@Override
	public String toString() {
		return start + "," + cardinality + ":" + Integer.toBinaryString(mask);
	}

	/**
	 * Create and return a set of masks that can be used to split subcontext
	 * labels for distributed processing.
	 * @param cardinality
	 *            the number of features in the exemplar
	 * @param numMasks
	 *            the number of masks to be created, or the number of separate
	 *            labels that a given label will be separated into. If the
	 *            number of masks exceeds the cardinality, then the number will
	 *            be reduced to match the cardinality (creating masks of one bit
	 *            each)
	 * 
	 * @return A set of masks for splitting labels
	 */
	static LabelMask[] getMasks(int cardinality, int numMasks) {
		if (numMasks > cardinality)
			numMasks = cardinality;
		if (numMasks < 2)
			throw new IllegalArgumentException(
					"numMasks should be greater than 1");
		LabelMask[] masks = new LabelMask[numMasks];

		int latticeSize = (int) Math.floor((double) cardinality / numMasks);
		// an extra bit will be given to remainder masks, since numMasks probably does not divide cardinality
		int remainder = cardinality % numMasks;
		int index = 0;
		for (int i = 0; i < numMasks; i++) {
			int inc = (i < remainder) ? latticeSize + 1 : latticeSize;
			masks[i] = new LabelMask(index, inc);
			index += inc;
		}
		return masks;
	}
}