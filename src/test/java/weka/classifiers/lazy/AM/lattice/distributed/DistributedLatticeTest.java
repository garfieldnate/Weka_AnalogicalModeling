package weka.classifiers.lazy.AM.lattice.distributed;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.ILattice;
import weka.classifiers.lazy.AM.lattice.Labeler;
import weka.classifiers.lazy.AM.lattice.LatticeTest;
import weka.classifiers.lazy.AM.lattice.MissingDataCompare;
import weka.classifiers.lazy.AM.lattice.SubcontextList;
import weka.core.Instance;
import weka.core.Instances;

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
	
	@Test
	public void worksWithOnlyOneMask() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_TRAIN);
		Instance test = TestUtils.getInstanceFromFile(TestUtils.CHAPTER_3_TEST, 0);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				test, false);

		SubcontextList subList = new SubcontextList(labeler, train);
		
	    exception.expect(IllegalArgumentException.class);
	    exception.expectMessage("numLattices should be greater than 1");
		new DistributedLattice(subList, labeler, 1);
	}

}
