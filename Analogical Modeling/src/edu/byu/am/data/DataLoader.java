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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * @author Nate Glenn
 * This class is for initially loading AM data files. The index of the feature outcome, feature 
 * separator, and commentor can be specified, so that files can be either in the old AM format,
 * <a href="http://www.cs.washington.edu/dm/vfml/appendixes/c45.htm">C4.5</a> format, or anything
 * else.
 * TODO:something about the character used to mean UNKNOWN
 */
public class DataLoader {

	//regex to separate features in a vector
	String sepString = "[\\s\t,]";
	
	//
	String commentSep = "#|//";
	
	int outcomeIndex = -1;
	
	/**
	 * 
	 * @param Regex to split a line on.
	 */
	public void setFeatureSeparator(String sep){
		sepString = sep;
	}
	
	/**
	 * 
	 * @param sep String intended to demarcate comments; \#by default.
	 */
	public void setCommentor(String sep){
		commentSep = sep;
	}
	
	/**
	 * 
	 * @param i Index of feature which indicates the outcome for the given vector. The default, -1,
	 * will cause the loader to use the feature in each vector as the outcome.
	 */
	public void setOutcomeIndex(int i){
		outcomeIndex = i;
	}

	/**
	 * Loads file and converts the contents into a list of Exemplars
	 * @param fileName
	 * @return {@link java.util.LinkedList [LinkedList]} of {@link Exemplar [Exemplars]}, each
	 * corresponding to one line of the file.
	 * @throws IOException
	 */
	public LinkedList<Exemplar> exemplars(String fileName) throws IOException{
		List<LinkedList<String>> data = load(fileName);
		
		LinkedList<Exemplar> exemplars = new LinkedList<Exemplar>();
		int tempOutcomeIndex = outcomeIndex;
		//by default, the last feature will be the outcome
		if(tempOutcomeIndex == -1)
			tempOutcomeIndex = exemplars.size();
		String outcome;
		for(LinkedList<String> sl : data){
			outcome = sl.remove(tempOutcomeIndex);
			exemplars.add(new Exemplar(sl,outcome));
		}
		
		return exemplars;
	}
	
	/**
	 * 
	 * @param name of exemplar file
	 * @return List of String arrays. The first element will be the outcome, and the rest are
	 * features.
	 * @throws IOException if the file is empty.
	 * @throws IllegalArgumentException if a line has the wrong number of features on it. The 
	 * correct number of features is determined from the first line in the file.
	 */
	List<LinkedList<String>> load(String fileName) throws IOException {
		//TODO: check for blank line
		//TODO: grab outcome here so that we can make it the first or last item on the line
		Scanner sc = FileIO.fileScanner(fileName);
		if(!sc.hasNextLine())
			throw new IOException("Exemplar file is empty!");
		List<LinkedList<String>> exemplars = new LinkedList<LinkedList<String>>();
		
		//grab and trim first line; need it to set size of vector
		String line = sc.nextLine().trim();//TODO:=getNextNonBlankLine(sc)
		//remove comments
		line = line.split(commentSep)[0];
		exemplars.add(splitElim(line,sepString));
		
		//to make sure all lines are same length
		int length = exemplars.get(0).size();
		LinkedList<String> vector;
		int lineNum = 1;
		while(sc.hasNextLine()){
			lineNum++;
			line = sc.nextLine();
			//remove comments
			line = line.split(commentSep)[0];
			vector = splitElim(line,sepString);
			
			if(vector.size() != length)
				throw new IllegalArgumentException("\nLine " + lineNum + " of " + fileName + 
						" does not contain the correct number of features.\nShould have " +
						length + " but " + "has " + vector.size());
			exemplars.add(vector);
		}
		return exemplars;
	}
	
	/**
	 * 
	 * @param s String to split
	 * @param regex to split on
	 * @return split string array after removing all empty strings. Prevents creating false empty
	 * features.
	 * @TODO: shouldn't we do something with empty features?
	 */
	private static LinkedList<String> splitElim(String s, String regex){
		LinkedList<String> l = new LinkedList<String>();
		for(String s2 : s.split(regex))
			if(!s2.isEmpty())
				l.add(s2);
		return l;
	}
	
	public static void main(String[] args) throws Exception{
		DataLoader dl = new DataLoader();
		dl.setCommentor("//");
		dl.setFeatureSeparator("[ ,\t]+");
		dl.setOutcomeIndex(0);
		for(Exemplar ex: dl.exemplars("A-An corpus.txt")){
				System.out.println(ex);
		}
	}
}
