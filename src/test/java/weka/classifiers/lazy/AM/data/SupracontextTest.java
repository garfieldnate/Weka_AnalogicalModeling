package weka.classifiers.lazy.AM.data;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import weka.classifiers.lazy.AM.TestUtils;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.classifiers.lazy.AM.lattice.LinkedLatticeNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class SupracontextTest {
    @Parameter()
    public String testName;
    @Parameter(1)
    public SupraFactory supraFactory;
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private interface SupraFactory {
        Supracontext getSupra();
    }

    /**
     * @return A collection of parameter arrays for running tests: <ol> <li>arg[0] is the test name;</li> <li>arg[1] is
     * a factory for creating a supracontext of a specific implementation to be tested.</li> </ol>
     */
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> instancesToTest() {
		return new ArrayList<>() {
			{
				add(new Object[]{BasicSupra.class.getSimpleName(), (SupraFactory) BasicSupra::new});
				add(new Object[]{ClassifiedSupra.class.getSimpleName(), (SupraFactory) ClassifiedSupra::new});
				add(new Object[]{
						LinkedLatticeNode.class.getSimpleName(),
						(SupraFactory) () -> new LinkedLatticeNode<>(new BasicSupra())
				});
				add(new Object[]{
						Concept.class.getSimpleName(), (SupraFactory) () -> new Concept<>(null, new BasicSupra())
				});
			}
		};
    }

    @Test
    public void testCount() {
        Supracontext testSupra = supraFactory.getSupra();
        assertEquals(testSupra.getCount(), BigInteger.ONE);
        testSupra.setCount(BigInteger.valueOf(42));
        assertEquals(testSupra.getCount(), BigInteger.valueOf(42));
    }

    @Test
    public void testSetCountThrowsErrorWhenArgIsNull() {
        Supracontext testSupra = supraFactory.getSupra();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("count must not be null"));
        testSupra.setCount(null);
    }

	@Test
	public void testDefaultGetContext() {
		assumeThat("Concept determines context differently", supraFactory.getSupra(), not(instanceOf(Concept.class)));
		Supracontext testSupra = supraFactory.getSupra();
		for (int bits : List.of(0b01010, 0b01010, 0b10010, 0b11000)) {
			testSupra.add(new Subcontext(new IntLabel(bits, 5), "foo"));
		}
		assertEquals("Label should be intersect of subcontext labels", new IntLabel(0b11010, 5), testSupra.getContext());

    	testSupra.add(new Subcontext(new IntLabel(0b01001, 5), "foo"));
    	assertEquals("New context should be intersected with previous one", new IntLabel(0b11011, 5), testSupra.getContext());
	}

    @Test
    public void testSetCountThrowsErrorWhenArgIsLessThanZero() {
        Supracontext testSupra = supraFactory.getSupra();
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(new StringContains("count must not be less than zero"));
        testSupra.setCount(BigInteger.valueOf(-1));
    }

    @Test
    public void testIsEmpty() {
        Supracontext testSupra = supraFactory.getSupra();
        assertTrue(testSupra.isEmpty());
        testSupra.add(new Subcontext(new IntLabel(0b0, 1), "foo"));
        assertFalse(testSupra.isEmpty());
    }

    @Test
    public void testData() {
        Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1), "foo");
        Subcontext sub2 = new Subcontext(new IntLabel(0b0, 2), "foo");

        Supracontext testSupra = supraFactory.getSupra();

        testSupra.add(sub1);
        testSupra.add(sub2);
        assertTrue(testSupra.getData().contains(sub1));
        assertTrue(testSupra.getData().contains(sub2));
    }

    @Test
    public void testCopy() {
        Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1), "foo");
        Subcontext sub2 = new Subcontext(new IntLabel(0b0, 2), "foo");

        Supracontext testSupra1 = supraFactory.getSupra();

        testSupra1.add(sub1);
        testSupra1.add(sub2);

        Supracontext testSupra2 = testSupra1.copy();
        assertEquals(testSupra1.getClass(), testSupra2.getClass());
        TestUtils.supraDeepEquals(testSupra1, testSupra2);
		assertNotSame(testSupra1, testSupra2);
    }

    @Test
    public void testEqualsAndHashCode() {
        // equals and hash code work different for Concept
        if (testName.equals(Concept.class.getSimpleName())) return;
        Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1), "foo");
        Subcontext sub2 = new Subcontext(new IntLabel(0b0, 2), "foo");

        Supracontext testSupra1 = supraFactory.getSupra();
        Supracontext testSupra2 = supraFactory.getSupra();

        testSupra1.add(sub1);
        testSupra1.add(sub2);
		assertNotEquals(testSupra1, testSupra2);
		assertNotEquals(testSupra1.hashCode(), testSupra2.hashCode());

        testSupra2.add(sub1);
        testSupra2.add(sub2);
		assertEquals(testSupra1, testSupra2);
		assertEquals(testSupra1.hashCode(), testSupra2.hashCode());

        testSupra1.setCount(BigInteger.valueOf(29));
		assertEquals("count is not compared for equality", testSupra1, testSupra2);
		assertEquals("count does not affect hashCode", testSupra1.hashCode(), testSupra2.hashCode());
    }
}
