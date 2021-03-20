package weka.classifiers.lazy.AM.lattice;

import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;

import java.util.concurrent.ExecutionException;

/**
 * Factory for creating {@link Lattice Lattices}.
 *
 * @author Nathan Glenn
 */
public interface LatticeFactory {
	/**
	 * Fills a {@link Lattice} object with the {@link Subcontext subcontexts}
	 * contained in the input {@link SubcontextList}, and returns the resulting
	 * lattice. The implementation of lattice used depends on the cardinality of
	 * the instances in the subcontext list.
	 *
	 * @param subList List of subcontexts to add to lattice
	 * @return A lattice filled with the provided subcontexts.
	 * @throws ExecutionException   If execution is rejected for some reason
	 * @throws InterruptedException If any thread is interrupted for any reason (user presses ctrl-C, etc.)
	 */
	Lattice createLattice(SubcontextList subList) throws InterruptedException, ExecutionException;
}
