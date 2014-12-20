/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/

package weka.classifiers.lazy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import weka.classifiers.Evaluation;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.lazy.AM.data.AnalogicalSet;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.label.BitSetLabeler;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.LongLabel;
import weka.classifiers.lazy.AM.label.LongLabeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.classifiers.lazy.AM.lattice.BasicLattice;
import weka.classifiers.lazy.AM.lattice.DistributedLattice;
import weka.classifiers.lazy.AM.lattice.Lattice;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.Summarizable;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

/**
 * <!-- globalinfo-start --> * Implements the Analogical Modeling algorith,
 * invented by Royall Skousen. Analogical modeling is an instance-based
 * algorithm designed to model human behavior.For more information, see the
 * following references:<br/>
 * * <br/>
 * * Skousen, R. (1989). Analogical Modeling of Language. Kluwer Academic
 * Publishers.<br/>
 * * <br/>
 * * Theron Sanford (2002). Analogical modeling: an exemplar-based approach to
 * language. *
 * <p/>
 * 
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start --> * BibTeX: *
 * 
 * <pre>
 * * &#64;book{skousen1989analogical,
 * *    author = {Skousen, R.},
 * *    publisher = {Kluwer Academic Publishers},
 * *    title = {Analogical Modeling of Language},
 * *    year = {1989},
 * *    abstract = {Review: 'Skousen develops an analogical approach, which is claimed to handle not merely cases which are problematic for tructuralist approaches, but to be applicable equally to the cases with which structuralism is at its best - in short, to be an Einstein to the common Newton.This is altogether a stimulating and richly suggestive book whose fundamental notions are presented with formal rigour. Other, more psychologically adequate, formal analogical theories may be devised, but Skousen has shown the way forward.' Artificial Intelligence and Stimulation of Behaviour Quarterly, 1990, No. 72},
 * *    ISBN-13 = {9780792305170}
 * * }
 * * 
 * * &#64;inbook{skousen2002analogical,
 * *    author = {Theron Sanford},
 * *    editor = {Skousen, Royall and Lonsdale, Deryle and Parkinson, Dilworth},
 * *    pages = {385--409},
 * *    publisher = {John Benjamins Publishing Company},
 * *    title = {Analogical modeling: an exemplar-based approach to language},
 * *    year = {2002},
 * *    abstract = {Analogical Modeling (AM) is an exemplar-based general theory of description that uses both neighbors and non-neighbors (under certain well-defined conditions of homogeneity) to predict language behavior. This book provides a basic introduction to AM, compares the theory with nearest-neighbor approaches, and discusses the most recent advances in the theory, including psycholinguistic evidence, applications to specific languages, the problem of categorization, and how AM relates to alternative approaches of language description (such as instance families, neural nets, connectionism, and optimality theory). The book closes with a thorough examination of the problem of the exponential explosion, an inherent difficulty in AM (and in fact all theories of language description). Quantum computing (based on quantum mechanics with its inherent simultaneity and reversibility) provides a precise and natural solution to the exponential explosion in AM. Finally, an extensive appendix provides three tutorials for running the AM computer program (available online).},
 * *    ISBN-13 = {9789027223623}
 * * }
 * *
 * </pre>
 * 
 * *
 * <p/>
 * <!-- technical-bibtex-end -->
 * 
 * <!-- technical-plaintext-start --> * Skousen, R. (1989). Analogical Modeling
 * of Language. Kluwer Academic Publishers.<br/>
 * * <br/>
 * * Theron Sanford (2002). Analogical modeling: an exemplar-based approach to
 * language. <!-- technical-plaintext-end -->
 * 
 * 
 * <!-- options-start --> * Valid options are:
 * <p/>
 * * *
 * 
 * <pre>
 * -D
 * *  If set, classifier is run in debug mode and
 * *  may output additional info to the console
 * </pre>
 * 
 * * *
 * 
 * <pre>
 * -L
 * *  Use linear instead of quadratic calculation of pointers (default off)
 * </pre>
 * 
 * * *
 * 
 * <pre>
 * -R
 * *  Remove test exemplar from training set
 * </pre>
 * 
 * * * TODO: actually use user's input for missing comparison
 * 
 * <pre>
 * -M &lt;method&gt;
 * *  Method of dealing with missing data. The options are variable, match or mismatch; 'variable' means to treat missing data as a all one variable, 'match' means that missing data will be considered the same as whatever it is compared with, and 'mismatch' means that missing data will always be unequal to whatever it is compared with. Default is 'variable'
 * </pre>
 * 
 * * <!-- options-end -->
 * 
 * 
 * @author Nathan Glenn (garfieldnate at gmail dot com)
 * @version $Revision: 8034$
 * 
 */
