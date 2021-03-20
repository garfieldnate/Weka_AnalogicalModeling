package weka.classifiers.lazy.AM.label;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntLabelTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testConstructor() {
        IntLabel label = new IntLabel(0b101, 3);
        assertEquals(3, label.getCardinality());
        assertFalse(label.matches(0));
        assertTrue(label.matches(1));
        assertFalse(label.matches(2));
    }

    @Test
    public void testConstructorCardinalityTooHigh() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Input cardinality too high (33)"));
        new IntLabel(0b101, 33);
    }

    @Test
    public void testLabelBits() {
        IntLabel label = new IntLabel(0b0011, 4);
        assertEquals(label.labelBits(), 0b0011);
    }

    @Test
    public void testCopyConstructor() {
        IntLabel firstLabel = new IntLabel(0b100, 3);
        IntLabel secondLabel = new IntLabel(firstLabel);
        assertEquals(secondLabel, firstLabel);

        BitSet bitset = new BitSet();
        bitset.set(0);
        bitset.set(5);
        IntLabel thirdLabel = new IntLabel(new BitSetLabel(bitset, 6));
        assertEquals(new IntLabel(0b100001, 6), thirdLabel);
    }
}
