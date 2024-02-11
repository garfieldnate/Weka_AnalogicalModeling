package weka.classifiers.evaluation.output.prediction;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.Enum2TagUtils.TagInfo;

import java.time.Clock;

/**
 * Formatting choices for {@link AnalogicalModelingOutput}
 */
enum Format implements TagInfo {
    HUMAN("human", "Human-readable format"),
    CSV("csv", "Machine-readable CSV designed for analysis in Excel, Pandas, etc.");

//    TODO: name of relation and name of report (distribution, analogical set or gangs)
    public static final String getCsvCommentHeader(String relationName, String reportName) {
        return "# relation " + relationName + " (" + reportName + ")" + AMUtils.LINE_SEPARATOR +
            "# Generated via Weka Analogical Modeling plugin on " + Clock.systemDefaultZone().instant() + AMUtils.LINE_SEPARATOR +
            "# This data is in CSV format." + AMUtils.LINE_SEPARATOR +
            "# To load in Pandas: TODO" + AMUtils.LINE_SEPARATOR +
            "# To load in Excel: TODO" + AMUtils.LINE_SEPARATOR;
    }

    // string used on command line to indicate the use of this strategy
    private final String optionString;
    // string which describes comparison strategy for a given entry
    private final String description;

    /**
     * @param optionString The string required to choose this formatter from the command line
     * @param description  A description of the formatter for the given value
     */
    Format(String optionString, String description) {
        this.optionString = optionString;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getOptionString() {
        return optionString;
    }
}
