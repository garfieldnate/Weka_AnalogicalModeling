package weka.classifiers.lazy.AM.label;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.Label;

public class IntLabelTest {

	@Test
	public void testAccessors(){
		IntLabel label = new IntLabel(0b0011, 4);
		assertEquals(label.getCardinality(), 4);
		assertEquals(label.intLabel(), 0b0011);
	}
	
	@Test
	public void testIterator() {
		IntLabel label = new IntLabel(0b100, 3);
		Iterator<Label> si = label.descendantIterator();
		assertEquals(si.next(), new IntLabel(0b101, 3));
		assertEquals(si.next(), new IntLabel(0b111, 3));
		assertEquals(si.next(), new IntLabel(0b110, 3));
		assertFalse(si.hasNext());
	}

}
