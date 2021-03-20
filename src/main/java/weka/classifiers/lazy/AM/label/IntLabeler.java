/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package weka.classifiers.lazy.AM.label;

import weka.core.Attribute;
import weka.core.Instance;

/**
 * Analogical Modeling uses labels composed of boolean vectors in order to group
 * instances into subcontexts and subcontexts in supracontexts. Training set
 * instances are assigned labels by comparing them with the instance to be
 * classified and encoding matched attributes and mismatched attributes in a
 * boolean vector.
 *
 * For example, if we were classifying an instance &lt;a, b, c&gt;, and we had
 * three training instances &lt;x, y, c&gt;, &lt;w, m, c&gt; and &lt;a, b,
 * z&gt;, and used 'n' to represent mismatches and 'y' for matches, the labels
 * would be &lt;n, n, y&gt;, &lt;n, n, y&gt;, and &lt;y, y, n&gt;.
 *
 * The current implementation takes advantage of binary arithmetic by
 * representing mismatches as a 1 bit and matches as a 0 bit, all packed into a
 * 32-bit integer.
 *
 * @author Nathan Glenn
 */
public class IntLabeler extends Labeler {
    private final BitMask[] masks;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the cardinality of the input instance is greater than 32
     */
    public IntLabeler(Instance instance, boolean ignoreUnknowns, MissingDataCompare mdc) {
        super(instance, ignoreUnknowns, mdc);
        if (getCardinality() > IntLabel.MAX_CARDINALITY) throw new IllegalArgumentException(
            "Cardinality of instance too high (" + getCardinality() + "); max cardinality for this labeler is "
            + IntLabel.MAX_CARDINALITY);
        masks = new BitMask[numPartitions()];
        Partition[] spans = partitions();
        for (int i = 0; i < numPartitions(); i++) {
            masks[i] = new BitMask(spans[i]);
        }
    }

    @Override
    public IntLabel label(Instance data) {
        int label = 0;
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
                    label |= (1 << (length - 1 - index));
            } else if (getTestInstance().value(att) != data.value(att)) {
                // same as above
                label |= (1 << (length - 1 - index));
            }
            index++;
        }
        return new IntLabel(label, getCardinality());
    }

    @Override
    public Label getLatticeTop() {
        return new IntLabel(0, getCardinality());
    }

    @Override
	public Label getLatticeBottom() {
		return new IntLabel(-1, getCardinality());
	}

    @Override
	public Label fromBits(int labelBits) {
    	return new IntLabel(labelBits, getCardinality());
	}

    @Override
    public Label partition(Label label, int partitionIndex) {
        if (partitionIndex > numPartitions() || partitionIndex < 0)
            throw new IllegalArgumentException("Illegal partition index: " + partitionIndex);
        if (label.getCardinality() != getCardinality()) throw new IllegalArgumentException(
            "Label cardinality is " + label.getCardinality() + " but labeler cardinality is " + getCardinality());
        if (!(label instanceof IntLabel)) throw new IllegalArgumentException(
            "This labeler can only handle " + IntLabel.class.getCanonicalName() + "s; input label was an instance of "
            + label.getClass().getCanonicalName());
        IntLabel intLabel = (IntLabel) label;

        // create and cache the masks if they have not be created yet
        // loop through the bits and set the unmatched ones
        return masks[partitionIndex].mask(intLabel);
    }

    /**
     * Object used to partition IntLabels via an integer bit mask.
     */
    private static class BitMask {
        final int startIndex;
        final int cardinality;
        /**
         * This is an int such as 000111000 that can mask the bits in another
         * integer via ||-ing.
         */
        int maskBits;

        public BitMask(Partition s) {
            startIndex = s.getStartIndex();
            cardinality = s.getCardinality();
            maskBits = 0;
            for (int i = startIndex; i < startIndex + cardinality; i++)
                maskBits |= (1 << i);
        }

        public IntLabel mask(IntLabel label) {
            return new IntLabel((maskBits & label.labelBits()) >> startIndex, cardinality);
        }

        @Override
        public String toString() {
            return startIndex + "," + cardinality + ":" + Integer.toBinaryString(maskBits);
        }
    }
}
