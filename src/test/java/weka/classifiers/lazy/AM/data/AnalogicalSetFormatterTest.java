package weka.classifiers.lazy.AM.data;

import org.junit.Test;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static junit.framework.TestCase.assertEquals;
import static weka.classifiers.lazy.AM.data.AnalogicalSetFormatter.formatAnalogicalSet;

public class AnalogicalSetFormatterTest{

    @Test
    public void testChapter3AnalogicalSet() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.remove(0);

        AnalogicalModeling am = new AnalogicalModeling();
        am.buildClassifier(train);
        am.distributionForInstance(test);
        AMResults results = am.getResults();
        String actualOutput = formatAnalogicalSet(results);

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
}
