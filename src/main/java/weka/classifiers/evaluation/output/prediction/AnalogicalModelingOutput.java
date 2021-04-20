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

package weka.classifiers.evaluation.output.prediction;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AM.data.AnalogicalSetFormatter;
import weka.classifiers.lazy.AM.data.GangEffectsFormatter;
import weka.classifiers.lazy.AnalogicalModeling;
import weka.core.Instance;
import weka.core.Option;
import weka.core.Utils;
import weka.core.WekaException;

import java.util.*;

/**
 * This class implements a classification output scheme specific to the
 * Analogical Modeling classifier. In particular, it allows the user to print
 * gang effects and analogical sets. <!-- options-start --> Valid options are:
 * <p>
 *
 * <pre>
 * -p &lt;range&gt;
 *    The range of attributes to print in addition to the classification.
 *    (default: none)
 * </pre>
 * <pre>
 * -distribution
 *    Whether to turn on the output of the class distribution.
 *    Only for nominal class attributes.
 *    (default: off)
 * </pre>
 * <pre>
 * -decimals &lt;num&gt;
 *    The number of digits after the decimal point.
 *    (default: 3)
 * </pre>
 * <pre>
 * -file &lt;path&gt;
 *    The file to store the output in, instead of outputting it on stdout.
 *    Gets ignored if the supplied path is a directory.
 *    (default: .)
 * </pre>
 * <pre>
 * -suppress
 *    In case the data gets stored in a file, then this flag can be used
 *    to suppress the regular output.
 *    (default: not suppressed)
 * </pre>
 * <pre>
 * -summary
 *    Output short summary statistics
 * </pre>
 * <pre>
 * -as
 *    Output the analogical set
 * </pre>
 * <pre>
 * -gang
 *    Output gang effects
 * </pre>
 * <p>
 * <!-- options-end -->
 * <!-- globalinfo-start -->This output module enables
 * detailed reporting on the results of the Analogical Modeling classifier, such
 * as the analogical set and gang effects.
 * <p>
 * <!-- globalinfo-end -->
 *
 * @author Nathan Glenn
 */
public class AnalogicalModelingOutput extends AbstractOutput {
    private static final long serialVersionUID = -757810288402645056L;

    private boolean m_Summary = false;
    private boolean m_AnalogicalSet = true;
    private boolean m_Gangs = false;

    @Override
    public String globalInfo() {
        return "This output module enables detailed reporting on the results of the "
               + "Analogical Modeling classifier, such as the analogical set and gang effects.";
    }

    @Override
    public String getDisplay() {
        return "Output information specific to the Analogical Modeling classifier";
    }

    @Override
    protected void doPrintHeader() {
        append("=================================");
        append(AMUtils.LINE_SEPARATOR);
        append("Begin Analogical Modeling Results");
        append(AMUtils.LINE_SEPARATOR);
        append("=================================");
        append(AMUtils.LINE_SEPARATOR);
        append(AMUtils.LINE_SEPARATOR);
    }

    /**
     * This is the function that is called in the GUI (ClassifierPanel); it has to be overridden here because
     * the implementation in {@link AbstractOutput} calls {@link #doPrintClassification(double[], Instance, int)} instead
     */
    @Override
    public void printClassification(Classifier classifier, Instance inst,
                                    int index) throws Exception {
        {
            String error;
            if ((error = checkBasic()) != null) {
                throw new WekaException(error);
            }
        }

        doPrintClassification(classifier, preProcessInstance(inst, classifier), index);
    }

    /**
     * Make sure to call {@link #setHeader(weka.core.Instances) setHeader}
     * first, or this will throw a NullPointerException.
     */
    @Override
    protected void doPrintClassification(Classifier classifier, Instance inst, int index) throws Exception {
        if (!(classifier instanceof AnalogicalModeling)) throw new IllegalArgumentException(
            "You are using " + classifier.getClass()
            + ". This output can only be used with the Analogical Modeling classifier");

        AnalogicalModeling am = (AnalogicalModeling) classifier;
        Instance withMissing = (Instance) inst.copy();
        withMissing.setDataset(inst.dataset());
        inst = preProcessInstance(withMissing, classifier);

        // when you call the AM classifier, it stores the results for later
        double[] distribution = am.distributionForInstance(inst);

        AMResults results = am.getResults();

        if (getSummary()) {
            append("Classifying instance ");
            append(Integer.toString(index));
            append(" (");
            append(results.getLabeler().getInstanceAttsString(inst));
            append(", class: ");
            append(inst.stringValue(inst.classIndex()));
            append(")");
            append(AMUtils.LINE_SEPARATOR);

            append("Total pointers: " + results.getTotalPointers() + AMUtils.LINE_SEPARATOR);
            append("Instances in analogical set: " + results.getExemplarEffectMap().size());
            append(AMUtils.LINE_SEPARATOR);
            append(AMUtils.LINE_SEPARATOR);
        }

        if (getOutputDistribution()) {
            outputDistribution(distribution);
            append(AMUtils.LINE_SEPARATOR);
        }

        if (getAnalogicalSet()) {
            AnalogicalSetFormatter formatter = new AnalogicalSetFormatter(getNumDecimals());
            append("Analogical set:");
            append(AMUtils.LINE_SEPARATOR);
            append(formatter.formatAnalogicalSet(results));
            append(AMUtils.LINE_SEPARATOR);
        }

        if (getGangs()) {
            GangEffectsFormatter formatter = new GangEffectsFormatter(getNumDecimals());
            append("Gang effects:");
            append(AMUtils.LINE_SEPARATOR);
            append(formatter.formatGangs(results));
            append(AMUtils.LINE_SEPARATOR);
        }
    }

