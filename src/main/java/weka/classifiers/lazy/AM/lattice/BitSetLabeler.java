package weka.classifiers.lazy.AM.lattice;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instance;

public class BitSetLabeler extends Labeler {
	private final Set<Integer> ignoreSet;
	private int classIndex;
	private Partition[] masks;

	/**
	 * 
	 * @param mdc
	 *            Specifies how to compare missing attributes
	 * @param instance
	 *            Instance being classified
	 * @param ignroeUnknowns
	 *            true if attributes with undefined values in the test item
	 *            should be ignored; false if not.
	 */
	public BitSetLabeler(MissingDataCompare mdc, Instance test,
			boolean ignoreUnknowns) {
		this.mdc = mdc;
		this.testInstance = test;
		this.ignoreUnknowns = ignoreUnknowns;
		ignoreSet = new HashSet<>();
		classIndex = test.classIndex();
		if (ignoreUnknowns) {
			int length = testInstance.numAttributes() - 1;
			for (int i = 0; i < length; i++) {
				if (testInstance.isMissing(i))
					ignoreSet.add(i);
			}
		}
	}

	@Override
	public int getCardinality() {
		return testInstance.numAttributes() - ignoreSet.size() - 1;
	}

	@Override
	public Label label(Instance data) {
		if (!data.equalHeaders(testInstance))
			throw new IllegalArgumentException(
					"Input instance is not compatible with the test instance");
		BitSet label = new BitSet();
		int length = getCardinality();
		Attribute att;
		int index = 0;
		for (int i = 0; i < testInstance.numAttributes(); i++) {
			// skip ignored attributes and the class attribute
			if (ignoreSet.contains(i))
				continue;
			if (i == classIndex)
				continue;
			att = testInstance.attribute(i);
			// use mdc if were are comparing a missing attribute
			if (testInstance.isMissing(att) || data.isMissing(att)) {
				if (!mdc.matches(testInstance, data, att))
					// use length-1-index instead of index so that in binary the
					// labels show left to right, first to last feature.
					label.set(length - 1 - index);
			} else if (testInstance.value(att) != data.value(att)) {
				// same as above
				label.set(length - 1 - index);
			}
			index++;
		}
		return new BitSetLabel(label, getCardinality());
	}

	@Override
	public Label partition(Label label, int partitionIndex) {
		if (partitionIndex > numPartitions() || partitionIndex < 0)
			throw new IllegalArgumentException("Illegal partition index: "
					+ partitionIndex);
		if (label.getCardinality() != getCardinality())
			throw new IllegalArgumentException("Label cardinality is "
					+ label.getCardinality() + " but labeler cardinality is "
					+ getCardinality());
		if (!(label instanceof BitSetLabel))
			throw new IllegalArgumentException("This labeler can only handle "
					+ BitSetLabel.class.getCanonicalName()
					+ "s; input label was an instance of "
					+ label.getClass().getCanonicalName());

		// create and cache the masks if they have not be created yet
		if (masks == null) {
			masks = new Partition[numPartitions()];
			Span[] spans = partitions();
			for (int i = 0; i < numPartitions(); i++) {
				masks[i] = new Partition(spans[i].getStart(), spans[i].getEnd()
						- spans[i].getStart());
			}
		}

		// loop through the bits and set the unmatched ones
		Partition mask = masks[partitionIndex];
		BitSet newLabel = new BitSet(mask.getCardinality());
		int index = mask.getStartIndex();
		for (int i = 0; i < mask.getCardinality(); i++) {
			if (!label.matches(index))
				newLabel.set(i);
			index++;
		}
		// TODO: would it be possible/worth while to create an IntLabel if
		// the cardinality were small enough?
		return new BitSetLabel(newLabel, mask.getCardinality());
	}

	/**
	 * Private class for storing label paritions
	 */
	private class Partition {
		private int startIndex;
		private int cardinality;

		public Partition(int i, int c) {
			startIndex = i;
			cardinality = c;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getCardinality() {
			return cardinality;
		}
	}
}
