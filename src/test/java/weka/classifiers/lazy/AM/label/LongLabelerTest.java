package weka.classifiers.lazy.AM.label;

import org.junit.Test;

import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instances;

import static org.junit.Assert.assertEquals;

public class LongLabelerTest {

    /**
     * Test that LongLabeler can work with data larger than 32 bits.
     *
     * @throws Exception if there's a problem loading the Soybean dataset
     */
    @Test
    public void testLabelLargeInstance() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
        LongLabeler labeler = new LongLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        Label label = labeler.label(data.get(1));
        // bits 15, 25, 26, 27, 28, 29 and 34 are set
        long bits = 0b10000111110000000001000000000000000L;
        assertEquals(new LongLabel(bits, 35), label);
    }

    @Test
    public void testPartitionLargeLabel() throws Exception {
        // 35 features, 7 partitions
        Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
        LongLabeler labeler = new LongLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        assertEquals(7, labeler.numPartitions());
        Label label = labeler.label(data.get(1));

        assertEquals(new IntLabel(0b00000, 5), labeler.partition(label, 0));
        assertEquals(new IntLabel(0b00000, 5), labeler.partition(label, 1));
        assertEquals(new IntLabel(0b00000, 5), labeler.partition(label, 2));
        assertEquals(new IntLabel(0b00001, 5), labeler.partition(label, 3));
        assertEquals(new IntLabel(0b00000, 5), labeler.partition(label, 4));
        assertEquals(new IntLabel(0b11111, 5), labeler.partition(label, 5));
        assertEquals(new IntLabel(0b10000, 5), labeler.partition(label, 6));

    }

}
