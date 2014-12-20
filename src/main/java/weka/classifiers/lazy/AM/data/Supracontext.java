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
 * duplicate supracontexts should be kept track of using count methods instead
 * of by saving seperate Supracontext objects. The count is stored in a
 * {@link BigInteger} object and starts out as {@link BigInteger#ONE one} and is
 * never allowed to fall below {@link BigInteger#ZERO zero}, which indicates
 * that the object should be discarded.
 * 
 * @author Nathan Glenn
 * 
 */
public abstract class Supracontext {

	protected BigInteger count = BigInteger.ONE;

	/**
	 * @return an unmodifiable view of the set of subcontexts contained in this
	 *         supracontext.
	 */
	public abstract Set<Subcontext> getData();

	/**
	 * @return true if this supracontext contains no subcontexts; false
	 *         otherwise.
	 */
	public boolean isEmpty() {
		return getData().isEmpty();
	}

	/**
	 * 
	 * @return the count, or number of instances, of this supracontext
	 */
	public BigInteger getCount() {
		return count;
	}

	/**
	 * Set the count of the supracontext.
	 * 
	 * @param count
	 *            the count
	 * @throws IllegalArgumentException
	 *             if c is null
	 */
	public void setCount(BigInteger count) {
		if (count == null)
			throw new IllegalArgumentException("count must not be null");
		if (count.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException(
					"count must not be less than zero");
		this.count = count;
	}

	/**
	 * Increases count by one.
	 */
	public void incrementCount() {
		count = count.add(BigInteger.ONE);
	}

	/**
	 * Decreases the count by one; if this reaches 0, then this Supracontext
	 * should be destroyed (by the caller).
	 * 
	 * @throws IllegalStateException
	 *             if the count is already zero.
	 */
	public void decrementCount() {
		if (count.equals(BigInteger.ZERO))
			throw new IllegalStateException("Count cannot be less than zero");
		count = count.subtract(BigInteger.ONE);
	}

	/**
	 * {@inheritDoc} Two Supracontexts are equal if they contain the same
	 * subcontexts.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Supracontext))
			return false;
		Supracontext otherSupra = (Supracontext) other;
		return getData().equals(otherSupra.getData());
	}

	@Override
	public int hashCode() {
		return getData().hashCode();
	}
}
