package weka.classifiers.lazy.AM.label;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.Labeler.Partition;
import weka.classifiers.lazy.AM.label.LabelerFactory.IntLabelerFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static weka.classifiers.lazy.AM.TestUtils.mockInstance;
import static weka.classifiers.lazy.AM.TestUtils.sixCardinalityData;
import static weka.classifiers.lazy.AM.label.LabelerFactory.BitSetLabelerFactory;
import static weka.classifiers.lazy.AM.label.LabelerFactory.LongLabelerFactory;
import static weka.classifiers.lazy.AM.label.MissingDataCompare.MATCH;

/**
 * Test all of the {@link Labeler} implementations.
 *
 * @author Nathan Glenn
 */
@RunWith(Parameterized.class)
public class LabelerTest {
    @Parameter()
    public String testName;
    @Parameter(1)
    public LabelerFactory labelerFactory;

	/**
	 * @return A collection of parameter arrays for running tests: <ol> <li>arg[0] is the test name;</li> <li>arg[1] is
	 * the {@link LabelerFactory} for the {@link Labeler} to be tested.</li> </ol>
	 */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() {
		return List.of(
				new Object[]{
						"IntLabeler", new IntLabelerFactory()
				},
				new Object[]{
						"LongLabeler", new LongLabelerFactory()
				},
				new Object[]{
						"BitSetLabeler", new BitSetLabelerFactory()
				});
	}

    @Test
    public void testAccessors() throws Exception {
        Instance instance = TestUtils.getInstanceFromFile(TestUtils.CHAPTER_3_DATA, 0);
        Labeler labeler = labelerFactory.createLabeler(instance, false, MissingDataCompare.MATCH);
        assertEquals(labeler.getCardinality(), 3);
		assertFalse(labeler.getIgnoreUnknowns());
        assertEquals(labeler.getMissingDataCompare(), MissingDataCompare.MATCH);
        assertEquals(labeler.getTestInstance(), instance);
    }

    /**
     * Test the default behavior for {@link Labeler#isIgnored(int)}.
     *
     * @throws Exception If there's a problem loading the Finnverb dataset
     */
    @Test
    public void testIsIgnored() throws Exception {
        Instance instance = TestUtils.getInstanceFromFile(TestUtils.FINNVERB, 0);

        Labeler labeler = labelerFactory.createLabeler(instance, false, MissingDataCompare.MATCH);
        for (int i = 0; i < instance.numAttributes(); i++)
            assertFalse(labeler.isIgnored(i));

        labeler = labelerFactory.createLabeler(instance, true, MissingDataCompare.MATCH);
        for (int i = 0; i < instance.numAttributes(); i++)
            if (instance.isMissing(i)) assertTrue(labeler.isIgnored(i));
            else assertFalse(labeler.isIgnored(i));
    }

    @Test
    public void testLabel() {
        Instances dataset = sixCardinalityData();
        Labeler labeler = labelerFactory.createLabeler(dataset.get(0), false, MissingDataCompare.MATCH);
        assertLabelEquals(new IntLabel(0b00000, 5), labeler.label(dataset.get(1)));
        assertLabelEquals(new IntLabel(0b10110, 5), labeler.label(dataset.get(2)));
        assertLabelEquals(new IntLabel(0b00011, 5), labeler.label(dataset.get(3)));
        assertLabelEquals(new IntLabel(0b10011, 5), labeler.label(dataset.get(4)));
        assertLabelEquals(new IntLabel(0b11111, 5), labeler.label(dataset.get(5)));
    }

