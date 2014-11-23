package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

//TODO: next finish testing labels. Will need some unknown data for this to work. Then look at the todo file.
public class LabelerTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();
	private static Instances dataset;
	private static List<Instance> exemplars;

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
				new double[] { 0, 1, 2, 1, Double.NaN, 1 },
				new double[] { 0, 1, 2, Double.NaN, 1, 1 } };
		exemplars = new ArrayList<>();
		for (double[] datum : data) {
			Instance instance = new DenseInstance(6, datum);
			instance.setDataset(dataset);
			exemplars.add(instance);
		}
	}

	@Test
	public void testGetCardinality() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, exemplars.get(0));
		assertEquals(labeler.getCardinality(), 5);
	}

	@Test
	public void testGetContextLabel() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				exemplars.get(0));
		assertEquals(0b00000, labeler.getContextLabel(exemplars.get(1)));
		System.out.println(Integer.toBinaryString(labeler.getContextLabel(exemplars.get(2))));
		assertEquals(0b10110, labeler.getContextLabel(exemplars.get(2)));
		assertEquals(0b00011, labeler.getContextLabel(exemplars.get(3)));
		assertEquals(0b10011, labeler.getContextLabel(exemplars.get(4)));
		assertEquals(0b11111, labeler.getContextLabel(exemplars.get(5)));
	}
	
	@Test
	public void testGetContextLabelWithMissingData() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				exemplars.get(6));
		//TODO: test missing data labeling
	}

	@Test
	public void testGetMasks() {
		LabelMask[] masks = Labeler.getMasks(2, 5);
		assertEquals(2, masks.length);
		assertEquals(3, masks[0].getCardinality());
		assertEquals(2, masks[1].getCardinality());
		
		assertEquals(0b111, masks[0].mask(0b11111));
		assertEquals(0b000, masks[0].mask(0b00000));
		assertEquals(0b101, masks[0].mask(0b11101));

		assertEquals(0b11, masks[1].mask(0b11111));
		assertEquals(0b00, masks[1].mask(0b00000));
		assertEquals(0b10, masks[1].mask(0b10111));
		
		masks = Labeler.getMasks(4, 3);
		assertEquals("Number of masks does not exceed cardinality", masks.length, 3);
	}

}
