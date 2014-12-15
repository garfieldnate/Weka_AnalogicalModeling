package weka.classifiers.lazy.AM.lattice;

import weka.core.Instance;

//TODO: next, unify with IntLabler and IntLabel, and add tests.
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
	 * The default number of lattices to use during distributional processing.
	 */
	private static final int NUM_LATTICES = 4;

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

	/**
	 * In distributed processing, it is necessary to split labels into
	 * partitions. This method returns a partition for the given label. A full
	 * label is partitioned into pieces 0 through {@link #numPartitions()}, so
	 * code to process labels in pieces should look like this: <code>
	 * 	Label myLabel = myLabeler.label(myInstance);
	 * 	for(int i = 0; i < myLabeler.numPartitions(); i++)
	 * 		process(myLabeler.partition(myLabel, i);
	 * </code>
	 * 
	 * @param partitionIndex
	 *            index of the partition to return
	 * @return a new label representing a portion of the attributes represented
	 *         by the input label.
	 * @throws IllegalArgumentException
	 *             if the partitionIndex is greater than
	 *             {@link #numPartitions()} or less than zero.
	 * @throws IllegalArgumentException
	 *             if the input label is not compatible with this labeler.
	 */
	public abstract Label partition(Label label, int partitionIndex);

	/**
	 * @return The number of label partitions available via {@link #partition}
	 */
	public int numPartitions() {
		// just for now, we use a maximum of 4 partitions
		if (getCardinality() < NUM_LATTICES)
			return getCardinality();
		else
			return NUM_LATTICES;
	}

	protected Span[] partitions() {
		Span[] spans = new Span[numPartitions()];

		int spanSize = (int) Math.floor((double) getCardinality()
				/ numPartitions());
		// an extra bit will be given to remainder masks, since numMasks
		// probably does not divide cardinality
		int remainder = getCardinality() % numPartitions();
		int index = 0;
		for (int i = 0; i < numPartitions(); i++) {
			int inc = (i < remainder) ? spanSize + 1 : spanSize;
			spans[i] = new Span(index, inc);
			index += inc;
		}
		return spans;
	}

	/**
	 * Simple class for storing index spans.
	 * 
	 */
	protected class Span {
		private int start;
		private int end;

		protected Span(int s, int e) {
			start = s;
			end = e;
		}

		/**
		 * @return The beginning of the span
		 */
		protected int getStart() {
			return start;
		}

		/**
		 * @return The end of the span
		 */
		protected int getEnd() {
			return end;
		}
	}

}
