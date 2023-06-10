package weka.classifiers.evaluation.output.prediction;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.core.Instances;

public class DistributionFormatter {
    private final int numDecimals;
    private Format format;

    /**
     * @param numDecimals the number of digits to output after the decimal point
     */
    public DistributionFormatter(int numDecimals, Format format) {
        this.numDecimals = numDecimals;
        this.format = format;
    }

    public String formatDistribution(AMResults results, double[] distribution, Instances m_Header) {
        String doubleFormat = String.format("%%.%df", numDecimals);
        StringBuilder sb = new StringBuilder();
        sb.append("Class probability distribution:").append(AMUtils.LINE_SEPARATOR);
        switch(format) {
            case HUMAN: {
                for (int i = 0; i < distribution.length; i++) {
                    sb.append(m_Header.classAttribute().value(i));
                    sb.append(": ");
                    sb.append(String.format(doubleFormat, distribution[i]));
                    sb.append(AMUtils.LINE_SEPARATOR);
                }
                break;
            }
            case CSV: {
                sb.append("TODO");
                break;
            }
            default: {
                throw new IllegalStateException("Unknown formatter: " + format.getOptionString());
            }
        }
        return sb.toString();
    }
}
