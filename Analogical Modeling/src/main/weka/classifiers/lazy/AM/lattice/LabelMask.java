package weka.classifiers.lazy.AM.lattice;

/**
 * A class for masking binary labels.
 * 
 */
public class LabelMask {

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
	LabelMask(int start, int end) {
		length = end - start + 1;
		mask = 0;
		for (int i = start; i <= end; i++)
			mask |= 1 << i;
		// TODO: test with print statement
	}

}