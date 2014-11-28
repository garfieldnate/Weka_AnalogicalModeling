package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class MissingDataCompareTest {
	private static Instances dataset;
	private static ArrayList<Instance> instances;
	private static Attribute att;

	@BeforeClass
	public static void setupBeforeClass() {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		att = new Attribute("a");
		atts.add(att);
		
		dataset = new Instances("TestInstances", atts, 0);
		dataset.setClassIndex(dataset.numAttributes() - 1);

		double[][] data = new double[][] {
				new double[] { Double.NaN },
				new double[] { 0 } };
		instances = new ArrayList<>();
		for (double[] datum : data) {
			Instance instance = new DenseInstance(6, datum);
			instance.setDataset(dataset);
			instances.add(instance);
		}
	}

	@Test
	public void testMatch() {
		MissingDataCompare mc = MissingDataCompare.MATCH;
		assertTrue(mc.outcome(instances.get(0), instances.get(0), att) == 0);
		assertTrue(mc.outcome(instances.get(0), instances.get(1), att) == 0);
		assertTrue(mc.outcome(instances.get(1), instances.get(0), att) == 0);
	}

	@Test
	public void testMismatch() {
		MissingDataCompare mc = MissingDataCompare.MISMATCH;
		assertTrue(mc.outcome(instances.get(0), instances.get(0), att) == 1);
		assertTrue(mc.outcome(instances.get(0), instances.get(1), att) == 1);
		assertTrue(mc.outcome(instances.get(1), instances.get(0), att) == 1);
	}

	@Test
	public void testVariable() {
		MissingDataCompare mc = MissingDataCompare.VARIABLE;
		assertTrue(mc.outcome(instances.get(0), instances.get(0), att) == 0);
		assertTrue(mc.outcome(instances.get(0), instances.get(1), att) == 1);
		assertTrue(mc.outcome(instances.get(1), instances.get(0), att) == 1);
	}

}
