package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.Label;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class SupracontextTest {
	private static Instances dataset;
	// contains subs with these outcomes: nondeterministic, nondeterminstic, 1,
	// 0, 0
	private static List<Subcontext> subs;

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

		subs = new ArrayList<>();
		Subcontext sub = new Subcontext(new IntLabel(0b0, 1));
		sub.add(dataset.get(0));
		sub.add(dataset.get(1));
		subs.add(sub);

		sub = new Subcontext(new IntLabel(0b0, 1));
		sub.add(dataset.get(0));
		sub.add(dataset.get(2));
		subs.add(sub);

		sub = new Subcontext(new IntLabel(0b0, 1));
		sub.add(dataset.get(0));
		subs.add(sub);

		sub = new Subcontext(new IntLabel(0b0, 1));
		sub.add(dataset.get(1));
		subs.add(sub);

		sub = new Subcontext(new IntLabel(0b0, 1));
		sub.add(dataset.get(2));
		subs.add(sub);
	}
	
	private double DELTA = 1e-10;

	@Test
	public void testEmpty() {
		Supracontext testSupra = new Supracontext();
		assertEquals(testSupra.getCount(), BigInteger.ZERO);
		assertTrue(testSupra.getData().isEmpty());
		assertFalse(testSupra.hasData());
		assertEquals(testSupra.getNext(), null);
		assertEquals(testSupra.getOutcome(), AMUtils.EMPTY, DELTA);
		assertFalse(testSupra.isHeterogeneous());
		assertTrue(testSupra.equals(testSupra));
	}

	@Test
	public void testCount() {
		Supracontext testSupra = new Supracontext();
		testSupra.incrementCount();
		assertEquals(testSupra.getCount(), BigInteger.ONE);
		testSupra.decrementCount();
		assertEquals(testSupra.getCount(), BigInteger.ZERO);

		Supracontext testSupra2 = new Supracontext();
		testSupra2.setCount(BigInteger.valueOf(5));
		assertEquals(testSupra2.getCount(), BigInteger.valueOf(5));
	}

	@SuppressWarnings("serial")
	@Test
	public void testData() {
		Supracontext testSupra = new Supracontext();
		assertEquals(testSupra.getData(), new HashSet<Subcontext>());
		Label label = new IntLabel(0b001, 3);
		final Subcontext sub1 = new Subcontext(label);
		final Subcontext sub2 = new Subcontext(label);
		final Subcontext sub3 = new Subcontext(label);
		testSupra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		}, BigInteger.ZERO);
		assertTrue(testSupra.hasData());
		assertEquals(testSupra.getData(), new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		});
	}
	
	//make sure that the subs used for testing are set up properly
	@Test
	public void testSetup(){
		assertTrue(subs.get(0).isNondeterministic());
		assertTrue(subs.get(1).isNondeterministic());
		assertFalse(subs.get(2).isNondeterministic());
		assertFalse(subs.get(3).isNondeterministic());
		assertFalse(subs.get(4).isNondeterministic());
	}

	@Test
	public void testWouldBeHeterogeneous() {
		//one sub, even nondeterministic, does not make a supra heterogeneous
		assertCausesHeterogeneity(new Supracontext(), subs.get(0), false);
		assertCausesHeterogeneity(new Supracontext(), subs.get(2), false);

		//two subs of same outcome do not make it heterogeneous
		Supracontext testSupra = new Supracontext();
		testSupra.add(subs.get(3));
		assertCausesHeterogeneity(testSupra, subs.get(4), false);

		// conditions for heterogeneity:
		// nondeterministic sub with anything else
		testSupra = new Supracontext();
		testSupra.add(subs.get(0));
		assertCausesHeterogeneity(testSupra, subs.get(1), true);

		testSupra = new Supracontext();
		testSupra.add(subs.get(0));
		assertCausesHeterogeneity(testSupra, subs.get(2), true);

		// subs with differing outcomes
		testSupra = new Supracontext();
		testSupra.add(subs.get(2));
		assertCausesHeterogeneity(testSupra, subs.get(3), true);
	}
	
	private void assertCausesHeterogeneity(Supracontext supra, Subcontext sub, boolean causes){
		assertFalse(supra.isHeterogeneous());
		assertEquals(supra.wouldBeHetero(sub), causes);
		supra.add(sub);
		assertEquals(supra.isHeterogeneous(), causes);
	}

	@Test
	public void testIsHeterogeneousConstructor() {
		BigInteger count = BigInteger.ZERO;
		Set<Subcontext> subSet = new HashSet<>();
		
		//empty supra is never heterogeneous
		assertFalse(new Supracontext(subSet, count).isHeterogeneous());
		
		//supra with two subs of the same outcome is not heterogeneous
		subSet = new HashSet<>();
		subSet.add(subs.get(3));
		subSet.add(subs.get(4));
		assertFalse(new Supracontext(subSet, count).isHeterogeneous());

		// conditions for heterogeneity:
		// nondeterministic sub with anything else
		subSet = new HashSet<>();
		subSet.add(subs.get(0));
		subSet.add(subs.get(1));
		assertTrue(new Supracontext(subSet, count).isHeterogeneous());

		subSet = new HashSet<>();
		subSet.add(subs.get(0));
		subSet.add(subs.get(2));
		assertTrue(new Supracontext(subSet, count).isHeterogeneous());

		// subs with differing outcomes
		subSet = new HashSet<>();
		subSet.add(subs.get(2));
		subSet.add(subs.get(3));
		assertTrue(new Supracontext(subSet, count).isHeterogeneous());
	}

	@SuppressWarnings("serial")
	@Test
	public void testEquals() {
		Supracontext testSupra = new Supracontext();
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
		}, BigInteger.ZERO);
		assertNotEquals(supra, testSupra);
		Supracontext testSupra2 = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.ZERO);
		assertEquals(testSupra2, supra);
		// count and outcome are not considered
		testSupra2 = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.valueOf(2));
		assertEquals(testSupra2, supra);
	}

	// Test the constructor that creates a new supra from an old one
	@SuppressWarnings("serial")
	public void testGenerationalConstructor() {
		Supracontext testSupra = new Supracontext();
		final Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
		sub1.add(dataset.get(0));
		final Subcontext sub2 = new Subcontext(new IntLabel(0b1, 1));
		sub2.add(dataset.get(1));
		sub2.add(dataset.get(2));
		final Subcontext sub3 = new Subcontext(new IntLabel(0b0, 1));

		Supracontext testSupra1 = new Supracontext(testSupra, sub1, 99);
		Supracontext expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
			}
		}, BigInteger.ZERO);
		assertEquals(testSupra1, expected);
		assertEquals(testSupra1, 99);
		assertTrue(testSupra.getNext() == testSupra1);
		assertEquals(testSupra1.getNext(), null);
		assertEquals(testSupra.getOutcome(), sub1.getOutcome(), DELTA);

		testSupra1.incrementCount();
		Supracontext testSupra2 = new Supracontext(testSupra1, sub2, 88);
		expected = new Supracontext();
		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.ZERO);
		assertEquals(testSupra2, expected);
		assertEquals(testSupra2, 88);
		assertTrue(testSupra1.getNext() == testSupra2);
		assertTrue(testSupra2.getNext() == null);
		assertEquals(testSupra2.getOutcome(), AMUtils.NONDETERMINISTIC, DELTA);

		Supracontext testSupra3 = new Supracontext(testSupra1, sub3, 77);
		expected = new Supracontext();
		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub3);
			}
		}, BigInteger.ZERO);
		assertEquals(testSupra3, expected);
		assertTrue(testSupra1.getNext() == testSupra3);
		assertTrue(testSupra3.getNext() == null);
	}
}
