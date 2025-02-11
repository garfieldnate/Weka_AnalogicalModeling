package weka.classifiers.evaluation.output.prediction;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Attribute;
import weka.core.Instance;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DistributionFormatter {
    private final int numDecimals;
    private final Format format;
    private final String lineSeparator;

    /**
     * @param numDecimals the number of digits to output after the decimal point
     * @param format
     */
    public DistributionFormatter(int numDecimals, Format format, String lineSeparator) {
        this.numDecimals = numDecimals;
        this.format = format;
        this.lineSeparator = lineSeparator;
    }

    public String formatDistribution(AMResults results, double[] distribution) {
        String doubleFormat = String.format("%%.%df", numDecimals);
        switch (format) {
            case HUMAN: {
                StringBuilder sb = new StringBuilder();
                sb.append("Class probability distribution:").append(lineSeparator);
                Attribute classAttribute = results.getClassifiedEx().classAttribute();
                for (int i = 0; i < distribution.length; i++) {
                    sb.append(classAttribute.value(i));
                    sb.append(": ");
                    sb.append(String.format(doubleFormat, distribution[i]));
                    sb.append(lineSeparator);
                }
                return sb.toString();
            }
            case CSV: {
                AMUtils.CsvDoc doc = getCsvDoc(results);
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(lineSeparator).setHeader(doc.headers.toArray(new String[]{})).build();
                StringWriter sw = new StringWriter();
                try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
                    for (List<String> entry : doc.entries) {
                        printer.printRecord(entry);
                    }
                } catch (IOException e) {
                    return "Error printing results to CSV: " + e;
                }
                return sw.toString();
            }
            default: {
                throw new IllegalStateException("Unknown formatter: " + format.getOptionString());
            }
        }
    }

    private AMUtils.CsvDoc getCsvDoc(AMResults results) {
        Labeler labeler = results.getLabeler();
        List<String> headers = new ArrayList<>();
        List<String> values = new ArrayList<>();

        headers.add("Judgement");
        values.add(results.getJudgement().toString().toLowerCase());

        headers.add("Expected");
        values.add(results.getExpectedClassName());

        Instance classifiedExemplar = results.getClassifiedEx();

        // value of each feature
        for (int i = 0; i < classifiedExemplar.numAttributes(); i++) {
            // skip ignored attributes and the class attribute
            if (labeler.isIgnored(i)) {
                continue;
            }
            if (i == classifiedExemplar.classIndex()) {
                continue;
            }
            Attribute classAtt = classifiedExemplar.attribute(i);
            headers.add("f_" + classAtt.name());
            values.add(classifiedExemplar.stringValue(classAtt));
        }

        // each potential class value
        Iterator<Object> classNameIterator = classifiedExemplar.classAttribute().enumerateValues().asIterator();
        int classIndex = 1;
        while (classNameIterator.hasNext()) {
            headers.add("Class " + classIndex);
            values.add((String) classNameIterator.next());
            classIndex += 1;
        }

        results.getClassPointers().forEach((className, pointers) -> {
            headers.add(className + "_ptrs");
            values.add(pointers.toString());
        });
        results.getClassLikelihood().forEach((className, likelihood) -> {
            headers.add(className + "_pct");
            BigDecimal percentage = likelihood.multiply(BigDecimal.valueOf(100)).round(MathContext.DECIMAL32);
            values.add(percentage.toString());
        });

        headers.add("train_size");
        values.add("" + results.getSubList().getConsideredExemplarCount());

        headers.add("num_feats");
        values.add("" + results.getLabeler().getCardinality());

        // column for each setting
        headers.add("ignore_unknowns");
        values.add("" + results.getLabeler().getIgnoreUnknowns());
        headers.add("missing_data_compare");
        values.add(results.getLabeler().getMissingDataCompare().getOptionString());
        headers.add("ignore_given");
        values.add("" + results.getSubList().getIgnoreFullMatches());
        headers.add("count_strategy");
        values.add(results.getPointerCountingStrategy().toString().toLowerCase());

        // just one row in this CSV (one exemplar classified)
        List<List<String>> entries = new ArrayList<>();
        entries.add(values);

        return new AMUtils.CsvDoc(headers, entries);
    }
}
