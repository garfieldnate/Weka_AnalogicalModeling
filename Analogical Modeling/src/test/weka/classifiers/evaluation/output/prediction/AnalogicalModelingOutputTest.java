package weka.classifiers.evaluation.output.prediction;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.evaluation.output.prediction.AnalogicalModelingOutput;
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

	//TODO: test with different options
	//TODO: test with normalized whitespace
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
		String expected = "Analogical Set:\n"
				+ "<2,1,2|r> : 3 (0.23077)\n"
				+ "<3,1,1|r> : 4 (0.30769)\n"
				+ "<3,1,0|e> : 4 (0.30769)\n"
				+ "<0,3,2|r> : 2 (0.15385)\n"
				+ "Class totals:\n"
				+ "r : 9 (0.69231)\n" + "e : 4 (0.30769)";
		System.out.println(buf);
		assertEquals(buf.toString().trim(), expected.trim());
	}
}
