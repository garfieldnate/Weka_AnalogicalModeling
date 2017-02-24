package weka.classifiers.lazy.AM.label;

import weka.core.Attribute;
import weka.core.Instance;

import java.util.BitSet;

/**
 * A {@link Labeler} implementation that creates {@link BitSetLabel
 * BitSetLabels}.
 *
 * @author Nathan Glenn
 */
public class BitSetLabeler extends Labeler {
    private final Partitioner[] partitioners;

    public BitSetLabeler(MissingDataCompare mdc, Instance test, boolean ignoreUnknowns) {
        super(mdc, test, ignoreUnknowns);
        partitioners = new Partitioner[numPartitions()];
        Partition[] spans = partitions();
        for (int i = 0; i < numPartitions(); i++) {
            partitioners[i] = new Partitioner(spans[i]);
        }
    }

    @Override
    public Label label(Instance data) {
        if (!data.equalHeaders(getTestInstance()))
            throw new IllegalArgumentException("Input instance is not compatible with the test instance");
        BitSet label = new BitSet();
        int length = getCardinality();
        Attribute att;
        int index = 0;
        for (int i = 0; i < getTestInstance().numAttributes(); i++) {
            // skip ignored attributes and the class attribute
            if (isIgnored(i)) continue;
            if (i == getTestInstance().classIndex()) continue;
            att = getTestInstance().attribute(i);
            // use mdc if were are comparing a missing attribute
            if (getTestInstance().isMissing(att) || data.isMissing(att)) {
                if (!getMissingDataCompare().matches(getTestInstance(), data, att))
                    // use length-1-index instead of index so that in binary the
                    // labels show left to right, first to last feature.
                    label.set(length - 1 - index);
            } else if (getTestInstance().value(att) != data.value(att)) {
                // same as above
                label.set(length - 1 - index);
            }
            index++;
        }
        return new BitSetLabel(label, getCardinality());
    }

    @Override
    public Label getAllMatchLabel() {
        return new BitSetLabel(new BitSet(), getCardinality());
    }

    @Override
    public Label partition(Label label, int partitionIndex) {
        if (partitionIndex > numPartitions() || partitionIndex < 0)
            throw new IllegalArgumentException("Illegal partition index: " + partitionIndex);
        if (label.getCardinality() != getCardinality()) throw new IllegalArgumentException(
            "Label cardinality is " + label.getCardinality() + " but labeler cardinality is " + getCardinality());
        // technically this is not currently required since we only access the
        // label via the Label interface, but it might be useful in the future.
        if (!(label instanceof BitSetLabel)) throw new IllegalArgumentException(
            "This labeler can only handle " + BitSetLabel.class.getCanonicalName()
            + "s; input label was an instance of " + label.getClass().getCanonicalName());

        // create and cache the masks if they have not be created yet
        return partitioners[partitionIndex].extract((BitSetLabel) label);
    }

    /**
     * Private class for storing label partitions
     */
    private class Partitioner {
        private final int startIndex;
        private final int cardinality;

        public Partitioner(Partition s) {
            startIndex = s.getStartIndex();
            cardinality = s.getCardinality();
        }

        public Label extract(BitSetLabel label) {
            BitSet newLabel = new BitSet(cardinality);
            // loop through the bits and set the unmatched ones
            for (int i = 0; i < cardinality; i++) {
                if (!label.matches(i + startIndex)) newLabel.set(i);
            }
            // int labels are faster and smaller, so use them if the cardinality
            // turns out to be small enough
            if (cardinality <= IntLabel.MAX_CARDINALITY) return new IntLabel(new BitSetLabel(newLabel, cardinality));
            return new BitSetLabel(newLabel, cardinality);
        }

        @Override
        public String toString() {
            return startIndex + "," + cardinality;
        }
    }
}