public class AnalogicalModeling extends weka.classifiers.AbstractClassifier
		implements TechnicalInformationHandler, UpdateableClassifier,
		Summarizable {

	/** The training instances used for classification. */
	private Instances trainingInstances;

	/** The training exemplars used for classification. */
	private List<Instance> trainingExemplars;

	/** The number of attributes. */
	protected int cardinality;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1212462913157286103L;

	private MissingDataCompare mdc = MissingDataCompare.VARIABLE;

	/**
	 * This method is where all of the action happens! Given test item, it uses
	 * existing exemplars to assign outcome probabilities to it.
	 * 
	 * @param testItem
	 *            Item to make context base on
	 * @return Analogical set which holds results of the classification for the
	 *         given item
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private AnalogicalSet classify(Instance testItem)
			throws InterruptedException, ExecutionException {
		if (getDebug())
			System.out.println("Classifying: " + testItem);

		// int and long labels are faster and smaller, so use them if the
		// cardinality turns out to be small enough
		Labeler labeler = new BitSetLabeler(mdc, testItem, m_ignoreUnknowns);
		if (labeler.getCardinality() <= IntLabel.MAX_CARDINALITY)
			labeler = new IntLabeler(mdc, testItem, m_ignoreUnknowns);
		else if (labeler.getCardinality() <= LongLabel.MAX_CARDINALITY)
			labeler = new LongLabeler(mdc, testItem, m_ignoreUnknowns);

		// 3 steps to assigning outcome probabilities:
		// 1. Place each data item in a subcontext
		SubcontextList subList = new SubcontextList(labeler, trainingExemplars);
		// 2. Place subcontexts into a supracontextual lattice
		Lattice lattice;
		if (labeler.numPartitions() > 1) {
			lattice = new DistributedLattice(subList);
		} else {
			if (getDebug())
				System.out.println("Subcontexts: " + subList);
			lattice = new BasicLattice(subList);
			if (getDebug())
				System.out.println("Lattice: " + lattice);
		}
		// 3. pointers in homogeneous supracontexts are used to give the
		// analogical set and predicted outcome.
		as = new AnalogicalSet(lattice.getSupracontextList(), testItem,
				m_linearCount);
		return as;
	}

	// ////OPTION STORAGE VARIABLES

	/**
	 * By default, we use quadratic calculation of pointer values.
	 */
	private boolean m_linearCount = false;

	private boolean m_ignoreUnknowns = false;

	/**
	 * 
	 * @return True if counting of pointers is linear; false if quadratic.
	 */
	public boolean getLinearCount() {
		return m_linearCount;
	}

	/**
	 * 
	 * @param lc
	 *            True if counting of pointers should be linear; false if
	 *            quadratic.
	 */
	public void setLinearCount(boolean lc) {
		m_linearCount = lc;
	}

	/**
	 * 
	 * @return Tooltip text describing the linearCount option
	 */
	public String linearCountTipText() {
		return "Set this to true if counting of pointers within homogeneous supracontexts should be "
				+ "done linearly instead of quadratically.";
	}

	public String parallelTipText() {
		return "set to true if the distributed lattice algorithm should be used.";
	}

	public boolean getIgnoreUnknowns() {
		return m_ignoreUnknowns;
	}

	public void setIgnoreUnknowns(boolean parallel) {
		m_ignoreUnknowns = parallel;
	}

	public String ignoreUnknownsTipText() {
		return "set to true attributes with unknown values in the test item should be ignored";
	}

	/**
	 * By default, we leave the test exemplar in the training set if it is
	 * there.
	 */
	private boolean removeTestExemplar = false;

	/**
	 * 
	 * @return true if we remove a test instance from training before predicting
	 *         its outcome
	 */
	public boolean getRemoveTestExemplar() {
		return removeTestExemplar;
	}

	/**
	 * 
	 * @param true if we should remove a test instance from training before
	 *        predicting its outcome
	 */
	public void setRemoveTestExemplar(boolean removeTestExemplar) {
		this.removeTestExemplar = removeTestExemplar;
	}

	/**
	 * 
	 * @return Tooltip text describing the removeTestExemplar option
	 */
	public String removeTestExemplarTipText() {
		return "Set to true if you wish to remove a test instance from the training set before "
				+ "attempting to predict its outcome.";
	}

	/** Define possible missing value handling methods */
	private static final Tag[] TAGS_MISSING = MissingDataCompare.getTags();

	/**
	 * 
	 * @return String representation of strategy used when comparing missing
	 *         values with other data
	 */
	public SelectedTag getMissingDataCompare() {
		return new SelectedTag(mdc.ordinal(), TAGS_MISSING);
	}

	/**
	 * 
	 * @param SelectedTag
	 *            representing choice for strategy to compare missing data
	 * @throws IllegalArgumentException
	 *             if input is something other than variable, match or mismatch.
	 */
	public void setMissingDataCompare(SelectedTag newMode) {
		if (newMode.getTags() == TAGS_MISSING) {
			mdc = MissingDataCompare.getElement(newMode);
		}
	}

	/**
	 * 
	 * @return Tooltip text describing the missingDataCompare option
	 */
	public String missingDataCompareTipText() {
		return "The strategy to use when comparing missing attribute values with other values while filling "
				+ "subcontexts and supracontexts";
	}

	/**
	 * Returns basic human readable information about the classifier, including
	 * references.
	 * 
	 * @return General information and references about the Analogical Modeling
	 *         classifier
	 */
	public String globalInfo() {
		StringBuilder info = new StringBuilder();
		info.append("Implements the Analogical Modeling algorithm, invented by Royall Skousen. "
				+ "Analogical modeling is an instance-based algorithm designed to model "
				+ "human behavior."
				+ "For more information, see the following references:\n\n");
		info.append(getTechnicalInformation().toString());
		return info.toString();
	}

	// /**
	// * Returns the revision string.
	// *
	// * @return the revision
	// */
	// @Override
	// public String getRevision() {
	// return RevisionUtils.extract("$Revision: 8034 $");
	// }

	/**
	 * Lists the options available for this classifier.
	 * 
	 * @see weka.classifiers.AbstractClassifier#listOptions()
	 * @return {@inheritDoc}
	 */
	@Override
	public Enumeration<Option> listOptions() {

		Vector<Option> options = getOptionsOfSuper();
		options.add(new Option(
				"\tUse linear instead of quadratic calculation of "
						+ "pointers (default off)", "L", 0, "-L"));
		options.add(new Option("\tRemove test exemplar from training set", "r",
				0, "-R"));
		options.add(new Option(
				"\tMethod of dealing with missing data. The options are "
						+ "variable, match or mismatch; 'variable' means to treat missing data as "
						+ "a all one variable, 'match' means that missing data will be considered "
						+ "the same as whatever it is compared with, and 'mismatch' means that missing "
						+ "data will always be unequal to whatever it is compared with. Default is 'variable'",
				"M", 1, "-M <method>"));

		return options.elements();
	}

	/**
	 * Gets the options of super.
	 * 
	 * @return Vector of all Options given in parent object(s).
	 */
	private Vector<Option> getOptionsOfSuper() {
		Vector<Option> v = new Vector<Option>();
		// super will always return Enumeration<Option>
		Enumeration<Option> e = super.listOptions();
		while (e.hasMoreElements()) {
			Option option = e.nextElement();
			v.add(option);
		}
		return v;
	}

	/**
	 * Returns the options currently set.
	 * 
	 * @see weka.classifiers.AbstractClassifier#getOptions()
	 * @return {@inheritDoc}
	 */
	@Override
	public String[] getOptions() {
		Vector<String> options = new Vector<String>();
		if (getLinearCount())
			options.add("-L");
		if (getRemoveTestExemplar())
			options.add("-R");
		options.add("-M");
		options.add(mdc.getOptionString());
		// add all options of the superclass
		options.addAll(Arrays.asList(super.getOptions()));
		return options.toArray(new String[options.size()]);
	}

	/**
	 * <!-- options-start --> * Parses a given list of options. Valid options
	 * are:
	 * <p/>
	 * * *
	 * 
	 * <pre>
	 * -D
	 * *  If set, classifier is run in debug mode and
	 * *  may output additional info to the console
	 * </pre>
	 * 
	 * * *
	 * 
	 * <pre>
	 * -L
	 * *  Use linear instead of quadratic calculation of pointers (default off)
	 * </pre>
	 * 
	 * * *
	 * 
	 * <pre>
	 * -R
	 * *  Remove test exemplar from training set
	 * </pre>
	 * 
	 * * *
	 * 
	 * <pre>
	 * -M &lt;method&gt;
	 * *  Method of dealing with missing data. The options are variable, match or mismatch; 'variable' means to treat missing data as a all one variable, 'match' means that missing data will be considered the same as whatever it is compared with, and 'mismatch' means that missing data will always be unequal to whatever it is compared with. Default is 'variable'
	 * </pre>
	 * 
	 * * <!-- options-end -->
	 * 
	 * @param options
	 *            the new options
	 * @throws {@inheritDoc}
	 */
	@Override
	public void setOptions(String[] options) {
		try {
			if (Utils.getFlag('L', options))
				setLinearCount(true);
			if (Utils.getFlag('R', options))
				setRemoveTestExemplar(true);
			String optionString = Utils.getOption('M', options);
			if (optionString.length() != 0)
				for (MissingDataCompare mdc : MissingDataCompare.values())
					if (mdc.getOptionString().equals(optionString))
						this.mdc = mdc;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Analogical Modeling can handle only nominal class attributes. Missing
	 * classes are handled, too, although you must specify how to handle them
	 * (see {@link #setOptions}).
	 * 
	 * @return {@inheritDoc}
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();
		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);
		// classes
		result.enable(Capability.NOMINAL_CLASS); // only nominal classes allowed
		// not sure what to do with these yet...
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.setMinimumNumberInstances(1);
		return result;
	}

	// cite Royall's green book and the implementation chapter of the red book
	/**
	 * @see weka.core.TechnicalInformationHandler#getTechnicalInformation()
	 * @return {@inheritDoc}
	 */
	@Override
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation ti = new TechnicalInformation(
				TechnicalInformation.Type.BOOK, "skousen1989analogical");
		ti.setValue(TechnicalInformation.Field.TITLE,
				"Analogical Modeling of Language");
		ti.setValue(TechnicalInformation.Field.AUTHOR, "Skousen, R.");
		ti.setValue(TechnicalInformation.Field.ISBN13, "9780792305170");
		ti.setValue(TechnicalInformation.Field.YEAR, "1989");
		ti.setValue(TechnicalInformation.Field.PUBLISHER,
				"Kluwer Academic Publishers");
		ti.setValue(
				TechnicalInformation.Field.ABSTRACT,
				"Review: 'Skousen develops an analogical approach, which is claimed "
						+ "to handle not merely cases which are problematic for tructuralist "
						+ "approaches, but to be applicable equally to the cases with which "
						+ "structuralism is at its best - in short, to be an Einstein to the "
						+ "common Newton.This is altogether a stimulating and richly suggestive "
						+ "book whose fundamental notions are presented with formal rigour. Other, "
						+ "more psychologically adequate, formal analogical theories may be devised, "
						+ "but Skousen has shown the way forward.' Artificial Intelligence and "
						+ "Stimulation of Behaviour Quarterly, 1990, No. 72");

		TechnicalInformation ti2 = new TechnicalInformation(Type.INBOOK,
				"skousen2002analogical");
		ti2.setValue(TechnicalInformation.Field.EDITOR,
				"Skousen, Royall and Lonsdale, Deryle and Parkinson, Dilworth");
		ti2.setValue(TechnicalInformation.Field.YEAR, "2002");
		ti2.setValue(TechnicalInformation.Field.PUBLISHER,
				"John Benjamins Publishing Company");
		ti2.setValue(TechnicalInformation.Field.TITLE,
				"Analogical modeling: an exemplar-based approach to language");
		ti2.setValue(TechnicalInformation.Field.AUTHOR, "Theron Sanford");
		ti2.setValue(
				TechnicalInformation.Field.ABSTRACT,
				"Analogical Modeling (AM) is an exemplar-based general theory of description "
						+ "that uses both neighbors and non-neighbors (under certain well-defined conditions "
						+ "of homogeneity) to predict language behavior. This book provides a basic "
						+ "introduction to AM, compares the theory with nearest-neighbor approaches, and "
						+ "discusses the most recent advances in the theory, including psycholinguistic "
						+ "evidence, applications to specific languages, the problem of categorization, "
						+ "and how AM relates to alternative approaches of language description (such as "
						+ "instance families, neural nets, connectionism, and optimality theory). The book "
						+ "closes with a thorough examination of the problem of the exponential explosion, "
						+ "an inherent difficulty in AM (and in fact all theories of language description). "
						+ "Quantum computing (based on quantum mechanics with its inherent simultaneity and "
						+ "reversibility) provides a precise and natural solution to the exponential explosion "
						+ "in AM. Finally, an extensive appendix provides three tutorials for running the AM "
						+ "computer program (available online).");
		ti2.setValue(TechnicalInformation.Field.PAGES, "385--409");
		ti2.setValue(TechnicalInformation.Field.ISBN13, "9789027223623");

		TechnicalInformation ti3 = new TechnicalInformation(Type.MISC,
				"wiki:AnalgocialModeling");
		ti3.setValue(TechnicalInformation.Field.AUTHOR, "Wikipedia");
		ti3.setValue(TechnicalInformation.Field.URL,
				"http://en.wikipedia.org/wiki/Analogical_modeling");
		ti3.setValue(TechnicalInformation.Field.NOTE,
				"[Online; accessed 15-June-2012]");
		ti3.setValue(TechnicalInformation.Field.YEAR, "2012");
		ti3.setValue(TechnicalInformation.Field.URL,
				"http://en.wikipedia.org/wiki/Analogical_modeling");
		ti.add(ti2);
		return ti;
	}

	/**
	 * This is used to build the classifier; it specifies the capabilities of
	 * the classifier and loads in examplars to be used for prediction. No
	 * actual analysis happens here because AM is a lazy classifier.
	 * 
	 * @see weka.classifiers.Classifier#buildClassifier(weka.core.Instances)
	 */
	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// test data against capabilities
		getCapabilities().testWithFail(instances);

		// remove instances with missing class value,
		// but don’t modify original data
		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		cardinality = instances.numAttributes();
		// save instances for checking headers
		trainingInstances = new Instances(instances, 0,
				instances.numInstances());

		// create exemplars for actually running the classifier
		trainingExemplars = new LinkedList<Instance>();
		for (Instance i : instances)
			trainingExemplars.add(i);
	}

	/**
	 * This is used to add more information to the classifier.
	 * 
	 * @see weka.classifiers.UpdateableClassifier#updateClassifier(weka.core.Instance)
	 */
	@Override
	public void updateClassifier(Instance instance) throws Exception {
		if (trainingInstances.equalHeaders(instance.dataset()) == false)
			throw new Exception("Incompatible instance types\n"
					+ trainingInstances.equalHeadersMsg(instance.dataset()));
		if (instance.classIsMissing())
			return;
		trainingInstances.add(instance);
		trainingExemplars.add(instance);
		if (getDebug())
			System.out.println("Added instance: " + instance);
	}

	/**
	 * @see weka.classifiers.AbstractClassifier#distributionForInstance(weka.core.Instance)
	 * @return {@inheritDoc}
	 * @throws Exception
	 *             if distribution can't be computed successfully
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		if (trainingInstances.equalHeaders(instance.dataset()) == false)
			throw new Exception("Incompatible instance types\n"
					+ trainingInstances.equalHeadersMsg(instance.dataset()));

		if (trainingInstances.numInstances() == 0) {
			throw new Exception("No training instances!");
		}

		if (trainingInstances.numClasses() == 1) {
			if (getDebug())
				System.out.println("Training data have only one class");
			// 100 percent likelihood of belonging to the one class
			return new double[] { 1 };
		}

		AnalogicalSet as = classify(instance);
		if (getDebug())
			System.out.println(as);

		double[] classProbability = new double[trainingInstances.numClasses()];
		int index = 0;
		for (Entry<String, BigDecimal> entry : as.getClassLikelihood()
				.entrySet())
			classProbability[index++] = entry.getValue().doubleValue();

		return classProbability;
	}

	/**
	 * The analogical set from the last call to distributionForInstance
	 */
	private AnalogicalSet as = null;

	/**
	 * 
	 * @return The analogical set from the last call to distributionForInstance
	 * @throws IllegalStateException
	 *             if you've never called distributionForInstance from this
	 *             object
	 */
	public AnalogicalSet getAnalogicalSet() {
		if (as == null)
			throw new IllegalStateException(
					"Call distributionForInstance before calling this");
		return as;
	}

	@Override
	public String toSummaryString() {
		return "Analogical Modeling module (2012) by Nathan Glenn";
	}

	/**
	 * @return String containing name of the classifier and number of training
	 *         instances.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Analogical Modeling Classifier (2012 Nathan Glenn)\n");
		if (trainingExemplars != null)
			sb.append("Training instances: " + trainingExemplars.size() + "\n");
		return sb.toString();
	}

	/**
	 * runs the classifier instance with the given options.
	 * 
	 * @param classifier
	 *            the classifier to run
	 * @param options
	 *            the commandline options
	 */
	public static void runClassifier(AnalogicalModeling classifier,
			String[] options) {
		try {
			System.out.println(Evaluation.evaluateModel(classifier, options));
		} catch (Exception e) {
			if (((e.getMessage() != null) && (e.getMessage().indexOf(
					"General options") == -1))
					|| (e.getMessage() == null))
				e.printStackTrace();
			else
				System.err.println(e.getMessage());
		}
	}

	// try with -t data/ch3example.arff -x 5
	/**
	 * Main method for testing this class.
	 * 
	 * @param argv
	 *            should contain command line options (see setOptions)
	 */
	public static void main(String[] argv) {
		runClassifier(new AnalogicalModeling(), argv);
	}

}
