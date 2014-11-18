package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MissingDataCompareTest {

	@Test
	public void testMatch() {
		MissingDataCompare mc = MissingDataCompare.MATCH;
		assertTrue(mc.outcome(1, 1) == 0);
		assertTrue(mc.outcome(1, 0) == 0);
		assertTrue(mc.outcome(0, 1) == 0);
		assertTrue(mc.outcome(0, 0) == 0);
	}

	@Test
	public void testMismatch() {
		MissingDataCompare mc = MissingDataCompare.MISMATCH;
		assertTrue(mc.outcome(1, 1) == 1);
		assertTrue(mc.outcome(1, 0) == 1);
		assertTrue(mc.outcome(0, 1) == 1);
		assertTrue(mc.outcome(0, 0) == 1);
	}

	@Test
	public void testVariable() {
		MissingDataCompare mc = MissingDataCompare.VARIABLE;
		assertTrue(mc.outcome(1, 1) == 0);
		assertTrue(mc.outcome(1, 0) == 1);
		assertTrue(mc.outcome(0, 1) == 1);
		assertTrue(mc.outcome(0, 0) == 0);
	}

}
