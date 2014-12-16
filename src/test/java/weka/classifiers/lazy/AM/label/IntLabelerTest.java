package weka.classifiers.lazy.AM.label;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instances;

public class IntLabelerTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	private static Instances dataset;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dataset = TestUtils.sixCardinalityData();
	}

	@Test
	public void testGetCardinality() {
		Labeler labeler = new IntLabeler(MissingDataCompare.MATCH,
				dataset.get(0), false);
		assertEquals(labeler.getCardinality(), 5);
	}

	@Test
	public void testGetContextLabel() {
		Labeler labeler = new IntLabeler(MissingDataCompare.MATCH,
				dataset.get(0), false);
		assertEquals(new IntLabel(0b00000, 5),
				labeler.label(dataset.get(1)));
		assertEquals(new IntLabel(0b10110, 5),
				labeler.label(dataset.get(2)));
		assertEquals(new IntLabel(0b00011, 5),
				labeler.label(dataset.get(3)));
		assertEquals(new IntLabel(0b10011, 5),
				labeler.label(dataset.get(4)));
		assertEquals(new IntLabel(0b11111, 5),
				labeler.label(dataset.get(5)));

		// now test with a different class index, to make sure it's not hard
		// coded
		dataset.setClassIndex(2);
		labeler = new IntLabeler(MissingDataCompare.MATCH, dataset.get(0),
				false);
		assertEquals(new IntLabel(0b10100, 5),
				labeler.label(dataset.get(2)));
		assertEquals(new IntLabel(0b00110, 5),
				labeler.label(dataset.get(3)));
		assertEquals(new IntLabel(0b10110, 5),
				labeler.label(dataset.get(4)));
		assertEquals(new IntLabel(0b11110, 5),
				labeler.label(dataset.get(5)));
		dataset.setClassIndex(dataset.numAttributes() - 1);
	}

	@Test
	public void testGetContextLabelMissingDataCompares() {
		Labeler labeler = new IntLabeler(MissingDataCompare.MATCH,
				dataset.get(6), false);
		assertEquals("MATCH: always matches", new IntLabel(0b00100, 5),
				labeler.label(dataset.get(0)));

		labeler = new IntLabeler(MissingDataCompare.MISMATCH, dataset.get(6),
				false);
		assertEquals("MISMATCH: always mismatches", new IntLabel(0b00101, 5),
				labeler.label(dataset.get(0)));

		labeler = new IntLabeler(MissingDataCompare.VARIABLE, dataset.get(6),
				false);
		assertEquals("VARIABLE: matches other unknowns", new IntLabel(0b00100,
				5), labeler.label(dataset.get(7)));
		assertEquals("VARIABLE: mismatches non-unknowns", new IntLabel(0b00111,
				5), labeler.label(dataset.get(8)));
	}

	@Test
	public void testIgnoreUnknowns() {
		Labeler labeler = new IntLabeler(MissingDataCompare.MATCH,
				dataset.get(6), true);
		assertEquals("IGNORE: unknown attributes removed from label",
				labeler.label(dataset.get(5)),
				new IntLabel(0b1101, 4));
		assertEquals("IGNORE: mdc used for data unknowns",
				labeler.label(dataset.get(8)),
				new IntLabel(0b0010, 4));
	}

	// @Test
	// public void testClassNotLastAttribute() {
	// TODO:
	// }

	@Test
	public void testConstructorCardinalityTooHigh() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception
				.expectMessage("max cardinality for this labeler is 32; input was 35");
		Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
		new IntLabeler(MissingDataCompare.MATCH, data.get(0), false);
	}

}
