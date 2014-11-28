package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

public class LabelTest {

	@Test
	public void testAccessors(){
		Label label = new Label(0b0011, 4);
		assertEquals(label.getCard(), 4);
		assertEquals(label.intLabel(), 0b0011);
	}
	
	@Test
	public void testIterator() {
		Label label = new Label(0b100, 3);
		Iterator<Label> si = label.subsetIterator();
		assertEquals(si.next(), new Label(0b101, 3));
		assertEquals(si.next(), new Label(0b111, 3));
		assertEquals(si.next(), new Label(0b110, 3));
		assertFalse(si.hasNext());
	}

}
