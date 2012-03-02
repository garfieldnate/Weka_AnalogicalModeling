package edu.byu.am.test;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import edu.byu.am.lattice.SubsetIterator;

public class TestSubsetIterator {

	@Test
	public void testIterator() {
		Set<Integer> cmp = new TreeSet<Integer>();
		// iterating over 1001011
		cmp.add(107);// 1101011
		cmp.add(123);// 1111011
		cmp.add(91);// 1011011
		cmp.add(95);// 1011111
		cmp.add(111);// 1111111
		cmp.add(127);// 1101111
		cmp.add(79);// 1001111
		SubsetIterator si = new SubsetIterator(75, 7);
		Set<Integer> ints = new TreeSet<Integer>();
		while (si.hasNext())
			ints.add(si.next());
		assertEquals(cmp, ints);

	}

}
