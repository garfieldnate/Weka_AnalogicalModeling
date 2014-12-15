package weka.classifiers.lazy.AM.lattice;

import weka.core.Instance;

/**
 * This class is used to assign context labels to training instances by
 * comparison with the instance being classified.
 * 
 * @author Nathan Glenn
 */
public abstract class Labeler {
	protected boolean ignoreUnknowns;
	protected MissingDataCompare mdc = null;
	protected Instance testInstance = null;

	/**
	 * @return The cardinality of the generated labels, or how many instance
	 *         attributes are considered during labeling.
	 */
	public abstract int getCardinality();

	/**
	 * @return true if attributes with undefined values in the test item are
	 *         ignored during labeling; false if not.
	 */
	public boolean getIgnoreUnknowns() {
		return ignoreUnknowns;
	}

	/**
	 * @return the MissingDataCompare strategy in use by this labeler
	 */
	public MissingDataCompare missingDataCompare() {
		return mdc;
	}

	/**
	 * 
	 * @return the test instance being used to label other instances
	 */
	public Instance testInstance() {
		return testInstance;
	}

	/**
	 * Create a context label for the input instance by comparing it with the
	 * test instance.
	 * 
	 * @param data
	 *            Instance to be labeled
	 * @return the label for the context that the instance belongs to. The
	 *         cardinality of the label will be the same as the test and data
	 *         items. At any given index i, {@link Label#matches(int)
	 *         label.matches(i)} will return true if that feature is the same in
	 *         the test and data instances.
	 * @throws IllegalArgumentException
	 *             if the test and data instances are not from the same data
	 *             set.
	 */
	public abstract Label label(Instance data);
}
