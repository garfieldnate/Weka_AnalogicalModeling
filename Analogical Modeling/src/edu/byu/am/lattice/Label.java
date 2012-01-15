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
	
	public void set(Long other) {
		if(!big)
			smallStorage = other;
		else
			setBitSet(other);
	}
	
	private void setBitSet(Long other) {
		bigStorage = new BitSet();
	}

	public static void main(String[] args) {
		System.out.println(Long.SIZE);
	}
}
