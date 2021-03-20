package weka.classifiers.lazy.AM.lattice;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Factory for creating {@link Lattice Lattices}.
 *
 * @author Nathan Glenn
 */
public interface LatticeFactory {
	/**
	 * @return {@link Lattice} implementation
	 */
	Lattice createLattice();

	/**
	 * Chooses the lattice implementation based on the cardinality of
	 * the instances in the subcontext list.
	 * {@inheritDoc}
	 */
	class CardinalityBasedLatticeFactory implements LatticeFactory {
		private final int cardinality;
		private final int numPartitions;
		private final Supplier<Random> randomProvider;

		public CardinalityBasedLatticeFactory(int cardinality, int numPartitions) {
			this.cardinality = cardinality;
			this.numPartitions = numPartitions;
			this.randomProvider = () -> new Random(ThreadLocalRandom.current().nextLong());
		}

		public CardinalityBasedLatticeFactory(int cardinality, int numPartitions, Supplier<Random> randomProvider) {
			this.cardinality = cardinality;
			this.numPartitions = numPartitions;
			this.randomProvider = randomProvider;
		}

		@Override
		public Lattice createLattice() {
			if (cardinality >= 50) {
				return new JohnsenJohanssonLattice(randomProvider);
			} else if (numPartitions > 1) {
				// TODO: is it weird that the labeler determines the lattice implementation? Choosing the
				// number of partitions should not be the labeler's responsibility
				return new DistributedLattice();
			} else {
				return new BasicLattice();
			}
		}
	}
}
