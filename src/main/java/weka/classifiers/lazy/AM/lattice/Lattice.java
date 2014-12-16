package weka.classifiers.lazy.AM.lattice;

import java.util.List;

import weka.classifiers.lazy.AM.data.Supracontext;

/**
 * Given a list of subcontexts, a lattice creates a collection of supracontexts,
 * which are then used to create analogical sets and classify data.
 * 
 * @author Nathan Glenn
 * 
 */
public interface Lattice {
	/**
	 * 
	 * @return The list of supracontexts that were created by filling the
	 *         supracontextual lattice. From this, you can compute the
	 *         analogical set.
	 */
	public abstract List<Supracontext> getSupracontextList();

}