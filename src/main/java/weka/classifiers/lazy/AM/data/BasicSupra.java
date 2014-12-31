package weka.classifiers.lazy.AM.data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
public class BasicSupra implements Supracontext {
	private BigInteger count = BigInteger.ONE;
	private final Set<Subcontext> data;

	public BasicSupra() {
		data = new HashSet<>();
	}

	public BasicSupra(Set<Subcontext> data, BigInteger count) {
		this.data = new HashSet<>(data);
		this.count = count;
	}

	@Override
	public void add(Subcontext sub) {
		data.add(sub);
	}

	@Override
	public Set<Subcontext> getData() {
		return Collections.unmodifiableSet(data);
	}

	@Override
	public boolean isEmpty() {
		return getData().isEmpty();
	}

	@Override
	public BigInteger getCount() {
		return count;
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

	@Override
	public BasicSupra copy() {
		return new BasicSupra(data, count);
	}

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
