package weka.classifiers.lazy.AM.label;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntLabel extends Label {
	private final int label;
	private final int card;

	/**
	 * 
	 * @param l
	 *            binary label represented by integer
	 * @param c
	 *            cardinality of the label
	 */
	public IntLabel(int l, int c) {
		label = l;
		card = c;
	}

	/**
	 * @return An integer whose 1 bits represent the mismatches and 0 bits
	 *         represent the matches in this label.
	 */
	public int labelBits() {
		return label;
	}

	@Override
	public int getCardinality() {
		return card;
	}

	@Override
	public boolean matches(int index) {
		if (index > getCardinality())
			throw new IllegalArgumentException("was given " + index
					+ " but cardinality is only " + getCardinality());
		int mask = (int) Math.pow(2, index);
		return (mask & label) == 0;
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
		// quick comparison if it's another IntLabel
		if (other instanceof IntLabel) {
			IntLabel otherLabel = (IntLabel) other;
			return otherLabel.labelBits() == labelBits()
					&& otherLabel.getCardinality() == getCardinality();
		} else {
			// slow comparison if it's another kind of label
			Label otherLabel = (Label) other;
			if (otherLabel.getCardinality() != getCardinality())
				return false;
			for (int i = 0; i < getCardinality(); i++) {
				if (matches(i) != otherLabel.matches(i))
					return false;
			}
			return true;
		}
	}

	private static final int SEED = 37;

	@Override
	public int hashCode() {
		return SEED * labelBits() + getCardinality();
	}

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
}
