package weka.classifiers.lazy.AM.lattice;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.*;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.mock;

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
    public Supplier<Lattice> latticeSupplier;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
     * @return A collection of argument arrays for running tests. In each array: <ol> <li>arg[0] is the test name.</li>
     * <li>arg[1] is a supplier of {@link Lattice} test instances.</li> </ol>
     */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() {
		// ensure deterministic behavior from JohnsenJohanssonLatice
		AtomicLong randomSeeder = new AtomicLong(0);
		return List.of(
				new Object[]{
						"BasicLattice", (Supplier<Lattice>) BasicLattice::new
				},
				new Object[]{
						"Distributed Lattice", (Supplier<Lattice>) DistributedLattice::new
				},
				new Object[]{
						"Sparse Lattice", (Supplier<Lattice>) SparseLattice::new
				},
				new Object[]{
						"Johnsen-Johansson Lattice", (Supplier<Lattice>) () -> new JohnsenJohanssonLattice(() -> new Random(randomSeeder.getAndIncrement()))
				},
				new Object[]{
						"Heterogeneous Lattice", (Supplier<Lattice>) () -> new HeterogeneousLattice(0)
				});
	}

	private void skipForLatticeClass(String reason, Class<? extends Lattice> clazz) {
		assumeThat(reason, latticeSupplier.get(), not(instanceOf(clazz)));
	}

	@Test
	public void testFillingWithEmptySubcontextList() throws Exception {
		Lattice lattice = latticeSupplier.get();
		lattice.fill(new SubcontextList(mock(IntLabeler.class), Collections.emptyList()));
	}

	@Test
	public void testLatticeCannotBeFilledTwice() throws Exception {
		Lattice lattice = latticeSupplier.get();
		lattice.fill(new SubcontextList(mock(IntLabeler.class), Collections.emptyList()));
		exception.expect(IllegalStateException.class);
		exception.expectMessage(new StringContains("already filled"));
		lattice.fill(new SubcontextList(mock(IntLabeler.class), Collections.emptyList()));
	}

	@Test
	public void testChapter3Data() throws Exception {
		skipForLatticeClass("Not designed for prediction", HeterogeneousLattice.class);
		skipForLatticeClass("Inaccurate for small datasets", JohnsenJohanssonLattice.class);
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
		skipForLatticeClass("Not designed for prediction", HeterogeneousLattice.class);
		skipForLatticeClass("Inaccurate for small datasets", JohnsenJohanssonLattice.class);
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
		skipForLatticeClass("Not designed for prediction", HeterogeneousLattice.class);
		skipForLatticeClass("Inaccurate for small datasets", JohnsenJohanssonLattice.class);

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
        Lattice testLattice = latticeSupplier.get();
        testLattice.fill(subList);
        Set<Supracontext> actualSupras = testLattice.getSupracontexts();

        assertEquals(expectedSupras.length, actualSupras.size());
        for (String expected : expectedSupras) {
            ClassifiedSupra supra = TestUtils.getSupraFromString(expected, train);
            TestUtils.assertContainsSupra(actualSupras, supra);
        }
    }

    // create a labeler which splits labels into labels of cardinality 1
    private Labeler getFullSplitLabeler(final Instance test) {
    	return new FullSplitLabeler(test, false, MissingDataCompare.VARIABLE);
    }

	private static class FullSplitLabeler extends Labeler {
		final Labeler wrapped;

		public FullSplitLabeler(Instance test, boolean ignoreUnknowns, MissingDataCompare mdc) {
			super(test, ignoreUnknowns, mdc);
			wrapped = new IntLabeler(test, false, MissingDataCompare.VARIABLE);
		}

		@Override
		public Label label(Instance data) {
			return wrapped.label(data);
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
		public Label getLatticeTop() {
			return wrapped.getLatticeTop();
		}

		@Override
		public Label getLatticeBottom() {
			return wrapped.getLatticeBottom();
		}

		@Override
		public Label fromBits(int labelBits) {
			return null;
		}
	}
}
