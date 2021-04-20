/*
 * **************************************************************************
 * Copyright 2021 Nathan Glenn                                              *
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

import junit.framework.TestSuite;
import org.junit.Assert;
import org.junit.Test;
import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.core.Instance;
import weka.core.Instances;

import java.math.BigInteger;
import java.util.HashMap;

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

    /**
     * Creates a default AnalogicalModeling
     */
    @Override
    public AnalogicalModeling getClassifier() {
        AnalogicalModeling am = new AnalogicalModeling();
        // Ensure Johnsen-Johansson lattice runs deterministically
        am.setRandomProvider(TestUtils.getDeterministicRandomProvider());
        return am;
    }

    private static final double DELTA = 1e-7;

    @Test
    public void testChapter3dataSerial() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.get(0);
        train.remove(0);
        AnalogicalModeling am = getClassifier();
        am.buildClassifier(train);

        double[] prediction = am.distributionForInstance(test);
        Assert.assertEquals("distribution given for two classes", prediction.length, 2);
        // test to 7 decimals places, the number used by AMUtils.matchContext
        Assert.assertEquals(0.6923077, prediction[0], DELTA);
        Assert.assertEquals(0.3076923, prediction[1], DELTA);
    }

    /**
     * Test accuracy with the finnverb dataset, a real data set with 10 features
     * and lots of unknowns. First check the class pointers on one
     * classification, then do a leave-one-out classification for the whole set
     * and verify the accuracy.
     *
     * @throws Exception If there's a problem loading the Finnverb dataset
     */
    @Test
    public void testFinnverb() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.FINNVERB);
        Assert.assertEquals(new HashMap<String, BigInteger>() {
            {
                put("A", BigInteger.valueOf(5094));
                put("C", BigInteger.valueOf(50));
            }
        }, leaveOneOut(train, 15).getClassPointers());

        int numCorrect = leaveOneOut(train);
        Assert.assertEquals("Leave-one-out accuracy when classifying of finnverb dataset", numCorrect, 160);
    }

    // larger set that forces use of LongLabel
    @Test
    public void testSoybean() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.SOYBEAN);
        Assert.assertEquals(new HashMap<String, BigInteger>() {
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
    }

    // larger set that forces use of BitSetLabel and JohnsenJohansson lattice
    // without JohnsenJohansson, this ends with "java.lang.OutOfMemoryError: GC overhead limit exceeded"
    @Test
    public void testAudiology() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.AUDIOLOGY);
        int numCorrect = leaveOneOut(train);
        assertTrue("Leave-one-out accuracy when classifying of audiology dataset", numCorrect >= 155);
    }

    private int leaveOneOut(Instances data) throws Exception {
        int correct = 0;
        for (int i = 0; i < data.numInstances(); i++) {
            AMResults set = leaveOneOut(data, i);
            if (set.getPredictedClasses().contains(data.get(i).stringValue(data.classIndex()))) correct++;
        }
        return correct;
    }

    private AMResults leaveOneOut(Instances data, int index) throws Exception {
        Instances train = new Instances(data);
        Instance test = train.get(index);
        train.remove(index);
        AnalogicalModeling am = getClassifier();
        am.buildClassifier(train);
        am.distributionForInstance(test);
        return am.getResults();
    }

    public static junit.framework.Test suite() {
        return new TestSuite(AnalogicalModelingTest.class);
    }
}
