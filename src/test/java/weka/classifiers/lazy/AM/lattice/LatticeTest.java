package weka.classifiers.lazy.AM.lattice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Label;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.classifiers.lazy.AM.lattice.LatticeFactory.BasicLatticeFactory;
import weka.classifiers.lazy.AM.lattice.LatticeFactory.DistributedLatticeFactory;
import weka.classifiers.lazy.AM.lattice.LatticeFactory.SparseLatticeFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

/**
 * Test the lattices that can be used for item classification. These are
 * implementations of the {@link Lattice} interface.
 *
 * @author Nathan Glenn
 */
@RunWith(Parameterized.class)
public class LatticeTest {
    @Parameter()
    public String testName;
    @Parameter(1)
    public LatticeFactory latticeFactory;

	/**
     * @return A collection of argument arrays for running tests. In each array: <ol> <li>arg[0] is the test name.</li>
     * <li>arg[1] is a {@link LatticeFactory} for the {@link Lattice} to be tested.</li> </ol>
     */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() {
		return List.of(
				// basic, non-distributed lattice
				new Object[]{
						"BasicLattice", new BasicLatticeFactory()
				},
				// distributed lattice
				new Object[]{
						"Distributed Lattice", new DistributedLatticeFactory()
				},
				// sparse lattice
				new Object[]{
						"Sparse Lattice", new SparseLatticeFactory()
				});
	}

    @Test
    public void testChapter3Data() throws Exception {
        Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);
        String[] expectedSupras = new String[]{
            "[2x(001|&nondeterministic&|3,1,0,e/3,1,1,r)]", "[1x(100|r|2,1,2,r)]", "[1x(100|r|2,1,2,r),(110|r|0,3,2,r)]"
        };
        testSupras(train, 0, expectedSupras);
    }

    /**
     * Test that supracontexts are properly marked heterogeneous.
     *
     * @throws Exception if the finnverb set can't be loaded
     */
    @Test
    public void testHeterogeneousMarking() throws Exception {
        Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN, "6-10");
        String[] expectedSupras = new String[]{
            "[1x(01010|&nondeterministic&|H,A,V,I,0,A/H,A,V,A,0,B)]", "[2x(10000|A|K,U,V,U,0,A)]",
            "[2x(00011|C|H,U,V,O,S,C)]", "[1x(10010|A|U,U,V,I,0,A),(10000|A|K,U,V,U,0,A)]",
            "[1x(10010|A|U,U,V,I,0,A),(10110|A|P,U,0,?,0,A),(10000|A|K,U,V,U,0,A)]"
        };
        testSupras(train, 0, expectedSupras);

        train = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
        expectedSupras = new String[]{
            "[6x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B)]", "[2x(00110|B|A,A,V,U,0,B)]",
            "[2x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B),(00110|B|A,A,V,U,0,B)]",
            "[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]"
        };
        testSupras(train, 0, expectedSupras);
    }

    /**
     * Test that {@link BasicLattice#cleanSupra()} is only run after a
     * subcontext is inserted completely, not after each single insertion
     */
    @Test
    public void testCleanSupraTiming() throws Exception {
        Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN, "1,7-10");

        String[] expectedSupras = new String[]{
            "[6x(00000|A|U,V,U,0,?,A)]", "[3x(00000|A|U,V,U,0,?,A),(00100|A|U,V,I,0,?,A)]",
            "[3x(00000|A|U,V,U,0,?,A),(01100|A|U,0,?,0,?,A),(00100|A|U,V,I,0,?,A)]"
        };
        testSupras(train, 0, expectedSupras);
    }

    /**
     * Test that the given test/train combination yields the given list of
     * supracontexts.
     *
     * @param train          Dataset to train with
     * @param testIndex      Index of item in dataset to remove and use as a test item
     * @param expectedSupras String representations of the supracontexts that should be created from the train/test
     *                       combo
     */
    private void testSupras(Instances train, int testIndex, String[] expectedSupras) throws ExecutionException, InterruptedException {
        final Instance test = train.get(testIndex);
        train.remove(testIndex);
        SubcontextList subList = new SubcontextList(getFullSplitLabeler(test), train);
        Lattice testLattice = latticeFactory.createLattice(subList);
        Set<Supracontext> actualSupras = testLattice.getSupracontexts();

        assertEquals(expectedSupras.length, actualSupras.size());
        for (String expected : expectedSupras) {
            ClassifiedSupra supra = TestUtils.getSupraFromString(expected, train);
            TestUtils.assertContainsSupra(actualSupras, supra);
        }
    }

    // create a labeler which splits labels into labels of cardinality 1
    private Labeler getFullSplitLabeler(final Instance test) {
        return new Labeler(MissingDataCompare.VARIABLE, test, false) {
            final Labeler internal = new IntLabeler(test, false, MissingDataCompare.VARIABLE);

            @Override
            public Label label(Instance data) {
                return internal.label(data);
            }

            @Override
            public Label partition(Label label, int partitionIndex) {
                int labelBit = label.matches(partitionIndex) ? 0 : 1;
                return new IntLabel(labelBit, 1);
            }

            @Override
            public int numPartitions() {
                return getCardinality();
            }

            @Override
            public Label getAllMatchLabel() {
                return internal.getAllMatchLabel();
            }

			@Override
			public Label fromBits(int labelBits) {
				return null;
			}
        };
    }
}
