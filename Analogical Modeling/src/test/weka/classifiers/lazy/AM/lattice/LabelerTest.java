package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class LabelerTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	private static Instances dataset;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classes = new ArrayList<String>();
		classes.add("e");
		classes.add("r");
		atts.add(new Attribute("a"));
		atts.add(new Attribute("b"));
		atts.add(new Attribute("c"));
		atts.add(new Attribute("d"));
		atts.add(new Attribute("e"));
		atts.add(new Attribute("class", classes));
		dataset = new Instances("TestInstances", atts, 0);
		dataset.setClassIndex(dataset.numAttributes() - 1);

		double[][] data = new double[][] {
				new double[] { 0, 1, 2, 1, 1, 1 },
				new double[] { 0, 1, 2, 1, 1, 1 },
				new double[] { 2, 1, 1, 2, 1, 1 },
				new double[] { 0, 1, 2, 0, 2, 1 },
				new double[] { 2, 1, 2, 0, 2, 1 },
				new double[] { 1, 0, 1, 0, 0, 1 },
				// NaN means a missing attribute
				new double[] { 0, 1, 1, 1, Double.NaN, 1 },
				new double[] { 0, 1, 0, 1, Double.NaN, 1 },
				new double[] { 0, 1, 2, Double.NaN, 1, 1 } };
		for (double[] datum : data) {
			Instance instance = new DenseInstance(6, datum);
			dataset.add(instance);
		}
	}

	@Test
	public void testGetCardinality() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, dataset.get(0),
				false);
		assertEquals(labeler.getCardinality(), 5);
	}

	@Test
	public void testGetContextLabel() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, dataset.get(0),
				false);
		assertEquals(new Label(0b00000, 5),
				labeler.getContextLabel(dataset.get(1)));
		assertEquals(new Label(0b10110, 5),
				labeler.getContextLabel(dataset.get(2)));
		assertEquals(new Label(0b00011, 5),
				labeler.getContextLabel(dataset.get(3)));
		assertEquals(new Label(0b10011, 5),
				labeler.getContextLabel(dataset.get(4)));
		assertEquals(new Label(0b11111, 5),
				labeler.getContextLabel(dataset.get(5)));
	}

	@Test
	public void testGetContextLabelMissingDataCompares() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, dataset.get(6),
				false);
		assertEquals("MATCH: always matches", new Label(0b00100, 5),
				labeler.getContextLabel(dataset.get(0)));

		labeler = new Labeler(MissingDataCompare.MISMATCH, dataset.get(6),
				false);
		assertEquals("MISMATCH: always mismatches", new Label(0b00101, 5),
				labeler.getContextLabel(dataset.get(0)));

		labeler = new Labeler(MissingDataCompare.VARIABLE, dataset.get(6),
				false);
		assertEquals("VARIABLE: matches other unknowns", new Label(0b00100, 5),
				labeler.getContextLabel(dataset.get(7)));
		assertEquals("VARIABLE: mismatches non-unknowns",
				new Label(0b00101, 5), labeler.getContextLabel(dataset.get(8)));
	}

	@Test
	public void testIgnoreUnknowns() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, dataset.get(6),
				true);
		assertEquals("IGNORE: unknown attributes removed from label",
				labeler.getContextLabel(dataset.get(5)), new Label(0b1101, 4));
		assertEquals("IGNORE: mdc used for data unknowns",
				labeler.getContextLabel(dataset.get(8)), new Label(0b0010, 4));
		// TODO: test missing data labeling
	}

	@Test
	public void testGetMasks() {
		LabelMask[] masks = Labeler.getMasks(2, 5);
		assertEquals(2, masks.length);
		assertEquals(3, masks[0].getCardinality());
		assertEquals(2, masks[1].getCardinality());

		assertEquals(new Label(0b111, 3), masks[0].mask(new Label(0b11111, 5)));
		assertEquals(new Label(0b000, 3), masks[0].mask(new Label(0b00000, 5)));
		assertEquals(new Label(0b101, 3), masks[0].mask(new Label(0b11101, 5)));

		assertEquals(new Label(0b11, 2), masks[1].mask(new Label(0b11111, 5)));
		assertEquals(new Label(0b00, 2), masks[1].mask(new Label(0b00000, 5)));
		assertEquals(new Label(0b10, 2), masks[1].mask(new Label(0b10111, 5)));

		masks = Labeler.getMasks(4, 3);
		assertEquals("Number of masks does not exceed cardinality",
				masks.length, 3);
	}

}
