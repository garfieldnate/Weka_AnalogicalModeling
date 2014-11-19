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
		assertEquals(1, mask.getLength());
		
		mask = new LabelMask(3, 4);
		assertEquals(2, mask.getLength());
		
		mask = new LabelMask(0, 10);
		assertEquals(11, mask.getLength());
	}

	@Test
	public void testMask() {
		int label = 0b1101101;
		LabelMask mask = new LabelMask(0, 0);
		assertEquals(0b1, mask.mask(label));
		
		mask = new LabelMask(2, 5);
		assertEquals(0b1011, mask.mask(label));
	}

}
