package weka.classifiers.lazy.AM.data;

import com.jakewharton.picnic.*;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.core.Instance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

public class GangEffectsFormatter {
	private static final CellStyle SUBHEADER_STYLE = new CellStyle.Builder().setBorderTop(true).setBorderBottom(true).build();

    /**
     * Format the provided gang effects nicely for human consumption; the returned string is <em>not</em> intended to be machine-readable.
     */
    public static String formatGangs(AMResults results) {
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
						new CellStyle.Builder()
								.setPaddingLeft(1).
								setPaddingRight(1).
								setBorderLeft(true).
								setBorderRight(true).
								setAlignment(TextAlignment.MiddleRight).build()
				).setHeader(
				new TableSection.Builder().
						addRow(
								"Percentage", "Pointers", "Num Items", "Class", "Context").build())
				.setBody(bodyBuilder.build())
				.setFooter(
						new TableSection.Builder()
								.build())
				.build()
				.toString();
	}

	private static Row getClassHeader(String className, BigInteger classPointers, BigDecimal totalPointers, int numInstances) {
		return new Row.Builder().
				addCell(AMUtils.formatPointerPercentage(classPointers, totalPointers)).
				addCell(classPointers.toString()).
				addCell(Integer.toString(numInstances)).
				addCell(className).
				addCell("").
				build();
	}

	@NotNull
	private static Row getSubcontextHeader(Labeler labeler, BigDecimal totalPointers, GangEffect effect) {
		return formatSubheaderRow(
				AMUtils.formatPointerPercentage(effect.getTotalPointers(), totalPointers),
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
