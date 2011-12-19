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
 * This class is for initially loading AM data files. Files can be either in the old AM format
 * or in <a href="http://www.cs.washington.edu/dm/vfml/appendixes/c45.htm">C4.5</a> format. 
 *
 */
public class DataLoader {

	String sepString = "[\\s\t,]";
	
	String commentSep = "#";
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
	 * @param name of exemplar file
	 * @return List of String arrays. The first element will be the outcome, and the rest are
	 * features.
	 * @throws IOException if the file is empty.
	 * @throws IllegalArgumentException if a line has the wrong number of features on it. The 
	 * correct number of features is determined from the first line in the file.
	 */
	List<String[]> load(String fileName) throws IOException {
		Scanner sc = FileIO.fileScanner(fileName);
		if(!sc.hasNextLine())
			throw new IOException("Exemplar file is empty!");
		List<String[]> exemplars = new LinkedList<String[]>();
		String line = sc.nextLine().trim();
		if(line.contains(commentSep))
			line = (String) line.subSequence(0, line.indexOf(commentSep));
		exemplars.add(splitElim(line,sepString));
		//make sure all lines are same length
		int length = exemplars.get(0).length;
		String[] temp2;
		int lineNum = 1;
		while(sc.hasNextLine()){
			lineNum++;
			line = sc.nextLine();
//			System.out.println(line);
			if(line.indexOf(commentSep) != -1)
				temp2 = splitElim((String) line.subSequence(0, line.indexOf(commentSep)),
						sepString);
			else
				temp2 = splitElim(line,sepString);
//			for(String s : temp2)
//				System.out.print(s + "=");
			if(temp2.length != length)
				throw new IllegalArgumentException("Line " + lineNum + " does not contain " +
						"the correct number of features.\nShould have " + length + " but " +
						"has " + temp2.length);
			exemplars.add(temp2);
		}
		return exemplars;
	}
	
	/**
	 * 
	 * @param s String to split
	 * @param regex to split on
	 * @return split string array after removing all empty strings. Prevents creating false empty
	 * features.
	 */
	private static String[] splitElim(String s, String regex){
		List<String> l = new ArrayList<String>();
		for(String s2 : s.split(regex))
			if(!s2.isEmpty())
				l.add(s2);
		String[] retVal = new String[l.size()];
		//can't use toArray, which returns Object[].
		for(int i = 0; i < l.size(); i++){
			retVal[i] = l.get(i);
		}
		return retVal;
	}
	
	public static void main(String[] args) throws Exception{
		DataLoader dl = new DataLoader();
		dl.setCommentor("//");
		dl.setFeatureSeparator("[ ,]+");
		for(String[] sa: dl.load("xPlural.txt")){
			for(String s : sa)
				System.out.print(s + ",");
			System.out.println();
		}
	}
}
