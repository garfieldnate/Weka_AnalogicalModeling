package weka.classifiers.lazy.AM.lattice.distributed;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.Label;
import weka.classifiers.lazy.AM.lattice.Labeler;
import weka.classifiers.lazy.AM.lattice.MissingDataCompare;
import weka.classifiers.lazy.AM.lattice.distributed.LabelMask;
import weka.core.Instances;

public class LabelMaskTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	private static Instances dataset;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		dataset = TestUtils.sixCardinalityTestSet();
	}

	@Test
	public void constructorStartIsNegative() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("start should be non-negative");
		new LabelMask(-1, 3);
	}

	@Test
	public void constructorEndLessThanStart() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("end should be greater than or equal to start");
		new LabelMask(4, 3);
	}

	@Test
	public void testGetLength() {
		LabelMask mask = new LabelMask(3, 3);
		assertEquals(1, mask.getCardinality());

		mask = new LabelMask(3, 4);
		assertEquals(2, mask.getCardinality());

		mask = new LabelMask(0, 10);
		assertEquals(11, mask.getCardinality());
	}

	@Test
	public void testMask() {
		Label label = new Label(0b1101101, 7);
		LabelMask mask = new LabelMask(0, 0);
		assertEquals(mask.mask(label), new Label(0b1, 1));

		mask = new LabelMask(2, 5);
		assertEquals(mask.mask(label), new Label(0b1011, 4));
	}

	@Test
	public void testGetMasks() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, dataset.get(0),
				false);
		LabelMask[] masks = LabelMask.getMasks(2, labeler.getCardinality());
		assertEquals(2, masks.length);
		assertEquals(new LabelMask(0, 2), masks[0]);
		assertEquals(new LabelMask(3, 4), masks[1]);

		masks = LabelMask.getMasks(8, labeler.getCardinality());
		assertEquals("Number of masks does not exceed cardinality",
				masks.length, 5);
	}

	@Test
	public void testGetMasksWithIgnoreUnknowns() {
		Labeler labeler = new Labeler(MissingDataCompare.MATCH, dataset.get(6),
				true);
		LabelMask[] masks = LabelMask.getMasks(2, labeler.getCardinality());
		assertEquals(2, masks.length);
		assertEquals(new LabelMask(0, 1), masks[0]);
		assertEquals(new LabelMask(2, 3), masks[1]);
	}

}
