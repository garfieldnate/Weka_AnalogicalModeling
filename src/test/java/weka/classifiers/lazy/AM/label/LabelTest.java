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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class LabelTest {
    // note that labels of all classes are normalized to IntLabels throughout so
    // that they can be hashed and compared.
    @Parameter(0)
    public String testName;
    @Parameter(1)
    public Constructor<Labeler> labelerConstructor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * @return A collection of parameter arrays for running tests: <ol> <li>arg[0] is the test name;</li> <li>arg[1] is
     * the {@link Constructor} for a {@link Labeler} class to be tested.</li> </ol>
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> instancesToTest() throws Exception {
        Collection<Object[]> parameters = new ArrayList<>();

        // There are three kinds of labels and associated labelers
        @SuppressWarnings("serial") List<Class> labelerClasses = new ArrayList<Class>() {
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
    public void testEquivalence() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label firstLabel = labeler.label(data.get(1));// 001
        Label secondLabel = labeler.label(data.get(5));// 001
        Label thirdLabel = labeler.label(data.get(2));// 101

        assertLabelEquals(firstLabel, secondLabel);
        assertLabelEquals(firstLabel, firstLabel);
        assertLabelEquals(secondLabel, secondLabel);

        assertLabelNotEquivalent(firstLabel, thirdLabel);
        assertLabelNotEquivalent(secondLabel, thirdLabel);
    }

    private void assertLabelEquals(Label firstLabel, Label secondLabel) {
        assertTrue(firstLabel.equals(secondLabel));
        assertTrue(secondLabel.equals(firstLabel));
        assertTrue(firstLabel.hashCode() == secondLabel.hashCode());
    }

    private void assertLabelNotEquivalent(Label firstLabel, Label secondLabel) {
        assertFalse(firstLabel.equals(secondLabel));
        assertFalse(secondLabel.equals(firstLabel));
        // technically not always true, but it's a good test on our small set
        assertTrue(firstLabel.hashCode() != secondLabel.hashCode());
    }

    @Test
    public void testGetCardinality() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label firstLabel = labeler.label(data.get(1));// 001
        assertEquals(firstLabel.getCardinality(), 3);
    }

    @Test
    public void testMatches() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label testLabel = labeler.label(data.get(1));// 001
        boolean[] matches = new boolean[]{false, true, true};
        for (int i = 0; i < matches.length; i++)
            assertEquals(testLabel.matches(i), matches[i]);

        matches = new boolean[]{false, true, false};
        testLabel = labeler.label(data.get(2));// 101
        for (int i = 0; i < matches.length; i++)
            assertEquals(testLabel.matches(i), matches[i]);
    }

    @Test
    public void testIntersect() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label label1 = labeler.label(data.get(1));// 001
        Label label2 = labeler.label(data.get(4));// 100
        boolean[] matches = new boolean[]{false, true, false};
        Label intersected = label1.intersect(label2);
        for (int i = 0; i < matches.length; i++)
            assertEquals(intersected.matches(i), matches[i]);
    }

    @Test
    public void testUnion() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label label1 = labeler.label(data.get(1));// 001
        Label label2 = labeler.label(data.get(4));// 100
        boolean[] matches = new boolean[]{true, true, true};
        Label intersected = label1.union(label2);
        for (int i = 0; i < matches.length; i++)
            assertEquals(intersected.matches(i), matches[i]);
    }

    @Test
    public void testMatchesThrowsExceptionForIndexTooLow() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label testLabel = labeler.label(data.get(1));// 001
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Illegal index"));
        testLabel.matches(-10);
    }

    @Test
    public void testMatchesThrowsExceptionForIndexTooHigh() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);

        Label testLabel = labeler.label(data.get(1));// 001
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("Illegal index"));
        testLabel.matches(3);
    }

    // Currently this also checks iterator order, which shouldn't matter.
    // For now, it's fine.
    @Test
    public void testDescendantIterator() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH, data.get(0), false);
        Label label = labeler.label(data.get(4));
        assertEquals(new IntLabel(label), new IntLabel(0b100, 3));

        Set<IntLabel> expectedLabels = new HashSet<>(Arrays.asList(new IntLabel[]{
            new IntLabel(0b101, 3), new IntLabel(0b111, 3), new IntLabel(0b110, 3)
        }));

        Set<IntLabel> actualLabels = new HashSet<>();
        Iterator<Label> si = label.descendantIterator();
        while (si.hasNext()) actualLabels.add(new IntLabel(si.next()));
        assertEquals(expectedLabels, actualLabels);

        // comparing:
        // V , O , V , I , 0 , ? , O , T , T , A , A
        // V , U , V , O , 0 , ? , 0 , ? , L , E , A
        data = TestUtils.getDataSet(TestUtils.FINNVERB);
        labeler = labelerConstructor.newInstance(MissingDataCompare.VARIABLE, data.get(165), false);
        label = labeler.label(data.get(166));
        assertEquals(new IntLabel(label), new IntLabel(0b0101001111, 10));

        expectedLabels = new HashSet<>(Arrays.asList(new IntLabel[]{
            new IntLabel(0b0101011111, 10), new IntLabel(0b0101111111, 10), new IntLabel(0b0101101111, 10),
            new IntLabel(0b0111101111, 10), new IntLabel(0b0111111111, 10), new IntLabel(0b0111011111, 10),
            new IntLabel(0b0111001111, 10), new IntLabel(0b1111001111, 10), new IntLabel(0b1111011111, 10),
            new IntLabel(0b1111111111, 10), new IntLabel(0b1111101111, 10), new IntLabel(0b1101101111, 10),
            new IntLabel(0b1101111111, 10), new IntLabel(0b1101011111, 10), new IntLabel(0b1101001111, 10)
        }));

        actualLabels = new HashSet<>();
        si = label.descendantIterator();
        while (si.hasNext()) actualLabels.add(new IntLabel(si.next()));
        assertEquals(expectedLabels, actualLabels);
    }
}
