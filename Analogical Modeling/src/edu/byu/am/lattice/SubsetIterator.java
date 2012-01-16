package edu.byu.am.lattice;

import java.util.Iterator;

/**
 * Class for iterating over all of the binary labels of a supracontext's subsets.
 * @author Nate Glenn
 *
 */
public class SubsetIterator implements Iterator<Integer> {
	
	//each will be all zeros except where one of the zeros in the tested item is.
	private int[] gaps;
	
	//number of gaps
	
	private boolean hasNext = true;
	
	int current;
	int binCounter;
	
	
	/**
	 * @param supracontext integer representing a label for a supracontext 
	 * @param size number of bits needed to represent the vector
	 * @return Iterator over all subsets of the given label
	 */
	public SubsetIterator(int supracontext,int size){
		current = supracontext;
		int card = size;
		gaps = new int[card];
		
		//iterate over the clear bits and create a list of gaps;
		//each gap in the list is an int with all 0s except in the index where the gap was found
		//in the supracontext
		int gapIndex = 0;
		for (int i = card-1; i > 0; i--) {
			if(((1 << i) & supracontext) == 0){
				//create an int with only bit i set to 1
				gaps[gapIndex] = 1 << i;
				gapIndex++;
			}
		 }
		//if there were no gaps, then there is nothing to iterate over
		if(gapIndex == 0) {
			hasNext = false;
			return;
		}
		//binCounter needs to be all ones for the last n bits, where n is numGaps;
		binCounter = 0;
		for(int i = 0; i < gapIndex; i++)
			binCounter |= 1 << i;
		hasNext = true;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public Integer next() {
		//choose gap to choose bit to flip; it's whichever is the rightmost 1 in binCounter
		// find the rightmost 1 in t; from HAKMEM, I believe 
	  	int i, tt;
		for (i = 0, tt = ~binCounter & (binCounter - 1); tt > 0; tt >>= 1, ++i);
//		System.out.println("Using " + Integer.toBinaryString(binCounter) + ", Rightmost 1 is " + i +
//				"; gaps[" + i + "] is " + Integer.toBinaryString(gaps[i]));
	  	current ^= gaps[i];
	  	binCounter--;
	  	if(binCounter == 0)
	  		hasNext = false;
//	  	System.out.println("printing " + current);
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public static void main(String[] args){
		System.out.println("Iterating over: " + Integer.toBinaryString(75));
		SubsetIterator si = new SubsetIterator(75, 7);
		while(si.hasNext()){
			System.out.println(Integer.toBinaryString(si.next()));
		}
	}
}
