package weka.classifiers.lazy.AM.label;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * This labeler creates labels via the {@link LongLabel} implementation.
 *
 * @author Nathan Glenn
 */
public class LongLabeler extends Labeler {
    private final BitMask[] masks;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the cardinality of the input instance is greater than {@link
     *                                  LongLabel#MAX_CARDINALITY}.
     */
    // TODO: since this throws an exception, perhaps a static factory method
    // would be better?
    public LongLabeler(Instance instance, boolean ignoreUnknowns, MissingDataCompare mdc) {
        super(instance, ignoreUnknowns, mdc);
        if (getCardinality() > LongLabel.MAX_CARDINALITY) throw new IllegalArgumentException(
            "Cardinality of instance too high (" + getCardinality() + "); max cardinality for this labeler is "
            + LongLabel.MAX_CARDINALITY);
        masks = new BitMask[numPartitions()];
        Partition[] spans = partitions();
        for (int i = 0; i < numPartitions(); i++) {
            masks[i] = new BitMask(spans[i]);
        }
    }

	@Override
    public LongLabel label(Instance data) {
        long label = 0;
        int length = getCardinality();
        Attribute att;
        int index = 0;
        for (int i = 0; i < getTestInstance().numAttributes(); i++) {
            // skip ignored attributes and the class attribute
            if (isIgnored(i)) continue;
            if (i == getTestInstance().classIndex()) continue;
            att = getTestInstance().attribute(i);
            // use mdc if were are comparing a missing attribute
            if (getTestInstance().isMissing(i) || data.isMissing(i)) {
                if (!getMissingDataCompare().matches(getTestInstance(), data, att))
                    // use length-1-index instead of index so that in binary the
                    // labels show left to right, first to last feature.
                    label |= (1L << (length - 1 - index));
            } else if (getTestInstance().value(att) != data.value(att)) {
                // same as above
                label |= (1L << (length - 1 - index));
            }
            index++;
        }
        return new LongLabel(label, getCardinality());
    }

	@Override
	public Label getLatticeTop() {
		return new LongLabel(0L, getCardinality());
	}

	@Override
	public Label getLatticeBottom() {
		return new LongLabel(-1L, getCardinality());
	}

	@Override
	public Label fromBits(int labelBits) {
		return new LongLabel(labelBits, getCardinality());
	}

    @Override
    public Label partition(Label label, int partitionIndex) {
        if (partitionIndex > numPartitions() || partitionIndex < 0)
            throw new IllegalArgumentException("Illegal partition index: " + partitionIndex);
        if (label.getCardinality() != getCardinality()) throw new IllegalArgumentException(
            "Label cardinality is " + label.getCardinality() + " but labeler cardinality is " + getCardinality());
        if (!(label instanceof LongLabel)) throw new IllegalArgumentException(
            "This labeler can only handle " + LongLabel.class.getCanonicalName() + "s; input label was an instance of "
            + label.getClass().getCanonicalName());
        LongLabel longLabel = (LongLabel) label;

        // create and cache the masks if they have not be created yet
        // loop through the bits and set the unmatched ones
        return masks[partitionIndex].mask(longLabel);
    }

    /**
     * Object used to partition LongLabels via an long bit mask.
     */
    private static class BitMask {
        final int startIndex;
        final int cardinality;
        /**
         * This is a long such as 000111000 that can mask the bits in another
         * long via ||-ing.
         */
        long maskBits;

        public BitMask(Partition s) {
            startIndex = s.getStartIndex();
            cardinality = s.getCardinality();
            maskBits = 0;
            for (int i = startIndex; i < startIndex + cardinality; i++)
                maskBits |= (1L << i);
        }

        public Label mask(LongLabel label) {
            LongLabel longLabel = new LongLabel((maskBits & label.labelBits()) >> startIndex, cardinality);

            // int labels are faster and smaller, so use them if the cardinality
            // turns out to be small enough
            if (cardinality <= IntLabel.MAX_CARDINALITY) return new IntLabel(longLabel);
            return longLabel;
        }

        @Override
        public String toString() {
            return startIndex + "," + cardinality + ":" + Long.toBinaryString(maskBits);
        }
    }
}
