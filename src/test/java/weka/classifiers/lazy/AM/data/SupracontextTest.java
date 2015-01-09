package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

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

@RunWith(Parameterized.class)
public class SupracontextTest {
	@Parameter(0)
	public String testName;
	@Parameter(1)
	public SupraFactory supraFactory;
	@Rule
	public ExpectedException exception = ExpectedException.none();

	private interface SupraFactory {
		Supracontext getSupra();
	}

	/**
	 * @return A collection of parameter arrays for running tests:
	 *         <ol>
	 *         <li>arg[0] is the test name;</li>
	 *         <li>arg[1] is a factory for creating a supracontext of a specific
	 *         implementation to be tested.</li>
	 *         </ol>
	 * @throws Exception
	 */
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		@SuppressWarnings("serial")
		Collection<Object[]> parameters = new ArrayList<Object[]>() {
			{
				add(new Object[] { BasicSupra.class.getSimpleName(),
						new SupraFactory() {
							@Override
							public Supracontext getSupra() {
								return new BasicSupra();
							}
						} });
				add(new Object[] { ClassifiedSupra.class.getSimpleName(),
						new SupraFactory() {
							@Override
							public Supracontext getSupra() {
								return new ClassifiedSupra();
							}
						} });
				add(new Object[] { LinkedLatticeNode.class.getSimpleName(),
						new SupraFactory() {
							@Override
							public Supracontext getSupra() {
								return new LinkedLatticeNode<BasicSupra>(
										new BasicSupra());
							}
						} });
				add(new Object[] { Concept.class.getSimpleName(),
						new SupraFactory() {
							@Override
							public Supracontext getSupra() {
								return new Concept<BasicSupra>(null,
										new BasicSupra());
							}
						} });
			}
		};

		return parameters;
	}

	@Test
	public void testCount() throws Exception {
		Supracontext testSupra = supraFactory.getSupra();
		assertEquals(testSupra.getCount(), BigInteger.ONE);
		testSupra.setCount(BigInteger.valueOf(42));
		assertEquals(testSupra.getCount(), BigInteger.valueOf(42));
	}

	@Test
	public void testSetCountThrowsErrorWhenArgIsNull() throws Exception {
		Supracontext testSupra = supraFactory.getSupra();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(new StringContains("count must not be null"));
		testSupra.setCount(null);
	}

	@Test
	public void testSetCountThrowsErrorWhenArgIsLessThanZero() throws Exception {
		Supracontext testSupra = supraFactory.getSupra();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(new StringContains(
				"count must not be less than zero"));
		testSupra.setCount(BigInteger.valueOf(-1));
	}

	@Test
	public void testIsEmpty() {
		Supracontext testSupra = supraFactory.getSupra();
		assertTrue(testSupra.isEmpty());
		testSupra.add(new Subcontext(new IntLabel(0b0, 1)));
		assertFalse(testSupra.isEmpty());
	}

	@Test
	public void testData() {
		Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
		Subcontext sub2 = new Subcontext(new IntLabel(0b0, 2));

		Supracontext testSupra = supraFactory.getSupra();

		testSupra.add(sub1);
		testSupra.add(sub2);
		assertTrue(testSupra.getData().contains(sub1));
		assertTrue(testSupra.getData().contains(sub2));
	}

	@Test
	public void testCopy() {
		Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
		Subcontext sub2 = new Subcontext(new IntLabel(0b0, 2));

		Supracontext testSupra1 = supraFactory.getSupra();

		testSupra1.add(sub1);
		testSupra1.add(sub2);

		Supracontext testSupra2 = testSupra1.copy();
		assertEquals(testSupra1.getClass(), testSupra2.getClass());
		TestUtils.supraDeepEquals(testSupra1, testSupra2);
		assertFalse(testSupra1 == testSupra2);
	}

	@Test
	public void testEqualsAndHashCode() {
		// equals and hash code work different for Concept
		if (testName.equals(Concept.class.getSimpleName()))
			return;
		Subcontext sub1 = new Subcontext(new IntLabel(0b0, 1));
		Subcontext sub2 = new Subcontext(new IntLabel(0b0, 2));

		Supracontext testSupra1 = supraFactory.getSupra();
		Supracontext testSupra2 = supraFactory.getSupra();

		testSupra1.add(sub1);
		testSupra1.add(sub2);
		assertFalse(testSupra1.equals(testSupra2));
		assertFalse(testSupra1.hashCode() == testSupra2.hashCode());

		testSupra2.add(sub1);
		testSupra2.add(sub2);
		assertTrue(testSupra1.equals(testSupra2));
		assertTrue(testSupra1.hashCode() == testSupra2.hashCode());

		testSupra1.setCount(BigInteger.valueOf(29));
		assertTrue("count is not compared for equality",
				testSupra1.equals(testSupra2));
		assertTrue("count does not affect hashCode",
				testSupra1.hashCode() == testSupra2.hashCode());
	}
}
