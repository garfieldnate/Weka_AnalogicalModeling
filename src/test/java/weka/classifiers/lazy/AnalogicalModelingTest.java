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
import weka.classifiers.lazy.AM.lattice.BasicLattice;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Tests AnalogicalModeling.
 * 
 * @author <a href="mailto:garfieldnate@gmail.com">Nate Glenn</a>
 */
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
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_TRAIN);
		Instance test = TestUtils.getInstanceFromFile(TestUtils.CHAPTER_3_TEST,
				0);

		AnalogicalModeling am = getClassifier();

		am.buildClassifier(train);
		double[] prediction = am.distributionForInstance(test);
		assertEquals("distribution given for two classes", prediction.length, 2);
		// test to 10 decimals places, the number used by AMUtils.matchContext
		assertEquals(0.6923076923076923, prediction[0], DELTA);
		assertEquals(0.3076923076923077, prediction[1], DELTA);
	}

	@Test
	public void testChapter3dataParallel() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_TRAIN);
		Instance test = TestUtils.getInstanceFromFile(TestUtils.CHAPTER_3_TEST,
				0);

		AnalogicalModeling am = getClassifier();
		am.setParallel(true);

		am.buildClassifier(train);
		double[] prediction = am.distributionForInstance(test);
		assertEquals("distribution given for two classes", prediction.length, 2);
		// test to 10 decimals places, the number used by AMUtils.matchContext
		assertEquals(0.6923076923076923, prediction[0], DELTA);
		assertEquals(0.3076923076923077, prediction[1], DELTA);
	}

	/**
	 * Test that supracontexts are properly marked heterogeneous.
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("serial")
	public void testHeterogeneousMarking() throws Exception {
		Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"6-10");
		assertEquals(new HashMap<String, BigInteger>() {
			{
				put("A", BigInteger.valueOf(17));
				put("B", BigInteger.valueOf(2));
				put("C", BigInteger.valueOf(2));
			}
		}, leaveOneOut(train, 0).getClassPointers());
		train = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
		assertEquals(new HashMap<String, BigInteger>() {
			{
				put("A", BigInteger.valueOf(652));
				put("B", BigInteger.valueOf(12));
				put("C", BigInteger.valueOf(2));
			}
		}, leaveOneOut(train, 15).getClassPointers());

	}

	/**
	 * Test that {@link BasicLattice#cleanSupra()} is only run after a
	 * subcontext is inserted completely, not after each single insertion
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("serial")
	public void testCleanSupraTiming() throws Exception {
		Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"1,7-10");
		assertEquals(new HashMap<String, BigInteger>() {
			{
				put("A", BigInteger.valueOf(45));
			}
		}, leaveOneOut(train, 0).getClassPointers());
	}

	/**
	 * This exposes an error where {@link BasicLattice#cleanSupra()} doesn't
	 * clean all of the zero supras out.
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("serial")
	public void testCleanSupraCorrect() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.FINNVERB);
		assertEquals(new HashMap<String, BigInteger>() {
			{
				put("A", BigInteger.valueOf(24941));
			}
		}, leaveOneOut(train, 16).getClassPointers());
	}

	/**
	 * Test accuracy with the finnverb dataset, a real data set with 10 features
	 * and lots of unknowns.
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
		for(int i = 0; i < train.numInstances(); i++){
			AnalogicalSet set = leaveOneOut(train, i);
			if(set.getPredictedClasses().contains(train.get(i).stringValue(train.classIndex())))
				correct++;
		}
		assertEquals("Leave-one-out accuracy when classifying of finnverb dataset", correct, 160);
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
