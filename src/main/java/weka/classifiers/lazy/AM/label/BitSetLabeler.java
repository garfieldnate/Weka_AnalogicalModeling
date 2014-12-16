package weka.classifiers.lazy.AM.label;

import java.util.BitSet;

import weka.core.Attribute;
import weka.core.Instance;

public class BitSetLabeler extends Labeler {
	
	public BitSetLabeler(MissingDataCompare mdc, Instance test,
			boolean ignoreUnknowns) {
		super(mdc, test, ignoreUnknowns);
	}

	private Partition[] partitions;

	@Override
	public Label label(Instance data) {
		if (!data.equalHeaders(getTestInstance()))
			throw new IllegalArgumentException(
					"Input instance is not compatible with the test instance");
		BitSet label = new BitSet();
		int length = getCardinality();
		Attribute att;
		int index = 0;
		for (int i = 0; i < getTestInstance().numAttributes(); i++) {
			// skip ignored attributes and the class attribute
			if (isIgnored(i))
				continue;
			if (i == getTestInstance().classIndex())
				continue;
			att = getTestInstance().attribute(i);
			// use mdc if were are comparing a missing attribute
			if (getTestInstance().isMissing(att) || data.isMissing(att)) {
				if (!getMissingDataCompare().matches(getTestInstance(), data, att))
					// use length-1-index instead of index so that in binary the
					// labels show left to right, first to last feature.
					label.set(length - 1 - index);
			} else if (getTestInstance().value(att) != data.value(att)) {
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
		// technically this is not currently required since we only access the
		// label via the Label interface, but it might be useful in the future.
		if (!(label instanceof BitSetLabel))
			throw new IllegalArgumentException("This labeler can only handle "
					+ BitSetLabel.class.getCanonicalName()
					+ "s; input label was an instance of "
					+ label.getClass().getCanonicalName());

		// create and cache the masks if they have not be created yet
		Partition[] partitions = getPartitions();

		// TODO: would it be possible/worth while to create an IntLabel if
		// the cardinality were small enough?
		return partitions[partitionIndex].extract((BitSetLabel) label) ;
	}

	/**
	 * @return The partition objects used to partition labels from this labeler.
	 */
	private Partition[] getPartitions() {
		//partitions are cached
		if (partitions == null) {
			partitions = new Partition[numPartitions()];
			Span[] spans = partitions();
			for (int i = 0; i < numPartitions(); i++) {
				partitions[i] = new Partition(spans[i]);
			}
		}
		return partitions;
	}

	/**
	 * Private class for storing label paritions
	 */
	private class Partition {
		private int startIndex;
		private int cardinality;

		public Partition(Span s) {
			startIndex = s.getStart();
			cardinality = s.getCardinality();
		}

		public BitSetLabel extract(BitSetLabel label){
			BitSet newLabel = new BitSet(cardinality);
			// loop through the bits and set the unmatched ones
			for (int i = startIndex; i < startIndex + cardinality; i++) {
				if (!label.matches(i))
					newLabel.set(i);
			}
			return new BitSetLabel(newLabel, cardinality);
		}
		
		@Override
		public String toString(){
			return startIndex + "," + cardinality;
		}
	}
}
