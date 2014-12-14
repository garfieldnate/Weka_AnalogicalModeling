package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.distributed.DistributedLattice;
import weka.core.Instance;
import weka.core.Instances;

@RunWith(Parameterized.class)
public class LatticeTest {

	@Parameter(0)
	public String testName;
	@Parameter(1)
	public Constructor<ILattice> latticeConstructor;
	@Parameter(2)
	public Object[] ctorArgs;

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
		Constructor<? extends ILattice> latticeConstructor;
		Object[] constructorArgs;

		testName = BasicLattice.class.getSimpleName();
		latticeConstructor = BasicLattice.class
				.getDeclaredConstructor(SubcontextList.class);
		constructorArgs = new Object[1];
		parameters.add(new Object[] { testName, latticeConstructor,
				constructorArgs });

		// default number of lattices
		testName = DistributedLattice.class.getSimpleName();
		latticeConstructor = DistributedLattice.class
				.getDeclaredConstructor(SubcontextList.class);
		parameters.add(new Object[] { testName, latticeConstructor,
				new Object[1] });

		// specify 2 lattices
		testName = DistributedLattice.class.getSimpleName()
				+ ": 2 sub-lattices";
		latticeConstructor = DistributedLattice.class.getDeclaredConstructor(
				SubcontextList.class, int.class);
		constructorArgs = new Object[] { null, new Integer(2) };
		parameters.add(new Object[] { testName, latticeConstructor,
				constructorArgs });

		// specify 10 lattices, which will have to be reduced in some cases to
		// match smaller cardinality data sets
		testName = DistributedLattice.class.getSimpleName()
				+ ": 10 sub-lattices";
		constructorArgs = new Object[] { null, new Integer(10) };
		parameters.add(new Object[] { testName, latticeConstructor,
				constructorArgs });

		return parameters;
	}

	@Test
	@SuppressWarnings("serial")
	public void testChapter3Data() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.CHAPTER_3_TRAIN);
		Instance test = TestUtils.getInstanceFromFile(TestUtils.CHAPTER_3_TEST,
				0);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH, test, false);

		SubcontextList subList = new SubcontextList(labeler, train);

		ctorArgs[0] = subList;
		ILattice testLattice = latticeConstructor.newInstance(ctorArgs);

		List<Supracontext> supras = testLattice.getSupracontextList();
		assertEquals(3, supras.size());

		final Subcontext sub1 = new Subcontext(new Label(0b100, 3));
		sub1.add(train.get(3)); // 212r
		Supracontext expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub1);
			}
		}, BigInteger.ONE, 0);// r
		TestUtils.assertContainsSupra(supras, expected);

		final Subcontext sub2 = new Subcontext(new Label(0b100, 3));
		sub2.add(train.get(3));// 212r
		final Subcontext sub3 = new Subcontext(new Label(0b110, 3));
		sub3.add(train.get(2));// 032r
		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub2);
				add(sub3);
			}
		}, BigInteger.ONE, 0);// r
		TestUtils.assertContainsSupra(supras, expected);

		final Subcontext sub4 = new Subcontext(new Label(0b001, 3));
		sub4.add(train.get(0));// 310e
		sub4.add(train.get(4));// 311r
		expected = new Supracontext(new HashSet<Subcontext>() {
			{
				add(sub4);
			}
		}, BigInteger.valueOf(2), AMUtils.NONDETERMINISTIC);
		TestUtils.assertContainsSupra(supras, expected);
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
		Instance test = train.get(0);
		train.remove(0);
		Labeler labeler = new Labeler(MissingDataCompare.VARIABLE, test, false);
		SubcontextList subList = new SubcontextList(labeler, train);
		ctorArgs[0] = subList;
		ILattice testLattice = latticeConstructor.newInstance(ctorArgs);

		List<Supracontext> actualSupras = testLattice.getSupracontextList();
		assertEquals(5, actualSupras.size());

		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[2x(10000|A|K,U,V,U,0,A)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[2x(10000|A|K,U,V,U,0,A)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[1x(01010|&nondeterministic&|H,A,V,A,0,B/H,A,V,I,0,A)]", train));
		
		train = TestUtils.getReducedDataSet(TestUtils.FINNVERB, "6-10");
		test = train.get(0);
		train.remove(0);
		labeler = new Labeler(MissingDataCompare.VARIABLE, test, false);
		subList = new SubcontextList(labeler, train);
		ctorArgs[0] = subList;
		testLattice = latticeConstructor.newInstance(ctorArgs);
		actualSupras = testLattice.getSupracontextList();
		System.out.println(testLattice.getSupracontextList());
		assertEquals(4, actualSupras.size());
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[6x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[2x(00110|B|A,A,V,U,0,B)]", train));
		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[2x(00001|B|A,A,0,?,S,B/A,A,0,?,S,B),(00110|B|A,A,V,U,0,B)]", train));
//		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]", train));
//		TestUtils.assertContainsSupra(actualSupras, TestUtils.getSupraFromString("[3x(10000|&nondeterministic&|J,A,0,?,0,B/L,A,0,?,0,A/M,A,0,?,0,B/J,A,0,?,0,B/J,A,0,?,0,B/S,A,0,?,0,B/V,A,0,?,0,B/H,A,0,?,0,A/M,A,0,?,0,B/K,A,0,?,0,B/K,A,0,?,0,B/P,A,0,?,0,B/P,A,0,?,0,A/T,A,0,?,0,B)]", train));
		System.out.println();
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
		Instance test = train.get(0);
		train.remove(0);
		Labeler labeler = new Labeler(MissingDataCompare.VARIABLE, test, false);
		SubcontextList subList = new SubcontextList(labeler, train);
		ctorArgs[0] = subList;
		ILattice testLattice = latticeConstructor.newInstance(ctorArgs);
		
		List<Supracontext> actualSupras = testLattice.getSupracontextList();
		assertEquals(actualSupras.size(), 1);
//		System.out.println(actualSupras);
	}
}
