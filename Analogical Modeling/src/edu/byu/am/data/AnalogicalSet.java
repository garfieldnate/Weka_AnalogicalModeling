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
import java.util.Map;
import java.util.Map.Entry;

import edu.byu.am.lattice.Subcontext;
import edu.byu.am.lattice.Supracontext;


//	TODO: check that analogical set is for exemplars, not subsets.
/**
 * This class holds a list of the exemplars that influenced the predicted
 * outcome of a certain test item, along with the analogical effect of each.
 * 
 * @author Nate Glenn
 * 
 */
public class AnalogicalSet {

	/**
	 * Mapping of an exemplar to its analogical effect
	 */
	private Map<Exemplar, Double> exEffectMap = new HashMap<Exemplar, Double>();

	/**
	 * Mapping of exemplar to the number of pointers to it
	 */
	private Map<Exemplar, Integer> exPointerMap;

	private Map<Integer, Integer> outcomePointerMap = new HashMap<Integer, Integer>();

	private Map<Integer, Double> outcomeLikeliehoodMap = new HashMap<Integer, Double>();

	private int totalPointers = 0;

	private int outcome = -1;
	private double outcomeVal = Double.NEGATIVE_INFINITY;

	/**
	 * The exemplar whose outcome is being predicted by this set
	 */
	private Exemplar predictedEx;

	private static String newline = System.getProperty("line.separator");

	public AnalogicalSet(List<Supracontext> supraList, Exemplar predict,
			boolean linear) {

		setPredictedEx(predict);

		// find numbers of pointers to individual exemplars
		setPointerMap(getPointers(supraList, linear));

		// find the total number of pointers
		for (Exemplar e : getPointerMap().keySet())
			setTotalPointers(getTotalPointers() + getPointerMap().get(e));

		// find the analogical effect of an exemplar given the total pointers
		// and the individual
		// numbers of pointers
		for (Exemplar e : getPointerMap().keySet())
			getEffectMap().put(e,
					getPointerMap().get(e) / ((double) getTotalPointers()));

		// find the likelihood for a given outcome based on the pointers
		for (Exemplar e : getPointerMap().keySet()) {
			if (outcomePointerMap.containsKey(e.getOutcome()))
				outcomePointerMap.put(
						e.getOutcome(),
						outcomePointerMap.get(e.getOutcome())
								+ exPointerMap.get(e));
			else
				outcomePointerMap.put(e.getOutcome(), exPointerMap.get(e));
		}

		for (Integer i : outcomePointerMap.keySet())
			outcomeLikeliehoodMap.put(i, outcomePointerMap.get(i)
					/ (double) totalPointers);
		// Set outcome to the most likely value
		Double temp;
		for (Integer i : outcomeLikeliehoodMap.keySet()) {
			temp = outcomeLikeliehoodMap.get(i);
			if (temp > getOutcomeVal()) {
				setOutcomeVal(temp);
				setOutcome(i);
			}
		}
		// System.out.println(getEffectMap());
		// System.out.println(outcomePointerMap);
		// System.out.println(outcomeLikeliehoodMap);
	}

	/**
	 * See page 392 of the red book.
	 * 
	 * @param supraList
	 *            List of Supracontexts created by filling the supracontextual
	 *            lattice.
	 * @param linear
	 *            True if pointer counting should be done linearly; false
	 *            if it should be done quadratically
	 * @return A mapping of each exemplar to the number of pointers pointing to
	 *         it.
	 */
	private Map<Exemplar, Integer> getPointers(List<Supracontext> supraList,
			boolean linear) {
		Map<Exemplar, Integer> pointers = new HashMap<Exemplar, Integer>();
		int pointersInList = 0;
		int pointersToSupra = 0;
		for (Supracontext supra : supraList) {
			if (!linear) {
				pointersInList = 0;
				for (Subcontext sub : supra.getData())
					pointersInList += sub.getData().size();
			}
			for (Subcontext sub : supra.getData()) {
				for (Exemplar e : sub.getData()) {
					pointersToSupra = supra.getCount();
					// add together if already in the map
					if (pointers.get(e) != null)
						pointers.put(e, pointers.get(e)
								+ (linear ? 1 : pointersInList)
								* pointersToSupra);
					else
						pointers.put(e, (linear ? 1 : pointersInList)
								* pointersToSupra);
				}
			}
		}
		return pointers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("predicting:");
		sb.append(getPredictedEx());
		sb.append(newline);

		sb.append("outcome: ");
		sb.append(Index.getString(outcome));
		sb.append(" (");
		sb.append(outcomeVal);
		sb.append(")");
		sb.append(newline);

		for (Entry<Exemplar, Double> e : getEffectMap().entrySet()) {
			sb.append(e.getKey());
			sb.append(": ");
			sb.append(e.getValue());
			sb.append(newline);
		}
		for (Integer i : outcomeLikeliehoodMap.keySet()) {
			sb.append(Index.getString(i) + ": ");
			sb.append(outcomeLikeliehoodMap.get(i) + newline);
		}
		return sb.toString();
	}

	public Map<Exemplar, Double> getEffectMap() {
		return exEffectMap;
	}

	public Map<Exemplar, Integer> getPointerMap() {
		return exPointerMap;
	}

	private void setPointerMap(Map<Exemplar, Integer> pointerMap) {
		this.exPointerMap = pointerMap;
	}

	public Map<Integer, Double> getOutcomeMap() {
		return outcomeLikeliehoodMap;
	}

	public int getTotalPointers() {
		return totalPointers;
	}

	private void setTotalPointers(int totalPointers) {
		this.totalPointers = totalPointers;
	}

	public Map<Integer, Integer> getOutcomePointers() {
		return outcomePointerMap;
	}

	public Map<Integer, Double> getOutcomeLikelihood() {
		return outcomeLikeliehoodMap;
	}

	public Exemplar getPredictedEx() {
		return predictedEx;
	}

	private void setPredictedEx(Exemplar predictedEx) {
		this.predictedEx = predictedEx;
	}

	public double getOutcomeVal() {
		return outcomeVal;
	}

	private void setOutcomeVal(double outcomeVal) {
		this.outcomeVal = outcomeVal;
	}

	public int getOutcome() {
		return outcome;
	}

	private void setOutcome(int outcome) {
		this.outcome = outcome;
	}
}
