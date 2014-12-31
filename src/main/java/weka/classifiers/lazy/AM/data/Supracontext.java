package weka.classifiers.lazy.AM.data;

import java.math.BigInteger;
import java.util.Set;

import weka.classifiers.lazy.AM.label.Label;

/**
 * A supracontext contains a set of {@link Subcontext Subcontexts} which have
 * certain commonalities in their {@link Label Labels}.
 * 
 * Classifying data sets with analogical modeling tends to create many
 * supracontexts with the exact same set of subcontexts. To save time and space,
 * duplicate supracontexts should be kept track of using the count instead of by
 * saving seperate Supracontext objects. The count is stored in a
 * {@link BigInteger} object and starts out as {@link BigInteger#ONE one} and is
 * never allowed to fall below {@link BigInteger#ZERO zero}, which indicates
 * that the object should be discarded.
 * 
 * @author Nathan Glenn
 * 
 */
public abstract interface Supracontext {

	/**
	 * Return an exact, deep copy of the supracontext. The new object should be
	 * an instance of the same class as the calling object.
	 * 
	 * @return a deep copy of this supracontext.
	 */
	public Supracontext copy();

	/**
	 * @return an unmodifiable view of the set of subcontexts contained in this
	 *         supracontext.
	 */
	public Set<Subcontext> getData();

	/**
	 * Add a subcontext to this supracontext.
	 * 
	 * @param sub
	 *            Subcontext to add to the supracontext.
	 */
	public void add(Subcontext sub);

	/**
	 * @return true if this supracontext contains no subcontexts; false
	 *         otherwise.
	 */
	public boolean isEmpty();

	/**
	 * 
	 * @return the count, or number of instances, of this supracontext
	 */
	public BigInteger getCount();

	/**
	 * Set the count of the supracontext.
	 * 
	 * @param count
	 *            the count
	 * @throws IllegalArgumentException
	 *             if c is null or less than {@link BigInteger#ZERO}
	 */
	public void setCount(BigInteger count);

	/**
	 * {@inheritDoc} Two Supracontexts are equal if they are of the same class
	 * and contain the same subcontexts.
	 */
	@Override
	public boolean equals(Object other);

	/**
	 * {@inheritDoc} The hashcode depends solely on the set of subcontexts
	 * contained in a supracontext.
	 */
	@Override
	public int hashCode();
}
