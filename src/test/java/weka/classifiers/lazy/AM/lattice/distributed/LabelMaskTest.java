package weka.classifiers.lazy.AM.lattice.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.lattice.Label;

public class LabelMaskTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testConstructorStartIsNegative() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("start should be non-negative");
		new LabelMask(-1, 3);
	}

	@Test
	public void testConstructorEndLessThanStart() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("card should be greater than or equal to one");
		new LabelMask(4, 0);
	}

	@Test
	public void testGetLength() {
		LabelMask mask = new LabelMask(3, 3);
		assertEquals(3, mask.getCardinality());
	}

	@Test
	public void testMask() {
		Label label = new Label(0b1101101, 7);
		LabelMask mask = new LabelMask(0, 1);
		assertEquals(mask.mask(label), new Label(0b1, 1));

		mask = new LabelMask(2, 4);
		assertEquals(mask.mask(label), new Label(0b1011, 4));
	}

	@Test
	public void testGetMasks() {
		LabelMask[] masks = LabelMask.getMasks(5, 2);
		assertEquals(2, masks.length);
		assertEquals(new LabelMask(0, 3), masks[0]);
		assertEquals(new LabelMask(3, 2), masks[1]);
		
		masks = LabelMask.getMasks(9, 4);
		assertNotNull(masks[3]);

		masks = LabelMask.getMasks(5, 8);
		assertEquals("Number of masks does not exceed cardinality",
				masks.length, 5);
	}
}
