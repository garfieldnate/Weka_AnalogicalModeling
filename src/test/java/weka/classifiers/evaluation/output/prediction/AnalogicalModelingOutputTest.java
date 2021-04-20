package weka.classifiers.evaluation.output.prediction;

import org.junit.Before;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnalogicalModelingOutputTest {

    private Classifier am;
    private AnalogicalModelingOutput output;
    private StringBuffer buf;

    @Before
    public void init() {
        am = new AnalogicalModeling();
        output = new AnalogicalModelingOutput();
        buf = new StringBuffer();
        output.setBuffer(buf);
    }

    @Test
    public void testDefaultSettings() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        output.setHeader(train);
        am.buildClassifier(train);
        output.printClassification(am, test, 0);
        String actualOutput = buf.toString();

        assertFalse("report should not contain distribution", actualOutput.contains("Class probability distribution:"));
        assertFalse("report should not contain summary", actualOutput.contains("Classifying instance"));
        assertTrue("report should contain analogical set", actualOutput.contains("Analogical set:"));
        assertFalse("report should not contain gang effects", actualOutput.contains("Gang effects:"));
    }

    @Test
    public void testInvertedDefaultSettingsGangs() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        output.setHeader(train);
        output.setSummary(true);
        output.setOutputDistribution(true);
        output.setAnalogicalSet(false);
        output.setGangs(true);
        am.buildClassifier(train);
        output.printClassification(am, test, 0);
        String actualOutput = buf.toString();

        assertTrue("report should distribution", actualOutput.contains("Class probability distribution:"));
        assertTrue("report should contain summary", actualOutput.contains("Classifying instance"));
        assertFalse("report should not contain analogical set", actualOutput.contains("Analogical set:"));
        assertTrue("report should contain gang effects", actualOutput.contains("Gang effects:"));
    }

    @Test
    public void testNumDecimalsSetting() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        output.setHeader(train);
        output.setNumDecimals(6);
        output.setSummary(true);
        output.setOutputDistribution(true);
        output.setAnalogicalSet(true);
        output.setGangs(true);
        am.buildClassifier(train);
        output.printClassification(am, test, 0);
        String actualOutput = buf.toString();

        assertTrue("Gang effects should be printed with 6 decimal places", actualOutput.contains("%61.538464 │        8 │         2 │       │   3 1 * │"));
        assertTrue("Analogical set should be printed with 6 decimal places", actualOutput.contains("%30.769232 │        4 │ 3 1 0 │     e │"));
        assertTrue("Distribution should be printed with 6 decimal places", actualOutput.contains("r: 0.692308"));
    }
}
