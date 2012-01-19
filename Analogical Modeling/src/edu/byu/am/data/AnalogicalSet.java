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
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class holds a list of the exemplars that influenced the predicted outcome of a certain test
 * item, along with the analogical effect of each.
 * @author Nate Glenn
 *
 */
public class AnalogicalSet {

	private Map<Exemplar,Double> set = new HashMap<Exemplar,Double>();
	private static String newline = System.getProperty("line.separator");
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		for(Entry<Exemplar, Double> e : set.entrySet()){
			s.append(e.getKey());
			s.append("\t");
			s.append(e.getValue());
			s.append(newline);
		}
		return s.toString();
	}
	
}
