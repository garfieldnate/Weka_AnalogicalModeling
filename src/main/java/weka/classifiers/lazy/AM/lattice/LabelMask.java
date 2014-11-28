package weka.classifiers.lazy.AM.lattice;

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

}