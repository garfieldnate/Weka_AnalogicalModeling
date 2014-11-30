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
package weka.classifiers.lazy.AM;

/**
 * This class holds constants and methods used in the AM classifier.
 * 
 * @author nathan.glenn
 * 
 */
public class AMUtils {
	
	//TODO: perhaps use this to create a dynamic number of lattices of maximum cardinality 4?
//	public static int LATTICE_SIZE_MAX = 4;

	/**
	 * NONDETERMINISTIC will be mapped to "&nondeterministic&", and is used by
	 * {@link weka.classifiers.lazy.AnalogicalModeling.lattice.Supracontext
	 * Supracontext} (this is '*' in the red book paper).
	 * 
	 */
	public static final int NONDETERMINISTIC = -1;

	public static final String NONDETERMINISTIC_STRING = "&nondeterministic&";

	/**
	 * EMPTY will be mapped to "&empty&", and is used by
	 * {@link weka.classifiers.lazy.AnalogicalModeling.lattice.Supracontext
	 * Supracontext} (this is *supralist in the red book paper).
	 * 
	 */
	public static final int EMPTY = -2;
	
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	//used for printing all decimal numbers
	private static final String DECIMAL_FORMAT = "%.5f";
	public static String formatDouble(double d){
		return String.format(DECIMAL_FORMAT, d);
	}
}
