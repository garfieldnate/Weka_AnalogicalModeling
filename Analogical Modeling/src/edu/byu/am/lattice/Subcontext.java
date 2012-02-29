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
package edu.byu.am.lattice;

import java.util.LinkedList;
import java.util.List;

import edu.byu.am.data.Exemplar;
import edu.byu.am.data.Index;


	/**
	 * Represents a subcontext, containing a list of Exemplars which belong to it.
	 */
	public class Subcontext {
		List<Exemplar> data;
		int outcome;
		int label;
		
		/**
		 * Initializes the subcontext by creating the list to hold the data
		 * @param l Binary label of the subcontext
		 */
		public Subcontext(int l){
			data = new LinkedList<Exemplar>();
			label = l;
		}
		
		/**
		 * Adds an exemplar to the subcontext and sets the outcome accordingly.
		 * If different outcomes are present in the contained exemplars, the 
		 * outcome is {@link Index#NONDETERMINISTIC}
		 * @param e
		 */
		public void add(Exemplar e){
			if(data.size() != 0){
				if(e.getOutcome() != data.get(0).getOutcome())
					outcome = Index.NONDETERMINISTIC;
			}
			else{
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
		int getLabel(){
			return label;
		}
		
	
	}