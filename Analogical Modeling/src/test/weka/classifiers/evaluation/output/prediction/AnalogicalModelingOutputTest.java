package weka.classifiers.evaluation.output.prediction;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

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
		DataSource source = new DataSource("data/ch3example.arff");
		Instances train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);

		source = new DataSource("data/ch3exampleTest.arff");
		Instances test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);

		output.setHeader(train);
		am.buildClassifier(train);
		output.doPrintClassification(am, test.firstInstance(), 0);
		assertTrue("report contained analogical set", buf.toString().contains("Exemplar effects:"));
	}
}
