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

import java.util.LinkedList;


/**
 * This class is used to represent one exemplar, or feature vector.
 * @author nate
 *
 */
public class Exemplar {
	
	int label = 0;
	/**
	 * 
	 * @param newFeats String array representing feature vector, including
	 * @param so the outcome for the given vector
	 */
	public Exemplar(LinkedList<String> newFeats, String so){
		stringOutcome = so;
		outcome = Index.insert(so);
		//convert to integer features
		features = Index.insert(newFeats);
		stringFeats = newFeats;
	}

	private LinkedList<String> stringFeats;
	private int[] features;
	
	private String stringOutcome;
	private int outcome;
	
	public String getStringOutcome(){
		return stringOutcome;
	}
	
	public int getOutcome(){
		return outcome;
	}
	
	/**
	 * 
	 * @return Size of the feature vector, not including the outcome
	 */
	public int size(){
		return features.length;
	}
	
	/**
	 * 
	 * @param i Test exemplar
	 * @return binary Supracontextual label of length n, where n is the length of the feature
	 * vectors. If the features of the test exemplar and the current exemplar are the same at
	 * index i, then the i'th bit will be 1; otherwise it will be 0.
	 */
	public void setContextLabel(Exemplar e){
		label = 0;
		System.out.println("Data: " + this + "\nWith: " + e);
		int length = features.length;
		int[] otherFeats = e.getFeatures();
		for(int i = 0; i < length; i++)
			if(otherFeats[i] != features[i]){
//				System.out.println(i + " is different, so |= " + Integer.toBinaryString(1 << i));
				label |= (1 << i);
			}
		System.out.println(Integer.toBinaryString(label));
		return;
	}
	
	/**
	 * 
	 * @return Array of integers representing the feature vector for this exemplar
	 */
	public int[] getFeatures(){
		return features;
	}
	
	/**
	 * 
	 * @return Original feature vector for this exemplar
	 */
	public LinkedList<String> stringFeats(){
		return stringFeats;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(String s : stringFeats){
			sb.append(s);
			sb.append(',');
		}
		sb.append("\t=\t");
		sb.append(stringOutcome);
		return sb.toString();
	}
	
	@Override
	public int hashCode(){
		return stringFeats.hashCode();
	}
}
