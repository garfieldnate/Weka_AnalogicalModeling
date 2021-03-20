package weka.classifiers.lazy.AM.label;

import org.junit.Test;

import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instances;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

public class BitSetLabelerTest {

    /**
     * Test that BitSetLabeler can work with data larger than 32 bits.
     *
     * @throws Exception if there's a problem loading the Soybean dataset
     */
    @Test
    public void testLabelLargeInstance() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
        BitSetLabeler labeler = new BitSetLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        Label label = labeler.label(data.get(1));
        BitSet bits = new BitSet();
        for (int i : new int[]{15, 25, 26, 27, 28, 29, 34})
            bits.set(i);
        assertEquals(label, new BitSetLabel(bits, 35));
    }

    @Test
    public void testPartitionLargeLabel() throws Exception {
        // 35 features, 7 partitions
        Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
        BitSetLabeler labeler = new BitSetLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        assertEquals(7, labeler.numPartitions());
    }

}
