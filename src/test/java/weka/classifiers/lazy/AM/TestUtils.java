package weka.classifiers.lazy.AM;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class TestUtils {
	
	/**
	 * @return The example chapter 3 training data
	 * @throws Exception
	 */
	public static Instances chapter3Train() throws Exception{
		DataSource source = new DataSource("data/ch3example.arff");
		Instances train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);
		return train;
	}
	
	/**
	 * @return The example chapter 3 test instance
	 * @throws Exception
	 */
	public static Instance chapter3Test() throws Exception{
		DataSource source = new DataSource("data/ch3exampleTest.arff");
		Instances test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);
		return test.firstInstance();
	}

}
