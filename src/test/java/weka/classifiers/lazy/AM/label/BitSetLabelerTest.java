package weka.classifiers.lazy.AM.label;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;

import org.junit.Test;

import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instances;

public class BitSetLabelerTest {

	/**
	 * Test that BitSetLabeler can work with data larger than 32 bits.
	 * @throws Exception
	 */
	@Test
	public void testLabelLargeInstance() throws Exception {
		Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
		BitSetLabeler labeler = new BitSetLabeler(MissingDataCompare.VARIABLE, data.get(0), false);
		Label label = labeler.label(data.get(1));
		BitSet bits = new BitSet();
		for(int i : new int[]{15, 25, 26, 27, 28, 29, 34})
			bits.set(i);
		assertEquals(label, new BitSetLabel(bits, 35));
	}

}
