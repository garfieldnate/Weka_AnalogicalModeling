package weka.classifiers.lazy.AM.lattice;

import java.math.BigInteger;
import java.util.Set;

import weka.classifiers.lazy.AM.data.Subcontext;
import weka.classifiers.lazy.AM.data.Supracontext;

/**
 * This class is a decorator which wraps a {@link Supracontext} and adds the
 * functionality of a linked node used in certain lattice-filling algorithms. An
 * index is also provided for use in determining when the node was created.
 * 
 * @author Nathan Glenn
 * 
 * @param <T>
 *            The implementation of Supracontext to be stored in this node.
 */
public class LinkedLatticeNode<T extends Supracontext> implements Supracontext {
	// the wrapped supracontext
	T supra;
	// a number representing when this supracontext was created
	private final int index;
	// pointer to the next node; this is used during lattice filling to create a
	// circular linked list
	private LinkedLatticeNode<T> next;

	/**
	 * Create a new node containing the given supracontext. The index is set to
	 * -1.
	 * 
	 * @param supra
	 *            Supracontext to store in this node.
	 */
	public LinkedLatticeNode(T supra) {
		this.supra = supra;
		index = -1;
	}

	// used privately by insertAfter
	private LinkedLatticeNode(T supra, int ind) {
		this.supra = supra;
		index = ind;
	}

	/**
	 * Create a new node by copying this one, adding the given subcontext and
	 * setting the index to that provided. Insert the new node between this node
	 * and its next node by setting the new node to be the next node and seting
	 * the previous next node to be the new node's next node.
	 * 
	 * @param sub
	 *            Subcontext to insert into the copied Supracontext
	 * @param ind
	 *            index of new node
	 */
	public LinkedLatticeNode<T> insertAfter(Subcontext sub, int ind) {
		// it's okay to cast to T here because of the contract that
		// Supracontext.copy() return its own type
		@SuppressWarnings("unchecked")
		T newSupra = (T) getSupracontext().copy();
		newSupra.setCount(BigInteger.ONE);
		newSupra.add(sub);
		LinkedLatticeNode<T> newNode = new LinkedLatticeNode<T>(newSupra, ind);
		newNode.setNext(getNext());
		setNext(newNode);
		return newNode;
	}

	/**
	 * @return the next node linked to by this node
	 */
	public LinkedLatticeNode<T> getNext() {
		return next;
	}

	/**
	 * Set the next node linked to by this node
	 * 
	 * @param next
	 *            the node to link to
	 */
	public void setNext(LinkedLatticeNode<T> next) {
		this.next = next;
	}

	/**
	 * @return The index of this node.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Increases count by one.
	 */
	public void incrementCount() {
		supra.setCount(supra.getCount().add(BigInteger.ONE));
	}

	/**
	 * Decreases the count by one; if this reaches 0, then this Supracontext
	 * should be discarded (by the caller).
	 * 
	 * @throws IllegalStateException
	 *             if the count is already zero.
	 */
	public void decrementCount() {
		if (supra.getCount().equals(BigInteger.ZERO))
			throw new IllegalStateException("Count cannot be less than zero");
		supra.setCount(supra.getCount().subtract(BigInteger.ONE));
	}

	/**
	 * @return The supracontext contained in this node.
	 */
	public T getSupracontext() {
		return supra;
	}

	@Override
	public Supracontext copy() {
		@SuppressWarnings("unchecked")
		T newSupra = (T) getSupracontext().copy();
		LinkedLatticeNode<T> newNode = new LinkedLatticeNode<T>(newSupra, index);
		newNode.setNext(next);
		return newNode;
	}

	// Below methods are delegated to the contained supracontext

	@Override
	public void add(Subcontext sub) {
		supra.add(sub);
	}

	@Override
	public Set<Subcontext> getData() {
		return supra.getData();
	}

	@Override
	public boolean isEmpty() {
		return supra.isEmpty();
	}

	@Override
	public BigInteger getCount() {
		return supra.getCount();
	}

	@Override
	public void setCount(BigInteger count) {
		supra.setCount(count);
	}

	@Override
	public boolean equals(Object other) {
		return supra.equals(other);
	}

	@Override
	public int hashCode() {
		return supra.hashCode();
	}

	@Override
	public String toString() {
		return supra.toString();
	}
}
