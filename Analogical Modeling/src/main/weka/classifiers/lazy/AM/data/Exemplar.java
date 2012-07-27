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

import java.io.Serializable;
import java.util.Arrays;

import weka.classifiers.lazy.AM.AMconstants;
import weka.core.Instance;

/**
 * This class is used to represent one exemplar, or feature vector. This class
 * is immutable.
 * 
 * @author nate
 * 
 */
public class Exemplar implements Serializable {

	private static final long serialVersionUID = 7683867732093134488L;
	
	// Instance is mutable and we're not deep copying it!
	// So we save it only for access to attribute indeces and strings, which
	// cannot be changed, only added to.
	Instance instance;
	
	//size of the attribute in features (minus the class)
	int cardinality;

	/**
	 * 
	 * @param instance
	 *            containing information about the exemplar
	 */
	public Exemplar(Instance instance) {
		if(instance.isMissing(instance.classAttribute())){
			stringOutcome = AMconstants.MISSING_STRING;
			outcome = AMconstants.MISSING;
		}else{
			stringOutcome = instance.stringValue(instance.classAttribute());
			outcome = (int) instance.classValue();
		}
		// convert to integer features
		// also copy string features
		features = new int[instance.numAttributes() - 1];// one of the atts is a
															// class
//		stringFeats = new String[features.length];
		for (int i = 0; i < features.length; i++) {
			features[i] = (int) instance.value(i);
//			stringFeats[i] = instance.stringValue(i);
		}
		// save hashCode for easier processing
		hashCode = instance.hashCode();
		// save instance for access to attributes
		this.instance = (Instance) instance.copy();
		cardinality = features.length;
	}

//	private String[] stringFeats;
	private int[] features;

	private String stringOutcome;
	private int outcome;

	private int hashCode;

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
		return cardinality;
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
		// defensive copy
		return Arrays.copyOf(features, features.length);
	}

//	/**
//	 * 
//	 * @return Original feature vector for this exemplar
//	 */
//	public String[] stringFeats() {
//		return Arrays.copyOf(stringFeats, stringFeats.length);
//	}

	/**
	 * Return string in this form: <a,b,c|R>
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('<');
		for (int i = 0; i < cardinality - 1; i++) {
			sb.append(instance.attribute(i).value(features[i]));
			sb.append(',');
		}
		sb.append(instance.attribute(cardinality - 1).value(features[cardinality - 1]));
		sb.append('|');
		sb.append(stringOutcome);
		sb.append('>');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Exemplar))
			return false;
		Exemplar e2 = (Exemplar) other;
		// use hashCode as shortcut
		if (e2.hashCode == hashCode)
			return Arrays.equals(e2.features, features);
		return false;
	}

	/**
	 * 
	 * @param Index
	 *            of given value in attribute class
	 * @return String representation of given class attribute index value
	 */
	public String classString(Integer i) {
		return instance.classAttribute().value(i);
	}
}
