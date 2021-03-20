package weka.classifiers.lazy.AM.lattice;

import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;

import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Given a list of subcontexts, a lattice creates a collection of supracontexts,
 * which are then used to create analogical sets and classify data.
 *
 * @author Nathan Glenn
 */
public interface Lattice {

	/**
	 * Fill the lattice with given subcontexts. This is meant to be done only once for a given Lattice instance.
	 * @throws IllegalStateException if the lattice was already filled
	 */
	void fill(SubcontextList sublist) throws InterruptedException, ExecutionException;
    /**
     * @return The list of supracontexts that were created by filling the supracontextual lattice. From this, you can
     * compute the analogical set.
     */
    Set<Supracontext> getSupracontexts();

}
