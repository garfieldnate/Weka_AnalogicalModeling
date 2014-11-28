package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;

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
		assertTrue(empty.getData().isEmpty());
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

	@SuppressWarnings("serial")
	@Test
	public void testData() {
		Label label = new Label(0b001, 3);
		final Subcontext sub1 = new Subcontext(label);
		final Subcontext sub2 = new Subcontext(label);
		final Subcontext sub3 = new Subcontext(label);
		empty.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		});
		assertTrue(empty.hasData());
		assertEquals(empty.getData(), new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		});
	}

	@SuppressWarnings("serial")
	@Test
	public void testEquals() {
		final Subcontext sub1 = new Subcontext(new Label(0b0, 1));
		sub1.add(dataset.get(0));
		final Subcontext sub2 = new Subcontext(new Label(0b1, 1));
		sub2.add(dataset.get(1));

		// equality depends only on the exact subcontexts contained
		empty.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		});
		Supracontext testSupra = new Supracontext();
		assertNotEquals(testSupra, empty);
		testSupra.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		});
		assertEquals(testSupra, empty);
		// count and outcome are not considered
		testSupra.setCount(2);
		assertEquals(testSupra, empty);
		empty.setCount(2);
		empty.setOutcome(5.0);
		assertEquals(testSupra, empty);
	}

	// I called it generational because it creates a new supra from an old one
	@SuppressWarnings("serial")
	public void testGenerationalConstructor() {
		final Subcontext sub1 = new Subcontext(new Label(0b0, 1));
		sub1.add(dataset.get(0));
		final Subcontext sub2 = new Subcontext(new Label(0b1, 1));
		sub2.add(dataset.get(1));
		sub2.add(dataset.get(2));
		final Subcontext sub3 = new Subcontext(new Label(0b0, 1));

		Supracontext testSupra1 = new Supracontext(empty, sub1, 99);
		Supracontext expected = new Supracontext();
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
			}
		});
		assertEquals(testSupra1, expected);
		assertEquals(testSupra1, 99);
		assertTrue(empty.getNext() == testSupra1);
		assertEquals(testSupra1.getNext(), null);
		assertEquals(empty.getOutcome(), sub1.getOutcome(), DELTA);

		testSupra1.incrementCount();
		Supracontext testSupra2 = new Supracontext(testSupra1, sub2, 88);
		expected = new Supracontext();
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		});
		assertEquals(testSupra2, expected);
		assertEquals(testSupra2, 88);
		assertTrue(testSupra1.getNext() == testSupra2);
		assertTrue(testSupra2.getNext() == null);
		assertEquals(testSupra2.getOutcome(), AMconstants.NONDETERMINISTIC,
				DELTA);
		assertFalse(testSupra2.isDeterministic());

		Supracontext testSupra3 = new Supracontext(testSupra1, sub3, 77);
		expected = new Supracontext();
		expected.setData(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub3);
			}
		});
		assertEquals(testSupra3, expected);
		assertTrue(testSupra1.getNext() == testSupra3);
		assertTrue(testSupra3.getNext() == null);
	}
}
