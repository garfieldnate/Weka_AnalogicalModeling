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

package weka.classifiers.lazy.AM.data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.lattice.Subcontext;
import weka.classifiers.lazy.AM.lattice.Supracontext;
import weka.core.Instance;

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
	private Map<Instance, Double> exEffectMap = new HashMap<>();

	/**
	 * Mapping of exemplar to the number of pointers to it
	 */
	private Map<Instance, Integer> exPointerMap;

	private Map<String, Integer> classPointerMap = new HashMap<>();

	private Map<String, Double> classLikelihoodMap = new HashMap<>();

	private int totalPointers = 0;

	private String predictedClass = null;
	private double classProbability = Double.NEGATIVE_INFINITY;

	/**
	 * The exemplar whose class is being predicted by this set
	 */
	private Instance classifiedExemplar;

	private static String newline = System.getProperty("line.separator");

	//these are used for sorting items to be printed
	Comparator<Map.Entry<Instance, Integer>> entryComparator1 = new Comparator<Entry<Instance, Integer>>() {
		@Override
		public int compare(Entry<Instance, Integer> arg1,
				Entry<Instance, Integer> arg2) {
			//compare all attribute string values and then the number of pointers
			int compare = 0;
			for(int i = 0; i < arg1.getKey().numAttributes(); i++){
				compare = arg1.getKey().stringValue(i).compareTo(arg2.getKey().stringValue(i));
				if(compare != 0){
					return compare;
				}
			}
			return arg1.getValue().compareTo(arg2.getValue());
		}
	};

	Comparator<Entry<String, Integer>> entryComparator2 = new Comparator<Entry<String, Integer>>() {
		@Override
		public int compare(Entry<String, Integer> arg1,
				Entry<String, Integer> arg2) {
			//compare number of pointers
			return arg1.getValue().compareTo(arg2.getValue());
		}
	};
	
	/**
	 * 
	 * @param supraList
	 *            Supracontext list generated by a Lattice
	 * @param testItem
	 *            Exemplar being classified
	 * @param linear
	 *            True if counting of pointers should be done linearly; false if
	 *            quadratically.
	 */
	public AnalogicalSet(List<Supracontext> supraList, Instance testItem,
			boolean linear) {

		this.classifiedExemplar = testItem;

		// find numbers of pointers to individual exemplars
		exPointerMap = getPointers(supraList, linear);

		// find the total number of pointers
		for (Instance e : exPointerMap.keySet())
			totalPointers += exPointerMap.get(e);

		// find the analogical effect of an exemplar by dividing its pointer
		// count by the total pointer count
		for (Instance e : exPointerMap.keySet())
			exEffectMap.put(e, exPointerMap.get(e)
					/ ((double) getTotalPointers()));

		// find the likelihood for a given outcome based on the pointers
		for (Instance e : exPointerMap.keySet()) {
			String className = e.stringValue(e.classAttribute());
			if (classPointerMap.containsKey(className))
				classPointerMap.put(
						className,
						classPointerMap.get(className)
								+ exPointerMap.get(e));
			else
				classPointerMap.put(className, exPointerMap.get(e));
		}

		// set the likelihood of each possible class index to be its share of
		// the total pointers
		for (String className : classPointerMap.keySet())
			classLikelihoodMap.put(className, classPointerMap.get(className)
					/ (double) totalPointers);
		// Set the class index to that with the highest likelihood
		Double temp;
		for (String className : classLikelihoodMap.keySet()) {
			temp = classLikelihoodMap.get(className);
			if (temp > getClassProbability()) {
				classProbability = temp;
				predictedClass = className;
			}
		}
	}

	/**
	 * See page 392 of the red book.
	 * 
	 * @param supraList
	 *            List of Supracontexts created by filling the supracontextual
	 *            lattice.
	 * @param linear
	 *            True if pointer counting should be done linearly; false if it
	 *            should be done quadratically
	 * @return A mapping of each exemplar to the number of pointers pointing to
	 *         it.
	 */
	private Map<Instance, Integer> getPointers(List<Supracontext> supraList,
			boolean linear) {
		Map<Instance, Integer> pointers = new HashMap<>();
		
		//number of pointers in a supracontext,
		//that is the number of exemplars in the whole thing
		int pointersInList = 0;
		int pointersToSupra = 0;
		//iterate all supracontext
		for (Supracontext supra : supraList) {
			if (!linear) {
				pointersInList = 0;
				//sum number of exemplars for each subcontext
				for (int index : supra.getData())
					pointersInList += Subcontext.getSubcontext(index).getExemplars().size();
			}
			//iterate subcontexts in supracontext
			for (int index : supra.getData()) {
				//number of supras containing this subcontext
				pointersToSupra = supra.getCount();
				//iterate exemplars in subcontext
				for (Instance e : Subcontext.getSubcontext(index).getExemplars()) {
					//pointers to exemplar = pointersToSupra * pointers in list
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

		sb.append("classifying: ");
		sb.append(getClassifiedEx());
		sb.append(newline);

		sb.append("outcome: ");
		sb.append(predictedClass);
		sb.append(" (");
		sb.append(String.format(AMconstants.DECIMAL_FORMAT, classProbability));
		sb.append(")");
		sb.append(newline);

		Set<Entry<Instance, Integer>> sortedEntries1 = new TreeSet<>(entryComparator1);
		sortedEntries1.addAll(getExemplarPointers().entrySet());
		sb.append("Exemplar effects:");
		sb.append("\n");
		for (Entry<Instance, Integer> e : sortedEntries1)
			sb.append(e.getKey()
					+ " : "
					+ e.getValue()
					+ " ("
					+ String.format(AMconstants.DECIMAL_FORMAT,
							e.getValue() / (double)totalPointers) + ")\n");

		Set<Entry<String, Integer>> sortedEntries2 = new TreeSet<>(entryComparator2);
		sortedEntries2.addAll(getClassPointers().entrySet());
		sb.append("Outcome likelihoods:" + newline);
		for (Entry<String, Integer> e : sortedEntries2)
			sb.append(e.getKey()
					+ " : "
					+ e.getValue()
					+ " ("
					+ String.format(AMconstants.DECIMAL_FORMAT,
							e.getValue() / (double) totalPointers) + ")\n");

		return sb.toString();
	}

	/**
	 * 
	 * @return A mapping between exemplars and their analogical effect (decimal
	 *         percentage)
	 */
	public Map<Instance, Double> getExemplarEffectMap() {
		return exEffectMap;
	}

	/**
	 * 
	 * @return Mapping of exemplars in the analogical set to the number of
	 *         pointers to it
	 */
	public Map<Instance, Integer> getExemplarPointers() {
		return exPointerMap;
	}

	/**
	 * 
	 * @return A mapping between a possible class index and its likelihood
	 *         (decimal probability)
	 */
	public Map<String, Double> getClassLikelihoodMap() {
		return classLikelihoodMap;
	}

	/**
	 * 
	 * @return The total number of pointers in this analogical set
	 */
	public int getTotalPointers() {
		return totalPointers;
	}

	/**
	 * 
	 * @return A mapping between a class value index the number of pointers
	 *         pointing to it
	 */
	public Map<String, Integer> getClassPointers() {
		return classPointerMap;
	}

	/**
	 * 
	 * @return A mapping between the class value index and its selection
	 *         probability
	 */
	public Map<String, Double> getClassLikelihood() {
		return classLikelihoodMap;
	}

	/**
	 * 
	 * @return The exemplar which was classified
	 */
	public Instance getClassifiedEx() {
		return classifiedExemplar;
	}

	/**
	 * 
	 * @return Probability of the predicted class
	 */
	public double getClassProbability() {
		return classProbability;
	}

	/**
	 * 
	 * @return Index of the predicted class attribute value
	 */
	//TODO: this could actually be a tie, so it should return multiple
	public String getPredictedClass() {
		return predictedClass;
	}
}
