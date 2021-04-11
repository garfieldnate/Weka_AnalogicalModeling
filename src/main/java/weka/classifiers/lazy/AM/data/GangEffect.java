package weka.classifiers.lazy.AM.data;

import lombok.Value;
import weka.core.Instance;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Exemplars that seem less similar to the test item than those that seem
 * more similar can still have a magnified effect if there are many of
 * them. This is known as the <em>gang effect</em>.
 *
 * <p/>This class represents the total effect that exemplars in one subcontext have
 * on the predicted outcome.
 */
@Value
public class GangEffect {
	Subcontext subcontext;
	/**
	 * Maps outcome to the exemplars supporting that outcome
	 */
	// TODO: list or SortedSet would be better, but Instance is not comparable :(
	Map<String, Set<Instance>> classToInstances;
	/**
	 * Maps each outcome to the total pointers for all exemplars supporting that outcome
	 */
	Map<String, BigInteger> classToPointers;
	/**
	 * Total pointers for all exemplars in the subcontext
	 */
	BigInteger totalPointers;

	GangEffect(Subcontext sub, Map<Instance, BigInteger> exemplarPointers) {
		subcontext = sub;
		classToInstances = sub.getExemplars().stream().
				collect(groupingBy(i -> i.stringValue(i.classIndex()), toSet()));
		classToPointers = classToInstances.entrySet().stream().
				collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> e.getValue().stream().map(exemplarPointers::get).
								reduce(BigInteger.ZERO, BigInteger::add)));
		totalPointers = classToPointers.values().stream().reduce(BigInteger.ZERO, BigInteger::add);
	}
}
