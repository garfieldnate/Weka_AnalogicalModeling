/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package weka.classifiers.lazy;

import java.math.BigInteger;
import java.util.HashMap;

import junit.framework.TestSuite;

import org.junit.Test;

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AnalogicalSet;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Tests AnalogicalModeling.
 * 
 * @author <a href="mailto:garfieldnate@gmail.com">Nate Glenn</a>
 */
// TODO: see if this can be parameterized for parallel/non-parallel
public class AnalogicalModelingTest extends AbstractClassifierTest {
	public AnalogicalModelingTest(String name) {
		super(name);
		// DEBUG = true;
	}

	/** Creates a default AnalogicalModeling */
	@Override
	public AnalogicalModeling getClassifier() {
		return new AnalogicalModeling();
	}

	private static final double DELTA = 1e-10;

	@Test
	public void testChapter3dataSerial() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		Instance test = train.get(0);
		train.remove(0);
		AnalogicalModeling am = getClassifier();
		am.buildClassifier(train);

		double[] prediction = am.distributionForInstance(test);
		assertEquals("distribution given for two classes", prediction.length, 2);
		// test to 10 decimals places, the number used by AMUtils.matchContext
		assertEquals(0.6923076923076923, prediction[0], DELTA);
		assertEquals(0.3076923076923077, prediction[1], DELTA);
	}

	/**
	 * Test accuracy with the finnverb dataset, a real data set with 10 features
	 * and lots of unknowns. First check the class pointers on one
	 * classification, then do a leave-one-out classification for the whole set
	 * and verify the accuracy.
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("serial")
	public void testFinnverb() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.FINNVERB);
		assertEquals(new HashMap<String, BigInteger>() {
			{
				put("A", BigInteger.valueOf(5094));
				put("C", BigInteger.valueOf(50));
			}
		}, leaveOneOut(train, 15).getClassPointers());

		int correct = 0;
		for (int i = 0; i < train.numInstances(); i++) {
			AnalogicalSet set = leaveOneOut(train, i);
			if (set.getPredictedClasses().contains(
					train.get(i).stringValue(train.classIndex())))
				correct++;
		}
		assertEquals(
				"Leave-one-out accuracy when classifying of finnverb dataset",
				correct, 160);
	}

	// larger set that forces use of LongLabel
	@Test
	@SuppressWarnings("serial")
	public void testSoybean() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.SOYBEAN);
		assertEquals(new HashMap<String, BigInteger>() {
			{
				put("anthracnose", BigInteger.valueOf(5358272));
				put("bacterial-blight", BigInteger.valueOf(2880000));
				put("alternarialeaf-spot", BigInteger.valueOf(3016836));
				put("powdery-mildew", BigInteger.valueOf(11869024));
				put("downy-mildew", BigInteger.valueOf(50688));
				put("charcoal-rot", new BigInteger("337300810464"));
				put("frog-eye-leaf-spot", BigInteger.valueOf(890880));
				put("phytophthora-rot", BigInteger.valueOf(2028992));
				put("brown-spot", BigInteger.valueOf(2134080));
				put("diaporthe-pod-&-stem-blight", BigInteger.valueOf(140));
				put("purple-seed-stain", BigInteger.valueOf(1463456));
				put("diaporthe-stem-canker", BigInteger.valueOf(10013312));
				put("brown-stem-rot", BigInteger.valueOf(976826156));
			}
		}, leaveOneOut(train, 15).getClassPointers());
		// overall set accuracy should be 628/682, but running the whole thing
		// would take too long.
	}

	// larger set that forces use of BitSetLabel and JohnsenJohansson lattice
	@Test
	public void testAudiology() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.AUDIOLOGY);
		// TODO: test details once we can specify RNG
		leaveOneOut(train, 1);
		// int correct = 0;
		// int total = 0;
		// for (int i = 0; i < train.numInstances(); i++) {
		// total++;
		// AnalogicalSet set = leaveOneOut(train, i);
		// if (set.getPredictedClasses().contains(
		// train.get(i).stringValue(train.classIndex()))) {
		// correct++;
		// System.out.print("right: ");
		// } else {
		// System.out.println("wrong (expected "
		// + train.get(i).stringValue(train.classIndex()) + "): ");
		// }
		// System.out.println(set.getClassLikelihoodMap());
		// }
		// System.out.println(correct + "/" + total);

		// result is 148/226 using 1000 x_s experiments
	}

	private AnalogicalSet leaveOneOut(Instances data, int index)
			throws Exception {
		Instances train = new Instances(data);
		Instance test = train.get(index);
		train.remove(index);
		AnalogicalModeling am = getClassifier();
		am.buildClassifier(train);
		am.distributionForInstance(test);
		return am.getAnalogicalSet();
	}

	public static junit.framework.Test suite() {
		return new TestSuite(AnalogicalModelingTest.class);
	}

}
