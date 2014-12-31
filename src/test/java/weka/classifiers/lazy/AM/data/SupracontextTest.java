package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;

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

	// TODO: isEmpty(), data(), add(), and copy()
	// TODO: equals() and hashCode()
}
