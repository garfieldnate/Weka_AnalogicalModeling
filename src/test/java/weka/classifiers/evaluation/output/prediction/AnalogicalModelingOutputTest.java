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

    // TODO: test with different options
    // TODO: test with normalized whitespace
    @Test
    public void testChapter3basic() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        output.setHeader(train);
        am.buildClassifier(train);
        output.doPrintClassification(am, test, 0);
        String actualOutput = buf.toString();

        assertFalse("report should not contain distribution", actualOutput.contains("Class probability distribution:"));
        assertFalse("report should not contain summary", actualOutput.contains("Classifying instance"));
        assertTrue("report should contain analogical set", actualOutput.contains("Analogical set:"));
        assertFalse("report should not contain gang effects", actualOutput.contains("Gang effects:"));
    }

    @Test
    public void testChapter3InvertedDefaultSettingsGangs() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        output.setHeader(train);
        output.setSummary(true);
        output.setOutputDistribution(true);
        output.setAnalogicalSet(false);
        output.setGangs(true);
        am.buildClassifier(train);
        output.doPrintClassification(am, test, 0);
        String actualOutput = buf.toString();

        assertTrue("report should distribution", actualOutput.contains("Class probability distribution:"));
        assertTrue("report should contain summary", actualOutput.contains("Classifying instance"));
        assertFalse("report should not contain analogical set", actualOutput.contains("Analogical set:"));
        assertTrue("report should contain gang effects", actualOutput.contains("Gang effects:"));
    }
}
