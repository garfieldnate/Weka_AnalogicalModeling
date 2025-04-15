package weka.classifiers.lazy.AM.lattice;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The approximation algorithm from "Efficient Modeling of Analogy", Johnsen and
 * Johansson, DOI 10.1007/978-3-540-30586-6_77.
 *
 * Terminology from the paper is as follows:
 *
 * <ul>
 * <li>$p$: the subcontext whose count is being approximated</li>
 * <li>$size(p)$: the size of the subcontext $p$; or, the number of 0's in
 * its label</li>
 * <li>$\mathcal{H}(p)$: the sets found by intersecting $p$ with any
 * subcontext that has a different outcome; the labels of such intersections</li>
 * <li>$max(p)$: the cardinality of the union of all $x\in\mathcal{H}(p)$;
 * the number of 0's in the union of the labels of all subcontexts in
 * $\mathcal{H}(p)$</li>
 * <li>$\mathcal{H}_{limit(p)}$: the heterogeneous elements under $p$ in the
 * lattice</li>
 * <li>$min(p)$: the size of the largest child of $p$; or, the number of 0's
 * in the label of the subcontext whose label has the most 0's and matches all
 * of the 1's in $p$'s label.</li>
 *
 * </ul>
 *
 * We estimate the count of each subcontext by randomly unioning sets of
 * subcontexts from $\{x_s\}$ and checking for heterogeneity (union means
 * OR'ing labels). The count of a subcontext $p$ is the size of its power set
 * minus the heterogeneous elements in this set (or $|\wp(p)| -
 * |\mathcal{H}_{limit(p)}|$). We use these bounds in approximating
 * $|\mathcal{H}_{limit(p)}|$:
 *
 * <ul>
 * <li>lower bound ($lb(p)$): the cardinality of the powerset of $min(p)$.</li>
 * <li>upper bound ($ub(p)$): $\sum_{k=1}^{min(p)}{max(p)\choose k}$</li>
 * </ul>
 *
 * The estimate $\hat{h}_p$ of $|\mathcal{H}_{limit(p)}|$ is computed by
 * sampling random sets of subcontexts ${x_s}$ and combining them with :
 *
 * $\frac{|\{x_s \in \mathcal{H}(p)|}{|\{x_s\}|}=\frac{\hat{h}_p}{ub(p)}$
 *
 * or
 *
 * $\hat{h}_p = \frac{ub(p)|x_s\in \mathcal{H}(p)|}{|\{x_s\}|}$
 *
 * <br>
 *
 * TODO: maybe if H(p) is small enough we could do exact counting with
 * include-exclude
 *
 * @author Nate
 */
public class JohnsenJohanssonLattice implements Lattice {
    // TODO: should run until convergence, not a constant number of times
    private static final int NUM_EXPERIMENTS = 10;
    private final Set<Supracontext> supras = new HashSet<>();
    private static final BigInteger TWO = BigInteger.valueOf(2);
	private boolean filled;
	private Label bottom;
	private final Supplier<Random> randomProvider;

	/**
	 * @param randomProvider Provides randomness used for performing Monte Carlo simulation in child threads
	 */
	JohnsenJohanssonLattice(Supplier<Random> randomProvider) {
		this.randomProvider = randomProvider;
	}

	@Override
	public void fill(SubcontextList sublist) throws InterruptedException, ExecutionException {
		if (filled) {
			throw new IllegalStateException("Lattice is already filled and cannot be filled again.");
		}
		filled = true;
		bottom = sublist.getLabeler().getLatticeBottom();
        // first organize sub labels by outcome for quick H(p) construction
        Map<Double, List<Label>> outcomeSubMap = new HashMap<>();
        for (Subcontext s : sublist) {
            List<Label> l = outcomeSubMap.computeIfAbsent(s.getOutcome(), k -> new ArrayList<>());
            l.add(s.getLabel());
        }
        // Estimate the counts for each supracontext in parallel
        ExecutorService executor = Executors.newWorkStealingPool();
        CompletionService<Supracontext> taskCompletionService = new ExecutorCompletionService<>(executor);
        for (Subcontext p : sublist) {
            taskCompletionService.submit(new SupraApproximator(p, outcomeSubMap, randomProvider.get()));
        }
        for (int i = 0; i < sublist.size(); i++) {
            supras.add(taskCompletionService.take().get());
        }
        executor.shutdownNow();
    }

    class SupraApproximator implements Callable<Supracontext> {
        private final Subcontext p;
        private final Map<Double, List<Label>> outcomeSubMap;
		private final Random random;

		SupraApproximator(Subcontext p, Map<Double, List<Label>> outcomeSubMap, Random random) {
            this.p = p;
            this.outcomeSubMap = outcomeSubMap;
			this.random = random;
		}

        @Override
        public Supracontext call() {
            return approximateSupra(p, outcomeSubMap);
        }

		private Supracontext approximateSupra(Subcontext p, Map<Double, List<Label>> outcomeSubMap) {
			Label pLabel = p.getLabel();
			// H(p) is p intersected with labels of any subcontexts with a
			// different class, or all other sub labels if p is non-deterministic
			// (combination with these would lead to heterogeneity)
			List<Label> hp = new ArrayList<>();
			for (Entry<Double, List<Label>> e : outcomeSubMap.entrySet()) {
				if (p.getOutcome() != e.getKey() || p.getOutcome() == AMUtils.HETEROGENEOUS) {
					for (Label x : e.getValue())
						hp.add(pLabel.intersect(x));
				}
			}
			// min(p) is the number of matches in the label in H(p) with the most matches
			// max(p) is the number of matches in the union of all labels in H(p)
			int minP = 0;
			Label hpUnion = pLabel;
			for (Label l : hp) {
				if (l.numMatches() > minP) {
					minP = l.numMatches();
				}
				hpUnion = hpUnion.union(l);
			}
			int maxP = hpUnion.numMatches();
			// the upper bound on H_limit(p)
			BigInteger ubP = BigInteger.ZERO;
			for (int k = 1; k <= minP; k++) {
				ubP = ubP.add(memoizedNcK.apply(new Pair(maxP, k)));
			}
			// ratio of |{x_s in H(p)}| to |{x_s}|
			double heteroRatio = estimateHeteroRatio(hp, hpUnion, NUM_EXPERIMENTS);
			// final estimation of total count of space subsumed by elements of
			// H(p); rounds down
			BigInteger heteroCountEstimate = new BigDecimal(ubP).multiply(new BigDecimal(heteroRatio)).toBigInteger();
			// final count is 2^|p| - heteroCountEstimate
			BigInteger count = TWO.pow(pLabel.numMatches());
			count = count.subtract(heteroCountEstimate);

			// add the approximated sub as its own supra with the given count
			Supracontext approximatedSupra = new ClassifiedSupra();
			approximatedSupra.add(p);
			approximatedSupra.setCount(count);
			return approximatedSupra;
		}

		private double estimateHeteroRatio(List<Label> hp, Label hpUnion, int numExperiments) {
			int heteroCount = 0;

			Map<Label, Boolean> cache = new HashMap<>();
			for (int i = 0; i < numExperiments; i++) {
				// choose x_s, a union of random items from H(p)
				Label Xs = bottom;
				Collections.shuffle(hp);
				for (Label l : hp) {
					// cannot use Math.random() in parallel code
					if (this.random.nextDouble() > .5) {
						// further union operations would do nothing since we are supposed to compare against hpUnion
						Label unioned = Xs.union(l);
						if (unioned.equals(hpUnion))
							break;
						Xs = unioned;
					}
				}
				Boolean wasHetero = cache.get(Xs);
				if (wasHetero != null) {
					if (wasHetero) {
						heteroCount++;
					}
					continue;
				}
				// x_s is hetero if it is a child of any element of H(p)
				boolean hetero = false;
				for (Label l : hp) {
					// use union to discover ancestor relationship
					if (l.union(Xs).equals(l)) {
						heteroCount++;
						hetero = true;
						break;
					}
				}
				cache.put(Xs, hetero);
			}
			return heteroCount / (double) numExperiments;
		}
	}

    private static class Memoizer<T, U> {
        private final Map<T, U> cache = new ConcurrentHashMap<>();

        private Memoizer() {
        }

        private Function<T, U> doMemoize(final Function<T, U> function) {
            return input -> cache.computeIfAbsent(input, function);
        }

        public static <T, U> Function<T, U> memoize(final Function<T, U> function) {
            return new Memoizer<T, U>().doMemoize(function);
        }
    }

    private static class Pair {
        final int first;
        final int second;

        private Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int hashCode() {
            return 37 * first + second;
        }

        @Override
        public boolean equals(Object o) {
            Pair other = (Pair) o;
            return other.first == first && other.second == second;
        }
    }

    private static final Function<Pair, BigInteger> memoizedNcK = Memoizer.memoize(JohnsenJohanssonLattice::binomialCoefficient);

    // from http://stackoverflow.com/a/9620533/474819
    private static BigInteger binomialCoefficient(Pair p) {
        int n = p.first;
        int k = p.second;
        if (n == 0) return BigInteger.ONE;
        if (k == 0) return BigInteger.ZERO;
        // (n C k) and (n C (n-k)) are the same, so pick the smaller as k:
        if (k > n - k) {
            k = n - k;
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 1; i <= k; ++i) {
            result = result.multiply(BigInteger.valueOf(n - k + i));
            result = result.divide(BigInteger.valueOf(i));
        }
        return result;
    }

    @Override
    public Set<Supracontext> getSupracontexts() {
        return supras;
    }

}
