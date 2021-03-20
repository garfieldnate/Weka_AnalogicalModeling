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
	 * lattice.
	 *
	 * @param subList List of subcontexts to add to lattice
	 * @return A lattice filled with the provided subcontexts.
	 * @throws ExecutionException   If execution is rejected for some reason
	 * @throws InterruptedException If any thread is interrupted for any reason (user presses ctrl-C, etc.)
	 */
	Lattice createLattice(SubcontextList subList) throws InterruptedException, ExecutionException;

	class BasicLatticeFactory implements LatticeFactory {
		@Override
		public Lattice createLattice(SubcontextList subList) {
			return new BasicLattice(subList);
		}
	}

	class DistributedLatticeFactory implements LatticeFactory {
		@Override
		public Lattice createLattice(SubcontextList subList) throws ExecutionException, InterruptedException {
			return new DistributedLattice(subList);
		}
	}

	class JohnsenJohanssonLatticeFactory implements LatticeFactory {
		@Override
		public Lattice createLattice(SubcontextList subList) throws ExecutionException, InterruptedException {
			return new JohnsenJohanssonLattice(subList);
		}
	}

	class SparseLatticeFactory implements LatticeFactory {
		@Override
		public Lattice createLattice(SubcontextList subList) {
			return new SparseLattice(subList);
		}
	}

	/**
	 * Chooses the lattice implementation based on the cardinality of
	 * the instances in the subcontext list.
	 */
	class CardinalityBasedLatticeFactory implements LatticeFactory {
		@Override
		public Lattice createLattice(SubcontextList subList) throws InterruptedException, ExecutionException {
			Lattice lattice;
			if (subList.getCardinality() >= 50) {
				lattice = new JohnsenJohanssonLattice(subList);
			} else if (subList.getLabeler().numPartitions() > 1) {
				// TODO: is it weird that the labeler determines the lattice implementation? Choosing the
				// number of partitions should not be the labeler's responsibility
				lattice = new DistributedLattice(subList);
			} else {
				lattice = new BasicLattice(subList);
			}
			return lattice;
		}
	}
}