    @Override
    protected void doPrintClassification(double[] classDistribution, Instance classifiedInstance, int index) {
        throw new UnsupportedOperationException(
            "These method should not be used; doPrintClassification should be called with the AM classifier as the first argument");
    }

    private void outputDistribution(double[] distribution) {
        String doubleFormat = String.format("%%.%df", getNumDecimals());
        append("Class probability distribution:" + AMUtils.LINE_SEPARATOR);
        for (int i = 0; i < distribution.length; i++) {
            append(m_Header.classAttribute().value(i));
            append(": ");
            append(String.format(doubleFormat, distribution[i]));
            append(AMUtils.LINE_SEPARATOR);
        }
    }

    @Override
    protected void doPrintFooter() {
        append(AMUtils.LINE_SEPARATOR);
        append("===============================");
        append(AMUtils.LINE_SEPARATOR);
        append("End Analogical Modeling Results");
        append(AMUtils.LINE_SEPARATOR);
        append("===============================");
        append(AMUtils.LINE_SEPARATOR);
        append(AMUtils.LINE_SEPARATOR);
    }

    /**
     * Returns an enumeration of all the available options..
     *
     * @return an enumeration of all available options.
     */
    @Override
    public Enumeration<Option> listOptions() {
        Vector<Option> options = getOptionsOfSuper();

        options.add(new Option("\tOutput short summary statistics", "summary", 0, "-summary"));
        options.add(new Option("\tOutput the analogical set", "as", 0, "-as"));
        options.add(new Option("\tOutput gang effects", "gang", 0, "-gang"));

        return options.elements();
    }

    /**
     * Gets the options of super.
     *
     * @return Vector of all Options given in parent object(s).
     */
    private Vector<Option> getOptionsOfSuper() {
        Vector<Option> v = new Vector<>();
        // super will always return Enumeration<Option>
        Enumeration<Option> e = super.listOptions();
        while (e.hasMoreElements()) {
            Option option = e.nextElement();
            v.add(option);
        }
        return v;
    }

    /**
     * Sets the OptionHandler's options using the given list. All options will
     * be set (or reset) during this call (i.e. incremental setting of options
     * is not possible).
     *
     * <!-- options-start --> Valid options are:
	 * <p>
     * <pre>
     * -p &lt;range&gt;
     *    The range of attributes to print in addition to the classification.
     *    (default: none)
     * </pre>
     * <pre>
     * -distribution
     *    Whether to turn on the output of the class distribution.
     *    Only for nominal class attributes.
     *    (default: off)
     * </pre>
     * <pre>
     * -decimals &lt;num&gt;
     *    The number of digits after the decimal point.
     *    (default: 3)
     * </pre>
     * <pre>
     * -file &lt;path&gt;
     *    The file to store the output in, instead of outputting it on stdout.
     *    Gets ignored if the supplied path is a directory.
     *    (default: .)
     * </pre>
     * <pre>
     * -suppress
     *    In case the data gets stored in a file, then this flag can be used
     *    to suppress the regular output.
     *    (default: not suppressed)
     * </pre>
     * <pre>
     * -summary
     *    Output short summary statistics
     * </pre>
     * <pre>
     * -as
     *    Output the analogical set
     * </pre>
     * <pre>
     * -gang
     *    Output gang effects
     * </pre>
     *
     * * <!-- options-end -->
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    @Override
    public void setOptions(String[] options) throws Exception {
        setAnalogicalSet(Utils.getFlag("as", options));
        setSummary(Utils.getFlag("summary", options));
        setGangs(Utils.getFlag("gang", options));

        super.setOptions(options);
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return the list of current option settings as an array of strings
     */
    @Override
    public String[] getOptions() {

		List<String> options = new LinkedList<>(Arrays.asList(super.getOptions()));

        if (getSummary()) options.add("-summary");
        if (getAnalogicalSet()) options.add("-as");
        if (getGangs()) options.add("-gang");
        return options.toArray(new String[0]);
    }

    /**
     * @param value whether gang effects will be printed
     */
    public void setGangs(boolean value) {
        m_Gangs = value;
    }

    /**
     * @return whether gang effects will be printed
     */
    public boolean getGangs() {
        return m_Gangs;
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the GUI
     */
    @SuppressWarnings("unused") // used by Weka
    public String gangsTipText() {
        return "Whether to print the gang effects.";
    }

    /**
     * @param value whether a summary is to be printed
     */
    public void setSummary(boolean value) {
        m_Summary = value;
    }

    /**
     * @return whether a summary will be printed
     */
    public boolean getSummary() {
        return m_Summary;
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the GUI
     */
	@SuppressWarnings("unused") // used by Weka
    public String summaryTipText() {
        return "Whether to print a short summary.";
    }

    /**
     * @return true if the analogical set will be printed
     */
    public boolean getAnalogicalSet() {
        return m_AnalogicalSet;
    }

    /**
     * @param value True if the analogical set should be printed
     */
    public void setAnalogicalSet(boolean value) {
        m_AnalogicalSet = value;
    }

    /**
     * Returns the tip text for this property.
     *
     * @return tip text for this property suitable for displaying in the GUI
     */
	@SuppressWarnings("unused") // used by Weka
    public String analogicalSetTipText() {
        return "Whether to print analogical sets";
    }
}
