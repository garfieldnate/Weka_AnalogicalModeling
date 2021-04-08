package weka.classifiers.lazy.AM.data;

import org.junit.Test;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static junit.framework.TestCase.assertEquals;
import static weka.classifiers.lazy.AM.data.GangEffectsFormatter.formatGangs;

public class GangEffectsFormatterTest {

    @Test
    public void testChapter3Gangs() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        AnalogicalModeling am = new AnalogicalModeling();
        am.buildClassifier(train);
        am.distributionForInstance(test);
        AMResults results = am.getResults();
        String actualOutput = formatGangs(results);

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
}
