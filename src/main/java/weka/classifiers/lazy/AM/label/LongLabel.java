package weka.classifiers.lazy.AM.label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link Label} implementation that stores match/mismatch data in a single
 * long for compactness and speed. The use of an long as storage, however,
 * creates a limit to the size of the label. See {@link #MAX_CARDINALITY}.
 * 
 * @author Nathan Glenn
 * 
 */
public class LongLabel extends Label {
	/**
	 * The maximum cardinality of a long label, which is limited by the number
	 * of bits in a long in Java.
	 */
	public static final int MAX_CARDINALITY = 64;

	private final long labelBits;
	private final int card;
	private final int hashCode;

	/**
	 * 
	 * @param l
	 *            binary label represented by bits in a long
	 * @param c
	 *            cardinality of the label
	 */
	public LongLabel(long l, int c) {
		if (c > MAX_CARDINALITY)
			throw new IllegalArgumentException("Input cardinality too high ("
					+ c + "); max cardinality for this labeler is "
					+ MAX_CARDINALITY);
		labelBits = l;
		card = c;
		hashCode = calculateHashCode();
	}

	/**
	 * Create an IntLabel by copying the contents of another {@link Label}.
	 * 
	 * @param other
	 */
	public LongLabel(Label other) {
		if (other.getCardinality() > MAX_CARDINALITY)
			throw new IllegalArgumentException(
					"Cardinality of label too high (" + other.getCardinality()
							+ "); max cardinality for this type of label is "
							+ MAX_CARDINALITY);
		card = other.getCardinality();
		long labelBits = 0;
		for (int i = 0; i < other.getCardinality(); i++)
			if (!other.matches(i))
				labelBits |= (1l << i);
		this.labelBits = labelBits;
		hashCode = calculateHashCode();
	}

	private int calculateHashCode() {
		int seed = 37;

		return seed * Long.valueOf(labelBits()).hashCode() + getCardinality();
	}

	/**
	 * @return A long whose 1 bits represent the mismatches and 0 bits represent
	 *         the matches in this label.
	 */
	public long labelBits() {
		return labelBits;
	}

	@Override
	public int getCardinality() {
		return card;
	}

	@Override
	public boolean matches(int index) {
		if (index > getCardinality() - 1 || index < 0)
			throw new IllegalArgumentException("Illegal index: " + index);
		long mask = 1l << index;
		return (mask & labelBits) == 0;
	}

	@Override
	public Label intersect(Label other) {
		if (!(other instanceof LongLabel))
			throw new IllegalArgumentException(getClass().getSimpleName()
					+ "can only be intersected with other "
					+ getClass().getSimpleName());
		LongLabel otherLabel = (LongLabel) other;
		return new LongLabel(labelBits | otherLabel.labelBits, getCardinality());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String binary = Long.toBinaryString(labelBits());

		int diff = getCardinality() - binary.length();
		for (int i = 0; i < diff; i++)
			sb.append('0');

		sb.append(binary);
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LongLabel)) {
			return false;
		}
		LongLabel otherLabel = (LongLabel) other;
		return otherLabel.labelBits() == labelBits()
				&& otherLabel.getCardinality() == getCardinality();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public Iterator<Label> descendantIterator() {
		return new SubsetIterator();
	}

	private class SubsetIterator implements Iterator<Label> {

		// each will be all zeros except where one of the zeros in the tested
		// item is.
		private long[] gaps;
		private final int card;
		private boolean hasNext = true;
		private long current;
		private long binCounter;

		/**
		 * @return Iterator over all subsets of the given label
		 */
		public SubsetIterator() {
			long supraContext = LongLabel.this.labelBits();
			card = LongLabel.this.getCardinality();
			current = supraContext;
			gaps = new long[card];
			List<Long> gapsTemp = new ArrayList<Long>();

			// iterate over the clear bits and create a list of gaps;
			// each gap in the list is a long with all 0 bits except
			// where the gap was found in the supracontext. So 10101
			// would create two gaps: 01000 and 00010.
			for (int i = 0; i < card; i++) {
				if (((1 << i) & supraContext) == 0) {
					// create a long with only bit i set to 1
					gapsTemp.add(1l << i);
				}
			}
			int size = gapsTemp.size();
			// if there were no gaps, then there is nothing to iterate over
			if (size == 0) {
				hasNext = false;
				return;
			}
			// binCounter needs to be all ones for the last n bits, where n is
			// numGaps;
			binCounter = 0;
			for (int i = 0; i < size; i++)
				binCounter |= 1l << i;
			hasNext = true;

			gaps = new long[size];
			for (int i = 0; i < size; i++)
				gaps[i] = gapsTemp.get(i);
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public Label next() {
			// choose gap to choose bit to flip; it's whichever is the rightmost
			// 1 in binCounter
			// first find the rightmost 1 in t; from HAKMEM, I believe
			int i;
			long tt;
			for (i = 0, tt = ~binCounter & (binCounter - 1); tt > 0; tt >>= 1, ++i)
				;
			current ^= gaps[i];
			binCounter--;
			if (binCounter == 0)
				hasNext = false;
			return new LongLabel(current, card);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean isDescendantOf(Label possibleDescendant) {
		if (!(possibleDescendant instanceof LongLabel)) {
			return false;
		}
		LongLabel otherLabel = (LongLabel) possibleDescendant;
		// boolean lattice ancestor/descendants yield the descendant when ORed
		return (otherLabel.labelBits | labelBits) == labelBits;
	}
}
