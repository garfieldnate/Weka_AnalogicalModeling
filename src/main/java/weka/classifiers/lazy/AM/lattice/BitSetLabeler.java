package weka.classifiers.lazy.AM.lattice;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instance;

public class BitSetLabeler extends Labeler {
	private final Set<Integer> ignoreSet;
	private int classIndex;

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
}
