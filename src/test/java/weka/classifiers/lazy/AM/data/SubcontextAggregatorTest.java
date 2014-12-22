package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;

public class SubcontextAggregatorTest {

	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		Instance test = train.get(0);
		train.remove(0);

		Labeler labeler = new IntLabeler(MissingDataCompare.MATCH, test, false);

		SubcontextAggregator subs = new SubcontextAggregator(labeler, train);
		subs.finish();
		assertEquals(subs.getCardinality(), 3);

		List<Subcontext> subList = subs.subcontextList();
		assertEquals(subList.size(), 4);

		Set<Instance> instances = new HashSet<>();
		instances.add(train.get(0));// 310e
		instances.add(train.get(4));// 311r
		Subcontext expected = new Subcontext(new IntLabel(0b001, 3), instances);
		assertTrue("subList contains " + expected,
				subListContains(subList, expected));

		instances.clear();
		instances.add(train.get(3));// 212r
		expected = new Subcontext(new IntLabel(0b100, 3), instances);
		assertTrue("subList contains " + expected,
				subListContains(subList, expected));

		instances.clear();
		instances.add(train.get(1));// 210r
		expected = new Subcontext(new IntLabel(0b101, 3), instances);
		assertTrue("subList contains " + expected,
				subListContains(subList, expected));

		instances.clear();
		instances.add(train.get(2));// 032r
		expected = new Subcontext(new IntLabel(0b110, 3), instances);
		assertTrue("subList contains " + expected,
				subListContains(subList, expected));
	}

	private boolean subListContains(List<Subcontext> subList,
			Subcontext expected) {
		boolean found = false;
		for (Subcontext sub : subList)
			if (sub.deepEquals(expected)) {
				found = true;
				break;
			}
		return found;
	}

	@Test
	public void testAccessors() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		Instance test = train.get(0);
		train.remove(0);

		Labeler labeler = new IntLabeler(MissingDataCompare.MATCH, test, false);

		SubcontextAggregator subs = new SubcontextAggregator(labeler, train);
		assertEquals("getLabeler returns the labeler used in the constructor",
				subs.getLabeler(), labeler);
		assertEquals("getCardinality returns the cardinality of the test item",
				subs.getCardinality(), 3);
	}

}
