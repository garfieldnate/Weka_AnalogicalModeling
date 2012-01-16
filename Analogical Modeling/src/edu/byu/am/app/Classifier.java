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

package edu.byu.am.app;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.byu.am.data.AnalogicalSet;
import edu.byu.am.data.DataLoader;
import edu.byu.am.data.Exemplar;

/**
 * This controls all of the other AM classes in predicting item outcomes.
 * @author Nate Glenn
 *
 */
public class Classifier {
	List<Exemplar> data;
	
	public Classifier(String fileName){
		DataLoader dl = new DataLoader();
		dl.setCommentor("//");
		dl.setFeatureSeparator("[ ,\t]+");
		try {
			data = dl.exemplars(fileName);
//			for(Exemplar e: dl.exemplars(fileName)){
//				System.out.println(e);
//				for(int i : e.getFeatures())
//					System.out.print(i + ",");
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<AnalogicalSet> classify(String fileName){
		DataLoader dl = new DataLoader();
		dl.setCommentor("//");
		dl.setFeatureSeparator("[ ,\t]+");
		
		List<AnalogicalSet> sets = new LinkedList<AnalogicalSet>();
		try {
			for(Exemplar ex : dl.exemplars(fileName))
				sets.add(classify(ex));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public AnalogicalSet classify(Exemplar testItem){
		for(Exemplar ex : data)
			ex.setContextLabel(testItem);
		return null;
	}
	
	public static void main(String[] args){
		Classifier cl = new Classifier("ch3example.txt");
		cl.classify("ch3examplePredict.txt");
	}
}
