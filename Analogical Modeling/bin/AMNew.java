/*
 * 	Analogical Modeling Java module
 *  Copyright (C) 2011  Nathan Glenn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package weka.classifiers.lazy;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.UpdateableClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import edu.byu.am.lattice.MissingDataCompare;

/**
 * <!-- globalinfo-start -->
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * <!-- technical-bibtex-end -->
 * 
 * <!-- technical-plaintext-start -->
 * <!-- technical-plaintext-end -->
 * 
 * <!-- options-start -->
 * <!-- options-end -->
 * 
 * @author Nathan Glenn (garfieldnate at gmail dot com)
 * 
 */
public class AM extends weka.classifiers.AbstractClassifier implements
		TechnicalInformationHandler, UpdateableClassifier {

	/** The training instances used for classification. */
	private Instances trainingExemplars;

	/** The number of attributes */
	protected int cardinality;

	private static final long serialVersionUID = 1212462913157286103L;

	// ////OPTION STORAGE VARIABLES
	/**
	 * By default, we use quadratic calculation of pointer values.
	 */
	private boolean useLinear = false;

	/**
	 * By default, we leave the test exemplar in the training set if it is
	 * there.
	 */
	private boolean removeTestExemplar = false;

	/**
	 * This is the default strategy for handling missing data.
	 */
	// other classes use Tag and SelectedTag, but this seems a lot better (for
	// now)
	private MissingDataCompare missingDataStrategy = MissingDataCompare.VARIABLE;

	/**
	 * 
	 * @return General information and references about the Analogical Modeling
	 *         classifier
	 */
	public String globalInfo() {
		StringBuilder info = new StringBuilder();
		info.append("Implements the Analogical Modeling algorith, invented by Royall Skousen. "
				+ "Analogical modeling is an instance-based algorithm."
				+ "For more information, see the following references:\n\n");
		info.append(getTechnicalInformation().toString());
		return info.toString();
	}

	@Override
	public Enumeration<Option> listOptions() {

		Vector<Option> options = getOptionsOfSuper();
		options.add(new Option(
				"\tUse linear instead of quadratic calculation of "
						+ "pointers (default off)", "l", 0, "-l"));
		options.add(new Option("\tRemove test exemplar from training set", "r",
				0, "-r"));
		options.add(new Option(
				"\tMethod of dealing with missing data. The options are "
						+ "variable, match or mismatch; 'variable' means to treat missing data as "
						+ "a all one variable, 'match' means that missing data will be considered "
						+ "the same as whatever it is compared with, and 'mismatch' means that missing "
						+ "data will always be unequal to whatever it is compared with. Default is 'variable'",
				"m", 1, "-m <method>"));

		return options.elements();
	}

	/**
	 * 
	 * @return Vector of all Options given in parent object(s).
	 */
	private Vector<Option> getOptionsOfSuper() {
		Vector<Option> v = new Vector<Option>();
		Enumeration<Option> e = super.listOptions();
		while (e.hasMoreElements()) {
			Option option = e.nextElement();
			v.add(option);
		}
		return v;
	}

	@Override
	public String[] getOptions() {
		Vector<String> options = new Vector<String>();
		if (useLinear)
			options.add("-l");
		if (removeTestExemplar)
			options.add("-r");
		options.add("-m");
		options.add(missingDataStrategy.getOptionString());
		// add all options of the superclass
		options.addAll(Arrays.asList(super.getOptions()));
		return options.toArray(new String[options.size()]);
	}

	/**
	 * 
	 * <!-- options-start -->
	 * <!-- options-end -->
	 */
	@Override
	public void setOptions(String[] options) {
		try {
			if (Utils.getFlag('l', options))
				;
			useLinear = true;
			if (Utils.getFlag('r', options))
				removeTestExemplar = true;
			String optionString = Utils.getOption('m', options);
			if (optionString.length() != 0)
				for (MissingDataCompare mdc : MissingDataCompare.values())
					if (mdc.getOptionString().equals(optionString))
						missingDataStrategy = mdc;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Analogical Modeling can handle only nominal class attributes. Missing
	 * classes are also handled.
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

	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// test data against capabilities
		getCapabilities().testWithFail(instances);
		// remove instances with missing class value,
		// but don’t modify original data
		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		cardinality = instances.numAttributes();
		trainingExemplars = new Instances(instances, 0,
				instances.numInstances());
	}

	@Override
	public void updateClassifier(Instance instance) throws Exception {
		if (trainingExemplars.equalHeaders(instance.dataset()) == false)
			throw new Exception("Incompatible instance types\n"
					+ trainingExemplars.equalHeadersMsg(instance.dataset()));
		if (instance.classIsMissing())
			return;
		trainingExemplars.add(instance);
	}

	public double[] distributionForInstance(Instance instance) {
		double[] classProbability = new double[trainingExemplars.numClasses()];
		return classProbability;
	}

	/**
	 * runs the classifier instance with the given options.
	 * 
	 * @param classifier
	 *            the classifier to run
	 * @param options
	 *            the commandline options
	 */
	public static void runClassifier(Classifier classifier, String[] options) {
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

	/**
	 * Main method for testing this class.
	 * 
	 * @param argv
	 *            should contain command line options (see setOptions)
	 */
	public static void main(String[] argv) {
		runClassifier(new KStar(), argv);
	}

}