    /**
     * Test with a different class index to make sure its location is not hard
     * coded.
	 */
    @Test
    public void testLabelWithAlternateClassIndex() {
        Instances dataset = sixCardinalityData();
        dataset.setClassIndex(2);
        Labeler labeler = labelerFactory.createLabeler(dataset.get(0), false, MissingDataCompare.MATCH);
        assertLabelEquals(new IntLabel(0b10100, 5), labeler.label(dataset.get(2)));
        assertLabelEquals(new IntLabel(0b00110, 5), labeler.label(dataset.get(3)));
        assertLabelEquals(new IntLabel(0b10110, 5), labeler.label(dataset.get(4)));
        assertLabelEquals(new IntLabel(0b11110, 5), labeler.label(dataset.get(5)));
        dataset.setClassIndex(dataset.numAttributes() - 1);
    }

    /**
     * Test that missing values are compared based on the input
     * {@link MissingDataCompare} value.
     */
    @Test
    public void testGetContextLabelMissingDataCompares() {
        Instances dataset = sixCardinalityData();
        Labeler labeler = labelerFactory.createLabeler(dataset.get(6), false, MissingDataCompare.MATCH);
        assertLabelEquals("MATCH: always matches", new IntLabel(0b00100, 5), labeler.label(dataset.get(0)));

        labeler = labelerFactory.createLabeler(dataset.get(6), false, MissingDataCompare.MISMATCH);
        assertLabelEquals("MISMATCH: always mismatches", new IntLabel(0b00101, 5), labeler.label(dataset.get(0)));

        labeler = labelerFactory.createLabeler(dataset.get(6), false, MissingDataCompare.VARIABLE);
        assertLabelEquals("VARIABLE: matches other unknowns", new IntLabel(0b00100, 5), labeler.label(dataset.get(7)));
        assertLabelEquals("VARIABLE: mismatches non-unknowns", new IntLabel(0b00111, 5), labeler.label(dataset.get(8)));
    }

	@Test
	public void testGetContextString() {
		Instances dataset = sixCardinalityData();
		Labeler labeler = labelerFactory.createLabeler(dataset.get(0), false, MATCH);
		Label label = new IntLabel(0b01011, 5);
		String actual = labeler.getContextString(label);
		assertEquals("a * v * *", actual);
	}

	@Test
	public void testGetInstanceAttsString() {
		Instances dataset = sixCardinalityData();
		Labeler labeler = labelerFactory.createLabeler(dataset.get(0), false, MATCH);
		String actual = labeler.getInstanceAttsString(dataset.get(0));
		assertEquals("a x v u s", actual);
	}

	@Test
	public void testGetContextStringWithIgnoredAttribute() {
		Instances dataset = sixCardinalityData();
		Labeler labeler = spy(labelerFactory.createLabeler(dataset.get(0), false, MATCH));
		Label label = new IntLabel(0b0011, 4);
		when(labeler.isIgnored(0)).thenReturn(true);
		String actual = labeler.getContextString(label);
		assertEquals("x v * *", actual);
	}

	@Test
	public void testGetInstanceAttsStringWithIgnoredAttribute() {
		Instances dataset = sixCardinalityData();
		Labeler labeler = spy(labelerFactory.createLabeler(dataset.get(0), false, MATCH));
		when(labeler.isIgnored(0)).thenReturn(true);
		String actual = labeler.getInstanceAttsString(dataset.get(0));
		assertEquals("x v u s", actual);
	}

	@Test
	public void testGetContextStringWithAlternativeClassIndex() {
		Instances dataset = sixCardinalityData();
		Labeler labeler = labelerFactory.createLabeler(dataset.get(0), false, MATCH);
		dataset.setClassIndex(2);
		Label label = new IntLabel(0b01011, 5);
		String actual = labeler.getContextString(label);
		assertEquals("a * u * *", actual);
	}

	@Test
	public void testGetInstanceAttsStringWithAlternativeClassIndex() {
		Instances dataset = sixCardinalityData();
		Labeler labeler = labelerFactory.createLabeler(dataset.get(0), false, MATCH);
		dataset.setClassIndex(2);
		String actual = labeler.getInstanceAttsString(dataset.get(0));
		assertEquals("a x u s r", actual);
	}

