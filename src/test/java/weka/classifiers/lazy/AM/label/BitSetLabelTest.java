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

    @Test
    public void testToString() {
        BitSet bs = new BitSet(100);
        bs.set(0, 50);
        Label label = new BitSetLabel(bs, 100);
        assertEquals(
            "Should be 0000...1111...",
            "00000000000000000000000000000000000000000000000000" +
                "11111111111111111111111111111111111111111111111111",
            label.toString());

        bs = new BitSet(100);
        bs.set(50, 100);
        label = new BitSetLabel(bs, 100);
        assertEquals(
            "Should be 1111...0000...",
            "11111111111111111111111111111111111111111111111111" +
                "00000000000000000000000000000000000000000000000000",
            label.toString()
        );

        bs = new BitSet(100);
        bs.set(50, 100);
        bs.set(150, 200);
        label = new BitSetLabel(bs, 200);
        assertEquals(
            "Should be 1111...0000...1111...000...",
            "11111111111111111111111111111111111111111111111111" +
                "00000000000000000000000000000000000000000000000000" +
                "11111111111111111111111111111111111111111111111111" +
                "00000000000000000000000000000000000000000000000000",
            label.toString()
        );

        bs = new BitSet(100);
        label = new BitSetLabel(bs, 100);
        assertEquals(
            "Should be 000...",
            "00000000000000000000000000000000000000000000000000" +
                "00000000000000000000000000000000000000000000000000",
            label.toString()
        );
    }
}
