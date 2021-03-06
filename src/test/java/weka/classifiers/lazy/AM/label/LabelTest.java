package weka.classifiers.lazy.AM.label;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static weka.classifiers.lazy.AM.TestUtils.mockInstance;
import static weka.classifiers.lazy.AM.label.LabelerFactory.*;
import static weka.classifiers.lazy.AM.label.MissingDataCompare.MATCH;
import static weka.classifiers.lazy.AM.label.MissingDataCompare.VARIABLE;

@RunWith(Parameterized.class)
public class LabelTest {
    @Parameter()
    public String testName;
    @Parameter(1)
    public LabelerFactory labelerFactory;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

	/**
	 * @return A collection of parameter arrays for running tests: <ol> <li>arg[0] is the test name;</li> <li>arg[1] is
	 * the {@link LabelerFactory} for the {@link Label} class to be tested.</li></ol>
	 */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() {
		return List.of(
				new Object[]{
						"IntLabel", new IntLabelerFactory()
				},
				new Object[]{
						"LongLabel", new LongLabelerFactory()
				},
				new Object[]{
						"BitSetLabel", new BitSetLabelerFactory()
				});
	}

    // test that equals() and hashCode() work correctly and agree
    @Test
    public void testEquivalence() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

        Label firstLabel = labeler.fromBits(0b001);
        Label secondLabel = labeler.fromBits(0b001);
        Label thirdLabel = labeler.fromBits(0b101);

        assertLabelEquals(firstLabel, secondLabel);
        assertLabelEquals(firstLabel, firstLabel);
        assertLabelEquals(secondLabel, secondLabel);

        assertLabelNotEquivalent(firstLabel, thirdLabel);
        assertLabelNotEquivalent(secondLabel, thirdLabel);
    }

    private void assertLabelEquals(Label firstLabel, Label secondLabel) {
		assertEquals(firstLabel, secondLabel);
		assertEquals(secondLabel, firstLabel);
		assertEquals(firstLabel.hashCode(), secondLabel.hashCode());
    }

    private void assertLabelNotEquivalent(Label firstLabel, Label secondLabel) {
		assertNotEquals(firstLabel, secondLabel);
		assertNotEquals(secondLabel, firstLabel);
        // technically not always true, but it's a good test on our small set
        assertTrue(firstLabel.hashCode() != secondLabel.hashCode());
    }

    @Test
    public void testGetCardinality() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);
		Label testLabel = labeler.fromBits(0b001);
        assertEquals(testLabel.getCardinality(), 3);
    }

    @Test
    public void testMatches() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

		Label testLabel = labeler.fromBits(0b001);
        boolean[] matches = new boolean[]{false, true, true};
        for (int i = 0; i < matches.length; i++)
            assertEquals(testLabel.matches(i), matches[i]);

        matches = new boolean[]{false, true, false};
        testLabel = labeler.fromBits(0b101);
        for (int i = 0; i < matches.length; i++)
            assertEquals(testLabel.matches(i), matches[i]);
    }

    @Test
    public void testIntersect() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

		Label label1 = labeler.fromBits(0b001);
		Label label2 = labeler.fromBits(0b100);
        boolean[] matches = new boolean[]{false, true, false};
        Label intersected = label1.intersect(label2);
        for (int i = 0; i < matches.length; i++)
            assertEquals(intersected.matches(i), matches[i]);
    }

    @Test
    public void testUnion() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

		Label label1 = labeler.fromBits(0b001);
		Label label2 = labeler.fromBits(0b100);
        Label intersected = label1.union(label2);
        for (int i = 0; i < intersected.getCardinality(); i++)
			assertTrue(intersected.matches(i));
    }

    @Test
    public void testAllMatching() {
        Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);
        assertTrue("Label composed of all 0's", labeler.fromBits(0b000).allMatching());
        for (int bits : List.of(0b100, 0b001, 0b010, 0b111)) {
            assertFalse("Label with a 1 in it", labeler.fromBits(bits).allMatching());
        }
    }

    @Test
    public void testMatchesThrowsExceptionForIndexTooLow() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

		Label testLabel = labeler.fromBits(0b001);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Illegal index"));
        testLabel.matches(-10);
    }

    @Test
    public void testMatchesThrowsExceptionForIndexTooHigh() {
        Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

        Label testLabel = labeler.fromBits(0b001);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Illegal index"));
        testLabel.matches(3);
    }

	// Currently this also checks iterator order, which shouldn't matter.
	// For now, it's fine.
	@Test
	public void testDescendantIterator() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);
		Label label = labeler.fromBits(0b100);

		Set<Label> expectedLabels = new HashSet<>(Arrays.asList(labeler.fromBits(0b101), labeler.fromBits(0b111), labeler.fromBits(0b110)));

		Set<Label> actualLabels = new HashSet<>();
		Iterator<Label> si = label.descendantIterator();
		while (si.hasNext()) actualLabels.add(si.next());
		assertEquals(expectedLabels, actualLabels);

		// comparing:
		// V , O , V , I , 0 , ? , O , T , T , A , A
		// V , U , V , O , 0 , ? , 0 , ? , L , E , A
		labeler = labelerFactory.createLabeler(mockInstance(10), false, VARIABLE);
		label = labeler.fromBits(0b0101001111);

		expectedLabels = new HashSet<>(Arrays.asList(labeler.fromBits(0b0101011111), labeler.fromBits(0b0101111111), labeler.fromBits(0b0101101111),
				labeler.fromBits(0b0111101111), labeler.fromBits(0b0111111111), labeler.fromBits(0b0111011111),
				labeler.fromBits(0b0111001111), labeler.fromBits(0b1111001111), labeler.fromBits(0b1111011111),
				labeler.fromBits(0b1111111111), labeler.fromBits(0b1111101111), labeler.fromBits(0b1101101111),
				labeler.fromBits(0b1101111111), labeler.fromBits(0b1101011111), labeler.fromBits(0b1101001111)));

		actualLabels = new HashSet<>();
		si = label.descendantIterator();
		while (si.hasNext()) actualLabels.add(si.next());
		assertEquals(expectedLabels, actualLabels);
	}

	@Test
	public void testIsDescendantOf() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(3), false, MATCH);

		Label parentLabel = labeler.fromBits(0b100);

		Label descendantLabel = labeler.fromBits(0b101);
		assertTrue(descendantLabel.isDescendantOf(parentLabel));

		Label nonDescendantLabel = labeler.fromBits(0b001);
		assertFalse(nonDescendantLabel.isDescendantOf(parentLabel));
	}

	@Test
	public void testToString() {
		int labelBits = 0b1010101000111;
		Labeler labeler = labelerFactory.createLabeler(mockInstance(13), false, MATCH);
		Label label = labeler.fromBits(labelBits);
		assertEquals("toString of label with elements", Integer.toString(labelBits,2), label.toString());
	}

	@Test
	public void testToStringAllZeroes() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(13), false, MATCH);
		Label label = labeler.fromBits(0);
		assertEquals("0000000000000", label.toString());
	}

	// It seems like a dumb case to handle, but Weka's classifier test actually exercises this case
	@Test
	public void testToStringNoAttributes() {
		Labeler labeler = labelerFactory.createLabeler(mockInstance(0), false, MATCH);
		Label label = labeler.fromBits(0);
		assertEquals("", label.toString());
	}
}
