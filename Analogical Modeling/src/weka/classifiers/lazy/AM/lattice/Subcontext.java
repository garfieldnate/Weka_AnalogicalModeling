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
package weka.classifiers.lazy.AM.lattice;

import java.util.LinkedList;
import java.util.List;

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.data.Exemplar;



/**
 * Represents a subcontext, containing a list of Exemplars which belong to it.
 */
public class Subcontext {
	List<Exemplar> data;
	int outcome;
	int label;

	/**
	 * Initializes the subcontext by creating the list to hold the data
	 * 
	 * @param l
	 *            Binary label of the subcontext
	 */
	public Subcontext(int l) {
		data = new LinkedList<Exemplar>();
		label = l;
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