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


/**
 * This class is used to represent one item, or feature vector.
 * @author nate
 *
 */
public class Item {
	
	/**
	 * 
	 * @param features String array representing feature vector, including
	 */
	public Item(String[] features){
		
	}

//	private String[] features;
	private int[] features;
	
	private String outcome;
	private int intOutcome;
	
	/**
	 * 
	 * @param i Data item 
	 * @return
	 */
//	public Label getLabel(Item i){
//		return null;
//	}
	
	/**
	 * 
	 * @return Array of strings 
	 */
	public int[] getFeatures(){
		return features;
	}
}
