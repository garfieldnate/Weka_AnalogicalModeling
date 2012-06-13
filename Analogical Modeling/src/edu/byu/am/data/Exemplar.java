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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.event.ListSelectionEvent;

/**
 * This class is used to represent one exemplar, or feature vector.
 * This class is immutable.
 * 
 * @author nate
 * 
 */
public class Exemplar {

	/**
	 * 
	 * @param newFeats
	 *            String array representing feature vector, including
	 * @param so
	 *            the outcome for the given vector
	 */
	//it's checked as the parameter
	@SuppressWarnings("unchecked")
	public Exemplar(LinkedList<String> newFeats, String so) {
		stringOutcome = so;
		outcome = Index.insert(so);
		// convert to integer features
		features = Index.insert(newFeats);
		//defensive copy
//		stringFeats = new LinkedList<String>();
		stringFeats = (LinkedList<String>) newFeats.clone();
//		Collections.copy(stringFeats, newFeats);
	}

	private LinkedList<String> stringFeats;
	private int[] features;

	private String stringOutcome;
	private int outcome;

	public String getStringOutcome() {
		return stringOutcome;
	}

	public int getOutcome() {
		return outcome;
	}

	/**
	 * 
	 * @return Size of the feature vector, not including the outcome
	 */
	public int size() {
		return features.length;
	}

	// /**
	// * This function will return 0 if {@link #setContextLabel(Exemplar)} has
	// not been called yet.
	// * @return Integer label
	// */
	// public int getLabel(){
	// return label;
	// }

	/**
	 * 
	 * @return Array of integers representing the feature vector for this
	 *         exemplar
	 */
	public int[] getFeatures() {
		//defensive copy
		return Arrays.copyOf(features, features.length);
	}

	/**
	 * 
	 * @return Original feature vector for this exemplar
	 */
	public LinkedList<String> stringFeats() {
		return stringFeats;
	}

	/**
	 * Return string in this form: <a,b,c|R>
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		for (int i = 0; i < stringFeats.size() - 1; i++) {
			sb.append(stringFeats.get(i));
			sb.append(',');
		}
		sb.append(stringFeats.getLast());
		sb.append('|');
		sb.append(stringOutcome);
		sb.append('>');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return stringFeats.hashCode();
	}
}
