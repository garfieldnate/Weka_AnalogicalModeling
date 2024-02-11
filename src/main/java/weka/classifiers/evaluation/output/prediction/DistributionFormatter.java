package weka.classifiers.evaluation.output.prediction;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static weka.classifiers.evaluation.output.prediction.Format.getCsvCommentHeader;

public class DistributionFormatter {
    private final int numDecimals;
    private final String lineSeparator;

    /**
     * @param numDecimals the number of digits to output after the decimal point
     */
    public DistributionFormatter(int numDecimals, String lineSeparator) {
        this.numDecimals = numDecimals;
        this.lineSeparator = lineSeparator;
    }

    public String formatDistribution(AMResults results, double[] distribution, String relationName, Format format) {
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
                CsvDoc doc = getCSVDoc(results);

                CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setRecordSeparator(lineSeparator).setHeader(doc.headers.toArray(new String[]{})).build();
                StringWriter sw = new StringWriter();
                // for now this is too much to write for just a single row of output
//                sw.write(getCsvCommentHeader(relationName, "Class Probability Distribution"));
//                sw.write(lineSeparator);
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

    private static class CsvDoc {
        final List<String> headers;
        final List<List<String>> entries;

        private CsvDoc(List<String> headers, List<List<String>> entries) {
            this.headers = headers;
            this.entries = entries;
        }
    }

    private CsvDoc getCSVDoc(AMResults results) {
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
            headers.add(classAtt.name());
            values.add(classifiedExemplar.stringValue(classAtt));
        }

        // each potential class value
        Iterator<Object> classNameIterator = classifiedExemplar.classAttribute().enumerateValues().asIterator();
        int classIndex = 1;
        while (classNameIterator.hasNext()) {
            headers.add("Class " + classIndex);
            values.add((String) classNameIterator.next());
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
        // subract one for the class
        values.add("" + (classifiedExemplar.numAttributes() - 1));

        // just one row in this CSV (one exemplar classified)
        List<List<String>> entries = new ArrayList<>();
        entries.add(values);

        return new CsvDoc(headers, entries);
    }
}
