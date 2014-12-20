package weka.classifiers.lazy.AM.lattice;

/**
 * This node is used to create circular linked lists while filling a lattice.
 * 
 * @author Nathan Glenn
 * 
 * @param <T>
 *            This should be another type that implements LatticeNode so that a
 *            linked list can be created.
 */
public interface LatticeNode<T extends LatticeNode<T>> {
	/**
	 * @return the next object linked to by this node
	 */
	public T getNext();

	/**
	 * Set the next object linked to by this node
	 * 
	 * @param next
	 *            the object to link to
	 */
	public void setNext(T next);

	/**
	 * @return The index of this node. The index of a node should be -1 by
	 *         default unless set in a constructor, and it should never change
	 *         after object construction.
	 */
	// should be -1 if not set in a constructor or something
	public int getIndex();
}
