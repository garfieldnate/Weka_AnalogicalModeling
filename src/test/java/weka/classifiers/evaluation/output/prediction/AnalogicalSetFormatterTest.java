package weka.classifiers.evaluation.output.prediction;

import org.junit.Before;
import org.junit.Test;
import weka.classifiers.evaluation.output.prediction.AnalogicalSetFormatter;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static junit.framework.TestCase.assertEquals;

public class AnalogicalSetFormatterTest {

    private AMResults results;

    @Before
    public void init() throws Exception {
        AnalogicalModeling am = new AnalogicalModeling();
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        am.buildClassifier(train);
        am.distributionForInstance(test);
        results = am.getResults();
    }

    @Test
    public void testHumanFormatter() {
        AnalogicalSetFormatter formatter = new AnalogicalSetFormatter(3, Format.HUMAN, "\n");
        String actualOutput = formatter.formatAnalogicalSet(results);

        String expectedOutput =
            "┌────────────┬──────────┬───────┬───────┐\n" +
                "│ Percentage │ Pointers │  Item │ Class │\n" +
                "│    %30.769 │        4 │ 3 1 0 │     e │\n" +
                "│    %30.769 │        4 │ 3 1 1 │     r │\n" +
                "│    %23.077 │        3 │ 2 1 2 │     r │\n" +
                "│    %15.385 │        2 │ 0 3 2 │     r │\n" +
                "└────────────┴──────────┴───────┴───────┘";

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testCsvFormatter() {
        AnalogicalSetFormatter formatter = new AnalogicalSetFormatter(3, Format.CSV, "\n");
        String actualOutput = formatter.formatAnalogicalSet(results);

        String expectedOutput =
            "item,class,pointers,percentage\n" +
                "3 1 0,e,4,30.769\n" +
                "3 1 1,r,4,30.769\n" +
                "2 1 2,r,3,23.077\n" +
                "0 3 2,r,2,15.385\n";

        assertEquals(expectedOutput, actualOutput);
    }
}
