package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.SubcontextAggregator;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabeler;
import weka.classifiers.lazy.AM.label.Labeler;
import weka.classifiers.lazy.AM.label.MissingDataCompare;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Tests functionality/edge cases specific to the distributed lattice. Basic
 * functionality conforming to the {@link Lattice} interface is tested in
 * {@link LatticeTest}.
 * 
 * @author Nathan Glenn
 * 
 */
public class DistributedLatticeTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	// TODO: put some tests here; check the returned supras for a
	// large-cardinality set, for example.Labeler[] labelers = new Labeler[] {

	/**
	 * This tests a bug where the count was off by 1 in the distributed lattice
	 * implementation due to failing to set the supracontext count the first
	 * time, leaving it at zero.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFinnverb() throws Exception {
		Instances train = TestUtils.getDataSet(TestUtils.FINNVERB);

		String[] expectedSupras = new String[] {
				"[18x(1000110000|C|K,U,V,U,S,L,0,?,T,A,C)]",
				"[9x(0101000011|A|H,A,V,I,0,?,0,?,S,E,A),(0101000000|A|H,O,V,I,0,?,0,?,T,A,A)]",
				"[9x(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[18x(1000001100|A|M,U,V,U,0,?,O,T,T,A,A)]",
				"[3x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A)]",
				"[54x(0101000000|A|H,O,V,I,0,?,0,?,T,A,A)]",
				"[18x(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[15x(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A)]",
				"[9x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1011000011|A|L,U,0,?,0,?,0,?,K,E,A/S,U,0,?,0,?,0,?,R,E,A/T,U,0,?,0,?,0,?,L,E,A/P,U,0,?,0,?,0,?,K,E,A/T,U,0,?,0,?,0,?,K,E,A/P,U,0,?,0,?,0,?,R,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[9x(0101000011|A|H,A,V,I,0,?,0,?,S,E,A),(0101000000|A|H,O,V,I,0,?,0,?,T,A,A),(0111000011|A|H,A,0,?,0,?,0,?,K,E,A)]",
				"[32x(0001110000|C|H,U,V,O,S,L,0,?,T,A,C)]",
				"[6x(1000001100|A|M,U,V,U,0,?,O,T,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[3x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001001111|A|J,U,V,O,0,?,O,K,S,E,A),(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1011000011|A|L,U,0,?,0,?,0,?,K,E,A/S,U,0,?,0,?,0,?,R,E,A/T,U,0,?,0,?,0,?,L,E,A/P,U,0,?,0,?,0,?,K,E,A/T,U,0,?,0,?,0,?,K,E,A/P,U,0,?,0,?,0,?,R,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A),(1011001111|A|P,U,0,?,0,?,O,S,K,E,A)]",
				"[3x(1001001111|A|J,U,V,O,0,?,O,K,S,E,A),(1001000011|A|V,U,V,O,0,?,0,?,L,E,A/K,U,V,O,0,?,0,?,L,E,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A),(1001001100|A|S,U,V,I,0,?,O,S,T,A,A/T,U,V,O,0,?,O,T,T,A,A/U,U,V,I,0,?,O,T,T,A,A/L,U,V,O,0,?,O,T,T,A,A/L,U,V,I,0,?,O,S,T,A,A/M,U,V,I,0,?,O,S,T,A,A),(1000001100|A|M,U,V,U,0,?,O,T,T,A,A),(1000000011|A|L,U,V,U,0,?,0,?,L,E,A/K,U,V,U,0,?,0,?,L,E,A)]",
				"[45x(1001000000|A|V,U,V,O,0,?,0,?,T,A,A)]",
				"[9x(1011000010|A|S,U,0,?,0,?,0,?,L,A,A),(1001000000|A|V,U,V,O,0,?,0,?,T,A,A)]",
				"[36x(1100000000|A|N,O,V,U,0,?,0,?,T,A,A/S,O,V,U,0,?,0,?,T,A,A)]", };
		testSupras(train, 15, expectedSupras);
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
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void testSupras(Instances train, int testIndex,
			String[] expectedSupras) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, InterruptedException, ExecutionException {
		final Instance test = train.get(testIndex);
		train.remove(testIndex);

		// test with the contrived full splitting labeler as well as with a
		// normal one
		Labeler labeler = new IntLabeler(MissingDataCompare.VARIABLE, test,
				false);
		SubcontextAggregator subList = new SubcontextAggregator(labeler, train);
		subList.finish();
		Lattice testLattice = new DistributedLattice(subList);
		List<Supracontext> actualSupras = testLattice.getSupracontextList();

		assertEquals(expectedSupras.length, actualSupras.size());
		for (String expected : expectedSupras) {
			ClassifiedSupra supra = TestUtils.getSupraFromString(expected,
					train);
			TestUtils.assertContainsSupra(actualSupras, supra);
		}
	}
}
