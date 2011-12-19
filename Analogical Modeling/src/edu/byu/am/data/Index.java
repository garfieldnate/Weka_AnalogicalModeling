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

/**
 * This class implements a double index for feature names; comparing String objects is relatively
 * slow, so the strings in feature vectors are converted to integers. This class stores which
 * strings are represented by what integers and vice versa.
 * @author nate
 *
 */
public class Index {

	static HashMap<String,Integer> featToInt = new HashMap<String,Integer>();
	static HashMap<Integer,String> intToFeat = new HashMap<Integer,String>();
	
	static Integer getInt(String feat){
		return featToInt.get(feat);
	}
	
	static String getString(int i){
		return intToFeat.get(i);
	}
	
	/**
	 * Indexes a new feature value.
	 * @param newFeat String representing new feature value that should be indexed
	 */
	static public void insert(String newFeat){
		//do nothing if the indeces already contain the value
		if(featToInt.containsKey(newFeat))
			return;
	}
	
	/**
	 * Inserts each of the features into the index, if they are not already indexed, and returns
	 * an integer array representing the features.
	 * @param features
	 * @return
	 */
	static public int[] insert(String[] features){
		
		return null;
	}
}
