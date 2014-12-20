package weka.classifiers.lazy.AM.lattice;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.UnclassifiedSupra;

/**
 * Test all implementations of {@link LatticeNode} for correctness.
 * 
 * @author Nathan Glenn
 * 
 */
@RunWith(Parameterized.class)
public class LatticeNodeTest {
	@Parameter(0)
	public String testName;
	@Parameter(1)
	// note that we simply have to use raw types throughout
	// for this test to work
	@SuppressWarnings("rawtypes")
	public Constructor<LatticeNode> nodeConstructor;

	/**
	 * 
	 * @return A collection of argument arrays for running tests. In each array:
	 *         <ol>
	 *         <li>arg[0] is the test name.</li>
	 *         <li>arg[1] is the {@link Constructor} for the {@link LatticeNode}
	 *         to be tested.</li>
	 *         </ol>
	 * @throws Exception
	 */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		Collection<Object[]> parameters = new ArrayList<>();

		// Two Supracontext classes implement LatticeNode
		parameters.add(new Object[] { ClassifiedSupra.class.getSimpleName(),
				ClassifiedSupra.class.getConstructor() });
		parameters.add(new Object[] { UnclassifiedSupra.class.getSimpleName(),
				UnclassifiedSupra.class.getConstructor() });
		return parameters;
	}

	@Test
	public void testDefaultIndexIsMinus1() throws Exception {
		@SuppressWarnings("rawtypes")
		LatticeNode testNode = nodeConstructor.newInstance();
		assertEquals(testNode.getIndex(), -1);
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testNext() throws Exception {
		LatticeNode testNode1 = nodeConstructor.newInstance();
		assertEquals(testNode1.getNext(), null);
		testNode1.setNext(testNode1);
		assertEquals(testNode1.getNext(), testNode1);
	}
}
