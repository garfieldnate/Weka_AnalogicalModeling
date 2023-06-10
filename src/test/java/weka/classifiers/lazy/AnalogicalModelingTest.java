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
import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.math.BigInteger;
import java.util.HashMap;

import static weka.classifiers.lazy.AnalogicalModeling.TAGS_MISSING;

/**
 * Tests AnalogicalModeling.
 *
 * @author <a href="mailto:garfieldnate@gmail.com">Nate Glenn</a>
 */
public class AnalogicalModelingTest extends AbstractClassifierTest {
    public AnalogicalModelingTest(String name) {
        super(name);
    }

    /**
     * Creates a default AnalogicalModeling
     */
    @Override
    public AnalogicalModeling getClassifier() {
        AnalogicalModeling am = new AnalogicalModeling();
        am.setRemoveTestExemplar(false);
        // Ensure Johnsen-Johansson lattice runs deterministically
        am.setRandomProvider(TestUtils.getDeterministicRandomProvider());
        return am;
    }

    private static final double DELTA = 1e-7;

    public void testChapter3data() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.get(0);
        AnalogicalModeling am = getClassifier();
        // test that this method removes the exemplar
        am.setRemoveTestExemplar(true);
        am.buildClassifier(train);

        double[] prediction = am.distributionForInstance(test);
        Assert.assertArrayEquals("Class distribution", new double[]{0.6923077, 0.3076923}, prediction, DELTA);
        Assert.assertEquals("Class pointer counts", new HashMap<String, BigInteger>() {
            {
                put("r", BigInteger.valueOf(9));
                put("e", BigInteger.valueOf(4));
            }
        }, am.getResults().getClassPointers());
    }

    /**
     * Test accuracy with the finnverb dataset, a real data set with 10 features
     * and lots of unknowns. First check the class pointers on one
     * classification, then do a leave-one-out classification for the whole set
     * and verify the accuracy.
     *
     * @throws Exception If there's a problem loading the Finnverb dataset
     */
    public void testFinnverb() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.FINNVERB);

        Instance test = train.remove(15);
        AnalogicalModeling am = getClassifier();
        am.buildClassifier(train);
        double[] prediction = am.distributionForInstance(test);
        Assert.assertArrayEquals("Class distribution", new double[]{0.0, 0.9902799, 0.0097201}, prediction, DELTA);

        Assert.assertEquals("Class pointer counts", new HashMap<String, BigInteger>() {
            {
                put("A", BigInteger.valueOf(5094));
                put("C", BigInteger.valueOf(50));
            }
        }, am.getResults().getClassPointers());

        train.add(test);
        int numCorrect = TestUtils.leaveOneOut(getClassifier(), train);
        Assert.assertEquals("Leave-one-out accuracy on entire finnverb dataset", numCorrect, 160);
    }

    // larger set that forces use of LongLabel
    public void testSoybean() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.SOYBEAN);
        Instance test = train.remove(15);
        AnalogicalModeling am = getClassifier();
        am.buildClassifier(train);

        double[] prediction = am.distributionForInstance(test);
        Assert.assertArrayEquals("Class distribution", new double[]{
                0.0000296, 0.9969953, 0.0, 0.000006, 0.0028873,
                0.0000351, 0.0000001, 0.0000063, 0.0000085, 0.0,
                0.0000043, 0.0000158, 0.0000000, 0.0000089, 0.0000026,
                0.0, 0.0, 0.0, 0.0},
            prediction, DELTA);
        Assert.assertEquals("Class pointr counts", new HashMap<String, BigInteger>() {
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
        }, am.getResults().getClassPointers());
    }

    // larger set that forces use of BitSetLabel and JohnsenJohansson lattice
    // without JohnsenJohansson, this ends with "java.lang.OutOfMemoryError: GC overhead limit exceeded"
    public void testAudiology() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.AUDIOLOGY);
        int numCorrect = TestUtils.leaveOneOut(getClassifier(), train);
        assertTrue("Leave-one-out accuracy on audiology dataset should be >= 155; was " + numCorrect, numCorrect >= 155);
    }

    public void testGetOptions() {
        AnalogicalModeling am = new AnalogicalModeling();
        Assert.assertArrayEquals("Default options", am.getOptions(), new String[]{"-R", "-M", "variable"});

        am.setRemoveTestExemplar(false);
        am.setMissingDataCompare(new SelectedTag(MissingDataCompare.MISMATCH.ordinal(), TAGS_MISSING));
        am.setLinearCount(true);
        am.setIgnoreUnknowns(true);
        Assert.assertArrayEquals("Custom options", am.getOptions(), new String[]{"-L", "-I", "-M", "mismatch"});
    }

    public static junit.framework.Test suite() {
        return new TestSuite(AnalogicalModelingTest.class);
    }
}
