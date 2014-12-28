package weka.classifiers.lazy.AM.lattice;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.SubcontextList;
import weka.classifiers.lazy.AM.data.Supracontext;
import weka.classifiers.lazy.AM.label.Label;

public class SparseLattice implements Lattice {
	// List<Concept> lattice = new ArrayList<>();

	public SparseLattice(SubcontextList subList) {
		Concept bottom = new Concept(subList.getLabeler().getMinimum());
		// lattice.add(bottom);
		for (Subcontext sub : subList) {
			addIntent(sub, sub.getLabel(), bottom);
		}
		dumpLattice("lattice", bottom);
	}

	// TODO: next finish this, making it a dot graph. Then figure out how to
	// save subs in concepts properly.
	private static void dumpLattice(String graphName, Concept bottom) {
		System.out.println("digraph " + graphName + " {");
		Set<Concept> visited = new HashSet<>();
		Queue<Concept> queue = new LinkedList<>();
		queue.add(bottom);
		while (queue.size() != 0) {
			Concept current = queue.poll();
			if (visited.contains(current))
				continue;
			visited.add(current);

			System.out.println(current.hashCode() + " [label=\""
					+ current.getIntent() + ":" + current.getExtent() + "\"];");
			for (Concept parent : current.getParents())
				System.out.println(current.hashCode() + " -> "
						+ parent.hashCode() + ";");
			queue.addAll(current.getParents());
		}
		System.out.println("}");
	}

	Concept addIntent(Subcontext sub, Label intent, Concept inputConcept) {
		Concept generatorConcept = getMaximalConcept(intent, inputConcept);
		if (generatorConcept.getIntent().equals(intent)) {
			// concept with given label is already present, so just add the sub
			generatorConcept.combineExtent(inputConcept);
			// markIfHetero(generatorConcept);
			return generatorConcept;
		}
		Set<Concept> newParents = new HashSet<>();
		for (Concept candidate : generatorConcept.getParents()) {
			if (!candidate.getIntent().isAncestorOf(intent))
				// this possible parent turned out not to be a parent, so
				// generate a parent by intersecting it with the new label, and
				// save the new parent
				candidate = addIntent(sub,
						intent.intersect(candidate.getIntent()), candidate);
			boolean addParent = true;
			for (Concept parent : newParents) {
				if (parent.getIntent().isAncestorOf(candidate.getIntent())) {
					addParent = false;
					break;
				} else if (candidate.getIntent().isAncestorOf(
						parent.getIntent()))
					newParents.remove(parent);// assert(newParents.contains(parent))
												// may be instructive here
			}
			if (addParent)
				newParents.add(candidate);
		}
		Concept newConcept = new Concept(intent, generatorConcept.getExtent());
		// Lattice.add(newConcept);
		for (Concept parent : newParents) {
			generatorConcept.removeParent(parent);
			newConcept.addParent(parent);
		}
		generatorConcept.addParent(newConcept);
		return newConcept;
	}

	Concept getMaximalConcept(Label intent, Concept generatorConcept) {
		boolean parentIsMaximal = true;
		while (parentIsMaximal) {
			parentIsMaximal = false;
			for (Concept parent : generatorConcept.getParents()) {
				if (parent.getIntent().isAncestorOf(intent)) {
					generatorConcept = parent;
					parentIsMaximal = true;
					break;
				}
			}
		}
		return generatorConcept;
	}

	@Override
	public Set<Supracontext> getSupracontexts() {
		// TODO how should we get the supracontexts?
		return null;
	}

	private class Concept {
		Set<Subcontext> extent;
		Label intent;
		Set<Concept> parents;

		// TODO: track heterogeneity
		// boolean isHetero;
		// double outcome;

		public Concept(Label intent) {
			this.intent = intent;
			extent = new HashSet<Subcontext>();
			parents = new HashSet<Concept>();
		}

		public Concept(Subcontext sub) {
			intent = sub.getLabel();
			extent = new HashSet<Subcontext>();
			extent.add(sub);
			parents = new HashSet<Concept>();
		}

		public Concept(Label intent, Set<Subcontext> extent) {
			this.intent = intent;
			this.extent = new HashSet<>(extent);
			parents = new HashSet<Concept>();
		}

		public Set<Subcontext> getExtent() {
			return Collections.unmodifiableSet(extent);
		}

		public void addToExtent(Subcontext sub) {
			assert (!extent.contains(sub));
			extent.add(sub);
			// markIfHetero
		}

		public void combineExtent(Concept other) {
			extent.addAll(other.extent);
			// markIfHetero
		}

		// public int getOutcome(){}

		public Label getIntent() {
			return intent;
		}

		public Set<Concept> getParents() {
			return Collections.unmodifiableSet(parents);
		}

		public void addParent(Concept newParent) {
			parents.add(newParent);
		}

		public void removeParent(Concept oldParent) {
			assert (parents.contains(oldParent));
			parents.remove(oldParent);
		}

		// public boolean isHeterogeneous() {
		// return isHetero;
		// }

		@Override
		public String toString() {
			return intent + "(" + extent + ")->[" + parents + "]";
		}
	}

}
