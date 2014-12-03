package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.lattice.distributed.DistributedLattice;
import weka.core.Instance;
import weka.core.Instances;

@RunWith(Parameterized.class)
public class LatticeTest {

	private ILattice testLattice;
	private static Instances train;
	private static Instance test;

	public LatticeTest(ILattice lattice, String implName) {
		testLattice = lattice;
	}

	/**
	 * 
	 * @return Implementations of Lattice, and their class names
	 * @throws Exception
	 */
	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		train = TestUtils.getDataSet(TestUtils.CHAPTER_3_TRAIN);
		test = TestUtils.getInstanceFromFile(TestUtils.CHAPTER_3_TEST, 0);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH, test, false);

		SubcontextList subList = new SubcontextList(labeler, train);

		Collection<Object[]> parameters = new ArrayList<>(3);
		parameters.add(new Object[] { new BasicLattice(subList),
				BasicLattice.class.getSimpleName() });
		parameters.add(new Object[] { new DistributedLattice(subList, labeler),
				DistributedLattice.class.getSimpleName() });
		parameters
				.add(new Object[] {
						new DistributedLattice(subList, labeler, 2),
						DistributedLattice.class.getSimpleName()
								+ ": 2 sub-lattices" });
		// 10 should be reduced to 3, since there are only three attributes.
		parameters
				.add(new Object[] {
						new DistributedLattice(subList, labeler, 10),
						DistributedLattice.class.getSimpleName()
								+ ": 10 sub-lattices" });

		return parameters;
	}

	@SuppressWarnings("serial")
	@Test
	public void testChapter3Data() throws Exception {
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
}
