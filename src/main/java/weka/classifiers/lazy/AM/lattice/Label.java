package weka.classifiers.lazy.AM.lattice;

import java.util.Iterator;

/**
 * Labels are used to indicate what subcontexts or supracontexts a given
 * instance belongs to, and they are assigned by comparing test instance
 * attribute values with a training instance attribute values and assigning a
 * boolean "match" or "mismatch" value for each one.
 * 
 */
public abstract class Label {

	// The lattice implementations use hashmaps to store labels and
	// supracontexts
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object other);

	/**
	 * @return The number of attributes represented in this label.
	 */
	public abstract int cardinality();

	/**
	 * Determine if the given index is marked as a match or a mismatch.
	 * 
	 * @param index
	 *            Index of the attribute being represented
	 * @return True if the index is a match, false otherwise.
	 * @throws IllegalArgumentException
	 *             if index is greater than the cardinality.
	 */
	public abstract boolean matches(int index);

	/**
	 * The "descendants" of a label are the set of labels with the same
	 * "mismatch" entries, but with one or more of the "match" entries changed
	 * into a "mismatch" entry. For example, the children of
	 * <code>{match, mismatch, mistmatch, match}</code> are:
	 * <ul>
	 * <li><code>{mismatch, mismatch, mistmatch, match}</code>,</li>
	 * <li><code>{match, mismatch, mistmatch, mismatch}</code>, and</li>
	 * <li><code>{mismatch, mismatch, mistmatch, mismatch}</code></li>
	 * </ul>
	 * .
	 * 
	 * @return An iterator over the label descendants
	 */
	public abstract Iterator<Label> descendantIterator();

}
