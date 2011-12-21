package edu.byu.am.lattice;

import java.util.BitSet;

import edu.byu.am.data.Exemplar;

/**
 * Class for general computation involving context.
 * @author nate
 *
 */
public class Context {
	
	/**
	 * Returns a context label for dataItem, given we are testing against testItem
	 * @param dataItem
	 * @param testItem
	 * @return a binary context label, e.g. 1001011, where 1 indicates that features match,
	 * and 0 indicates that features do not match.
	 */
	public static BitSet getLabel( Exemplar dataItem, Exemplar testItem){
		//all bits start as false
		BitSet label = new BitSet(dataItem.size());
		int[] dataArray = dataItem.getFeatures();
		int[] testArray = testItem.getFeatures();
		int length = dataArray.length;
		
		//set matching bits to true
		for(int i = 0; i < length; i++)
			if(dataArray[i] == testArray[i])
				label.set(i);
		return label;
	}
	
	/**
	 * 
	 * @param superset
	 * @param subset
	 * @return True if superset is a superset of subset; that is, if superset & subset == superset.
	 */
	public static boolean isSupersetOf(BitSet superset, BitSet subset){
		BitSet temp = superset;
		temp.and(subset);
		return temp.equals(superset);
	}
}
