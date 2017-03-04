package weka.classifiers.lazy.AM.label;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BitSetLabelTest {
    @Test
    public void testConstructor() {
        BitSet bitset = new BitSet();
        bitset.set(0);
        bitset.set(2);
        BitSetLabel label = new BitSetLabel(bitset, 3);
        assertEquals(3, label.getCardinality());
        assertFalse(label.matches(0));
        assertTrue(label.matches(1));
        assertFalse(label.matches(2));
    }
}