	@Test
	public void testGetLatticeTop() {
		int cardinality = labelerFactory.getMaximumCardinality();
		if (cardinality < 0) {
			// no maximum cardinality; use arbitrarily high value
			cardinality = 100;
		}
		Labeler labeler = labelerFactory.createLabeler(mockInstance(cardinality), false, MATCH);
		Label top = labeler.getLatticeTop();
		assertEquals("top cardinality should be " + cardinality, cardinality, top.getCardinality());
		for (int i = 0; i < cardinality; i++) {
			assertTrue("Attribute " + i + " should match", top.matches(i));
		}
		assertEquals("All attributes should match", cardinality, top.numMatches());
	}

	@Test
	public void testGetLatticeBottom() {
		int cardinality = labelerFactory.getMaximumCardinality();
		if (cardinality < 0) {
			// no maximum cardinality; use arbitrarily high value
			cardinality = 100;
		}
		Labeler labeler = labelerFactory.createLabeler(mockInstance(cardinality), false, MATCH);
		Label bottom = labeler.getLatticeBottom();
		assertEquals("bottom cardinality should be " + cardinality, cardinality, bottom.getCardinality());
		for (int i = 0; i < cardinality; i++) {
			assertFalse("Attribute " + i + " should not match", bottom.matches(i));
		}
		assertEquals("No attributes should match", 0, bottom.numMatches());
	}

    @Test
    public void testNumPartitions() throws Exception {
        // current behavior is to always limit the size of a label to 5
        // 3 features, 1 partition
        Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Labeler labeler = labelerFactory.createLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        assertEquals(1, labeler.numPartitions());

        // 8 features, 2 partitions
        data = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "1-2");
        labeler = labelerFactory.createLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        assertEquals(2, labeler.numPartitions());

        // 10 features, 2 partitions
        data = TestUtils.getDataSet(TestUtils.FINNVERB);
        labeler = labelerFactory.createLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        assertEquals(2, labeler.numPartitions());
    }

    /**
     * Tests the protected default partitioning scheme.
     */
    @Test
    public void testPartitions() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.FINNVERB);
        Labeler labeler = labelerFactory.createLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        Partition[] partitions = labeler.partitions();
        assertEquals(partitions.length, 2);
        assertPartitionEquals(partitions[0], 0, 5);
        assertPartitionEquals(partitions[1], 5, 5);

        data = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "1-2");
        labeler = labelerFactory.createLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        partitions = labeler.partitions();
        assertEquals(2, partitions.length);
        assertPartitionEquals(partitions[0], 0, 4);
        assertPartitionEquals(partitions[1], 4, 4);
    }

    private void assertPartitionEquals(Partition partition, int startIndex, int cardinality) {
        assertEquals(partition.getStartIndex(), startIndex);
        assertEquals(partition.getCardinality(), cardinality);
    }

    /**
     * Tests the default partitioning functionality.
     *
     * @throws Exception if there's an issue loading the Finnverb dataset
     */
    @Test
    public void testPartition() throws Exception {
        Instances data = TestUtils.getDataSet(TestUtils.FINNVERB);
        Labeler labeler = labelerFactory.createLabeler(data.get(0), false, MissingDataCompare.VARIABLE);
        Label label = labeler.label(data.get(1));
        assertLabelEquals(label, new IntLabel(0b0000110010, 10));
        assertEquals(labeler.numPartitions(), 2);

        assertLabelEquals(labeler.partition(label, 0), new IntLabel(0b10010, 5));
        assertLabelEquals(labeler.partition(label, 1), new IntLabel(0b00001, 5));
    }

    private void assertLabelEquals(String message, Label firstLabel, Label secondLabel) {
        assertTrue(message, TestUtils.labelEquivalent(firstLabel, secondLabel));
    }

    private void assertLabelEquals(Label firstLabel, Label secondLabel) {
        assertTrue(TestUtils.labelEquivalent(firstLabel, secondLabel));
    }
}
