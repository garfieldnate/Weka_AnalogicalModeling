package weka.classifiers.lazy.AM.data;

import com.jakewharton.picnic.*;
import lombok.Value;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.function.BiFunction;

public class AnalogicalSetFormatter {

    @Value
    private static class TableEntry {
        BigInteger pointers;
        String percentage;
        String instanceAtts;
        String instanceClass;
    }

    public static String formatAnalogicalSet(AMResults results) {
        final Labeler labeler = results.getLabeler();
        final BigDecimal totalPointers = new BigDecimal(results.getTotalPointers());

        BiFunction<Instance, BigInteger, TableEntry> getTableEntry = (inst, pointers) -> {
            String percentage = AMUtils.formatPointerPercentage(pointers, totalPointers);
            String instanceAtts = labeler.getInstanceAttsString(inst);
            String instanceClass = inst.stringValue(inst.classIndex());
            return new TableEntry(pointers, percentage, instanceAtts, instanceClass);
        };

        TableSection.Builder bodyBuilder = new TableSection.Builder(); // 🏋️
        results.getExemplarPointers().entrySet().stream()
            .map(e -> getTableEntry.apply(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(TableEntry::getPointers).reversed().thenComparing(TableEntry::getInstanceAtts).thenComparing(TableEntry::getInstanceClass))
            .forEach(e -> bodyBuilder.addRow(e.getPercentage(), e.getPointers().toString(), e.getInstanceAtts(), e.getInstanceClass()));

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
}
