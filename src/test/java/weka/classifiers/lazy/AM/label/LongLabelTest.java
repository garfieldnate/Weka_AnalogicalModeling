package weka.classifiers.lazy.AM.label;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LongLabelTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    // set bits 0, 2, and 32 to test that label cardinalities above 32 are OK
    private final long testLong = 0b100000000000000000000000000000101L;

    @Test
    public void testConstructor() {
        LongLabel label = new LongLabel(testLong, 33);
        assertEquals(33, label.getCardinality());
        assertFalse(label.matches(0));
        assertTrue(label.matches(1));
        assertFalse(label.matches(2));
        for (int i = 3; i < 32; i++)
            assertTrue(label.matches(i));
        assertFalse(label.matches(32));
    }

    @Test
    public void testConstructorCardinalityTooHigh() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Input cardinality too high (65)"));
        new IntLabel(0b101, 65);
    }

    @Test
    public void testLabelBits() {
        LongLabel label = new LongLabel(0b0011, 4);
        assertEquals(label.labelBits(), 0b0011L);

        label = new LongLabel(testLong, 33);
        assertEquals(label.labelBits(), testLong);
    }

    @Test
    public void testCopyConstructor() {
        LongLabel firstLabel = new LongLabel(testLong, 33);
        LongLabel secondLabel = new LongLabel(firstLabel);
        assertEquals(secondLabel, firstLabel);

        BitSet bitset = new BitSet();
        bitset.set(0);
        bitset.set(2);
        bitset.set(32);
        LongLabel thirdLabel = new LongLabel(new BitSetLabel(bitset, 33));
        assertEquals(new LongLabel(testLong, 33), thirdLabel);
    }
}
