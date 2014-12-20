package weka.classifiers.lazy.AM.data;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public class SupracontextTest {
	@Parameter(0)
	public String testName;
	@Parameter(1)
	public Constructor<Supracontext> supraConstructor;
	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * @return A collection of parameter arrays for running tests:
	 *         <ol>
	 *         <li>arg[0] is the test name;</li>
	 *         <li>arg[1] is the {@link Constructor} for a {@link Supracontext}
	 *         class to be tested.</li>
	 *         </ol>
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> instancesToTest() throws Exception {
		Collection<Object[]> parameters = new ArrayList<>();

		// There are two kinds of supracontexts
		@SuppressWarnings("serial")
		List<Class> supracontextClasses = new ArrayList<Class>() {
			{
				add(ClassifiedSupra.class);
				add(UnclassifiedSupra.class);
			}
		};
		for (Class c : supracontextClasses)
			parameters
					.add(new Object[] { c.getSimpleName(), c.getConstructor() });
		return parameters;
	}

	@Test
	public void testCount() throws Exception {
		Supracontext testSupra = supraConstructor.newInstance();
		assertEquals(testSupra.getCount(), BigInteger.ONE);
		testSupra.incrementCount();
		assertEquals(testSupra.getCount(), BigInteger.valueOf(2));
		testSupra.decrementCount();
		assertEquals(testSupra.getCount(), BigInteger.ONE);
		testSupra.setCount(BigInteger.valueOf(42));
		assertEquals(testSupra.getCount(), BigInteger.valueOf(42));
	}

	@Test
	public void testDecrementCountThrowsErrorWhenCountIsZero() throws Exception {
		Supracontext testSupra = supraConstructor.newInstance();
		testSupra.setCount(BigInteger.ZERO);
		exception.expect(IllegalStateException.class);
		exception.expectMessage(new StringContains(
				"Count cannot be less than zero"));
		testSupra.decrementCount();
	}

	@Test
	public void testSetCountThrowsErrorWhenArgIsNull() throws Exception {
		Supracontext testSupra = supraConstructor.newInstance();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(new StringContains("count must not be null"));
		testSupra.setCount(null);
	}

	@Test
	public void testSetCountThrowsErrorWhenArgIsLessThanZero() throws Exception {
		Supracontext testSupra = supraConstructor.newInstance();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(new StringContains(
				"count must not be less than zero"));
		testSupra.setCount(BigInteger.valueOf(-1));
	}

	// TODO: isEmpty(), data(); but there's no addData() in the superclass, so
	// this can't really be tested here yet.
	// TODO: equals() and hashCode()
}
