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
	private final Set<Subcontext> data;

	public UnclassifiedSupra() {
		index = -1;
		data = new HashSet<>();
	}

	public UnclassifiedSupra(Set<Subcontext> data, BigInteger count) {
		index = -1;
		this.data = data;
		this.count = count;
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
