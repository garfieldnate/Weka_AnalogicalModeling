package weka.classifiers.lazy.AM.lattice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.lazy.AM.data.ClassifiedSupra;
import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.Supracontext;

/**
 * This lattice combines multiple lattices into one large lattice.
 * 
 * @author Nathan Glenn
 * 
 */
public class CombiningLattice implements Lattice {

	private final List<Supracontext> finalSupras = new ArrayList<>();
	private Map<ClassifiedSupra, ClassifiedSupra> finalSuprasTemp;

	@Override
	public List<Supracontext> getSupracontextList() {
		return finalSupras;
	}

	public CombiningLattice(List<List<Supracontext>> partitionedSupras) {
		runPermutes(partitionedSupras);
	}

	/**
	 * We permute our way through all combinations of supracontexts contained in
	 * both lattices.
	 * 
	 * @param partitionedSupras
	 */
	private void runPermutes(List<List<Supracontext>> partitionedSupras) {
		finalSuprasTemp = new HashMap<ClassifiedSupra, ClassifiedSupra>();
		List<Supracontext> firstList = partitionedSupras.get(0);
		for (Supracontext supra : firstList) {
			// System.out.println("Running permute");
			permute(supra.getData(), supra.getCount(), partitionedSupras, 1);
		}
		finalSupras.addAll(finalSuprasTemp.values());
		finalSuprasTemp = null;
	}

	/**
	 * Recursively permutes lattice combinations.
	 * 
	 * @param subsSoFar
	 *            Intersection of the sucontexts found in every supracontext
	 *            combined so far
	 * @param count
	 *            Count of the currently combining supracontext, which is equal
	 *            to the product of the counts of each combined supracontext.
	 * @param supraLists
	 *            The list of supracontext lists that need to be combined
	 *            together
	 * @param currentIndex
	 *            The index of the supracontext list that we are currently
	 *            permuting.
	 */
	private void permute(Set<Subcontext> subsSoFar, BigInteger count,
			List<List<Supracontext>> supraLists, int currentIndex) {
		if (currentIndex == supraLists.size()) {
			addSupra(subsSoFar, count);
			return;
		}
		for (Supracontext supra : supraLists.get(currentIndex)) {
			Set<Subcontext> intersection = intersection(subsSoFar,
					supra.getData());
			if (!intersection.isEmpty())
				permute(intersection, count.multiply(supra.getCount()),
						supraLists, currentIndex + 1);
		}
	}

	/**
	 * 
	 * @return the intersection of the two sets of subcontexts
	 */
	private Set<Subcontext> intersection(Set<Subcontext> set1,
			Set<Subcontext> set2) {
		Set<Subcontext> larger, smaller;
		if (set1.size() > set2.size()) {
			larger = set1;
			smaller = set2;
		} else {
			larger = set2;
			smaller = set1;
		}
		Set<Subcontext> intersection = new HashSet<>();
		for (Subcontext s : smaller) {
			if (larger.contains(s))
				intersection.add(s);
		}
		return intersection;
	}

	/**
	 * Adds a supracontext to the final list if it is not heterogeneous.
	 * 
	 * @param subcontexts
	 *            The subcontexts to place in the supracontext
	 * @param count
	 *            The count of the supracontext.
	 */
	private void addSupra(Set<Subcontext> subcontexts, BigInteger count) {
		ClassifiedSupra supra = new ClassifiedSupra();
		for (Subcontext sub : subcontexts) {
			supra.add(sub);
			if (supra.isHeterogeneous())
				return;
		}
		supra.setCount(count);
		if (finalSuprasTemp.containsKey(supra)) {
			ClassifiedSupra existing = finalSuprasTemp.get(supra);
			existing.setCount(existing.getCount().add(supra.getCount()));
			// System.err.println("Adding new supra: " + existing);
		} else {
			// System.err.println("Adding new supra: " + supra);
			finalSuprasTemp.put(supra, supra);
		}
	}
}
