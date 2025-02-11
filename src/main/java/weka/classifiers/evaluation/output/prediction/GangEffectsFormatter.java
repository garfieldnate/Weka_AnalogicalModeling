package weka.classifiers.evaluation.output.prediction;

import com.jakewharton.picnic.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AM.data.GangEffect;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static weka.classifiers.lazy.AM.AMUtils.*;

public class GangEffectsFormatter {
    private static final CellStyle SUBHEADER_STYLE = new CellStyle.Builder().setBorderTop(true).setBorderBottom(true).build();

    private final int numDecimals;
    private Format format;
    private final String lineSeparator;

    /**
     * @param numDecimals the number of digits to output after the decimal point
     */
    public GangEffectsFormatter(int numDecimals, Format format, String lineSeparator) {
        this.numDecimals = numDecimals;
        this.format = format;
        this.lineSeparator = lineSeparator;
    }

    /**
     * Format the provided gang effects using the specified format.
     */
    public String formatGangs(AMResults results) {
        switch (format) {
            case HUMAN: {
                return getHumanFormatted(results);
            }
            case CSV: {
                CsvDoc doc = getCsvDoc(results);
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
    private String getHumanFormatted(AMResults results) {
        Labeler labeler = results.getLabeler();
        BigDecimal totalPointers = new BigDecimal(results.getTotalPointers());
        TableSection.Builder bodyBuilder = new TableSection.Builder(); // 🏋️
        for (GangEffect effect : results.getGangEffects()) {
            // Subcontext header
            bodyBuilder.addRow(getSubcontextHeader(labeler, totalPointers, effect));
            effect.getClassToPointers().entrySet().stream().
                // sort by count then alphabetically by class name
                    sorted(
                    Map.Entry.<String, BigInteger>comparingByValue(Comparator.reverseOrder()).
                        thenComparing(Map.Entry.comparingByKey())).
                forEach(classToPointers -> {
                    Set<Instance> instances = effect.getClassToInstances().get(classToPointers.getKey());

                    // Class header
                    bodyBuilder.addRow(getClassHeader(classToPointers.getKey(), classToPointers.getValue(), totalPointers, instances.size()));

                    // sort and print instances
                    instances.stream().map(labeler::getInstanceAttsString).sorted().forEach(s -> bodyBuilder.addRow("", "", "", "", s));
                });
        }

        return new Table.Builder().
            setTableStyle(
                new TableStyle.Builder().
                    setBorder(true).build()).
            setCellStyle(
                REPORT_TABLE_STYLE
            ).setHeader(
                new TableSection.Builder().
                    addRow(
                        "Percentage", "Pointers", "Num Items", "Class", "Context").build())
            .setBody(bodyBuilder.build())
            .build()
            .toString();
    }

    private Row getClassHeader(String className, BigInteger classPointers, BigDecimal totalPointers, int numInstances) {
        return new Row.Builder().
            addCell(formatPointerPercentage(classPointers, totalPointers, numDecimals, true)).
            addCell(classPointers.toString()).
            addCell(Integer.toString(numInstances)).
            addCell(className).
            addCell("").
            build();
    }

    @NotNull
    private Row getSubcontextHeader(Labeler labeler, BigDecimal totalPointers, GangEffect effect) {
        return formatSubheaderRow(
            formatPointerPercentage(effect.getTotalPointers(), totalPointers, numDecimals, true),
            effect.getTotalPointers().toString(),
            "" + effect.getSubcontext().getExemplars().size(),
            "",
            labeler.getContextString(effect.getSubcontext().getLabel()));
    }

    private static Row formatSubheaderRow(String... content) {
        Row.Builder row = new Row.Builder();
        for (String c : content) {
            row.addCell(subheaderCell(c));
        }
        return row.build();
    }

    private static Cell subheaderCell(String content) {
        return new Cell.Builder(content).setStyle(SUBHEADER_STYLE).build();
    }

    private CsvDoc getCsvDoc(AMResults results) {
        CsvBuilder builder = new CsvBuilder();
        Labeler labeler = results.getLabeler();
        BigInteger totalPointers = results.getTotalPointers();
        int rank = 0;
        BigInteger previousPointers = null;
        for (GangEffect effect : results.getGangEffects()) {
            BigInteger totalEffectPointers = effect.getTotalPointers();
            Map<String, String> commonRowData = new HashMap<>();
            if (!totalEffectPointers.equals(previousPointers)) {
                rank += 1;
                previousPointers = totalEffectPointers;
            }
            commonRowData.put("rank", Integer.toString(rank));
            commonRowData.put("total_ptrs", totalPointers.toString());
            commonRowData.put("gang_ptrs", totalEffectPointers.toString());
            commonRowData.put("gang_pct", formatPointerPercentage(totalEffectPointers, new BigDecimal(totalPointers), numDecimals, false));
            commonRowData.put("size", Integer.toString(effect.getSubcontext().getExemplars().size()));
            effect.getClassToPointers().entrySet().stream().
                // sort by count then alphabetically by class name
                    sorted(
                    Map.Entry.<String, BigInteger>comparingByValue(Comparator.reverseOrder()).
                        thenComparing(Map.Entry.comparingByKey())).
                forEach(classToPointers -> {
                    Set<Instance> instances = effect.getClassToInstances().get(classToPointers.getKey());
                    String className = classToPointers.getKey();
                    BigInteger classPointers = classToPointers.getValue();

                    // Class data
                    commonRowData.put("class", className);

                    String classPtrsColumn = className + "_ptrs";
                    commonRowData.put(classPtrsColumn, classPointers.toString());
                    builder.setDefault(classPtrsColumn, "0");

                    String classPctColumn = className + "_pct";
                    commonRowData.put(classPctColumn, formatPointerPercentage(classPointers, new BigDecimal(totalPointers), numDecimals, false));
                    builder.setDefault(classPctColumn, "0.0");

                    String classNumInstancesColumn = className + "_size";
                    commonRowData.put(classNumInstancesColumn, Integer.toString(instances.size()));
                    builder.setDefault(classNumInstancesColumn, "0");

                    List<String> contextLabelList = labeler.getContextList(effect.getSubcontext().getLabel(), "*");
                    for (int i = 0; i < contextLabelList.size(); i++) {
                        commonRowData.put("GF" + (i + 1), contextLabelList.get(i));
                    }
                    instances.forEach(instance -> {
                        Map<String, String> finalRowData = new HashMap<>(commonRowData);
                        List<String> atts = labeler.getInstanceAttsList(instance);
                        for (int i = 0; i < atts.size(); i++) {
                            finalRowData.put("F" + (i + 1), atts.get(i));
                        }
                        builder.addEntry(finalRowData);
                    });

                });
        }

        return builder.build();
    }
}
