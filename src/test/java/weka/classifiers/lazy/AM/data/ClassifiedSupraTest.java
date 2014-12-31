package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

public class ClassifiedSupraTest {
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

	private final double DELTA = 1e-10;

	@Test
	public void testDefaultContents() {
		ClassifiedSupra testSupra = new ClassifiedSupra();
		assertTrue(testSupra.getData().isEmpty());
		assertTrue(testSupra.isEmpty());
		// AMUtils.UNKNOWN is Double.NaN
		assertTrue(Double.isNaN(testSupra.getOutcome()));
		assertFalse(testSupra.isHeterogeneous());
		assertTrue(testSupra.equals(testSupra));
	}

	// TODO: remove this test, and instead don't have a constructor that takes
	// data like this (add() is tested in SupracontextTest).
	@SuppressWarnings("serial")
	@Test
	public void testData() {
		ClassifiedSupra testSupra = new ClassifiedSupra();
		assertEquals(testSupra.getData(), new HashSet<Subcontext>());
		Label label = new IntLabel(0b001, 3);
		final Subcontext sub1 = new Subcontext(label);
		final Subcontext sub2 = new Subcontext(label);
		final Subcontext sub3 = new Subcontext(label);
		testSupra = new ClassifiedSupra(new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		}, BigInteger.ZERO);
		assertFalse(testSupra.isEmpty());
		assertEquals(testSupra.getData(), new HashSet<Subcontext>() {
			{
				add(sub1);
				add(sub2);
				add(sub3);
			}
		});
	}

	// make sure that the subs used for testing are set up properly
	@Test
	public void testSetup() {
		assertTrue(subs.get(0).isNondeterministic());
		assertTrue(subs.get(1).isNondeterministic());
		assertFalse(subs.get(2).isNondeterministic());
		assertFalse(subs.get(3).isNondeterministic());
		assertFalse(subs.get(4).isNondeterministic());
	}

	@Test
	public void testWouldBeHeterogeneous() {
		// one sub, even nondeterministic, does not make a supra heterogeneous
		assertCausesHeterogeneity(new ClassifiedSupra(), subs.get(0), false);
		assertCausesHeterogeneity(new ClassifiedSupra(), subs.get(2), false);

		// two subs of same outcome do not make it heterogeneous
		ClassifiedSupra testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(3));
		assertCausesHeterogeneity(testSupra, subs.get(4), false);

		// conditions for heterogeneity:
		// nondeterministic sub with anything else
		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(0));
		assertCausesHeterogeneity(testSupra, subs.get(1), true);

		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(0));
		assertCausesHeterogeneity(testSupra, subs.get(2), true);

		// subs with differing outcomes
		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(2));
		assertCausesHeterogeneity(testSupra, subs.get(3), true);

		// supra is already heterogeneous
		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(2));
		testSupra.add(subs.get(3));
		assertTrue(testSupra.isHeterogeneous());
		assertTrue(testSupra.wouldBeHetero(subs.get(4)));
		testSupra.add(subs.get(4));
		assertTrue(testSupra.isHeterogeneous());
	}

	private void assertCausesHeterogeneity(ClassifiedSupra supra,
			Subcontext sub, boolean causes) {
		assertFalse(supra.isHeterogeneous());
		assertEquals(supra.wouldBeHetero(sub), causes);
		supra.add(sub);
		assertEquals(supra.isHeterogeneous(), causes);
	}

	@Test
	public void testIsHeterogeneous() {
		BigInteger count = BigInteger.ZERO;
		Set<Subcontext> subSet = new HashSet<>();

		// empty supra is never heterogeneous
		assertFalse(new ClassifiedSupra(subSet, count).isHeterogeneous());

		// supra with two subs of the same outcome is not heterogeneous
		subSet = new HashSet<>();
		subSet.add(subs.get(3));
		subSet.add(subs.get(4));
		assertFalse(new ClassifiedSupra(subSet, count).isHeterogeneous());

		// conditions for heterogeneity:
		// nondeterministic sub with anything else
		subSet = new HashSet<>();
		subSet.add(subs.get(0));
		subSet.add(subs.get(1));
		assertTrue(new ClassifiedSupra(subSet, count).isHeterogeneous());

		subSet = new HashSet<>();
		subSet.add(subs.get(0));
		subSet.add(subs.get(2));
		assertTrue(new ClassifiedSupra(subSet, count).isHeterogeneous());

		// subs with differing outcomes
		subSet = new HashSet<>();
		subSet.add(subs.get(2));
		subSet.add(subs.get(3));
		assertTrue(new ClassifiedSupra(subSet, count).isHeterogeneous());
	}

	@Test
	public void testOutcome() {
		BigInteger count = BigInteger.ZERO;
		Set<Subcontext> subSet = new HashSet<>();
		ClassifiedSupra testSupra;

		subSet = new HashSet<>();
		subSet.add(subs.get(3));// 0
		subSet.add(subs.get(4));// 0
		assertEquals(new ClassifiedSupra(subSet, count).getOutcome(), 0.0,
				DELTA);
		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(3));// 0
		testSupra.add(subs.get(4));// 0
		assertEquals(testSupra.getOutcome(), 0.0, DELTA);

		subSet = new HashSet<>();
		subSet.add(subs.get(2));// 1
		assertEquals(new ClassifiedSupra(subSet, count).getOutcome(), 1.0,
				DELTA);
		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(2));// 1
		assertEquals(testSupra.getOutcome(), 1.0, DELTA);

		subSet = new HashSet<>();
		subSet.add(subs.get(0));// non-deterministic
		assertEquals(new ClassifiedSupra(subSet, count).getOutcome(),
				AMUtils.NONDETERMINISTIC, DELTA);
		testSupra = new ClassifiedSupra();
		testSupra.add(subs.get(0));// non-deterministic
		assertEquals(testSupra.getOutcome(), AMUtils.NONDETERMINISTIC, DELTA);
	}
}
