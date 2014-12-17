package weka.classifiers.lazy.AM.label;

import java.util.Iterator;

/**
 * Labels are used to indicate what subcontexts or supracontexts a given
 * instance belongs to, and they are assigned by comparing test instance
 * attribute values with a training instance attribute values and assigning a
 * boolean "match" or "mismatch" value for each one.
 * 
 * Labels should also implement equals() and hashCode() for use in hashed
 * collections; however, Labels of two different classes do not have to be
 * equals(), even if the information they contain is equivalent.
 * 
 */
public abstract class Label {
	/**
	 * @return The number of attributes represented in this label.
	 */
	public abstract int getCardinality();

	/**
	 * Determine if the given index is marked as a match or a mismatch.
	 * 
	 * @param index
	 *            Index of the attribute being represented
	 * @return True if the index is a match, false otherwise.
	 * @throws IllegalArgumentException
	 *             if the index is less than 0 or greater than
	 *             {@link #getCardinality} - 1.
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
