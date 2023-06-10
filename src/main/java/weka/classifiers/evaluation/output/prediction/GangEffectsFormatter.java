package weka.classifiers.evaluation.output.prediction;

import com.jakewharton.picnic.*;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.AMResults;
import weka.classifiers.lazy.AM.data.GangEffect;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import static weka.classifiers.lazy.AM.AMUtils.REPORT_TABLE_STYLE;

public class GangEffectsFormatter {
	private static final CellStyle SUBHEADER_STYLE = new CellStyle.Builder().setBorderTop(true).setBorderBottom(true).build();

	private final int numDecimals;
    private Format format;

    /**
     * @param numDecimals the number of digits to output after the decimal point
     */
    public GangEffectsFormatter(int numDecimals, Format format) {
        this.numDecimals = numDecimals;
        this.format = format;
    }

    /**
     * Format the provided gang effects nicely for human consumption; the returned string is <em>not</em> intended to be machine-readable.
     */
    public String formatGangs(AMResults results) {
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

        switch(format) {
            case HUMAN: {
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
            case CSV: {
                return "TODO";
            }
            default: {
                throw new IllegalStateException("Unknown format " + format.getOptionString());
            }
        }
    }

	private Row getClassHeader(String className, BigInteger classPointers, BigDecimal totalPointers, int numInstances) {
		return new Row.Builder().
				addCell(AMUtils.formatPointerPercentage(classPointers, totalPointers, numDecimals)).
				addCell(classPointers.toString()).
				addCell(Integer.toString(numInstances)).
				addCell(className).
				addCell("").
				build();
	}

	@NotNull
	private Row getSubcontextHeader(Labeler labeler, BigDecimal totalPointers, GangEffect effect) {
		return formatSubheaderRow(
				AMUtils.formatPointerPercentage(effect.getTotalPointers(), totalPointers, numDecimals),
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
}
