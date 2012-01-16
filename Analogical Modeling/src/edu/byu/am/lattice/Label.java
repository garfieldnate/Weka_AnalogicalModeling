package edu.byu.am.lattice;

import java.util.BitSet;

/**
 * Implements a binary label for labeling contexts. If the size of the label is
 * {@link Long#SIZE} or less, then a long will be used as the label. Otherwise, an instance of
 * {@link BitSet} will be used instead. Usually longs are 64 bits, so they will be used most of the
 * time.
 * @author nate
 *
 */
public class Label {
	
	//need functions for gray code iteration, set bit
	private BitSet bigStorage;
	private long smallStorage;
	private int cardinality;
	
	/**
	 * True if size of label is greater than {@link Long#SIZE};
	 * false otherwise.
	 */
	private boolean big;
	
	public Label(int size) {
		if(size > Long.SIZE) {
			big = true;
			bigStorage = new BitSet(size);
		}
		else {
			big = false;
			smallStorage  = 0;
		}
	}
	
	/**
	 * 
	 * @param startIndex
	 * @return
	 */
	public int nextClearBit(int startIndex){
		if(!big)
			return smallNextClearBit(startIndex);
			else
				return bigStorage.nextClearBit(startIndex);
		}
	private int smallNextClearBit(int startIndex) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int rightMostClearBit(){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @return Size of the label in bits
	 */
	public int cardinality(){
		return cardinality;
	}
	
	/**
	 * Sets bits
	 * @param other Long to set current bits to
	 */
	public void set(Long other) {
		if(!big)
			smallStorage = other;
		else
			setBitSet(other);
	}
	
	/**
	 * 
	 * @param index to set to true in the label.
	 */
	public void set(int index){
		if(!big)
			smallStorage |= (1 << index);
		else
			bigStorage.set(index);
	}
	
	/**
	 * Set all of the set bits of other to true in bigStorage
	 * @param other
	 */
	private void setBitSet(Long other) {
		bigStorage = new BitSet();
		for(int i = 0; i < Long.SIZE; i++){
			if(((1 << i) & other) != 0)
				bigStorage.set(i);
		}
	}

	public static void main(String[] args) {
		System.out.println(Long.SIZE);
	}
}
