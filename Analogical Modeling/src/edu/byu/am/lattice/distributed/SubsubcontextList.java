package edu.byu.am.lattice.distributed;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.byu.am.data.Exemplar;
import edu.byu.am.data.Index;
import edu.byu.am.lattice.MissingDataCompare;
import edu.byu.am.lattice.Subcontext;
import edu.byu.am.lattice.SubcontextList;

/**
 * This class accomplishes the same thing as {@link Subcontextlist}, but only using a portion of
 * each exemplar to assign it to a subcontext. To find the predicted outcomes, use this class with
 * {@link Sublattice} and {@link CompoundLattice}.
 * @author Nate Glenn
 *
 */
public final class SubsubcontextList implements Iterable<SubcontextList>{

	/**
	 * Defines how missing data will be treated. TODO:move to Options
	 */
	MissingDataCompare mdc = MissingDataCompare.MATCH;
	
	/**
	 * Number of exemplars in the lists
	 */
	private int size = 0;
	/**
	 * The number of sublattices to use.
	 */
	private final int SPLIT_NUM = 4;
	
	/**
	 * Caches the integer masks used to make the labels in the 
	 * sublattices. Is also used to remember the number of sublattices
	 * being used.
	 */
	int[] masks;
	
	/**
	 * Exemplar which is being classified and assigns contexts
	 */
	Exemplar test;
	
	/**
	 * size of the feature vectors
	 */
	int cardinality;
	
	List<SubcontextList> subcontextLists = new LinkedList<SubcontextList>();

	private int splitPoints[];
	
	/**
	 * 
	 * @param testEx
	 *            Exemplar which is being classified and assigns contexts
	 * @param data
	 *            to add to subcontexts
	 */
	public SubsubcontextList(Exemplar testEx, List<Exemplar> data){
		test = testEx;
		cardinality = test.size();
		createMasks();
		for(int i = 0; i <= masks.length; i++){
			subcontextLists.add(new SubcontextList(test));
		}
		System.out.println("Sublattices: " + subcontextLists.size());
		for (Exemplar e : data)
			add(e);
	}
	
	/**
	 * Adds the exemplar to the correct subcontext
	 * 
	 * @param data
	 */
	public void add(Exemplar data) {
		if(data.size() != cardinality)
			throw new IllegalArgumentException("Exemplar size differs from that of the test" +
					" exemplar. Should be " + cardinality + " but is " + data.size());
		if(subcontextLists.size() == 1){
			subcontextLists.get(0).add(data);
			return;
		}
		int label = getContextLabel(data);
		//fill each sublattice using a portion of the label,
		//as determined by masks
		//splitPoints is guarunteed to have at least 1 entry at this point
		Iterator<SubcontextList> iter = subcontextLists.iterator();
		for(int i = 0; i < masks.length; i++){
			System.out.println("adding " + Integer.toBinaryString(label) + " as " + 
		Integer.toBinaryString((label & masks[i]) >> splitPoints[i]));
			iter.next().add(data, (label & masks[i]) >> splitPoints[i]);//TODO: need to right shift these bits
		}
		size++;
	}
	
	/**
	 * @param data
	 *            Exemplar to be added to a subcontext
	 * @return binary label of length n, where n is the length of the feature
	 *         vectors. If the features of the test exemplar and the data
	 *         exemplar are the same at index i, then the i'th bit will be 1;
	 *         otherwise it will be 0.
	 */
	public int getContextLabel(Exemplar data) {
		int label = 0;
		// System.out.println("Data: " + data + "\nWith: " + test);
		int length = test.getFeatures().length;
		int[] testFeats = test.getFeatures();
		int[] dataFeats = data.getFeatures();
		for (int i = 0; i < length; i++) {
			if (testFeats[i] == Index.MISSING || dataFeats[i] == Index.MISSING)
				label |= (mdc.outcome(testFeats[i], dataFeats[i]));
			else if (testFeats[i] != dataFeats[i]) {
				// use length-1-i instead of i so that it's easier to understand
				// how to match a
				// binary label to an exemplar (using just i produces mirror
				// images; printouts make more sense this way)
				label |= (1 << length - 1 - i);
			}
		}
		 System.out.println("Label: " + Integer.toBinaryString(label));

		return label;
	}
	
	/**
	 * Sets the masks to use in assigning sublattice labels.
	 */
	private void createMasks(){
		setSplitPoints();
		System.out.println("split into: " + Arrays.toString(splitPoints));
		masks = new int[splitPoints.length];
		masks[0] = getMask(0,splitPoints[0]);
		for(int i = 1; i < splitPoints.length; i++)
			masks[i] = getMask(splitPoints[i - 1] + 1,splitPoints[i]);
	}
	
	/**
	 * Sets the boundaries for splitting the exemplars (and then the lattices).
	 * It does it based on the SPLIT_NUM, which defines the number of lattices to use
	 * The length of the array will be SPLIT_NUM-1 unlles the cardinality of the exemplars
	 * is smaller than or equal to SPLIT_NUM, in which case it will be 2.
	 * If splitPoints = {0,3,7,11}, then that means there will be 3 lattices, the first using
	 * features 0-3, and the last using features 7-11, etc. The if the cardinality of the exemplars
	 * does not divide evenly by SPLIT_NUM, then the last lattice will always be the one with less
	 * exemplars.
	 */
	private void setSplitPoints(){
		if(SPLIT_NUM >= cardinality){
			System.out.println("1-1 split");
			splitPoints = new int[] { cardinality - 1 };
//			return splitPoints;
		}
		//need extra space for ending index
		int splitTimes = (int) Math.ceil(cardinality/SPLIT_NUM) + 1;
		splitPoints = new int[splitTimes];
		//the first index is 0 by default
		for(int i = 0, 
				//split here first, accounting for 0th index
				splitPoint = SPLIT_NUM - 1;
				i < splitTimes;
				i++, splitPoint += SPLIT_NUM){
			if(splitPoint > cardinality - 1){
				splitPoints[i] = cardinality - 1;
			}
			else
				splitPoints[i] = splitPoint;
		}
		System.out.println(Arrays.toString(splitPoints));
//		return splitPoints;
	}
	
	/**
	 * @param first First bit to set
	 * @param last Last bit to set
	 * @return an integer with bits first through last set to 1 and the rest
	 * set to zero.
	 */
	private int getMask(int first, int last){
		int mask = 0;
		for(int i = first; i <= last; i++)
			mask |= 1<<i;
		System.out.println("masking from " + first + " to " + last + ": "
			+ Integer.toBinaryString(mask));
		return mask;
	}
	
	/**
	 * @return an iterator which gives each of the {@link Subcontextlist
	 * Subcontext lists}.
	 */
	@Override
	public Iterator<SubcontextList> iterator() {
		return subcontextLists.iterator();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(SubcontextList sl : subcontextLists){
			sb.append(sl.toString());
			sb.append('|');
		}
		return sb.toString();
	}
	
//	public static void main(String[] args){
////		SubsubcontextList sscl = new SubsubcontextList(new Exemplar(), )
//	}

}
