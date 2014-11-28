package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import weka.classifiers.lazy.AM.AMconstants;
import weka.classifiers.lazy.AM.lattice.distributed.DistributedLattice;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

@RunWith(Parameterized.class)
public class LatticeTest {

	private ILattice testLattice;
	private static Instances train;
	private static Instances test;

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
		DataSource source = new DataSource("data/ch3example.arff");
		train = source.getDataSet();
		train.setClassIndex(train.numAttributes() - 1);

		source = new DataSource("data/ch3exampleTest.arff");
		test = source.getDataSet();
		test.setClassIndex(test.numAttributes() - 1);

		Labeler labeler = new Labeler(MissingDataCompare.MATCH,
				test.firstInstance(), false);

		SubcontextList subList = new SubcontextList(labeler, train);
		return Arrays.asList(
				new Object[] { new BasicLattice(subList),
						BasicLattice.class.getSimpleName() }, new Object[] {
						new DistributedLattice(subList, labeler),
						DistributedLattice.class.getSimpleName() });
	}

	@Test
	public void testChapter3Data() throws Exception {
		List<Supracontext> supras = testLattice.getSupracontextList();
		assertEquals(3, supras.size());

		Supracontext expected = new Supracontext();
		Subcontext sub1 = new Subcontext(new Label(0b100, 3));
		sub1.add(train.get(3)); // 212r
		expected.setData(new int[] { sub1.getIndex() });
		expected.setCount(1);
		expected.setOutcome(0);// r
		assertTrue(findSupra(supras, expected));

		expected = new Supracontext();
		sub1 = new Subcontext(new Label(0b100, 3));
		sub1.add(train.get(3));// 212r
		Subcontext sub2 = new Subcontext(new Label(0b110, 3));
		sub2.add(train.get(2));// 032r
		expected.setData(new int[] { sub1.getIndex(), sub2.getIndex() });
		expected.setCount(1);
		expected.setOutcome(0);// r
		assertTrue(findSupra(supras, expected));

		expected = new Supracontext();
		sub1 = new Subcontext(new Label(0b001, 3));
		sub1.add(train.get(0));// 310e
		sub1.add(train.get(4));// 311r
		expected.setData(new int[] { sub1.getIndex() });
		expected.setCount(2);
		expected.setOutcome(AMconstants.NONDETERMINISTIC);
		assertTrue(findSupra(supras, expected));
	}
	
	private boolean findSupra(List<Supracontext> supras, Supracontext expected) {
		for(Supracontext supra : supras)
			if(deepEquals(supra, expected))
				return true;
		return false;
	}

	private boolean deepEquals(Supracontext s1, Supracontext s2){
		 if (s1.getOutcome() != s2.getOutcome())
		 return false;
		
		 if (s1.hasData() != s2.hasData())
		 return false;
		 if (s1.getCount() != s2.getCount())
		 return false;
		
		 if (s1.getData().length != s2.getData().length)
		 return false;
		
		 for(int datum : s2.getData()){
		 if(!containsSub(s1.getData(), datum))
		 return false;
		 }
		 return true;
	}

	 private static boolean containsSub(int[] subIndices, int query){
	 for(int index : subIndices)
	 if(Subcontext.getSubcontext(query).equals(
	 Subcontext.getSubcontext(index)))
	 return true;
	 return false;
	 }

}
