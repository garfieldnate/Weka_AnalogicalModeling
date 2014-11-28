package weka.classifiers.evaluation.output.prediction;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instance;
import weka.core.Instances;

public class AnalogicalModelingOutputTest {

	private Classifier am;
	private AbstractOutput output;
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
		Instances train = TestUtils.chapter3Train();
		Instance test = TestUtils.chapter3Test();

		output.setHeader(train);
		am.buildClassifier(train);
		output.doPrintClassification(am, test, 0);
		assertTrue("report contained analogical set", buf.toString().contains("Exemplar effects:"));
	}
}
