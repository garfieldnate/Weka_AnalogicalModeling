package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import weka.classifiers.lazy.AnalogicalModeling;
import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Test the data contained in AnalogicalSet after classifying the chapter 3
 * data.
 * 
 * @author Nathan Glenn
 * 
 */
public class AnalogicalSetTest {

	private static Instances train;
	private static Instance test;
	private static AnalogicalSet as;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		test = train.get(0);
		train.remove(0);

		AnalogicalModeling am = new AnalogicalModeling();
		am.buildClassifier(train);
		am.distributionForInstance(test);

		as = am.getAnalogicalSet();
	}

	@Test
	public void exemplarEffectsTest() {
		Map<Instance, BigDecimal> effects = as.getExemplarEffectMap();
		assertEquals(effects.size(), 4);
		// we have to do it this long way because Instance implements
		// equals but not hashCode or comparable(!)
		for (Entry<Instance, BigDecimal> entry : effects.entrySet()) {
			Instance i = entry.getKey();
			if (i.equals(train.get(0)))
				assertEquals(entry.getValue(), new BigDecimal(
						"0.3076923077923077"));
			if (i.equals(train.get(2)))
				assertEquals(entry.getValue(), new BigDecimal(
						"0.15384615384615385"));
			if (i.equals(train.get(3)))
				assertEquals(entry.getValue(), new BigDecimal(
						"0.23076923076923078"));
			if (i.equals(train.get(4)))
				assertEquals(entry.getValue(), new BigDecimal(
						"0.307692307692307"));
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void pointersTest() {
		assertEquals(as.getTotalPointers(), BigInteger.valueOf(13));
		assertEquals(as.getClassPointers(), new HashMap<String, BigInteger>() {
			{
				put("r", BigInteger.valueOf(9));
				put("e", BigInteger.valueOf(4));
			}
		});
		Map<Instance, BigInteger> pointers = as.getExemplarPointers();
		assertEquals(pointers.size(), 4);
		// we can't do effects.get(train.get(x)) because Instance implements
		// equals but not hashCode or comparable(!)
		for (Entry<Instance, BigInteger> entry : pointers.entrySet()) {
			Instance i = entry.getKey();
			if (i.equals(train.get(0)))
				assertEquals(entry.getValue(), BigInteger.valueOf(4));
			if (i.equals(train.get(2)))
				assertEquals(entry.getValue(), BigInteger.valueOf(2));
			if (i.equals(train.get(3)))
				assertEquals(entry.getValue(), BigInteger.valueOf(3));
			if (i.equals(train.get(4)))
				assertEquals(entry.getValue(), BigInteger.valueOf(4));
		}
	}

	@Test
	@SuppressWarnings("serial")
	public void classDistributionTest() {
		Map<String, BigDecimal> distribution = as.getClassLikelihood();

		assertEquals(distribution.size(), 2);
		// test to 10 decimal places, the number used by AMUtils.mathContext
		assertEquals(distribution.get("r"), new BigDecimal("0.6923076923"));
		assertEquals(distribution.get("e"), new BigDecimal("0.3076923077"));

		assertEquals(as.getPredictedClasses(), new HashSet<String>() {
			{
				add("r");
			}
		});
		assertEquals(as.getClassProbability(), new BigDecimal("0.6923076923"));
	}

	@Test
	public void classifiedExTest() {
		assertEquals(as.getClassifiedEx(), test);
	}
	// TODO: test toString

}
