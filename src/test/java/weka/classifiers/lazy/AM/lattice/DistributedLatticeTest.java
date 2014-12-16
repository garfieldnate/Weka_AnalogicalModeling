package weka.classifiers.lazy.AM.lattice;

import org.junit.Rule;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.lattice.ILattice;

/**
 * Tests functionality/edge cases specific to the distributed lattice. Basic
 * functionality conforming to the {@link ILattice} interface is tested in
 * {@link LatticeTest}.
 * 
 * @author Nathan Glenn
 * 
 */
public class DistributedLatticeTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	// TODO: put some tests here; check the returned supras for a
	// large-cardinality set, for example.
}
