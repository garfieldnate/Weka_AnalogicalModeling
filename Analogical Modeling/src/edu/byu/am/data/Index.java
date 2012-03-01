/*
 * 	Analogical Modeling Java module
 *  Copyright (C) 2011  Nathan Glenn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.byu.am.data;

import java.util.HashMap;
import java.util.List;

/**
 * This class implements a double index for feature names; comparing String objects is relatively
 * slow, so the strings in feature vectors are converted to integers. This class stores which
 * strings are represented by what integers and vice versa.
 * @author nate
 *
 */
public class Index {


	/**
	 * NONDETERMINISTIC will be mapped to "&nondeterministic&", and is used by 
	 * {@link edu.byu.am.lattice.Supracontext Supracontext} (this is '*' in the red book paper).
	 * 
	 */
	public static final int NONDETERMINISTIC = 0;

	/**
	 * EMPTY will be mapped to "&empty&", and is used by 
	 * {@link edu.byu.am.lattice.Supracontext Supracontext} (this is *supralist in the red book paper).
	 * 
	 */
	public static final int EMPTY = -1;
	
	private static HashMap<String,Integer> featToInt = new HashMap<String,Integer>();
	private static HashMap<Integer,String> intToFeat = new HashMap<Integer,String>();
	public static int counter = Integer.MIN_VALUE;
	
	static{
		featToInt.put("&nondeterministic&", NONDETERMINISTIC);
		intToFeat.put(NONDETERMINISTIC,"&nondeterministic&");

		featToInt.put("&empty&", EMPTY);
		intToFeat.put(EMPTY,"&empty&");
	}
	
	/**
	 * @return Integer representing feature indicated by feat
	 */
	public static Integer getInt(String feat){
		return featToInt.get(feat);
	}
	
	/**
	 * @return string for feature represented by i
	 */
	public static String getString(int i){
		return intToFeat.get(i);
	}
	
	/**
	 * Clears the indices and resets the counter which assigns ints to strings
	 */
	public static void reset(){
		featToInt.clear();
		intToFeat.clear();

		featToInt.put("&nondeterministic&", NONDETERMINISTIC);
		intToFeat.put(NONDETERMINISTIC,"&nondeterministic&");

		featToInt.put("&empty&", EMPTY);
		intToFeat.put(EMPTY,"&empty&");
		
		counter = Integer.MIN_VALUE;
	}
	
	/**
	 * Indexes a new feature value.
	 * @param newFeat String representing new feature value that should be indexed
	 * @return int new representation of the string feature
	 */
	public static int insert(String newFeat){
		//do nothing if the indeces already contain the value
		if(featToInt.containsKey(newFeat))
			return featToInt.get(newFeat);
		featToInt.put(newFeat, ++counter);
		intToFeat.put(counter, newFeat);
		return counter;
	}
	
	/**
	 * Inserts each of the features into the index, if they are not already indexed, and returns
	 * an integer array representing the features.
	 * @param features
	 * @return
	 */
	static public int[] insert(List<String> features){
		int length = features.size();
		int[] ints = new int[length];
		
		//iterate through features, convert each string to an int and add it to the return array
		int count = 0;
		for(String s : features){
			ints[count++] = insert(s);
		}
		return ints;
	}
}
