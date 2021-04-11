package weka.classifiers.lazy.AM.data;

import org.junit.Test;
import weka.classifiers.lazy.AM.label.IntLabel;
import weka.core.Instances;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;
import static weka.classifiers.lazy.AM.TestUtils.sixCardinalityData;

public class GangEffectTest {
	@Test
	public void testConstructor() {
		Instances dataset = sixCardinalityData();
		Subcontext sub = new Subcontext(new IntLabel(0b0101,4), "foo");
		IntStream.range(0,6).forEach(i -> sub.add(dataset.get(i)));
		GangEffect effect = new GangEffect(sub,
				Map.of(
						dataset.get(0), BigInteger.ONE,
						dataset.get(1), BigInteger.TWO,
						dataset.get(2), BigInteger.valueOf(3),
						dataset.get(3), BigInteger.valueOf(4),
						dataset.get(4), BigInteger.valueOf(5),
						dataset.get(5), BigInteger.valueOf(6)));

		assertEquals(sub, effect.getSubcontext());
		assertEquals(Map.of("e", Set.of(dataset.get(1),dataset.get(3),dataset.get(5)), "r", Set.of(dataset.get(0), dataset.get(2), dataset.get(4))), effect.getClassToInstances());
		assertEquals(Map.of("e", BigInteger.valueOf(12), "r", BigInteger.valueOf(9)), effect.getClassToPointers());
		assertEquals(BigInteger.valueOf(21), effect.getTotalPointers());
	}
}
