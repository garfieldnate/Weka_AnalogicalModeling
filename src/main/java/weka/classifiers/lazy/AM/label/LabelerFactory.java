package weka.classifiers.lazy.AM.label;

import weka.core.Instance;

/**
 * A factory for creating {@link Labeler} instances.
 *
 * @author Nathan Glenn
 */
public class LabelerFactory {

    /**
     * Create and return a new {@link Labeler} instance for labeling data
     * instances via comparison with a test instance. The cardinality of the
     * instances restricts what kinds of {@link Label labels} can be used, and
     * the Labeler which produces the smallest, fastest labels will be returned.
     *
     * @param testInstance   The instance being classified
     * @param mdc            The strategy for comparing missing data
     * @param ignoreUnknowns True if missing data should be ignored, false otherwise
     * @return A new Labeler
     */
    public static Labeler createLabeler(Instance testInstance, boolean ignoreUnknowns, MissingDataCompare mdc) {
        // cardinality may be significantly reduced if we are ignoring unknowns
        int cardinality = Labeler.getCardinality(testInstance, ignoreUnknowns);
        Labeler labeler;
        // int and long labels are faster and smaller, so use them if the
        // cardinality turns out to be small enough
        if (cardinality <= IntLabel.MAX_CARDINALITY) labeler = new IntLabeler(mdc, testInstance, ignoreUnknowns);
        else if (cardinality <= LongLabel.MAX_CARDINALITY) labeler = new LongLabeler(mdc, testInstance, ignoreUnknowns);
        else labeler = new BitSetLabeler(mdc, testInstance, ignoreUnknowns);

        return labeler;
    }
}
