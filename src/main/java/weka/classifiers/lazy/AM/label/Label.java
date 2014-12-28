package weka.classifiers.lazy.AM.label;

import java.util.Iterator;

/**
 * Analogical Modeling uses labels composed of boolean vectors in order to group
 * instances into subcontexts and subcontexts into supracontexts. Training set
 * instances are assigned labels by comparing them with the instance to be
 * classified and encoding matched attributes and mismatched attributes in a
 * boolean vector.
 * 
 * Labels should implement equals() and hashCode() for use in hashed
 * collections; however, Labels of two different classes do not have to be
 * equals() or have equal hashCodes, even if the information they contain is
 * equivalent.
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

	/**
	 * Determine if this label is the "ancestor" of, or contains, a given label.
	 * This label contains the other label if every matching entry in the other
	 * label is also a matching entry in this label. This method assumes that an
	 * equivalent label was not given as the argument.
	 * 
	 * @param possibleDescendant
	 *            possible label descendant
	 * @return true if possibleDescendant is a descendant of this label; false
	 *         otherwise.
	 */
	public abstract boolean isAncestorOf(Label possibleDescendant);

	/**
	 * Create a new label for which each location is marked as a match if this
	 * label and otherLabel both are marked match, otherwise mismatch.
	 * 
	 * @param otherLabel
	 *            the label to intersect with this one
	 * @return an intersected label
	 */
	public abstract Label intersect(Label otherLabel);
}
