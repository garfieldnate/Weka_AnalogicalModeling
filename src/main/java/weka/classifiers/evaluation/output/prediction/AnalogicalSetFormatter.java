package weka.classifiers.evaluation.output.prediction;

import com.jakewharton.picnic.Table;
import com.jakewharton.picnic.TableSection;
import com.jakewharton.picnic.TableStyle;
import lombok.Value;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.lang.System.lineSeparator;

public class AnalogicalSetFormatter {

    private final int numDecimals;
    private final Format format;
    private final String lineSeparator;

    /**
     * @param numDecimals the number of digits to output after the decimal point
     */
    public AnalogicalSetFormatter(int numDecimals, Format format, String lineSeparator) {
        this.numDecimals = numDecimals;
        this.format = format;
        this.lineSeparator = lineSeparator;
    }

    @Value
    private static class TableEntry {
        BigInteger pointers;
        String percentage;
        String instanceAtts;
        String instanceClass;
    }


    public String formatAnalogicalSet(AMResults results) {
        TableSection.Builder bodyBuilder = new TableSection.Builder(); // 🏋️
        streamTableEntries(results).forEach(e ->
            bodyBuilder.addRow(e.getPercentage(), e.getPointers().toString(), e.getInstanceAtts(), e.getInstanceClass()));

        switch (format) {
            case HUMAN: {
                return new Table.Builder().
                    setTableStyle(
                        new TableStyle.Builder().
                            setBorder(true).build()).
                    setCellStyle(
                        AMUtils.REPORT_TABLE_STYLE
                    ).setHeader(
                        new TableSection.Builder().
                            addRow(
                                "Percentage", "Pointers", "Item", "Class").build())
                    .setBody(bodyBuilder.build())
                    .build()
                    .toString();
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
                throw new IllegalStateException("Unknown format " + format.getOptionString());
            }
        }
    }

    @NotNull
    private Stream<TableEntry> streamTableEntries(AMResults results) {
        final Labeler labeler = results.getLabeler();
        final BigDecimal totalPointers = new BigDecimal(results.getTotalPointers());

        BiFunction<Instance, BigInteger, TableEntry> getTableEntry = (inst, pointers) -> {
            String percentage = AMUtils.formatPointerPercentage(pointers, totalPointers, numDecimals);
            String instanceAtts = labeler.getInstanceAttsString(inst);
            String instanceClass = inst.stringValue(inst.classIndex());
            return new TableEntry(pointers, percentage, instanceAtts, instanceClass);
        };

        return results.getExemplarPointers().entrySet().stream()
            .map(e -> getTableEntry.apply(e.getKey(), e.getValue()))
            .sorted(
                Comparator.comparing(TableEntry::getPointers)
                    .reversed()
                    .thenComparing(TableEntry::getInstanceAtts)
                    .thenComparing(TableEntry::getInstanceClass));
    }

    private AMUtils.CsvDoc getCsvDoc(AMResults results) {
        List<String> headers = Arrays.asList("item", "class", "pointers", "percentage");
        List<List<String>> entries = new ArrayList<>();

        streamTableEntries(results).forEach(e -> entries.add(
            Arrays.asList(e.getInstanceAtts(), e.getInstanceClass(), e.getPointers().toString(), e.getPercentage())));

        return new AMUtils.CsvDoc(headers, entries);
    }
}
