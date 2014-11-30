package weka.classifiers.lazy.AM;

import static org.junit.Assert.fail;

import java.util.List;

import weka.classifiers.lazy.AM.lattice.Supracontext;
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
	
	public static void assertContainsSupra(List<Supracontext> supras, Supracontext expected) {
		for (Supracontext supra : supras)
			if (supraDeepEquals(supra, expected))
				return;
		fail("Could not find " + expected + " in " + supras);
	}

	public static boolean supraDeepEquals(Supracontext s1, Supracontext s2) {
		if (s1.getOutcome() != s2.getOutcome())
			return false;

		if (s1.hasData() != s2.hasData())
			return false;
		if (s1.getCount() != s2.getCount())
			return false;

		return s1.getData().equals(s2.getData());
	}

}
