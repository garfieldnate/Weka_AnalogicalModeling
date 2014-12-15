package weka.classifiers.lazy.AM.lattice;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public class BitSetLabel extends Label {
	private final BitSet label;
	private final int card;

	/**
	 * 
	 * @param l
	 *            binary label represented by integer
	 * @param c
	 *            cardinality of the label
	 */
	public BitSetLabel(BitSet l, int c) {
		label = l;
		card = c;
	}

	@Override
	public int getCardinality() {
		return card;
	}

	@Override
	public boolean matches(int index) {
		if(index > getCardinality())
			throw new IllegalArgumentException("was given " + index + " but cardinality is only " + getCardinality());
		return label.get(index);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String binary = label.toString();

		int diff = getCardinality() - binary.length();
		for (int i = 0; i < diff; i++)
			sb.append('0');

		sb.append(binary);
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		// quick comparison if the other label is of the same class
		if (other instanceof BitSetLabel){
			BitSetLabel otherLabel = (BitSetLabel) other;
			return otherLabel.getCardinality() == getCardinality()
					&& otherLabel.label.equals(label);
		} else {
			if(!(other instanceof Label))
				return false;
			// otherwise a slow comparison of each individual bit
			Label otherLabel = (Label) other;
			if(getCardinality() != otherLabel.getCardinality())
				return false;
			for(int i = 0; i < card; i++)
				if(label.get(i) ^ otherLabel.matches(i))
					return false;
			return true;
		}
	}

	private static final int SEED = 37;

	@Override
	public int hashCode() {
		return SEED * getCardinality() + label.hashCode();
	}

	public Iterator<Label> descendantIterator() {
		return new SubsetIterator();
	}

	private class SubsetIterator implements Iterator<Label> {
		// the indices of the 0 entries
		private List<Integer> gaps;
		private final int card;
		private boolean hasNext = true;
		private BitSet current;
		private BitSet binCounter;

		/**
		 * @param supracontext
		 *            integer representing a label for a supracontext
		 * @param card
		 *            number of bits needed to represent the vector
		 * @return Iterator over all subsets of the given label
		 */
		public SubsetIterator() {
			BitSet supraContext = BitSetLabel.this.label;
			card = BitSetLabel.this.getCardinality();
			current = supraContext;
			gaps = new ArrayList<>();

			// iterate over the clear bits and record their locations
			for (int i = label.nextClearBit(0); i < card; i = label
					.nextClearBit(i + 1))
				gaps.add(i);

			// if there were no gaps, then there is nothing to iterate over
			if (gaps.isEmpty()) {
				hasNext = false;
				return;
			}

			// binCounter needs a trailing 1 for each gap
			binCounter = new BitSet();
			for (int i = 0; i < gaps.size(); i++)
				binCounter.set(i);
			hasNext = true;
		}

		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public Label next() {
			// we use binCounter like a binary integer in order to permute
			// all combinations of 1's and 0's for the gaps
			// choose gap bit to flip; it's whichever is the rightmost
			// 1 in binCounter.
			int rightMost = binCounter.nextSetBit(0);
			binCounter.clear(rightMost);
			// then subtract 1 from rightMost (do the binary arithmetic by hand
			// here)
			if (rightMost != 0)
				for (int i = rightMost - 1; i >= 0; i--)
					binCounter.set(i);

			current.flip(gaps.get(rightMost));
			// we are done permuting when binCounter hits all zeros
			if (binCounter.isEmpty())
				hasNext = false;
			return new BitSetLabel(current, card);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
