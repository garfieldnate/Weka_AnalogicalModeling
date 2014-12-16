package weka.classifiers.lazy.AM.label;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import weka.core.Instance;

/**
 * This class is used to assign context labels to training instances by
 * comparison with the instance being classified.
 * 
 * @author Nathan Glenn
 */
public abstract class Labeler {
	private final boolean ignoreUnknowns;
	private final MissingDataCompare mdc;
	private final Instance testInstance;
	private final Set<Integer> ignoreSet;
	/**
	 * The default (max) number of partitions to split labels into
	 */
	private static final int NUM_PARTITIONS = 4;

	/**
	 * 
	 * @param mdc
	 *            Specifies how to compare missing attributes
	 * @param instance
	 *            Instance being classified
	 * @param ignroeUnknowns
	 *            true if attributes with undefined values in the test item
	 *            should be ignored; false if not.
	 */
	public Labeler(MissingDataCompare mdc, Instance test, boolean ignoreUnknowns) {
		this.mdc = mdc;
		this.testInstance = test;
		this.ignoreUnknowns = ignoreUnknowns;
		Set<Integer> ignoreSet = new HashSet<>();
		if (ignoreUnknowns) {
			int length = testInstance.numAttributes() - 1;
			for (int i = 0; i < length; i++) {
				if (testInstance.isMissing(i))
					ignoreSet.add(i);
			}
		}
		this.ignoreSet = Collections.unmodifiableSet(ignoreSet);
	}

	/**
	 * @return The cardinality of the generated labels, or how many instance
	 *         attributes are considered during labeling.
	 */
	public int getCardinality() {
		return testInstance.numAttributes() - ignoreSet.size() - 1;
	}

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
	public MissingDataCompare getMissingDataCompare() {
		return mdc;
	}

	/**
	 * @return the test instance being used to label other instances
	 */
	public Instance getTestInstance() {
		return testInstance;
	}

	/**
	 * @param index
	 *            Index of the attribute being queried
	 * @return True if the given attribute is ignored during labeling; false
	 *         otherwise.
	 */
	public boolean isIgnored(int index) {
		return ignoreSet.contains(index);
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
		if (getCardinality() < NUM_PARTITIONS)
			return getCardinality();
		else
			return NUM_PARTITIONS;
	}

	/**
	 * 
	 * @return An array of partitions providing the feature boundaries where
	 *         labels should be partitioned.
	 */
	// TODO: rename to spans
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
		private int startIndex;
		private int cardinality;

		protected Span(int s, int l) {
			startIndex = s;
			cardinality = l;
		}

		/**
		 * @return The beginning of the span
		 */
		protected int getStart() {
			return startIndex;
		}

		/**
		 * @return The length of the partition
		 */
		protected int getCardinality() {
			return cardinality;
		}
		
		@Override
		public String toString(){
			return "[" + startIndex + "," + cardinality + "]";
		}
	}

}
