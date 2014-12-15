package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
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
		assertEquals(empty.getCount(), BigInteger.ZERO);
		assertTrue(empty.getData().isEmpty());
		assertFalse(empty.hasData());
		assertEquals(empty.getNext(), null);
		assertEquals(empty.getOutcome(), AMUtils.EMPTY, DELTA);
		assertTrue(empty.isDeterministic());
		assertTrue(empty.equals(empty));
	}

	@Test
	public void testCount() {
		empty.incrementCount();
		assertEquals(empty.getCount(), BigInteger.ONE);
		empty.decrementCount();
		assertEquals(empty.getCount(), BigInteger.ZERO);
		
		Supracontext testSupra = new Supracontext(new HashSet<Subcontext>(), BigInteger.valueOf(4), 0);
		assertEquals(testSupra.getCount(), BigInteger.valueOf(4));
	}

	@SuppressWarnings("serial")
	@Test
	public void testData() {
		assertEquals(empty.getData(), new HashSet<Subcontext>());
		IntLabel label = new IntLabel(0b001, 3);
		final Subcontext sub1 = new Subcontext(label);
		final Subcontext sub2 = new Subcontext(label);
		final Subcontext sub3 = new Subcontext(label);
		Supracontext supra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		}, BigInteger.ZERO, 0);
		assertTrue(supra.hasData());
		assertEquals(supra.getData(), new HashSet<Subcontext>() {
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
		final Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
		sub1.add(dataset.get(0));
		final Subcontext sub2 = new Subcontext(new IntLabel(0b1, 1));
		sub2.add(dataset.get(1));

		// equality depends only on the exact subcontexts contained
		Supracontext supra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.ZERO, 0);
		assertNotEquals(supra, empty);
		Supracontext testSupra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.ZERO, 0);
		assertEquals(testSupra, supra);
		// count and outcome are not considered
		testSupra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.valueOf(2), 5);
		assertEquals(testSupra, supra);
	}

	// I called it generational because it creates a new supra from an old one
	@SuppressWarnings("serial")
	public void testGenerationalConstructor() {
		final Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
		sub1.add(dataset.get(0));
		final Subcontext sub2 = new Subcontext(new IntLabel(0b1, 1));
		sub2.add(dataset.get(1));
		sub2.add(dataset.get(2));
		final Subcontext sub3 = new Subcontext(new IntLabel(0b0, 1));

		Supracontext testSupra1 = new Supracontext(empty, sub1, 99);
		Supracontext expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
			}
		}, BigInteger.ZERO, 0);
		assertEquals(testSupra1, expected);
		assertEquals(testSupra1, 99);
		assertTrue(empty.getNext() == testSupra1);
		assertEquals(testSupra1.getNext(), null);
		assertEquals(empty.getOutcome(), sub1.getOutcome(), DELTA);

		testSupra1.incrementCount();
		Supracontext testSupra2 = new Supracontext(testSupra1, sub2, 88);
		expected = new Supracontext();
		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.ZERO, 0);
		assertEquals(testSupra2, expected);
		assertEquals(testSupra2, 88);
		assertTrue(testSupra1.getNext() == testSupra2);
		assertTrue(testSupra2.getNext() == null);
		assertEquals(testSupra2.getOutcome(), AMUtils.NONDETERMINISTIC, DELTA);
		assertFalse(testSupra2.isDeterministic());

		Supracontext testSupra3 = new Supracontext(testSupra1, sub3, 77);
		expected = new Supracontext();
		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub3);
			}
		}, BigInteger.ZERO, 0);
		assertEquals(testSupra3, expected);
		assertTrue(testSupra1.getNext() == testSupra3);
		assertTrue(testSupra3.getNext() == null);
	}
}
