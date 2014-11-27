package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.lazy.AM.AMconstants;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class SupracontextTest {

	private Supracontext empty;
	private static Instances dataset;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classes = new ArrayList<String>();
		classes.add("e");
		classes.add("r");
		atts.add(new Attribute("a"));
		atts.add(new Attribute("class", classes));
		dataset = new Instances("TestInstances", atts, 0);
		dataset.setClassIndex(dataset.numAttributes() - 1);

		double[][] data = new double[][] { new double[] { 1, 1 },
				new double[] { 1, 0 }, new double[] { 0, 0 } };
		for (double[] datum : data) {
			Instance instance = new DenseInstance(2, datum);
			dataset.add(instance);
		}
	}

	@Before
	public void setUp() {
		empty = new Supracontext();
	}

	private double DELTA = 1e-10;

	@Test
	public void testEmpty() {
		assertEquals(empty.getCount(), 0);
		assertEquals(empty.getData().length, 0);
		assertFalse(empty.hasData());
		assertEquals(empty.getNext(), null);
		assertEquals(empty.getOutcome(), AMconstants.EMPTY, DELTA);
		assertTrue(empty.isDeterministic());
		assertTrue(empty.equals(empty));
	}

	@Test
	public void testCount() {
		empty.incrementCount();
		assertEquals(empty.getCount(), 1);
		empty.decrementCount();
		assertEquals(empty.getCount(), 0);
		empty.setCount(10);
		assertEquals(empty.getCount(), 10);
	}

	@Test
	public void testData() {
		empty.setData(new int[] { 14, 13, 17 });
		assertTrue(empty.hasData());
		assertArrayEquals(empty.getData(), new int[] { 14, 13, 17 });
	}
	
	@Test
	public void testEquals() {
		Subcontext sub1 = new Subcontext(new Label(0b0, 1));
		sub1.add(dataset.get(0));
		Subcontext sub2 = new Subcontext(new Label(0b1, 1));
		sub2.add(dataset.get(1));
		
		empty.setData(new int[] {sub1.getIndex(), sub2.getIndex()});
		Supracontext testSupra = new Supracontext();
		testSupra.setData(new int[] {sub1.getIndex(), sub2.getIndex()});
		assertEquals(testSupra, empty);
		testSupra.setCount(2);
		assertNotEquals(testSupra, empty);
	}
	
	// I called it generational because it creates a new supra from an old one
	public void testGenerationalConstructor() {
		Subcontext sub1 = new Subcontext(new Label(0b0, 1));
		sub1.add(dataset.get(0));
		Subcontext sub2 = new Subcontext(new Label(0b1, 1));
		sub2.add(dataset.get(1));
		Subcontext sub3 = new Subcontext(new Label(0b0, 1));
		sub2.add(dataset.get(2));
		
		Supracontext testSupra1 = new Supracontext(empty, sub1, 99);
		Supracontext expected = new Supracontext();
		expected.setData(new int[]{sub1.getIndex()});
		assertEquals(testSupra1, expected);
		assertEquals(testSupra1.getIndex(), 99);
		assertTrue(empty.getNext() == testSupra1);
		assertEquals(testSupra1.getNext(), null);
		assertEquals(empty.getOutcome(), sub1.getOutcome(), DELTA);
		
		testSupra1.incrementCount();
		Supracontext testSupra2 = new Supracontext(testSupra1, sub2, 88);
		expected = new Supracontext();
		expected.setData(new int[]{sub1.getIndex(), sub2.getIndex()});
		assertEquals(testSupra2, expected);
		assertEquals(testSupra2.getIndex(), 88);
		assertTrue(testSupra1.getNext() == testSupra2);
		assertTrue(testSupra2.getNext() == null);
		assertEquals(testSupra2.getOutcome(), AMconstants.NONDETERMINISTIC, DELTA);
		assertFalse(testSupra2.isDeterministic());
		
		Supracontext testSupra3 = new Supracontext(testSupra1, sub3, 77);
		expected = new Supracontext();
		expected.setData(new int []{sub1.getIndex(), sub3.getIndex()});
		assertEquals(testSupra3, expected);
		assertTrue(testSupra1.getNext() == testSupra3);
		assertTrue(testSupra3.getNext() == null);
	}
}
