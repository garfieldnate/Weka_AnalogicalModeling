package weka.classifiers.lazy.AM.data;

import org.junit.Test;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SubcontextListTest {

    @Test
    public void testChapter3Data() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.get(0);
        train.remove(0);

        Labeler labeler = new IntLabeler(test, false, MissingDataCompare.MATCH);

        SubcontextList subs = new SubcontextList(labeler, train, false);
        assertEquals(subs.getCardinality(), 3);

        List<Subcontext> subList = getSubList(subs);
        assertEquals(subList.size(), 4);

        Subcontext expected = new Subcontext(new IntLabel(0b001, 3), "foo");
        expected.add(train.get(0));// 310e
        expected.add(train.get(4));// 311r
        assertTrue(subList.contains(expected));

        expected = new Subcontext(new IntLabel(0b100, 3), "foo");
        expected.add(train.get(3));// 212r
        assertTrue(subList.contains(expected));

        expected = new Subcontext(new IntLabel(0b101, 3), "foo");
        expected.add(train.get(1));// 210r
        assertTrue(subList.contains(expected));

        expected = new Subcontext(new IntLabel(0b110, 3), "foo");
        expected.add(train.get(2));// 032r
        assertTrue(subList.contains(expected));
    }

    @Test
    public void testIgnoreFullMatches() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.get(0);

        Labeler labeler = new IntLabeler(test, false, MissingDataCompare.MATCH);
        Subcontext allMatchingSub = new Subcontext(new IntLabel(0b000, 3), "foo");
        allMatchingSub.add(train.get(0));// 310e

        SubcontextList subs = new SubcontextList(labeler, train, false);
        assertTrue("Should contain 000 sub when not ignoring full matches", getSubList(subs).contains(allMatchingSub));

        subs = new SubcontextList(labeler, train, true);
        assertFalse("Should not contain 000 sub when ignoring full matches", getSubList(subs).contains(allMatchingSub));
    }

    private List<Subcontext> getSubList(final SubcontextList subcontextList) {
		return new ArrayList<>() {
			{
				for (Subcontext s : subcontextList)
					add(s);
			}
		};
    }

    @Test
    public void testAccessors() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        Instance test = train.get(0);
        train.remove(0);

        Labeler labeler = new IntLabeler(test, false, MissingDataCompare.MATCH);

        SubcontextList subs = new SubcontextList(labeler, train, false);
        assertEquals("getLabeler returns the labeler used in the constructor", subs.getLabeler(), labeler);
        assertEquals("getCardinality returns the cardinality of the test item", subs.getCardinality(), 3);
    }

}
