package weka.classifiers.lazy.AM.lattice.distributed;

import weka.classifiers.lazy.AM.data.AnalogicalSet;

/**
 * The function of this class is to find the intersection of several {@link Sublattice sublattices},
 * either producing an {@link AnalogicalSet} or another {@link Sbulattice}, depending on whether
 * the intersection is meant to be final.
 * @author Nate Glenn
 * TODO: work with Supracontexts instead of Sublattices?
 *
 */
public class CombineLattice {
	
	/**
	 * 
	 * @param subs Two or more sublattices that together comprise a full lattice
	 * @return An {@link AnalogicalSet} representing the relative outcome predictions given subs
	 */
	public static AnalogicalSet predict(Sublattice... subs){
		return null;
	}
	
	/**
	 * 
	 * @param subs Two or more sublattices that together comprise a full lattice
	 * @return Another {@link Sublattice} which represents the combination of both lattices.
	 */
	public static Sublattice combine(boolean isFinal, Sublattice... subs){
		return null;
		
	}
}
