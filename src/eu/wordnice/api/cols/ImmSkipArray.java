package eu.wordnice.api.cols;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Set;

public class ImmSkipArray<X> implements List<X>, Set<X>, RandomAccess {

	/**
	 * Array
	 * {key, val, key2, val2, ...}
	 */
	public Object[] arr;
	
	/**
	 * Max index
	 */
	public int maxi;
	
	/**
	 * size()
	 */
	public int size;
	
	/**
	 * Start from ? value from array
	 */
	public int start = 0;
	
	/**
	 * + ? to next value
	 */
	public int every = 1;
	
	/**
	 * Create Immutable list for array
	 * 
	 * @param arr Array of data
	 * @param size Size of array
	 * @param start Where array starts
	 * @param every Next element location
	 */
	public ImmSkipArray(Object[] arr, int size, int start, int every) {
		this.arr = arr;
		this.start = start;
		this.every = every;
		this.recomputeSize(size);
	}
	
	public void recomputeSize(int sz) {
		this.size = ((sz - this.start) / this.every);
		this.maxi = ((int) this.size * this.every) + this.start; 
	}
	
	@Override
	public boolean add(X arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean addAll(Collection<? extends X> arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean contains(Object key) {
		return this.indexOf(key) != -1;
	}

	@Override
	public boolean containsAll(Collection<?> col) {
		Iterator<?> it = col.iterator();
		while(it.hasNext()) {
			if(!this.contains(it.next())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.size() == 0;
	}

	@Override
	public Iterator<X> iterator() {
		return new ImmSkipArrayIterator<X>(this.arr, this.maxi, this.start, this.every, 0);
	}
	
	@Override
	public ListIterator<X> listIterator() {
		return new ImmSkipArrayIterator<X>(this.arr, this.maxi, this.start, this.every, 0);
	}

	@Override
	public ListIterator<X> listIterator(int index) {
		return new ImmSkipArrayIterator<X>(this.arr, this.maxi, this.start, this.every, index);
	}

	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public Object[] toArray() {
		Object[] arr = new Object[this.size()];
		for(int i = this.start, pi = 0; i < this.maxi; i += this.every, pi++) {
			arr[pi] = this.arr[i];
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arr) {
		int len = this.size();
		if(arr == null) {
			arr = (T[]) new Object[len];
		} else if(arr.length < len) {
			arr = (T[]) Array.newInstance(arr.getClass().getComponentType(), len);
		}
		for(int i = this.start, pi = 0; i < this.maxi; i += this.every, pi++) {
			arr[pi] = (T) this.arr[i];
		}
		return arr;
	}
	
	@Override
	public void add(int arg0, X arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends X> arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@SuppressWarnings("unchecked")
	@Override
	public X get(int i) {
		if(i < 0 || i >= this.size()) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		return (X) this.arr[this.start + (this.every * i)];
	}

	@Override
	public int indexOf(Object key) {
		if(key == null) {
			for(int i = this.start, pi = 0; i < this.maxi; i += this.every, pi++) {
				if(this.arr[i] == null) {
					return pi;
				}
			}
		} else {
			for(int i = this.start, pi = 0; i < this.maxi; i += this.every, pi++) {
				if(key.equals(this.arr[i])) {
					return pi;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object key) {
		if(key == null) {
			for(int i = this.maxi, pi = (this.size - 1); i <= this.start; i -= this.every, pi--) {
				if(this.arr[i] == null) {
					return pi;
				}
			}
		} else {
			for(int i = this.maxi, pi = (this.size - 1); i <= this.start; i -= this.every, pi--) {
				if(key.equals(this.arr[i])) {
					return pi;
				}
			}
		}
		return -1;
	}

	@Override
	public X remove(int index) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public X set(int index, X element) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public List<X> subList(int fromIndex, int toIndex) {
		if(fromIndex < 0 || fromIndex >= this.size()) {
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if(toIndex < 0 || toIndex >= this.size()) {
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}
		if(fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex > toIndex");
		}
		return new ImmSkipArray<X>(this.arr, this.start + (this.every * toIndex), (this.start + (this.every * fromIndex)), this.every);
	}
	
	@Override
	public String toString() {
		if(this.maxi < this.start) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = this.start; i < this.maxi; i += this.every) {
			Object key = this.arr[i];
			if(i != this.start) {
				sb.append(',').append(' ');
			}
			sb.append(key);
		}
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof Iterable) {
			if(obj instanceof Collection) {
				if(this.size != ((Collection<?>) obj).size()) {
					return false;
				}
			}
			Iterator<?> it = ((Iterable<?>) obj).iterator();
			int i = 0;
			while(it.hasNext()) {
				if(i >= this.size) {
					return false;
				}
				Object cur = it.next();
				Object tcur = this.arr[i++];
				if((cur == null) ? tcur != null : !cur.equals(tcur)) {
					return false;
				}
			}
			return (i == this.size);
		}
		return false;
	}
	
}
