package edu.byu.am.lattice;

import java.util.BitSet;
import java.util.Iterator;

import edu.byu.am.data.Exemplar;

/**
 * Class for iterating over all of the binary labels of a supracontext's subsets.
 * @author Nate Glenn
 *
 */
public class SubsetIterator implements Iterator {
	
	//each will be all zeros except where one of the zeros in the tested item is.
	private BitSet[] gaps;
	
	//number of gaps
	private int numGaps;
	
	
	/**
	 * @param supracontext BitSet representing a label for a supracontext 
	 * @return Iterator over all subsets of the given label
	 */
	public SubsetIterator(BitSet supracontext){
		int card = supracontext.cardinality();
		gaps = new BitSet[card];
		//iterate over the clear bits and create a list of gaps
		int gapIndex = 0;
		for (int i = supracontext.nextClearBit(0); i >= 0; i = supracontext.nextClearBit(i+1)) {
			//create a BitSet with only bit i set to 1 and the rest 0
		     gaps[gapIndex] = new BitSet(card);
		     gaps[gapIndex].set(i);
		     gapIndex++;
		 }
		
		//choose gap to choose bit to flip according to towers of Hanoi problem
		//treat gaps array like a Gray Code
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
}
