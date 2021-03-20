package weka.classifiers.lazy.AM.lattice;

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

		public CardinalityBasedLatticeFactory(int cardinality, int numPartitions) {
			this.cardinality = cardinality;
			this.numPartitions = numPartitions;
		}
		@Override
		public Lattice createLattice() {
			if (cardinality >= 50) {
				return new JohnsenJohanssonLattice();
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
