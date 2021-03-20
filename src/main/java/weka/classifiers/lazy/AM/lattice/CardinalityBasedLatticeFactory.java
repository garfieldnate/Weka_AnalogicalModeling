package weka.classifiers.lazy.AM.lattice;

import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;

import java.util.concurrent.ExecutionException;

public enum CardinalityBasedLatticeFactory implements LatticeFactory {
	INSTANCE;

	@Override
	public Lattice createLattice(SubcontextList subList) throws InterruptedException, ExecutionException {
		Lattice lattice;
		if (subList.getCardinality() >= 50) {
			lattice = new JohnsenJohanssonLattice(subList);
		} else if (subList.getLabeler().numPartitions() > 1) {
			// TODO: is it weird that the labeller determines the lattice implementation? Choosing the
			// number of partitions should not be the labeler's responsibility
			lattice = new DistributedLattice(subList);
		} else {
			lattice = new BasicLattice(subList);
		}
		return lattice;
	}
}
