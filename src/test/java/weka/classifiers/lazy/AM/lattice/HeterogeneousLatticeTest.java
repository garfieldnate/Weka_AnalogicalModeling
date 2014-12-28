package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Test the HeterogeneousLattice, which does not remove heterogeneous
 * supracontexts and therefore cannot be tested with the other {@link Lattice}
 * implementations in {@link LatticeTest}.
 * 
 * @author Nathan Glenn
 * 
 */
public class HeterogeneousLatticeTest {
	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
		final Instance test = train.get(0);
		train.remove(0);

		// Define a labeler which doesn't partition labels so that we can just
		// test with the chapter 3 data without it being reduced to a
		// cardinality of one
		Labeler noPartitionLabeler = new Labeler(MissingDataCompare.MATCH,
				test, false) {
			Labeler l = new IntLabeler(MissingDataCompare.MATCH, test, false);

			@Override
			public Label label(Instance data) {
				return l.label(data);
			}

			@Override
			public int numPartitions() {
				return 1;
			}

			@Override
			public Label partition(Label label, int partitionIndex) {
				return label;
			}

			@Override
			public Label getMinimum() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		SubcontextList subList = new SubcontextList(noPartitionLabeler, train);
		HeterogeneousLattice heteroLattice = new HeterogeneousLattice(subList,
				0);

		Set<Supracontext> actualSupras = heteroLattice.getSupracontexts();

		String[] expectedSupras = new String[] {
				"[2x(001|&nondeterministic&|3,1,0,e/3,1,1,r)]",
				"[1x(100|r|2,1,2,r)]",
				"[1x(001|&nondeterministic&|3,1,0,e/3,1,1,r),(100|r|2,1,2,r),(101|r|2,1,0,r)]",
				"[1x(110|r|0,3,2,r),(100|r|2,1,2,r)]",
				"[1x(001|&nondeterministic&|3,1,0,e/3,1,1,r),(110|r|0,3,2,r),(100|r|2,1,2,r),(101|r|2,1,0,r)]" };

		assertEquals(expectedSupras.length, actualSupras.size());
		for (String expected : expectedSupras) {
			ClassifiedSupra supra = TestUtils.getSupraFromString(expected,
					train);
			TestUtils.assertContainsSupra(actualSupras, supra);
		}
	}

}
