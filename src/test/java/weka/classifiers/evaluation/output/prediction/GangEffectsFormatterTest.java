package weka.classifiers.evaluation.output.prediction;

import org.junit.Before;
import org.junit.Test;
import weka.classifiers.evaluation.output.prediction.GangEffectsFormatter;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static junit.framework.TestCase.assertEquals;

public class GangEffectsFormatterTest {

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
    public void testHumanFormat() {
        GangEffectsFormatter formatter = new GangEffectsFormatter(3, Format.HUMAN, "\n");
        String actualOutput = formatter.formatGangs(results);

        String expectedOutput =
            "┌────────────┬──────────┬───────────┬───────┬─────────┐\n" +
                "│ Percentage │ Pointers │ Num Items │ Class │ Context │\n" +
                "├────────────┼──────────┼───────────┼───────┼─────────┤\n" +
                "│    %61.538 │        8 │         2 │       │   3 1 * │\n" +
                "├────────────┼──────────┼───────────┼───────┼─────────┤\n" +
                "│    %30.769 │        4 │         1 │     e │         │\n" +
                "│            │          │           │       │   3 1 0 │\n" +
                "│    %30.769 │        4 │         1 │     r │         │\n" +
                "│            │          │           │       │   3 1 1 │\n" +
                "├────────────┼──────────┼───────────┼───────┼─────────┤\n" +
                "│    %23.077 │        3 │         1 │       │   * 1 2 │\n" +
                "├────────────┼──────────┼───────────┼───────┼─────────┤\n" +
                "│    %23.077 │        3 │         1 │     r │         │\n" +
                "│            │          │           │       │   2 1 2 │\n" +
                "├────────────┼──────────┼───────────┼───────┼─────────┤\n" +
                "│    %15.385 │        2 │         1 │       │   * * 2 │\n" +
                "├────────────┼──────────┼───────────┼───────┼─────────┤\n" +
                "│    %15.385 │        2 │         1 │     r │         │\n" +
                "│            │          │           │       │   0 3 2 │\n" +
                "└────────────┴──────────┴───────────┴───────┴─────────┘";

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testCsvFormat() {
        GangEffectsFormatter formatter = new GangEffectsFormatter(3, Format.CSV, "\n");
        String actualOutput = formatter.formatGangs(results);

        String expectedOutput =
            "F:first,F:second,F:third,GF:first,GF:second,GF:third,class,e_pct,e_ptrs,e_size,gang_pct,gang_ptrs,r_pct,r_ptrs,r_size,rank,size,total_ptrs\n" +
                "3,1,0,3,1,*,e,30.769,4,1,61.538,8,0.0,0,0,1,2,13\n" +
                "3,1,1,3,1,*,r,30.769,4,1,61.538,8,30.769,4,1,1,2,13\n" +
                "2,1,2,*,1,2,r,0.0,0,0,23.077,3,23.077,3,1,2,1,13\n" +
                "0,3,2,*,*,2,r,0.0,0,0,15.385,2,15.385,2,1,3,1,13\n";

        assertEquals(expectedOutput, actualOutput);
    }
}
