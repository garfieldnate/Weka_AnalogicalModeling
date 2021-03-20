package weka.classifiers.lazy.AM.label;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static weka.classifiers.lazy.AM.TestUtils.mockInstance;
import static weka.classifiers.lazy.AM.label.MissingDataCompare.MATCH;

@RunWith(Parameterized.class)
public class LabelTest {
    @Parameter()
    public String testName;
    @Parameter(1)
    public Constructor<Labeler> labelerConstructor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * @return A collection of parameter arrays for running tests: <ol> <li>arg[0] is the test name;</li> <li>arg[1] is
     * the {@link Constructor} for a {@link Labeler} class to be tested.</li> </ol>
     * @throws NoSuchMethodException if one of the {@link Labeler} classes doesn't have the expected constructor
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> instancesToTest() throws NoSuchMethodException {
        Collection<Object[]> parameters = new ArrayList<>();

        // There are three kinds of labels and associated labelers
        List<Class> labelerClasses = new ArrayList<>() {
			{
				add(IntLabeler.class);
				add(LongLabeler.class);
				add(BitSetLabeler.class);
			}
		};
        for (Class c : labelerClasses)
            parameters.add(new Object[]{
                c.getSimpleName(), c.getConstructor(MissingDataCompare.class, Instance.class, boolean.class)
            });

        return parameters;
    }

    // test that equals() and hashCode() work correctly and agree
    @Test
    public void testEquivalence() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

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
    public void testGetCardinality() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);
		Label testLabel = labeler.fromBits(0b001);
        assertEquals(testLabel.getCardinality(), 3);
    }

    @Test
    public void testMatches() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

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
    public void testIntersect() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

		Label label1 = labeler.fromBits(0b001);
		Label label2 = labeler.fromBits(0b100);
        boolean[] matches = new boolean[]{false, true, false};
        Label intersected = label1.intersect(label2);
        for (int i = 0; i < matches.length; i++)
            assertEquals(intersected.matches(i), matches[i]);
    }

    @Test
    public void testUnion() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

		Label label1 = labeler.fromBits(0b001);
		Label label2 = labeler.fromBits(0b100);
        Label intersected = label1.union(label2);
        for (int i = 0; i < intersected.getCardinality(); i++)
			assertTrue(intersected.matches(i));
    }

    @Test
    public void testMatchesThrowsExceptionForIndexTooLow() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

		Label testLabel = labeler.fromBits(0b001);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Illegal index"));
        testLabel.matches(-10);
    }

    @Test
    public void testMatchesThrowsExceptionForIndexTooHigh() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

        Label testLabel = labeler.fromBits(0b001);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Illegal index"));
        testLabel.matches(3);
    }

	// Currently this also checks iterator order, which shouldn't matter.
	// For now, it's fine.
	// TODO: should use correct Label class instead of always using IntLabel.
	@Test
	public void testDescendantIterator() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);
		Label label = labeler.fromBits(0b100);

		Set<Label> expectedLabels = new HashSet<>(Arrays.asList(labeler.fromBits(0b101), labeler.fromBits(0b111), labeler.fromBits(0b110)));

		Set<Label> actualLabels = new HashSet<>();
		Iterator<Label> si = label.descendantIterator();
		while (si.hasNext()) actualLabels.add(si.next());
		assertEquals(expectedLabels, actualLabels);

		// comparing:
		// V , O , V , I , 0 , ? , O , T , T , A , A
		// V , U , V , O , 0 , ? , 0 , ? , L , E , A
		labeler = labelerConstructor.newInstance(MissingDataCompare.VARIABLE, mockInstance(10), false);
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
	public void testIsDescendantOf() throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Labeler labeler = labelerConstructor.newInstance(MATCH, mockInstance(3), false);

		Label parentLabel = labeler.fromBits(0b100);

		Label descendantLabel = labeler.fromBits(0b101);
		assertTrue(descendantLabel.isDescendantOf(parentLabel));

		Label nonDescendantLabel = labeler.fromBits(0b001);
		assertFalse(nonDescendantLabel.isDescendantOf(parentLabel));
	}

    @Test
    public void testBottom() {
        Label bottom;
        if (labelerConstructor.getDeclaringClass().equals(IntLabeler.class)) {
            bottom = new IntLabel(0,IntLabel.MAX_CARDINALITY).BOTTOM();
            assertEquals(IntLabel.MAX_CARDINALITY, bottom.getCardinality());
        } else if (labelerConstructor.getDeclaringClass().equals(LongLabeler.class)) {
            bottom = new LongLabel(0,LongLabel.MAX_CARDINALITY).BOTTOM();
            assertEquals(LongLabel.MAX_CARDINALITY, bottom.getCardinality());
        } else {
            bottom = new BitSetLabel(new BitSet(), 100).BOTTOM();
            assertEquals(100, bottom.getCardinality());
        }
        for(int i = 0; i < bottom.getCardinality(); i++) {
            assertFalse("index " + i + " should not be matching", bottom.matches(i));
        }
    }
}
