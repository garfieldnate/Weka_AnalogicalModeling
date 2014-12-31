package weka.classifiers.lazy.AM.data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of {@link Supracontext} with no extra features.
 * 
 * @author Nathan Glenn
 * 
 */
public class BasicSupra implements Supracontext {
	private BigInteger count = BigInteger.ONE;
	private final Set<Subcontext> data;

	/**
	 * Create a new supracontext with an empty data set.
	 */
	public BasicSupra() {
		data = new HashSet<>();
	}

	/**
	 * Creates a new supracontext with the given parameters as the contents.
	 * 
	 * @param data
	 *            The subcontexts contained in the supracontext
	 * @param count
	 *            The count of this supracontext
	 * @throws IllegalArgumentException
	 *             if data or count are null, or count is less than
	 *             {@link BigInteger#ZERO}
	 */
	public BasicSupra(Set<Subcontext> data, BigInteger count) {
		if (data == null)
			throw new IllegalArgumentException("data must not be null");
		setCount(count);
		this.data = new HashSet<>(data);
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
