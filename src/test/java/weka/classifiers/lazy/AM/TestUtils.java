package weka.classifiers.lazy.AM;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.Label;
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
	public static final String CHAPTER_3_DATA = "ch3example.arff";
	/**
	 * The name of the finnverb data file.
	 */
	public static final String FINNVERB = "finnverb.arff";
	/**
	 * A paired-down finnverb for minimal testing.
	 */
	public static final String FINNVERB_MIN = "finn_min_gangs.arff";
	public static final String SOYBEAN = "soybean.arff";

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
	// TODO: rename this for consistency with getInstanceFromFile
	public static Instances getDataSet(String fileInDataFolder)
			throws Exception {
		DataSource source = new DataSource("data/" + fileInDataFolder);
		Instances instances = source.getDataSet();
		instances.setClassIndex(instances.numAttributes() - 1);
		return instances;
	}

	/**
	 * Read a dataset from the given file and remove the specified attributes,
	 * then return it.
	 * 
	 * @param fileInDataFolder
	 *            name of file in the project data folder
	 * @param ignoreAtts
	 *            A string like "5-10, 12" specififying which attributes should
	 *            be removed. These numbers should be 1-indexed (required by
	 *            Weka API here).
	 * @return The altered dataset
	 * @throws Exception
	 */
	public static Instances getReducedDataSet(String fileInDataFolder,
			String ignoreAtts) throws Exception {

		Instances data = getDataSet(fileInDataFolder);

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
	 *            Index of instance to return
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

	/**
	 * An equals function that compares labels of different classes against
	 * eachother.
	 * 
	 * @param firstLabel
	 * @param secondLabel
	 * @return true if the labels have the same cardinality and mark the same
	 *         feature indices as matching or mismatching.
	 */
	public static boolean labelEquivalent(Label firstLabel, Label secondLabel) {
		// fastest possible comparison is the one contained in the
		// implementation classes
		if (firstLabel.getClass().equals(secondLabel.getClass()))
			return firstLabel.equals(secondLabel);
		// otherwise a slow comparison of each individual bit
		if (firstLabel.getCardinality() != secondLabel.getCardinality())
			return false;
		for (int i = 0; i < firstLabel.getCardinality(); i++)
			if (firstLabel.matches(i) != secondLabel.matches(i))
				return false;
		return true;
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

	/**
	 * Create the {@link Supracontext} object specified by the input string.
	 * 
	 * This method is somewhat slow due to restrictions of {@link Instance}, so
	 * use it only in testing, and only with small datasets if possible.
	 * 
	 * @param supraString
	 *            A string representing the supracontext to be created. This
	 *            should be in the same form as that produced by
	 *            {@link Supracontext#toString()}.
	 * @param data
	 *            The dataset containing the instances specified in the
	 *            Supracontext string. For example:
	 *            <code>[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]</code>
	 *            .
	 * @return
	 */
	public static Supracontext getSupraFromString(String supraString,
			Instances data) {
		String tempString;
		int loc;
		// get the count, between '[' and the first 'x'
		loc = supraString.indexOf('x');
		tempString = supraString.substring(1, loc);
		BigInteger count = BigInteger.valueOf(Integer.parseInt(tempString));
		if (count.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException(
					"count must be greater than zero");
		loc++;// skip x

		// parse subcontexts, which are each contained in parentheses
		Set<Subcontext> subs = new HashSet<Subcontext>();
		String subsString = supraString
				.substring(loc, supraString.indexOf(']'));
		for (String subString : subsString.split("\\),\\(")) {
			// remove extra parentheses
			subString = subString.replace(")", "");
			subString = subString.replace("(", "");
			// split into label, outcome, and instances
			String[] subComponents = subString.split("\\|");
			if (subComponents.length != 3)
				throw new IllegalArgumentException(
						"Incomplete subcontext specified: " + subString);

			// parse label
			Label label = new IntLabel(Integer.parseInt(subComponents[0], 2),
					subComponents[0].length());
			Subcontext sub = new Subcontext(label);

			// parse outcome
			int outcome;
			if (subComponents[1].equals(AMUtils.NONDETERMINISTIC_STRING))
				outcome = AMUtils.NONDETERMINISTIC;
			else {
				outcome = data.classAttribute().indexOfValue(subComponents[1]);
				if (outcome == -1) {
					throw new IllegalArgumentException(
							"Unknown outcome given: " + subComponents[1]);
				}
			}

			// parse instances; use this set to keep track of previous
			// instances,
			// in case there are several with the same string representation
			Set<Instance> seenInstances = new HashSet<>();
			for (String instanceString : subComponents[2].split("/")) {
				// we can't just create a new Instance from the given
				// attributes;
				// since there is no Instance.equals() method, the only way to
				// achieve Supra equality is by having the exact same Instance
				// instances, i.e. grep the set for the matching object :(
				boolean added = false;
				for (int i = 0; i < data.size(); i++) {
					if (data.get(i).toString().equals(instanceString)) {
						if (seenInstances.contains(data.get(i)))
							continue;
						sub.add(data.get(i));
						added = true;
						seenInstances.add(data.get(i));
						break;
					}
				}
				if (!added) {
					throw new IllegalArgumentException(
							instanceString
									+ " does not specify any instance in the given data set");
				}
			}
			if (sub.getOutcome() != outcome)
				throw new IllegalArgumentException(
						"Specified instances give an outcome of "
								+ sub.getOutcome() + ", not " + outcome);
			subs.add(sub);
		}

		Supracontext supra = new Supracontext(subs, count);
		return supra;
	}

	/**
	 * Test that the getSupraFromString utility function above works as desired.
	 * 
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("serial")
	public void getSupraFromStringTest() throws Exception {
		Instances data = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"6-10");

		final Subcontext sub1 = new Subcontext(new IntLabel(0b10110, 5));
		sub1.add(data.get(3)); // P,U,0,?,0,A
		final Subcontext sub2 = new Subcontext(new IntLabel(0b10000, 5));
		sub2.add(data.get(2));// K,U,V,U,0,A
		final Subcontext sub3 = new Subcontext(new IntLabel(0b10010, 5));
		sub3.add(data.get(1));// U,U,V,I,0,A
		Supracontext expectedSupra = new Supracontext(
				new HashSet<Subcontext>() {
					{
						add(sub1);
						add(sub2);
						add(sub3);
					}
				}, BigInteger.ONE);

		String supraString = "[1x(10110|A|P,U,0,?,0,A),(10000|A|K,U,V,U,0,A),(10010|A|U,U,V,I,0,A)]";
		Supracontext actualSupra = getSupraFromString(supraString, data);
		assertTrue("supra with multiple subs",
				supraDeepEquals(expectedSupra, actualSupra));
		assertTrue(
				"fromString mirrors toString",
				supraDeepEquals(
						getSupraFromString(expectedSupra.toString(), data),
						actualSupra));

		supraString = "[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]";
		actualSupra = getSupraFromString(supraString, data);
		final Subcontext sub4 = new Subcontext(new IntLabel(0b01010, 5));
		sub4.add(data.get(4)); // H,A,V,I,0,A
		sub4.add(data.get(5)); // H,A,V,A,0,B
		expectedSupra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub4);
			}
		}, BigInteger.ONE);

		assertTrue("sub with multiple instances",
				supraDeepEquals(expectedSupra, actualSupra));
		assertTrue(
				"fromString mirrors toString",
				supraDeepEquals(
						getSupraFromString(expectedSupra.toString(), data),
						actualSupra));

		data = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
		final Subcontext sub5 = new Subcontext(new IntLabel(0b00001, 5));
		sub5.add(data.get(1));// A,A,0,?,S,B
		sub5.add(data.get(2));// also A,A,0,?,S,B
		expectedSupra = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub5);
			}
		}, BigInteger.valueOf(6));
		supraString = "[6x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B)]";
		actualSupra = getSupraFromString(supraString, data);
		assertTrue("multiple instances with same string representation",
				supraDeepEquals(expectedSupra, actualSupra));
		assertTrue(
				"fromString mirrors toString",
				supraDeepEquals(
						getSupraFromString(expectedSupra.toString(), data),
						actualSupra));

		// TODO: test error conditions
	}
}
