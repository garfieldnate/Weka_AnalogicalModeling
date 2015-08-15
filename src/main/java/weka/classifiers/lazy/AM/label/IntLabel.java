package weka.classifiers.lazy.AM.label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link Label} implementation that stores match/mismatch data in a single
 * integer for compactness and speed. The use of an integer as storage, however,
 * creates a limit to the size of the label. See {@link #MAX_CARDINALITY}.
 * 
 * @author Nathan Glenn
 * 
 */
public class IntLabel extends Label {
	/**
	 * The maximum cardinality of an integer label, which is limited by the
	 * number of bits in an integer in Java.
	 */
	public static final int MAX_CARDINALITY = 32;

	private final int labelBits;
	private final int card;
	private final int hashCode;

	/**
	 * 
	 * @param l
	 *            binary label represented by integer
	 * @param c
	 *            cardinality of the label
	 */
	public IntLabel(int l, int c) {
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
	public IntLabel(Label other) {
		// fast copy if the other label is an IntLabel
		// TODO: since this is immutable, wouldn't it make more sense to return
		// it (make a factory method instead)?
		if (other instanceof IntLabel) {
			IntLabel otherIntLabel = (IntLabel) other;
			labelBits = otherIntLabel.labelBits;
			card = otherIntLabel.card;
			hashCode = otherIntLabel.hashCode;
			return;
		}
		if (other.getCardinality() > MAX_CARDINALITY)
			throw new IllegalArgumentException(
					"Cardinality of label too high (" + other.getCardinality()
							+ "); max cardinality for this type of label is "
							+ MAX_CARDINALITY);
		card = other.getCardinality();
		int labelBits = 0;
		for (int i = 0; i < other.getCardinality(); i++)
			if (!other.matches(i))
				labelBits |= (1 << i);
		this.labelBits = labelBits;
		hashCode = calculateHashCode();
	}

	private int calculateHashCode() {
		int seed = 37;
		return seed * labelBits() + getCardinality();
	}

	/**
	 * @return An integer whose 1 bits represent the mismatches and 0 bits
	 *         represent the matches in this label.
	 */
	public int labelBits() {
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
		int mask = 1 << index;
		return (mask & labelBits) == 0;
	}

	@Override
	public int numMatches() {
		return getCardinality() - Integer.bitCount(labelBits);
	}

	@Override
	public Label intersect(Label other) {
		if (!(other instanceof IntLabel))
			throw new IllegalArgumentException(getClass().getSimpleName()
					+ "can only be intersected with other "
					+ getClass().getSimpleName());
		IntLabel otherLabel = (IntLabel) other;
		return new IntLabel(labelBits | otherLabel.labelBits, getCardinality());
	}

	@Override
	public Label union(Label other) {
		if (!(other instanceof IntLabel))
			throw new IllegalArgumentException(getClass().getSimpleName()
					+ "can only be unioned with another "
					+ getClass().getSimpleName());
		IntLabel otherLabel = (IntLabel) other;
		return new IntLabel(labelBits & otherLabel.labelBits, getCardinality());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String binary = Integer.toBinaryString(labelBits());

		int diff = getCardinality() - binary.length();
		for (int i = 0; i < diff; i++)
			sb.append('0');

		sb.append(binary);
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (!(other instanceof IntLabel)) {
			return false;
		}
		IntLabel otherLabel = (IntLabel) other;
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
		private int[] gaps;
		private final int card;
		private boolean hasNext = true;
		private int current;
		private int binCounter;

		/**
		 * @param supracontext
		 *            integer representing a label for a supracontext
		 * @param cardinality
		 *            number of bits needed to represent the vector
		 * @return Iterator over all subsets of the given label
		 */
		public SubsetIterator() {
			int supraContext = IntLabel.this.labelBits();
			card = IntLabel.this.getCardinality();
			current = supraContext;
			gaps = new int[card];
			List<Integer> gapsTemp = new ArrayList<Integer>();

			// iterate over the clear bits and create a list of gaps;
			// each gap in the list is an int with all 0 bits except
			// where the gap was found in the supracontext. So 10101
			// would create two gaps: 01000 and 00010.
			for (int i = 0; i < card; i++) {
				if (((1 << i) & supraContext) == 0) {
					// create an int with only bit i set to 1
					gapsTemp.add(1 << i);
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
				binCounter |= 1 << i;
			hasNext = true;

			gaps = new int[size];
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
			int i, tt;
			for (i = 0, tt = ~binCounter & (binCounter - 1); tt > 0; tt >>= 1, ++i)
				;
			current ^= gaps[i];
			binCounter--;
			if (binCounter == 0)
				hasNext = false;
			return new IntLabel(current, card);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public boolean isDescendantOf(Label possibleDescendant) {
		if (!(possibleDescendant instanceof IntLabel)) {
			return false;
		}
		IntLabel otherLabel = (IntLabel) possibleDescendant;
		// boolean lattice ancestor/descendants yield the descendant when ORed
		return (otherLabel.labelBits | labelBits) == labelBits;
	}
}
