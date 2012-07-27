/*
 * **************************************************************************
 * Copyright 2012 Nathan Glenn                                              * 
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 * http://www.apache.org/licenses/LICENSE-2.0                               *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************/
package weka.classifiers.lazy.AM.lattice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.data.Exemplar;



/**
 * Represents a subcontext, containing a list of Exemplars which belong to it.
 * Also keeps track of all instances of Subcontext in a static index.
 */
public class Subcontext {
	//store an index of all existing instances of Subcontext
	private static List<Subcontext> index = new ArrayList<Subcontext>();
	
	/**
	 * 
	 * @param indexLocation Index of the desired Subcontext in the Subcontext index
	 * @return Subcontext contained in the index
	 */
	public static Subcontext getSubcontext(int indexLocation){
		assert(indexLocation < index.size());
		return index.get(indexLocation);
	}
	
	private List<Exemplar> data;
	private int outcome;
	private int label;
	
	/**
	 * The location of this instance in {@link #index}
	 */
	private int indexLocation;

	/**
	 * Initializes the subcontext by creating the list to hold the data
	 * 
	 * @param l
	 *            Binary label of the subcontext
	 */
	public Subcontext(int l) {
		data = new LinkedList<Exemplar>();
		label = l;
		
		index.add(this);
		indexLocation = index.size() - 1;
	}

	/**
	 * Adds an exemplar to the subcontext and sets the outcome accordingly. If
	 * different outcomes are present in the contained exemplars, the outcome is
	 * {@link Index#NONDETERMINISTIC}
	 * 
	 * @param e
	 */
	public void add(Exemplar e) {
		if (data.size() != 0) {
			if (e.getOutcome() != data.get(0).getOutcome())
				outcome = AMconstants.NONDETERMINISTIC;
		} else {
			outcome = e.getOutcome();
		}
		data.add(e);
	}

	public int getOutcome() {
		return outcome;
	}

	/**
	 * @return Binary label of of this subcontext
	 */
	public int getLabel() {
		return label;
	}
	
	/**
	 * 
	 * @return the location of this Subcontext in the static Subcontext index
	 */
	public int getIndex(){
		return indexLocation;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');

		sb.append(binaryLabel(data.get(0).size(), label));
		sb.append('|');

		//we know all of the exemplars must have the same outcome;
		//otherwise the outcome is nondeterministic
		if(outcome == AMconstants.NONDETERMINISTIC)
			sb.append(AMconstants.NONDETERMINISTIC_STRING);
		else
			sb.append(data.get(0).getStringOutcome());
		sb.append('|');

		for (int i = 0; i < data.size() - 1; i++) {
			sb.append(data.get(i));
			sb.append(',');
		}
		sb.append(data.get(data.size() - 1));

		sb.append(')');

		return sb.toString();
	}

	/**
	 * @return list of Exemplars contained in this subcontext
	 */
	public List<Exemplar> getData() {
		return data;
	}

	/**
	 * 
	 * @param card
	 *            Number of features in the subcontext
	 * @param label
	 *            Integer label for the subcontext
	 * @return String representation of binary label, with zeros padded in the
	 *         front
	 */
	public static String binaryLabel(int card, int label) {
		StringBuilder sb = new StringBuilder();
		String binary = Integer.toBinaryString(label);

		int diff = card - binary.length();
		for (int i = 0; i < diff; i++)
			sb.append('0');

		sb.append(binary);
		return sb.toString();
	}
}