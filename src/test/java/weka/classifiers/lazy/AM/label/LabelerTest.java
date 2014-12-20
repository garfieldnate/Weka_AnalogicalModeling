package weka.classifiers.lazy.AM.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.Labeler.Partition;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Test all of the {@link Labeler} implementations.
 * 
 * @author Nathan Glenn
 * 
 */
@RunWith(Parameterized.class)
public class LabelerTest {
	@Parameter(0)
	public String testName;
	@Parameter(1)
	public Constructor<Labeler> labelerConstructor;

	/**
	 * @return A collection of parameter arrays for running tests:
	 *         <ol>
	 *         <li>arg[0] is the test name;</li>
	 *         <li>arg[1] is the {@link Constructor} for a {@link Labeler} class
	 *         to be tested.</li>
	 *         </ol>
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		Collection<Object[]> parameters = new ArrayList<>();

		// There are three kinds of labelers and associated labels
		@SuppressWarnings("serial")
		List<Class> labelerClasses = new ArrayList<Class>() {
			{
				add(IntLabeler.class);
				add(LongLabeler.class);
				add(BitSetLabeler.class);
			}
		};
		for (Class c : labelerClasses)
			parameters.add(new Object[] {
					c.getSimpleName(),
					c.getConstructor(MissingDataCompare.class, Instance.class,
							boolean.class) });

		return parameters;
	}

	@Test
	public void testAccessors() throws Exception {
		Instance instance = TestUtils.getInstanceFromFile(
				TestUtils.CHAPTER_3_DATA, 0);
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.MATCH, instance, false);
		assertEquals(labeler.getCardinality(), 3);
		assertEquals(labeler.getIgnoreUnknowns(), false);
		assertEquals(labeler.getMissingDataCompare(), MissingDataCompare.MATCH);
		assertEquals(labeler.getTestInstance(), instance);
	}

	/**
	 * Test the default behavior for {@link Labeler#isIgnored(int)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIsIgnored() throws Exception {
		Instance instance = TestUtils
				.getInstanceFromFile(TestUtils.FINNVERB, 0);

		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.MATCH, instance, false);
		for (int i = 0; i < instance.numAttributes(); i++)
			assertFalse(labeler.isIgnored(i));

		labeler = labelerConstructor.newInstance(MissingDataCompare.MATCH,
				instance, true);
		for (int i = 0; i < instance.numAttributes(); i++)
			if (instance.isMissing(i))
				assertTrue(labeler.isIgnored(i));
			else
				assertFalse(labeler.isIgnored(i));
	}

	@Test
	public void testLabel() throws Exception {
		Instances dataset = TestUtils.sixCardinalityData();
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.MATCH, dataset.get(0), false);
		assertLabelEquals(new IntLabel(0b00000, 5),
				labeler.label(dataset.get(1)));
		assertLabelEquals(new IntLabel(0b10110, 5),
				labeler.label(dataset.get(2)));
		assertLabelEquals(new IntLabel(0b00011, 5),
				labeler.label(dataset.get(3)));
		assertLabelEquals(new IntLabel(0b10011, 5),
				labeler.label(dataset.get(4)));
		assertLabelEquals(new IntLabel(0b11111, 5),
				labeler.label(dataset.get(5)));
	}

	/**
	 * Test with a different class index to make sure its location is not hard
	 * coded.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLabelWithAlternateClassIndex() throws Exception {
		Instances dataset = TestUtils.sixCardinalityData();
		dataset.setClassIndex(2);
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.MATCH, dataset.get(0), false);
		assertLabelEquals(new IntLabel(0b10100, 5),
				labeler.label(dataset.get(2)));
		assertLabelEquals(new IntLabel(0b00110, 5),
				labeler.label(dataset.get(3)));
		assertLabelEquals(new IntLabel(0b10110, 5),
				labeler.label(dataset.get(4)));
		assertLabelEquals(new IntLabel(0b11110, 5),
				labeler.label(dataset.get(5)));
		dataset.setClassIndex(dataset.numAttributes() - 1);

	}

	/**
	 * test that missing values are compared based on the input
	 * {@link MissingDataCompare} value.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetContextLabelMissingDataCompares() throws Exception {
		Instances dataset = TestUtils.sixCardinalityData();
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.MATCH, dataset.get(6), false);
		assertLabelEquals("MATCH: always matches", new IntLabel(0b00100, 5),
				labeler.label(dataset.get(0)));

		labeler = labelerConstructor.newInstance(MissingDataCompare.MISMATCH,
				dataset.get(6), false);
		assertLabelEquals("MISMATCH: always mismatches", new IntLabel(0b00101,
				5), labeler.label(dataset.get(0)));

		labeler = labelerConstructor.newInstance(MissingDataCompare.VARIABLE,
				dataset.get(6), false);
		assertLabelEquals("VARIABLE: matches other unknowns", new IntLabel(
				0b00100, 5), labeler.label(dataset.get(7)));
		assertLabelEquals("VARIABLE: mismatches non-unknowns", new IntLabel(
				0b00111, 5), labeler.label(dataset.get(8)));
	}

	@Test
	public void testNumPartitions() throws Exception {
		// current behavior is to always limit the size of a label to 5
		// 3 features, 1 partition
		Instances data = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.VARIABLE, data.get(0), false);
		assertEquals(1, labeler.numPartitions());

		// 8 features, 2 partitions
		data = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "1-2");
		labeler = labelerConstructor.newInstance(MissingDataCompare.VARIABLE,
				data.get(0), false);
		assertEquals(2, labeler.numPartitions());

		// 10 features, 2 partitions
		data = TestUtils.getDataSet(TestUtils.FINNVERB);
		labeler = labelerConstructor.newInstance(MissingDataCompare.VARIABLE,
				data.get(0), false);
		assertEquals(2, labeler.numPartitions());
	}

	/**
	 * Tests the protected default partitioning scheme.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPartitions() throws Exception {
		Instances data = TestUtils.getDataSet(TestUtils.FINNVERB);
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.VARIABLE, data.get(0), false);
		Partition[] partitions = labeler.partitions();
		assertEquals(partitions.length, 2);
		assertPartitionEquals(partitions[0], 0, 5);
		assertPartitionEquals(partitions[1], 5, 5);

		data = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "1-2");
		labeler = labelerConstructor.newInstance(MissingDataCompare.VARIABLE,
				data.get(0), false);
		partitions = labeler.partitions();
		assertEquals(2, partitions.length);
		assertPartitionEquals(partitions[0], 0, 4);
		assertPartitionEquals(partitions[1], 4, 4);
	}

	private void assertPartitionEquals(Partition partition, int startIndex,
			int cardinality) {
		assertEquals(partition.getStartIndex(), startIndex);
		assertEquals(partition.getCardinality(), cardinality);
	}

	/**
	 * Tests the default partitioning functionality.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPartition() throws Exception {
		Instances data = TestUtils.getDataSet(TestUtils.FINNVERB);
		Labeler labeler = labelerConstructor.newInstance(
				MissingDataCompare.VARIABLE, data.get(0), false);
		Label label = labeler.label(data.get(1));
		assertLabelEquals(label, new IntLabel(0b0000110010, 10));
		assertEquals(labeler.numPartitions(), 2);

		assertLabelEquals(labeler.partition(label, 0), new IntLabel(0b10010, 5));
		assertLabelEquals(labeler.partition(label, 1), new IntLabel(0b00001, 5));
	}

	private void assertLabelEquals(String message, Label firstLabel,
			Label secondLabel) {
		assertTrue(message, TestUtils.labelEquivalent(firstLabel, secondLabel));
	}

	private void assertLabelEquals(Label firstLabel, Label secondLabel) {
		assertTrue(TestUtils.labelEquivalent(firstLabel, secondLabel));
	}
}
