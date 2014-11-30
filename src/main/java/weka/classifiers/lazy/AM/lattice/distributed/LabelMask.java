package weka.classifiers.lazy.AM.lattice.distributed;

import weka.classifiers.lazy.AM.lattice.Label;

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
	 * @param end
	 *            The last feature index to be considered
	 */
	LabelMask(int start, int end) {
		if (start < 0)
			throw new IllegalArgumentException("start should be non-negative");
		if (end < start)
			throw new IllegalArgumentException(
					"end should be greater than or equal to start");

		this.start = start;
		cardinality = end - start + 1;
		mask = 0;
		for (int i = start; i <= end; i++)
			mask |= (1 << i);
	}

	public Label mask(Label label) {
		return new Label((mask & label.intLabel()) >> start, getCardinality());
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LabelMask))
			return false;
		LabelMask otherMask = (LabelMask) other;
		return getCardinality() == otherMask.getCardinality()
				&& mask == otherMask.mask && start == otherMask.start;
	}

	@Override
	public String toString() {
		return start + "-" + (start + cardinality) + ":"
				+ Integer.toBinaryString(mask);
	}
	
	/**
	 * Create and return a set of masks that can be used to split subcontext
	 * labels for distributed processing.
	 * 
	 * @param numMasks
	 *            the number of masks to be created, or the number of separate
	 *            labels that a given label will be separated into. If the
	 *            number of masks exceeds the cardinality, then the number will
	 *            be reduced to match the cardinality (creating masks of one bit
	 *            each)
	 * @param cardinality
	 *            the number of features in the exemplar
	 * @return A set of masks for splitting labels
	 */
	static LabelMask[] getMasks(int numMasks, int cardinality) {
		if (numMasks > cardinality)
			numMasks = cardinality;
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