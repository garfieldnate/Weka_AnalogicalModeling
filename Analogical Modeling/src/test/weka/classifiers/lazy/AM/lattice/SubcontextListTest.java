package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

import weka.classifiers.lazy.AM.AMconstants;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SubcontextListTest {

	private double DELTA = 1e-10;

	@Test
	public void testChapter3Data() throws Exception {
		DataSource source = new DataSource("data/ch3example.arff");
		Instances train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);

		source = new DataSource("data/ch3exampleTest.arff");
		Instances test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				test.firstInstance(), false);

		SubcontextList subList = new SubcontextList(labeler, train);

		assertEquals(subList.getCardinality(), 3);

		Iterator<Subcontext> si = subList.iterator();
		Subcontext actual = si.next();
		Subcontext expected = new Subcontext(new Label(0b001, 3));
		expected.add(train.get(0));// 310e
		expected.add(train.get(4));// 311r
		assertEquals(actual, expected);
		assertEquals(actual.getOutcome(), AMconstants.NONDETERMINISTIC, DELTA);

		actual = si.next();
		expected = new Subcontext(new Label(0b100, 3));
		expected.add(train.get(3));// 212r
		assertEquals(actual, expected);

		actual = si.next();
		expected = new Subcontext(new Label(0b101, 3));
		expected.add(train.get(1));// 210r
		assertEquals(actual, expected);

		actual = si.next();
		expected = new Subcontext(new Label(0b110, 3));
		expected.add(train.get(2));// 032r
		assertEquals(actual, expected);

		assertFalse(si.hasNext());

		assertEquals(
				"(001|"
						+ AMconstants.NONDETERMINISTIC_STRING
						+ "|3,1,0,e/3,1,1,r),(100|r|2,1,2,r),(101|r|2,1,0,r),(110|r|0,3,2,r),",
				subList.toString());
	}

}
