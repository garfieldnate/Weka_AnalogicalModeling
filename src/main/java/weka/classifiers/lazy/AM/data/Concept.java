package weka.classifiers.lazy.AM.data;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import weka.classifiers.lazy.AM.label.Label;

/**
 * This class is a decorator which wraps a {@link Supracontext} and adds the
 * functionality of a node (concept) used in the AddIntent algorithm.
 * 
 * @author Nathan Glenn
 * 
 * @param <T>
 *            The implementation of Supracontext to be stored in this node.
 */
// TODO: did this really need to be genericized? Won't you always use a
// classifiedSupra?
public class Concept<T extends Supracontext> implements Supracontext {
	// the wrapped supracontext
	T extent;
	Label intent;
	Set<Concept<T>> parents;

	public Concept(Label intent, T extent) {
		this.intent = intent;
		this.extent = extent;
		parents = new HashSet<>();
	}

	public Set<Subcontext> getExtent() {
		return extent.getData();
	}

	public T getSupra() {
		return extent;
	}

	/**
	 * Add subcontexts to the extent of this concept as well as to all of its
	 * ancestors.
	 * 
	 * @param newSubs
	 */
	public void addToExtent(Set<Subcontext> newSubs) {
		// TODO: not needed. Is anything needed?
		// for (Subcontext sub : newSubs)
		// assert (!extent.contains(sub));
		for (Subcontext sub : newSubs)
			extent.add(sub);
		for (Concept<T> parent : getParents())
			parent.addToExtent(newSubs);
	}

	public Label getIntent() {
		return intent;
	}

	public Set<Concept<T>> getParents() {
		return Collections.unmodifiableSet(parents);
	}

	public void addParent(Concept<T> newParent) {
		parents.add(newParent);
	}

	public void removeParent(Concept<T> oldParent) {
		// TODO: this assert fails, but nothing seems to actually be wrong.
		// assert (parents.contains(oldParent));
		parents.remove(oldParent);
	}

	@Override
	public String toString() {
		return intent + "(" + extent + ")->[" + parents + "]";
	}

	@Override
	public Supracontext copy() {
		// copy() contract states it must return same type as calling object, so
		// unchecked cast is okay
		@SuppressWarnings("unchecked")
		T newSupra = (T) extent.copy();
		Concept<T> newNode = new Concept<T>(intent, newSupra);
		newNode.parents = new HashSet<>(parents);
		return newNode;
	}

	// the following methods forward to the contained Supracontext

	@Override
	public Set<Subcontext> getData() {
		return extent.getData();
	}

	@Override
	public void add(Subcontext sub) {
		extent.add(sub);
	}

	@Override
	public boolean isEmpty() {
		return extent.isEmpty();
	}

	@Override
	public BigInteger getCount() {
		return extent.getCount();
	}

	@Override
	public void setCount(BigInteger count) {
		extent.setCount(count);
	}

	@Override
	public boolean equals(Object other) {
		return extent.equals(other);
	}

	@Override
	public int hashCode() {
		return extent.hashCode();
	}
}
