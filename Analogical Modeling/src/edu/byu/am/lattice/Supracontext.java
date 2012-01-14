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

import java.util.BitSet;
import java.util.Iterator;


public class Supracontext {
	
	private BitSet label;//binary label
	
	/**
	 * 
	 * @return Binary label of this supracontext
	 */
	public BitSet getLabel(){
		return label;
	}
	
	public Iterator<Supracontext> iterator(){
		return null;
		//list the gaps
		//list g-bit integers
		//flip bits using gray code
		
	}
	
	/**
	 * @param superset Supracontext to test as a superset of subset
	 * @param subset To test as a subset of superset
	 * @return True if superset is a superset of subset;
	 * that is, if superset & subset == superset.
	 */
	public static boolean isSupersetOf(Supracontext superset, Supracontext subset){
		BitSet temp = superset.getLabel();
		temp.and(subset.getLabel());
		return temp.equals(superset.getLabel());
	}
	
}
