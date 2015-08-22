package weka.classifiers.lazy.AM.lattice;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import weka.classifiers.lazy.AM.AMUtils;
import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;

/**
 * The approximation algorithm from "Efficient Modeling of Analogy", Johnsen and
 * Johansson, DOI 10.1007/978-3-540-30586-6_77.
 * 
 * Terminology from the paper is as follows:
 * 
 * <ul>
 * <li>$$p$$: the subcontext whose count is being approximated</li>
 * <li>$$size(p)$$: the size of the subcontext $$p$$; or, the number of 0's in
 * its label</li>
 * <li>$$\mathcal{H}(p)$$: the sets found by intersecting $$p$$ with any
 * subcontext that has a different outcome; the labels of such intersections</li>
 * <li>$$max(p)$$: the cardinality of the union of all $$x\in\mathcal{H}(p)$$;
 * the number of 0's in the union of the labels of all subcontexts in
 * $$\mathcal{H}(p)$$</li>
 * <li>$$\mathcal{H}_{limit(p)}$$: the heterogeneous elements under $$p$$ in the
 * lattice</li>
 * <li>$$min(p)$$: the size of the largest child of $$p$$; or, the number of 0's
 * in the label of the subcontext whose label has the most 0's and matches all
 * of the 1's in $$p$$'s label.</li>
 * 
 * </ul>
 * 
 * We estimate the count of each subcontext by randomly unioning sets of
 * subcontexts from $$\{x_s\}$$ and checking for heterogeneity (union means
 * ORing labels). The count of a subcontext $$p$$ is the size of its power set
 * minus the heterogeneous elements in this set (or $$|\wp(p)| -
 * |\mathcal{H}_{limit(p)}|$$). We use these bounds in approximating
 * $$|\mathcal{H}_{limit(p)}|$$:
 * 
 * <ul>
 * <li>lower bound ($$lb(p)$$): the cardinality of the powerset of $$min(p)$$.</li>
 * <li>upper bound ($$ub(p)$$): $$\sum_{k=1}^{min(p)}{max(p)\choose k}$$</li>
 * </ul>
 * 
 * The estimate $$\hat{h}_p$$ of $$|\mathcal{H}_{limit(p)}|$$ is computed by
 * sampling random sets of subcontexts $${x_s}$$ and combining them with :
 * 
 * $$\frac{|\{x_s \in \mathcal{H}(p)|}{|\{x_s\}|}=\frac{\hat{h}_p}{ub(p)}$$
 * 
 * or
 * 
 * $$\hat{h}_p = \frac{ub(p)|x_s\in \mathcal{H}(p)|}{|\{x_s\}|}$$
 * 
 * TODO: maybe if H(p) is small enough we can do exact counting with
 * include-exclude
 * 
 * @author Nate
 * 
 */
public class JohnsenJohanssonLattice implements Lattice {

	Set<Supracontext> supras = new HashSet<>();

	public JohnsenJohanssonLattice(SubcontextList sublist) {
		// first organize sub labels by outcome for quick H(p) construction
		Map<Double, List<Label>> outcomeSubMap = new HashMap<>();
		for (Subcontext s : sublist) {
			List<Label> l = outcomeSubMap.get(s.getOutcome());
			if (null == l) {
				l = new ArrayList<Label>();
				outcomeSubMap.put(s.getOutcome(), l);
			}
			l.add(s.getLabel());
		}
		// for each p in subcontext
		for (Subcontext p : sublist) {
			Label pLabel = p.getLabel();

			// H(p) is p intersected with labels of any subcontexts with a
			// different
			// class, or all other sub labels if p is non-deterministic
			// (combination with these would lead to heterogeneity)
			List<Label> hp = new ArrayList<>();
			for (Entry<Double, List<Label>> e : outcomeSubMap.entrySet()) {
				if (p.getOutcome() != e.getKey()
						|| p.getOutcome() == AMUtils.HETEROGENEOUS) {
					for (Label x : e.getValue())
						hp.add(pLabel.intersect(x));
				}
			}
			// min(p) is the number of matches in the label in H(p) with the
			// most matches
			// max(p) is the number of matches in the union of all labels in
			// H(p)
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
				ubP = ubP.add(binomialCoefficient(maxP, k));
			}
			// ratio of |{x_s in H(p)}| to |{x_s}|
			double heterRatio = estimateHeteroRatio(hp);
			// final estimation of total count of space subsumed by elements of
			// H(p); rounds down
			BigInteger heteroCountEstimate = new BigDecimal(ubP).multiply(
					new BigDecimal(heterRatio)).toBigInteger();
			// final count is 2^|p| - heteroCountEstimate
			BigInteger count = BigInteger.valueOf(2).pow(pLabel.numMatches());
			count = count.subtract(heteroCountEstimate);

			// add the approximated sub as its own supra with the given count
			Supracontext approximatedSupra = new ClassifiedSupra();
			approximatedSupra.add(p);
			approximatedSupra.setCount(count);
			supras.add(approximatedSupra);
		}
		return;
	}

	private double estimateHeteroRatio(List<Label> hp) {
		int totalCount = 0;
		int heteroCount = 0;

		// TODO: for now, just repeat experiment 1000 times
		for (int i = 0; i < 1000; i++) {
			totalCount++;
			// choose x_s, a union of random items from H(p)
			Label Xs = null;
			for (Label l : hp) {
				// TODO: factor out RNG
				if (Math.random() > .5) {
					if (Xs == null)
						Xs = l;
					else
						Xs = Xs.union(l);
				}
			}
			// x_s is hetero if it is a child of any element of H(p)
			for (Label l : hp) {
				// use union to discover ancestor relationship
				if (l.union(Xs).equals(l)) {
					heteroCount++;
					break;
				}
			}
		}
		return heteroCount / (double) totalCount;
	}

	// from http://stackoverflow.com/a/9620533/474819
	static BigInteger binomialCoefficient(int n, int k) {
		if (n == 0)
			return BigInteger.ONE;
		if (k == 0)
			return BigInteger.ZERO;
		// (n C k) and (n C (n-k)) are the same, so pick the smaller as k:
		if (k > n - k)
			k = n - k;
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
