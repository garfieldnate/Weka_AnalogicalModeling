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

import edu.byu.am.data.DataLoader;
import edu.byu.am.data.Exemplar;

/**
 * This controls all of the other AM classes in predicting item outcomes.
 * @author Nate Glenn
 *
 */
public class Classifier {
	public Classifier(String fileName){
		DataLoader dl = new DataLoader();
		dl.setCommentor("//");
		dl.setFeatureSeparator("[ ,]+");
		try {
			for(Exemplar e: dl.exemplars("xPlural.txt")){
				System.out.println();
				for(int i : e.getFeatures())
					System.out.print(i + ",");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
