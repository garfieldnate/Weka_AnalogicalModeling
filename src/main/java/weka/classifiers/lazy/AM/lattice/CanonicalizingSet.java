package weka.classifiers.lazy.AM.lattice;


import java.util.*;
import java.util.function.BiFunction;

/**
 * A set implementation that can be used to retrieve canonical versions of objects; this is not
 * possible with {@link Set} because of the lack of a {@link #get(Object) get} method.
 */
public class CanonicalizingSet<T> implements Set<T> {
	@SuppressWarnings("rawtypes") // set is empty and immutable
	private static final CanonicalizingSet EMPTY_SET = new CanonicalizingSet<>(Collections.EMPTY_MAP);
	private final Map<T, T> backingMap;

	@SuppressWarnings({"unchecked", "rawtypes"}) // invoked under contolled circumstances
	private CanonicalizingSet(Map emptyMap) {
		backingMap = emptyMap;
	}

	@SuppressWarnings("unchecked") // set is empty and immutable
	public static <T> CanonicalizingSet<T> emptySet() {
		return EMPTY_SET;
	}

	public CanonicalizingSet() {
		backingMap = new HashMap<>();
	}

	/**
	 * @return null if {@code t} is not contained in the set; otherwise the object contained in the set for which
	 * {@code t.equals(theObject} is true.
	 */
	public T get(T t) {
		return backingMap.get(t);
	}

	/**
	 * Similar to {@link Map#merge(Object, Object, BiFunction)}, but with the key and value being the same.
	 */
	public void merge(T t, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
		backingMap.merge(t, t, remappingFunction);
	}

	/**
	 * @return the underlying set
	 */
	public Set<T> unwrap() {
		return backingMap.keySet();
	}

	@Override
	public int size() {
		return backingMap.size();
	}

	@Override
	public boolean isEmpty() {
		return backingMap.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return backingMap.containsKey(o);
	}

	@Override
	public Iterator<T> iterator() {
		return backingMap.keySet().iterator();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(T t) {
		backingMap.put(t, t);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
