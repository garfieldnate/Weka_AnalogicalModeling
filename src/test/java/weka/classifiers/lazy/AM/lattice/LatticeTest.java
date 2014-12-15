package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.distributed.DistributedLattice;
import weka.core.Instance;
import weka.core.Instances;

@RunWith(Parameterized.class)
public class LatticeTest {

	private interface LatticeFactory {
		public ILattice getLattice(SubcontextList subList);
	}

	@Parameter(0)
	public String testName;
	@Parameter(1)
	public LatticeFactory latticeFactory;

	/**
	 * 
	 * @return A collections of argument lists for running tests
	 *         <ol>
	 *         <li>arg[0] is the test name;</li>
	 *         <li>arg[1] is an {@link ILattice} {@link Constructor} object</li>
	 *         <li>arg[2] is another Object[] containing the arguments to be
	 *         passed to the constructor; element 0 is left null so that it can
	 *         be set to the desired {@link SubcontextList}.</li>
	 *         </ol>
	 * @throws Exception
	 */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		Collection<Object[]> parameters = new ArrayList<>();

		String testName;
		LatticeFactory latticeFactory;

		// basic, non-distributed lattice
		testName = BasicLattice.class.getSimpleName();
		latticeFactory = new LatticeFactory() {
			@Override
			public ILattice getLattice(SubcontextList subList) {
				return new BasicLattice(subList);
			}
		};
		parameters.add(new Object[] { testName, latticeFactory });

		// distributed lattices
		// default number of lattices
		testName = DistributedLattice.class.getSimpleName();
		latticeFactory = new LatticeFactory() {
			@Override
			public ILattice getLattice(SubcontextList subList) {
				return new DistributedLattice(subList);
			}
		};
		parameters.add(new Object[] { testName, latticeFactory });

		// specify 2 sub-lattices
		testName = DistributedLattice.class.getSimpleName()
				+ ": 2 sub-lattices";
		latticeFactory = new LatticeFactory() {
			@Override
			public ILattice getLattice(SubcontextList subList) {
				return new DistributedLattice(subList, 2);
			}
		};
		parameters.add(new Object[] { testName, latticeFactory });

		// specify 10 sub-lattices, which will have to be reduced in some cases
		// to match smaller cardinality data sets
		testName = DistributedLattice.class.getSimpleName()
				+ ": 10 sub-lattices";
		latticeFactory = new LatticeFactory() {
			@Override
			public ILattice getLattice(SubcontextList subList) {
				return new DistributedLattice(subList, 10);
			}
		};
		parameters.add(new Object[] { testName, latticeFactory });

		return parameters;
	}

	@Test
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_DATA);

		String[] expectedSupras = new String[] {
				"[2x(001|&nondeterministic&|3,1,0,e/3,1,1,r)]",
				"[1x(100|r|2,1,2,r)]", "[1x(100|r|2,1,2,r),(110|r|0,3,2,r)]" };
		testSupras(train, 0, expectedSupras);
	}

	/**
	 * Test that supracontexts are properly marked heterogeneous.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testHeterogeneousMarking() throws Exception {
		Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"6-10");
		String[] expectedSupras = new String[] {
				"[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]",
				"[2x(10000|A|K,U,V,U,0,A)]", "[2x(10000|A|K,U,V,U,0,A)]",
				"[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]",
				"[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]" };
		testSupras(train, 0, expectedSupras);

		train = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
		expectedSupras = new String[] {
				"[6x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B)]",
				"[2x(00110|B|A,A,V,U,0,B)]",
				"[2x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B),(00110|B|A,A,V,U,0,B)]",
				"[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]" };
		testSupras(train, 0, expectedSupras);
	}

	/**
	 * Test that {@link BasicLattice#cleanSupra()} is only run after a
	 * subcontext is inserted completely, not after each single insertion
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCleanSupraTiming() throws Exception {
		Instances train = TestUtils.getReducedDataSet(TestUtils.FINNVERB_MIN,
				"1,7-10");

		String[] expectedSupras = new String[] { "[6x(00000|A|U,V,U,0,?,A)]",
				"[3x(00000|A|U,V,U,0,?,A),(00100|A|U,V,I,0,?,A)]",
				"[3x(00000|A|U,V,U,0,?,A),(01100|A|U,0,?,0,?,A),(00100|A|U,V,I,0,?,A)]" };
		testSupras(train, 0, expectedSupras);
	}

	/**
	 * Test that the given test/train combination yields the given list of
	 * supracontexts.
	 * 
	 * @param train
	 *            Dataset to train with
	 * @param testIndex
	 *            Index of item in dataset to remove and use as a test item
	 * @param expectedSupras
	 *            String representations of the supracontexts that should be
	 *            created from the train/test combo
	 */
	private void testSupras(Instances train, int testIndex,
			String[] expectedSupras) {
		Instance test = train.get(testIndex);
		train.remove(testIndex);
		IntLabeler labeler = new IntLabeler(MissingDataCompare.VARIABLE, test, false);
		SubcontextList subList = new SubcontextList(labeler, train);
		ILattice testLattice = latticeFactory.getLattice(subList);
		List<Supracontext> actualSupras = testLattice.getSupracontextList();

		assertEquals(expectedSupras.length, actualSupras.size());
		for (String expected : expectedSupras) {
			Supracontext supra = TestUtils.getSupraFromString(expected, train);
			TestUtils.assertContainsSupra(actualSupras, supra);
		}
	}
}
