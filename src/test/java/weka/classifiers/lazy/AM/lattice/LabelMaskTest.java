package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LabelMaskTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void constructorStartIsNegative(){
	    exception.expect(IllegalArgumentException.class);
	    exception.expectMessage("start should be non-negative");
		new LabelMask(-1, 3);
	}
	
	@Test
	public void constructorEndLessThanStart(){
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

}
