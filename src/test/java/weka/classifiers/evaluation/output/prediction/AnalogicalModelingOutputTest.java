package weka.classifiers.evaluation.output.prediction;

import org.junit.Before;
import org.junit.Test;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Instances;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
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
        assertTrue("report contained analogical set", buf.toString().contains("Exemplar effects:"));
    }

	@Test
	public void testChapter3Gangs() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		Instance test = train.remove(0);

		output.setHeader(train);
		output.setGangs(true);
		am.buildClassifier(train);
		output.doPrintClassification(am, test, 0);

		String expectedGangs =
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

		assertThat(output.getBuffer().toString(), containsString(expectedGangs));
	}
}
