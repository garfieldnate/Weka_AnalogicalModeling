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

import java.util.List;

import edu.byu.am.data.Exemplar;

/**
 * This class holds the supracontextual lattice and does the work of filling itself during
 * classification. The supractontextual lattice is a boolean algebra which models supra- and
 * subcontexts for the AM algorithm. Using boolean algebra allows efficient computation of these
 * as well as traversal of all subcontexts within a supracontext.
 * @author Nate Glenn
 *
 */
public class Lattice {

	/**
	 * Exemplars
	 */
	List<Exemplar> data;
	
	/**
	 * List of homogeneous supracontexts
	 */
	
	/**
	 * 
	 * @param card Size of the feature vectors; lattice will be 2^card - 1 size.
	 */
	public Lattice(int card) {
		// TODO Auto-generated constructor stub
	}
	
	public void fill(List<Exemplar> d){
		data = d;
		for(Exemplar e : data){
			//add each to supracontexts according to label:
			//1. if supracontext is heterogeneous, skip. If first supra for item to be added to,
			// then ignore the item; all others will be the same.
			//2. Check list of items in supra for introduction of heterogeneity;
			//		if heterogeneous
			//			mark and forget about it.
			//		else
			//			add item to list pointed to be supracontext
			//	must keep minimal number of lists; keep track of which supracontexts are pointing
			//	to which lists, create new one only when necessary.
		}
	}

	
}
