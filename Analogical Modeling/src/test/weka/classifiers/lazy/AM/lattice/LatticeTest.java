package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import weka.classifiers.lazy.AM.AMconstants;
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
		train = TestUtils.chapter3Train();
		test = TestUtils.chapter3Test();

		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				test, false);

		SubcontextList subList = new SubcontextList(labeler, train);
		return Arrays.asList(new Object[] { new BasicLattice(subList),
				BasicLattice.class.getSimpleName() }, new Object[] {
				new DistributedLattice(subList, labeler),
				DistributedLattice.class.getSimpleName() });
	}

	@SuppressWarnings("serial")
	@Test
	public void testChapter3Data() throws Exception {
		List<Supracontext> supras = testLattice.getSupracontextList();
		assertEquals(3, supras.size());

		Supracontext expected = new Supracontext();
		final Subcontext sub1 = new Subcontext(new Label(0b100, 3));
		sub1.add(train.get(3)); // 212r
		expected.setData(new HashSet<Subcontext>() {{ add(sub1); }});
		expected.setCount(1);
		expected.setOutcome(0);// r
		assertTrue(findSupra(supras, expected));

		expected = new Supracontext();
		final Subcontext sub2 = new Subcontext(new Label(0b100, 3));
		sub2.add(train.get(3));// 212r
		final Subcontext sub3 = new Subcontext(new Label(0b110, 3));
		sub3.add(train.get(2));// 032r
		expected.setData(new HashSet<Subcontext>() {{ add(sub2); add(sub3);}});
		expected.setCount(1);
		expected.setOutcome(0);// r
		assertTrue(findSupra(supras, expected));

		expected = new Supracontext();
		final Subcontext sub4 = new Subcontext(new Label(0b001, 3));
		sub4.add(train.get(0));// 310e
		sub4.add(train.get(4));// 311r
		expected.setData(new HashSet<Subcontext>() {{ add(sub4); }});
		expected.setCount(2);
		expected.setOutcome(AMconstants.NONDETERMINISTIC);
		assertTrue(findSupra(supras, expected));
	}

	private boolean findSupra(List<Supracontext> supras, Supracontext expected) {
		for (Supracontext supra : supras)
			if (deepEquals(supra, expected))
				return true;
		return false;
	}

	private boolean deepEquals(Supracontext s1, Supracontext s2) {
		if (s1.getOutcome() != s2.getOutcome())
			return false;

		if (s1.hasData() != s2.hasData())
			return false;
		if (s1.getCount() != s2.getCount())
			return false;

		return s1.getData().equals(s2.getData());
	}
}

