package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
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
		train = TestUtils.chapter3Train();
		test = TestUtils.chapter3Test();

		AnalogicalModeling am = new AnalogicalModeling();
		am.buildClassifier(train);
		am.distributionForInstance(test);

		as = am.getAnalogicalSet();
	}

	private static final double DELTA = 1e-10;

	@Test
	public void exemplarEffectsTest() {
		Map<Instance, Double> effects = as.getExemplarEffectMap();
		assertEquals(effects.size(), 4);
		// we have to do it this long way because Instance implements
		// equals but not hashCode or comparable(!)
		for (Entry<Instance, Double> entry : effects.entrySet()) {
			Instance i = entry.getKey();
			if (i.equals(train.get(0)))
				assertEquals(entry.getValue(), 0.3076923077923077, DELTA);
			if (i.equals(train.get(2)))
				assertEquals(entry.getValue(), 0.15384615384615385, DELTA);
			if (i.equals(train.get(3)))
				assertEquals(entry.getValue(), 0.23076923076923078, DELTA);
			if (i.equals(train.get(4)))
				assertEquals(entry.getValue(), 0.307692307692307, DELTA);
		}
	}

	@SuppressWarnings("serial")
	@Test
	public void pointersTest() {
		assertEquals(as.getTotalPointers(), 13);
		assertEquals(as.getClassPointers(), new HashMap<String, Integer>() {
			{
				put("r", 9);
				put("e", 4);
			}
		});
		Map<Instance, Integer> pointers = as.getExemplarPointers();
		assertEquals(pointers.size(), 4);
		// we can't do effects.get(train.get(x)) because Instance implements
		// equals but not hashCode or comparable(!)
		for (Entry<Instance, Integer> entry : pointers.entrySet()) {
			Instance i = entry.getKey();
			if (i.equals(train.get(0)))
				assertEquals(entry.getValue(), new Integer(4));
			if (i.equals(train.get(2)))
				assertEquals(entry.getValue(), new Integer(2));
			if (i.equals(train.get(3)))
				assertEquals(entry.getValue(), new Integer(3));
			if (i.equals(train.get(4)))
				assertEquals(entry.getValue(), new Integer(4));
		}
	}

	@Test
	public void classDistributionTest() {
		Map<String, Double> distribution = as.getClassLikelihood();
		
		assertEquals(distribution.size(), 2);
		assertEquals(distribution.get("r"), 0.6923076923076923, DELTA);
		assertEquals(distribution.get("e"), 0.3076923076923077, DELTA);

		assertEquals(as.getPredictedClass(), "r");
		assertEquals(as.getClassProbability(), 0.6923076923076923, DELTA);
	}
	
	@Test
	public void classifiedExTest() {
		assertEquals(as.getClassifiedEx(), test);
	}
	//TODO: test toString

}
