package weka.classifiers.lazy.AM;

import weka.classifiers.lazy.AM.lattice.MissingDataCompare;

/**
 * This class holds several constants used in the AM classifier.
 * 
 * @author nathan.glenn
 * 
 */
public interface AMconstants {

	// Missing value handling modes
	/**
	 * Considers the missing attribute value to match anything
	 */
	public static final int M_VARIABLE = MissingDataCompare.VARIABLE.ordinal();

	/**
	 * Considers the missing attribute value to be a mismatch
	 */
	public static final int M_MATCH = MissingDataCompare.MATCH.ordinal();

	/**
	 * Treats missing value as like any other value; two missing values match,
	 * but a missing value matches nothing else.
	 */
	public static final int M_MISMATCH = MissingDataCompare.MISMATCH.ordinal();
	

	/**
	 * NONDETERMINISTIC will be mapped to "&nondeterministic&", and is used by
	 * {@link weka.classifiers.lazy.AnalogicalModeling.lattice.Supracontext Supracontext} (this is '*' in the
	 * red book paper).
	 * 
	 */
	public static final int NONDETERMINISTIC = -1;
	
	public static final String NONDETERMINISTIC_STRING = "&nondeterministic&";
	
	/**
	 * EMPTY will be mapped to "&empty&", and is used by
	 * {@link weka.classifiers.lazy.AnalogicalModeling.lattice.Supracontext Supracontext} (this is *supralist
	 * in the red book paper).
	 * 
	 */
	public static final int EMPTY = -2;
	


	/**
	 * MISSING will be mapped to "?" by default, and can be used to denote
	 * missing data.
	 * TODO: has Weka made this obsolete? Weka uses Double.NaN...
	 */
	public static final int MISSING = -3;
	public static final String MISSING_STRING = "?";

}
