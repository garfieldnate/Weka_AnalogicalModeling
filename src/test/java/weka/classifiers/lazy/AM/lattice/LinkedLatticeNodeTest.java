package weka.classifiers.lazy.AM.lattice;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.data.BasicSupra;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.core.Instances;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Test all implementations of {@link LinkedLatticeNode} for correctness.
 *
 * @author Nathan Glenn
 */
@RunWith(Parameterized.class)
public class LinkedLatticeNodeTest {
    @Parameter()
    public String testName;
    @Parameter(1)
    public Constructor<Supracontext> supraConstructor;
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    // Note that we have to use unchecked/raw types throughout because it is not
    // known until runtime what the generic type of the LinkedLatticeNode will
    // be.

    /**
     * @return A collection of argument arrays for running tests. In each array: <ol> <li>arg[0] is the test name.</li>
     * <li>arg[1] is a {@link Supracontext} to be decorated by a LatticeLinkedNode for testing.</li> </ol>
     * @throws NoSuchMethodException if one of the SupraContext implementations doesn't have a 0-argument constructor
     */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws NoSuchMethodException {
		// Two Supracontext classes can be decorated
		Class<?>[] supraClasses = new Class[]{ClassifiedSupra.class, BasicSupra.class};
		Collection<Object[]> parameters = new ArrayList<>();
		for (Class<?> clazz : supraClasses) {
			parameters.add(new Object[]{
					clazz.getSimpleName(), clazz.getConstructor()
			});
		}
		return parameters;
	}

    @Test
    public void testDefaultIndexIsMinus1() throws Exception {
        @SuppressWarnings({"rawtypes", "unchecked"}) LinkedLatticeNode testNode = new LinkedLatticeNode(supraConstructor
                                                                                                            .newInstance());
        assertEquals(testNode.getIndex(), -1);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testNext() throws Exception {
        LinkedLatticeNode testNode = new LinkedLatticeNode(supraConstructor.newInstance());
		assertNull(testNode.getNext());
        testNode.setNext(testNode);
        assertEquals(testNode.getNext(), testNode);
    }

    @Test
    public void testCount() throws Exception {
        @SuppressWarnings({"rawtypes", "unchecked"}) LinkedLatticeNode testNode = new LinkedLatticeNode(supraConstructor
                                                                                                            .newInstance());
        assertEquals(BigInteger.ONE, testNode.getCount());
        testNode.incrementCount();
        assertEquals(testNode.getCount(), BigInteger.valueOf(2));
        testNode.decrementCount();
        assertEquals(testNode.getCount(), BigInteger.ONE);
    }

    @Test
    public void testDecrementCountThrowsErrorWhenCountIsZero() throws Exception {
        @SuppressWarnings({"rawtypes", "unchecked"}) LinkedLatticeNode testNode = new LinkedLatticeNode(supraConstructor
                                                                                                            .newInstance());
        testNode.setCount(BigInteger.ZERO);
        exception.expect(IllegalStateException.class);
        exception.expectMessage(new StringContains("Count cannot be less than zero"));
        testNode.decrementCount();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testInsertAfter() throws Exception {
        LinkedLatticeNode testNode1 = new LinkedLatticeNode(supraConstructor.newInstance());

        Instances dataset = TestUtils.getDataSet(TestUtils.FINNVERB_MIN);

        final Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
        sub1.add(dataset.get(0));
        final Subcontext sub2 = new Subcontext(new IntLabel(0b1, 1));
        sub2.add(dataset.get(1));
        sub2.add(dataset.get(2));
        final Subcontext sub3 = new Subcontext(new IntLabel(0b0, 1));

        LinkedLatticeNode testNode2 = testNode1.insertAfter(sub1, 11);

        Supracontext expected = new BasicSupra(new HashSet<>() {
			{
				add(sub1);
			}
		}, BigInteger.ONE);
        assertEquals(testNode2, expected);
        assertEquals(testNode2.getIndex(), 11);
		assertSame(testNode1.getNext(), testNode2);
		assertNull(testNode2.getNext());

        LinkedLatticeNode testNode3 = testNode2.insertAfter(sub2, 29);
        expected = new BasicSupra(new HashSet<>() {
			{
				add(sub1);
				add(sub2);
			}
		}, BigInteger.ZERO);
        assertEquals(testNode3, expected);
        assertEquals(testNode3.getIndex(), 29);
		assertSame(testNode2.getNext(), testNode3);
		assertNull(testNode3.getNext());

        LinkedLatticeNode testNode4 = testNode2.insertAfter(sub3, 37);
        expected = new BasicSupra(new HashSet<>() {
			{
				add(sub1);
				add(sub3);
			}
		}, BigInteger.ZERO);
        assertEquals(testNode4, expected);
		assertSame(testNode2.getNext(), testNode4);
		assertSame(testNode4.getNext(), testNode3);
    }
    // TODO: test copy, equals and hashCode for correctness regarding next
    // variable
}
