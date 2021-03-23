package weka.classifiers.lazy.AM.data;

import weka.classifiers.lazy.AM.label.Label;

import java.math.BigInteger;
import java.util.Set;

/**
 * A supracontext contains a set of {@link Subcontext Subcontexts} which have
 * certain commonalities in their {@link Label Labels}.
 *
 * Classifying data sets with analogical modeling tends to create many
 * supracontexts with the exact same set of subcontexts. To save time and space,
 * duplicate supracontexts should be kept track of using the count instead of by
 * saving separate Supracontext objects. The count is stored in a
 * {@link BigInteger} object and starts out as {@link BigInteger#ONE one} and is
 * never allowed to fall below {@link BigInteger#ZERO zero}, which indicates
 * that the object should be discarded.
 *
 * @author Nathan Glenn
 */
public interface Supracontext {

    /**
     * Return an exact, deep copy of the supracontext. The new object should be
     * an instance of the same class as the calling object.
     *
     * @return a deep copy of this supracontext.
     */
    Supracontext copy();

    /**
     * @return an unmodifiable view of the set of subcontexts contained in this supracontext.
     */
    Set<Subcontext> getData();

    /**
     * Add a subcontext to this supracontext.
     *
     * @param sub Subcontext to add to the supracontext.
     */
    void add(Subcontext sub);

    /**
     * @return true if this supracontext contains no subcontexts; false otherwise.
     */
    boolean isEmpty();

    /**
     * @return the count, or number of instances, of this supracontext
     */
    BigInteger getCount();

    /**
     * Set the count of the supracontext.
     *
     * @param count the count
     * @throws IllegalArgumentException if c is null or less than {@link BigInteger#ZERO}
     */
    void setCount(BigInteger count);

	/**
	 * Retrieve the supracontextual context, represented with a {@link Label} object.
	 * Label mismatches should be interpreted as "contained subcontexts may or may not match
	 * for this attribute, while matches should be regarded as "all contained subcontexts
	 * matched for this attribute".
	 *
	 * The running time for this default implementation is linear in the number of contained
	 * subcontexts.
	 *
	 * @return The context for this supracontext, or {@code null} if the subcontexts are empty
	 */
	default Label getContext() {
		return getData().stream().
				map(Subcontext::getLabel).
				reduce(Label::intersect).
				orElse(null);
	}

	/**
     * {@inheritDoc} Two Supracontexts are equal if they are of the same class
     * and contain the same subcontexts.
     */
    @Override
    boolean equals(Object other);

    /**
     * {@inheritDoc} The hashcode depends solely on the set of subcontexts
     * contained in a supracontext.
     */
    @Override
    int hashCode();
}
