package weka.classifiers.lazy.AM;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.lazy.AM.lattice.Supracontext;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class TestUtils {

	/**
	 * The name of the chapter 3 training data file.
	 */
	public static final String CHAPTER_3_TRAIN = "ch3example.arff";
	/**
	 * The name of the chapter 3 test data file.
	 */
	public static final String CHAPTER_3_TEST = "ch3exampleTest.arff";
	/**
	 * The name of the finnverb data file.
	 */
	public static final String FINNVERB = "finnverb.arff";
	/**
	 * A paired-down finnverb for minimal testing.
	 */
	public static final String FINNVERB_MIN = "finn_min_gangs.arff";

	/**
	 * Read a dataset from disk and return the Instances object. It is assumed
	 * that the file is in the project data folder, and that the class attribute
	 * is the last one.
	 * 
	 * @param fileInDataFolder
	 *            Name of arff file in located in the project data folder
	 * @return The dataset contained in the given file.
	 * @throws Exception
	 */
	public static Instances getDataSet(String fileInDataFolder)
			throws Exception {
		DataSource source = new DataSource("data/" + fileInDataFolder);
		Instances instances = source.getDataSet();
		instances.setClassIndex(instances.numAttributes() - 1);
		return instances;
	}

	public static Instances getReducedDataSet(String fileInDataFolder,
			int numAtts) throws Exception {

		Instances data = getDataSet(fileInDataFolder);
		
		// string needs to be "X-Y", specifying that attributes X-Y should be
		// removed. -1 is used to prevent ignoring the class attribute, which is
		// assumed to be last. Oddly, the atts are 1-indexed for this, whereas
		// the get() methods are always 0-indexed.
		String ignoreAtts = (numAtts + 1) + "-" + (data.numAttributes() - 1);
		Remove remove = new Remove(); // new instance of filter
		remove.setOptions(new String[] { "-R", ignoreAtts });
		remove.setInputFormat(data);
		
		Instances newData = Filter.useFilter(data, remove); // apply filter
		newData.setClassIndex(newData.numAttributes() - 1);
		
		return newData;
	}

	/**
	 * Read a dataset from disk and return the Instance object at the specified
	 * index. It is assumed that the file is in the project data folder, and
	 * that the class attribute is the last one.
	 * 
	 * @param fileInDataFolder
	 *            Name of arff file in located in the project data folder
	 * @param index
	 *            TODO
	 * @return The instance at the specified index of the dataset contained in
	 *         the file
	 * @throws Exception
	 */
	public static Instance getInstanceFromFile(String fileInDataFolder,
			int index) throws Exception {
		Instances instances = getDataSet(fileInDataFolder);
		instances.setClassIndex(instances.numAttributes() - 1);
		return instances.get(index);
	}

	public static Instances sixCardinalityData() throws Exception {
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<String> classes = new ArrayList<String>();
		classes.add("e");
		classes.add("r");
		atts.add(new Attribute("a"));
		atts.add(new Attribute("b"));
		atts.add(new Attribute("c"));
		atts.add(new Attribute("d"));
		atts.add(new Attribute("e"));
		atts.add(new Attribute("class", classes));
		Instances dataset = new Instances("TestInstances", atts, 0);
		dataset.setClassIndex(dataset.numAttributes() - 1);

		double[][] data = new double[][] {
				new double[] { 0, 1, 2, 1, 1, 1 },
				new double[] { 0, 1, 2, 1, 1, 1 },
				new double[] { 2, 1, 1, 2, 1, 1 },
				new double[] { 0, 1, 2, 0, 2, 1 },
				new double[] { 2, 1, 2, 0, 2, 1 },
				new double[] { 1, 0, 1, 0, 0, 1 },
				// NaN means a missing attribute
				new double[] { 0, 1, 1, 1, Double.NaN, 1 },
				new double[] { 0, 1, 0, 1, Double.NaN, 1 },
				new double[] { 0, 1, 2, Double.NaN, 1, 1 } };
		for (double[] datum : data) {
			Instance instance = new DenseInstance(6, datum);
			dataset.add(instance);
		}
		return dataset;
	}

	public static void assertContainsSupra(List<Supracontext> supras,
			Supracontext expected) {
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
		if (!s1.getCount().equals(s2.getCount()))
			return false;

		return s1.getData().equals(s2.getData());
	}
}
