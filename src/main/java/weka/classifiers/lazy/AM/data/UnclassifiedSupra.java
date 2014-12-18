package weka.classifiers.lazy.AM.data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import weka.classifiers.lazy.AM.lattice.LatticeNode;

/**
 * This is a supracontext which does not keep track of its outcome in any way.
 * 
 * Partial supracontexts may be combined to create either new PartialSupras or
 * new {@link ClassifiedSupra ClassifiedSupras}. When this is done, the new
 * supracontext contains the intersection of subcontexts contained in both of
 * its parent supracontexts, and the count is set to the product of the two
 * original counts.
 * 
 * @author Nathan Glenn
 * 
 */
public class UnclassifiedSupra extends Supracontext implements
		LatticeNode<UnclassifiedSupra> {
	private UnclassifiedSupra next;
	private final int index;
	private BigInteger count = BigInteger.ONE;
	private Set<Subcontext> data;

	public UnclassifiedSupra() {
		index = -1;
		data = new HashSet<>();
	}

	public UnclassifiedSupra(UnclassifiedSupra other, Subcontext sub, int index) {
		data = new HashSet<>(other.data);
		data.add(sub);
		this.index = index;
		setNext(other.getNext());
		other.setNext(this);
	}

	@Override
	public UnclassifiedSupra getNext() {
		return next;
	}

	@Override
	public void setNext(UnclassifiedSupra next) {
		this.next = next;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public BigInteger getCount() {
		return count;
	}

	@Override
	public void incrementCount() {
		count = count.add(BigInteger.ONE);
	}

	@Override
	public void decrementCount() {
		if (count.equals(BigInteger.ZERO))
			throw new IllegalStateException("Count cannot be less than zero");
		count = count.subtract(BigInteger.ONE);
	}

	@Override
	public void setCount(BigInteger count) {
		if (count == null)
			throw new IllegalArgumentException("count must not be null");
		if (count.compareTo(BigInteger.ZERO) < 0)
			throw new IllegalArgumentException(
					"count must not be less than zero");
		this.count = count;
	}

	/**
	 * Combine this partial supracontext with another to make a third which
	 * contains the subcontexts in common between the two, and a count which is
	 * set to the product of the two counts. Return null if the resulting object
	 * would have no subcontexts.
	 * 
	 * @param other
	 *            other partial supracontext to combine with
	 * @return A new partial supracontext, or null if it would have been empty.
	 */
	public UnclassifiedSupra combine(UnclassifiedSupra other) {
		Set<Subcontext> smaller;
		Set<Subcontext> larger;
		if (data.size() > other.data.size()) {
			larger = data;
			smaller = other.data;
		} else {
			smaller = data;
			larger = other.data;
		}
		Set<Subcontext> combinedSubs = new HashSet<>(smaller);
		combinedSubs.retainAll(larger);

		if (combinedSubs.isEmpty())
			return null;
		UnclassifiedSupra supra = new UnclassifiedSupra();
		supra.data = combinedSubs;
		supra.setCount(getCount().multiply(other.getCount()));
		return supra;
	}

	/**
	 * Combine this partial supracontext with another to make a
	 * {@link ClassifiedSupra} object. The new one contains the subcontexts
	 * found in both, and the pointer count is set to the product of the two
	 * pointer counts. If it turns out that the resulting supracontext would be
	 * heterogeneous or empty, then return null instead.
	 * 
	 * @param otherSupra
	 *            other partial supracontext to combine with
	 * @return a combined supracontext, or null if supra1 and supra2 had no data
	 *         in common or if the new supracontext is heterogeneous
	 */
	public ClassifiedSupra combineFinalize(UnclassifiedSupra otherSupra) {
		Set<Subcontext> smaller;
		Set<Subcontext> larger;
		if (data.size() > otherSupra.data.size()) {
			larger = data;
			smaller = otherSupra.data;
		} else {
			larger = otherSupra.data;
			smaller = data;
		}

		ClassifiedSupra supra = new ClassifiedSupra();
		for (Subcontext sub : smaller)
			if (larger.contains(sub)) {
				supra.add(sub);
				if (supra.isHeterogeneous())
					return null;
			}
		if (supra.isEmpty())
			return null;

		supra.setCount(getCount().multiply(otherSupra.getCount()));
		return supra;
	}

	@Override
	public Set<Subcontext> getData() {
		return Collections.unmodifiableSet(data);
	}

	/**
	 * @return String representation of this supracontext in this form: "["
	 *         count "x" sub1.toString() "," sub2.toString() ... "]"
	 */
	@Override
	public String toString() {
		if (isEmpty())
			return "[EMPTY]";

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(count);
		sb.append('x');
		for (Subcontext sub : data) {
			sb.append(sub);
			sb.append(',');
		}
		// remove last commas
		sb.deleteCharAt(sb.length() - 1);
		sb.append(']');
		return sb.toString();
	}

}
