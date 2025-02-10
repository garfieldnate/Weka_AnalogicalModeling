package weka.classifiers.lazy.AM.label;

import com.google.common.annotations.VisibleForTesting;
import weka.core.Instance;

import java.util.*;

/**
 * Analogical Modeling uses labels composed of boolean vectors in order to group
 * instances into subcontexts and subcontexts in supracontexts. Training set
 * instances are assigned labels by comparing them with the instance to be
 * classified and encoding matched attributes and mismatched attributes in a
 * boolean vector.
 *
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
     * The default (max) size of a label partition
     */
    private static final int PARTITION_SIZE = 5;

    /**
	 * @param test           Instance being classified
	 * @param ignoreUnknowns true if attributes with undefined values in the test item should be ignored; false if not.
	 * @param mdc            Specifies how to compare missing attributes
	 */
    public Labeler(Instance test, boolean ignoreUnknowns, MissingDataCompare mdc) {
        this.mdc = mdc;
        this.testInstance = test;
        this.ignoreUnknowns = ignoreUnknowns;
        Set<Integer> ignoreSet = new HashSet<>();
        if (ignoreUnknowns) {
            int length = testInstance.numAttributes() - 1;
            for (int i = 0; i < length; i++) {
                if (testInstance.isMissing(i)) ignoreSet.add(i);
            }
        }
        this.ignoreSet = Collections.unmodifiableSet(ignoreSet);
    }

    /**
     * @return The cardinality of the generated labels, or how many instance attributes are considered during labeling.
     */
    public int getCardinality() {
        return testInstance.numAttributes() - ignoreSet.size() - 1;
    }

    /**
     * Calculate the label cardinality for a given test instance
     *
     * @param testInstance   instance to assign labels
     * @param ignoreUnknowns true if unknown values are ignored; false otherwise
     * @return the cardinality of labels generated from testInstance and ignoreUnknowns
     */
    public static int getCardinality(Instance testInstance, boolean ignoreUnknowns) {
        int cardinality = 0;
        for (int i = 0; i < testInstance.numAttributes(); i++) {
            if (i != testInstance.classIndex() && !(testInstance.isMissing(i) && ignoreUnknowns)) cardinality++;
        }
        return cardinality;
    }

    /**
     * @return true if attributes with undefined values in the test item are ignored during labeling; false if not.
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
     * Find if the attribute at the given index is ignored during labeling. The
     * default behavior is to ignore the attributes with unknown values in the
     * test instance if {@link #getIgnoreUnknowns()} is true.
     *
     * @param index Index of the attribute being queried
     * @return True if the given attribute is ignored during labeling; false otherwise.
     */
    public boolean isIgnored(int index) {
        return ignoreSet.contains(index);
    }

    /**
     * Create a context label for the input instance by comparing it with the
     * test instance.
     *
     * @param data Instance to be labeled
     * @return the label for the context that the instance belongs to. The cardinality of the label will be the same as
     * the test and data items. At any given index i, {@link Label#matches(int) label.matches(i)} will return true if
     * that feature is the same in the test and data instances.
     * @throws IllegalArgumentException if the test and data instances are not from the same data set.
     */
    public abstract Label label(Instance data);


	/**
	 * Returns a string representing the context. If the input test instance attributes are "A C D Z R",
	 * and the {@code label} is {@code 00101}, then the return string will be "A C * Z *".
	 */
	public String getContextString(Label label) {
        List<String> contextList = getContextList(label, "*");
        return String.join(" ", contextList);
	}

    /**
     * Returns a list representing the context. If the input test instance attributes are "A C D Z R",
     * the {@code label} is {@code 00101}, and the {@code mismatchString} is "*", then the return list
     * will be "A", "C", "*", "Z", "*".
     */
    public List<String> getContextList(Label label, String mismatchString) {
        String contextBitString = label.toString();
        List<String> result = new ArrayList<>();
        int labelIndex = 0;
        for (int i = 0; i < testInstance.numAttributes(); i++) {
            // skip the class attribute and ignored attributes
            if (i == testInstance.classIndex() || isIgnored(i)) continue;
            if (contextBitString.charAt(labelIndex) == '0') {
                result.add(testInstance.stringValue(i));
            } else {
                result.add(mismatchString);
            }
            labelIndex++;
        }
        return result;
    }

    /**
     * Returns a string containing the attributes of the input instance (minus the class
     * attribute and ignored attributes).
     */
    public String getInstanceAttsString(Instance instance) {
        List<String> atts = getInstanceAttsList(instance);
        return String.join(" ", atts);
    }

    /**
     * Returns a list containing the attributes of the input instance (minus the class
     * attribute and ignored attributes).
     */
    public List<String> getInstanceAttsList(Instance instance) {
        List<String> atts = new ArrayList<>();
        for(int i = 0; i < instance.numAttributes(); i++) {
            if (i == instance.classIndex() || isIgnored(i)) {
                continue;
            }
            atts.add(instance.stringValue(i));
        }
        return atts;
    }

    /**
     * Creates and returns the label which belongs at the top of the boolean
     * lattice formed by the subcontexts labeled by this labeler, i.e. the one for
	 * which every feature is a match.
     *
     * @return A label with all matches
     */
    public abstract Label getLatticeTop();

	/**
	 * Creates and returns the label which belongs at the bottom of the boolean
	 * lattice formed by the subcontexts labeled by this labeler, i.e. the one for
	 * which every feature is a mismatch.
	 *
	 * @return A label with all mismatches
	 */
    public abstract Label getLatticeBottom();

	/**
	 * For testing purposes, this method allows the client to directly specify the label using
	 * the bits of an integer
	 */
	@VisibleForTesting
    public abstract Label fromBits(int labelBits);

    /**
     * In distributed processing, it is necessary to split labels into
     * partitions. This method returns a partition for the given label. A full
     * label is partitioned into pieces 0 through {@link #numPartitions()}, so
     * code to process labels in pieces should look like this:
     *
     * <pre>
     * 	Label myLabel = myLabeler.label(myInstance);
     * 	for(int i = 0; i &lt; myLabeler.numPartitions(); i++)
     * 		process(myLabeler.partition(myLabel, i);
     * </pre>
     *
     * @param partitionIndex index of the partition to return
     * @return a new label representing a portion of the attributes represented by the input label.
     * @throws IllegalArgumentException if the partitionIndex is greater than {@link #numPartitions()} or less than
     *                                  zero.
     * @throws IllegalArgumentException if the input label is not compatible with this labeler.
     */
    public abstract Label partition(Label label, int partitionIndex);

    /**
     * @return The number of label partitions available via {@link #partition}
     */
    public int numPartitions() {
        if (getCardinality() < PARTITION_SIZE) return 1;
        else return (int) Math.ceil(getCardinality() / (double) PARTITION_SIZE);
    }

    /**
     * This provides a default partitioning implementation which is overridable
     * by child classes.
     *
     * @return An array of partitions indicating how labels can be split into partitions.
     */
    Partition[] partitions() {
        Partition[] spans = new Partition[numPartitions()];

        int spanSize = (int) Math.floor((double) getCardinality() / numPartitions());
        // an extra bit will be given to remainder masks, since numMasks
        // probably does not divide cardinality
        int remainder = getCardinality() % numPartitions();
        int index = 0;
        for (int i = 0; i < numPartitions(); i++) {
            int inc = (i < remainder) ? spanSize + 1 : spanSize;
            spans[i] = new Partition(index, inc);
            index += inc;
        }
        return spans;
    }

    /**
     * Simple class for storing index spans.
     */
    protected static class Partition {
        private final int startIndex;
        private final int cardinality;

        Partition(int s, int l) {
            startIndex = s;
            cardinality = l;
        }

        /**
         * @return The beginning of the span
         */
        int getStartIndex() {
            return startIndex;
        }

        /**
         * @return The cardinality of the partition, or number of represented features.
         */
        int getCardinality() {
            return cardinality;
        }

        @Override
        public String toString() {
            return "[" + startIndex + "," + cardinality + "]";
        }
    }

}
