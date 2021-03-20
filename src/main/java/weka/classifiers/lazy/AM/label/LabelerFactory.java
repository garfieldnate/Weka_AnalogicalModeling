package weka.classifiers.lazy.AM.label;

import weka.core.Instance;

/**
 * A factory for creating {@link Labeler} instances.
 *
 * @author Nathan Glenn
 */
public interface LabelerFactory {
	/**
	 * Create and return a new {@link Labeler} instance for labeling data
	 * instances via comparison with a test instance.
	 *
	 * @param testInstance   The instance being classified
	 * @param mdc            The strategy for comparing missing data
	 * @param ignoreUnknowns True if missing data should be ignored, false otherwise
	 * @return A new Labeler
	 */
	Labeler createLabeler(Instance testInstance, boolean ignoreUnknowns, MissingDataCompare mdc);

	/**
	 * @return The maximum cardinality for labels created by this labeler, or -1 if there is no maximum.
	 */
	default int getMaximumCardinality() {
		return -1;
	}

	class IntLabelerFactory implements LabelerFactory {
		@Override
		public Labeler createLabeler(Instance testInstance, boolean ignoreUnknowns, MissingDataCompare mdc) {
			return new IntLabeler(testInstance, ignoreUnknowns, mdc);
		}

		@Override
		public int getMaximumCardinality() {
			return IntLabel.MAX_CARDINALITY;
		}
	}
	class LongLabelerFactory implements LabelerFactory {
		@Override
		public Labeler createLabeler(Instance testInstance, boolean ignoreUnknowns, MissingDataCompare mdc) {
			return new LongLabeler(testInstance, ignoreUnknowns, mdc);
		}

		@Override
		public int getMaximumCardinality() {
			return LongLabel.MAX_CARDINALITY;
		}
	}
	class BitSetLabelerFactory implements LabelerFactory {
		@Override
		public Labeler createLabeler(Instance testInstance, boolean ignoreUnknowns, MissingDataCompare mdc) {
			return new BitSetLabeler(testInstance, ignoreUnknowns, mdc);
		}
	}

	/**
	 * {@inheritDoc}
	 * The cardinality of the instances restricts what kinds of {@link Label labels} can be used, and
	 * the Labeler which produces the smallest, fastest labels will be returned.
	 */
	class CardinalityBasedLabelerFactory implements LabelerFactory {
		@Override
		public Labeler createLabeler(Instance testInstance, boolean ignoreUnknowns, MissingDataCompare mdc) {
			// cardinality may be significantly reduced if we are ignoring unknowns
			int cardinality = Labeler.getCardinality(testInstance, ignoreUnknowns);
			Labeler labeler;
			// int and long labels are faster and smaller, so use them if the
			// cardinality turns out to be small enough
			if (cardinality <= IntLabel.MAX_CARDINALITY) labeler = new IntLabeler(testInstance, ignoreUnknowns, mdc);
			else if (cardinality <= LongLabel.MAX_CARDINALITY)
				labeler = new LongLabeler(testInstance, ignoreUnknowns, mdc);
			else labeler = new BitSetLabeler(testInstance, ignoreUnknowns, mdc);

			return labeler;
		}
	}
}
