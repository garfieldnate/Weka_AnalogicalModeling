package weka.classifiers.evaluation.output.prediction;

import org.junit.Before;
import org.junit.Test;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static org.junit.Assert.*;

public class DistributionFormatterTest {

    private AMResults results;
    private double[] distribution;

    @Before
    public void init() throws Exception {
        AnalogicalModeling am = new AnalogicalModeling();
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        am.buildClassifier(train);
        distribution = am.distributionForInstance(test);
        results = am.getResults();
    }

    @Test
    public void testHumanFormatter() {
        DistributionFormatter formatter = new DistributionFormatter(3, Format.HUMAN, "\n");
        String expected = "Class probability distribution:\n" +
            "r: 0.692\n" +
            "e: 0.308\n";
        String actual = formatter.formatDistribution(results, distribution);
        assertEquals(expected, actual);
    }

    @Test
    public void testCSVFormatter() {
        DistributionFormatter formatter = new DistributionFormatter(3, Format.CSV, "\n");
        String expected = "Judgement,Expected,first,second,third,Class 1,Class 1,r_ptrs,e_ptrs,r_pct,e_pct,train_size,num_feats,ignore_unknowns,missing_data_compare,ignore_given,count_strategy\n" +
            "correct,r,3,1,2,r,e,9,4,69.23077,30.76923,5,3,false,variable,true,quadratic\n";
        String actual = formatter.formatDistribution(results, distribution);
        assertEquals(expected, actual);
    }
}
