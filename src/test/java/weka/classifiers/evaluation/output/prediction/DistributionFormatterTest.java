package weka.classifiers.evaluation.output.prediction;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static org.junit.Assert.*;

public class DistributionFormatterTest {

    private AnalogicalModeling am;
    private DistributionFormatter formatter;

    @Before
    public void init() {
        am = new AnalogicalModeling();
        formatter = new DistributionFormatter(3, "\n");
    }

    @Test
    public void testHumanFormatter() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        am.buildClassifier(train);
        double[] distribution = am.distributionForInstance(test);
        AMResults results = am.getResults();

        String expected = "Class probability distribution:\n" +
            "r: 0.692\n" +
            "e: 0.308\n";
        String actual = formatter.formatDistribution(results, distribution, "foo", Format.HUMAN);
        assertEquals(expected, actual);
    }

    @Test
    public void testCSVFormatter() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        am.buildClassifier(train);
        double[] distribution = am.distributionForInstance(test);
        AMResults results = am.getResults();

        String expected = "Judgement,Expected,first,second,third,Class 1,Class 1,r_ptrs,e_ptrs,r_pct,e_pct,train_size,num_feats\n" +
            "correct,r,3,1,2,r,e,9,4,69.23077,30.76923,5,3\n";
        String actual = formatter.formatDistribution(results, distribution, "foo", Format.CSV);
        assertEquals(expected, actual);
    }
}
