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

import junit.framework.TestSuite;

import org.junit.Assert;
import org.junit.Test;

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AnalogicalSet;
import weka.core.Instance;
import weka.core.Instances;

import java.math.BigInteger;
import java.util.HashMap;

import static weka.classifiers.lazy.AM.AMUtils.NUM_CORES;

/**
 * Tests AnalogicalModeling.
 *
 * @author <a href="mailto:garfieldnate@gmail.com">Nate Glenn</a>
 */
// TODO: see if this can be parameterized for parallel/non-parallel
public class AnalogicalModelingTest extends AbstractClassifierTest {
    private static final boolean usingNiceComputer = NUM_CORES >= 8;

    public AnalogicalModelingTest(String name) {
        super(name);
        // DEBUG = true;
    }

    /**
     * Creates a default AnalogicalModeling
     */
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
        Assert.assertEquals("distribution given for two classes", prediction.length, 2);
        // test to 10 decimals places, the number used by AMUtils.matchContext
        Assert.assertEquals(0.6923076923076923, prediction[0], DELTA);
        Assert.assertEquals(0.3076923076923077, prediction[1], DELTA);
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
    @SuppressWarnings("serial")
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
//        if (usingNiceComputer) {
//            int numCorrect = leaveOneOut(train);
//            assertEquals("Leave-one-out accuracy when classifying of audiology dataset", 628, numCorrect);
//        }
    }

    // larger set that forces use of BitSetLabel and JohnsenJohansson lattice
    // without JohnsenJohansson, this ends with "java.lang.OutOfMemoryError: GC overhead limit exceeded"
    @Test
    public void testAudiology() throws Exception {
        org.junit.Assume.assumeTrue("Only run this if you have a very nice computer", usingNiceComputer);
        Instances train = TestUtils.getDataSet(TestUtils.AUDIOLOGY);
        Assert.assertEquals(new HashMap<String, BigInteger>() {
            {
                put("possible_menieres", BigInteger.valueOf(10));
                put("poss_central", BigInteger.valueOf(1));
                put("otitis_media", BigInteger.valueOf(9));
                put("mixed_poss_central_om", BigInteger.valueOf(1));
                put("cochlear_noise_and_heredity", BigInteger.valueOf(2));
                put("cochlear_age", new BigInteger("8038168730118937116672"));
                put("retrocochlear_unknown", BigInteger.valueOf(2));
                put("mixed_cochlear_unk_fixation", BigInteger.valueOf(23));
                put("cochlear_age_and_noise", BigInteger.valueOf(24));
                put("mixed_cochlear_age_fixation", BigInteger.valueOf(2));
                put("conductive_fixation", BigInteger.valueOf(33));
                put("mixed_cochlear_unk_discontinuity", BigInteger.valueOf(2));
                put("mixed_poss_noise_om", BigInteger.valueOf(4));
                put("normal_ear", BigInteger.valueOf(46));
                put("possible_brainstem_disorder", BigInteger.valueOf(8));
                put("cochlear_poss_noise", BigInteger.valueOf(24));
                put("bells_palsy", BigInteger.valueOf(1));
                put("cochlear_age_plus_poss_menieres", BigInteger.valueOf(1));
                put("cochlear_unknown", BigInteger.valueOf(66));
                put("mixed_cochlear_age_otitis_media", BigInteger.valueOf(4));
                put("conductive_discontinuity", BigInteger.valueOf(6));
                put("mixed_cochlear_age_s_om", BigInteger.valueOf(2));
                put("mixed_cochlear_unk_ser_om", BigInteger.valueOf(5));
                put("acoustic_neuroma", BigInteger.valueOf(1));
            }
        }, leaveOneOut(train, 1).getClassPointers());
//        int numCorrect = leaveOneOut(train);
//        assertEquals("Leave-one-out accuracy when classifying of audiology dataset", 148, numCorrect);
    }

    private int leaveOneOut(Instances data) throws Exception {
        int correct = 0;
        for (int i = 0; i < data.numInstances(); i++) {
            System.out.print(i + ",");
            AnalogicalSet set = leaveOneOut(data, i);
            if (set.getPredictedClasses().contains(data.get(i).stringValue(data.classIndex()))) correct++;
        }
        System.out.println();
        return correct;
    }

    private AnalogicalSet leaveOneOut(Instances data, int index) throws Exception {
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
