package weka.classifiers.lazy.AM.label;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.TestUtils;
import weka.core.Instances;

/**
 * Test aspects of {@link Intlabeler} that are not applicable to other
 * {@link Labeler} implementations.
 * 
 * @author Nathan Glenn
 * 
 */
public class IntLabelerTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testConstructorCardinalityTooHigh() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception
				.expectMessage("max cardinality for this labeler is 32; input was 35");
		Instances data = TestUtils.getDataSet(TestUtils.SOYBEAN);
		new IntLabeler(MissingDataCompare.MATCH, data.get(0), false);
	}

}
