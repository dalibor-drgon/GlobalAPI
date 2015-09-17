package eu.wordnice.cols;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ImmArrayIterator<T> implements ListIterator<T> {
	
	/**
	 * Current index
	 */
	public int i = 0;
	
	/**
	 * Array
	 */
	public Object[] arr;
	
	/**
	 * Array size
	 */
	public int size = 0;
	
	/**
	 * Create iterator
	 */
	protected ImmArrayIterator(Object[] arr, int size) {
		if(i < 0 || i >= size) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		this.arr = arr;
		this.i = 0;
		this.size = size;
	}
	
	/**
	 * Create iterator with current index
	 */
	protected ImmArrayIterator(Object[] arr, int size, int i) {
		if(i < 0 || i >= size) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		this.arr = arr;
		this.i = i;
		this.size = size;
	}

	
	@Override
	public boolean hasNext() {
		return (this.i != this.size);
	}
	
	@Override
	public boolean hasPrevious() {
		return (this.i != 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if(this.i >= this.size) {
			throw new NoSuchElementException();
		}
		T ret = (T) this.arr[this.i];
		this.i++;
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T previous() {
		if(this.i <= 0) {
			throw new NoSuchElementException();
		}
		this.i--;
		return (T) this.arr[this.i];
	}

	@Override
	public int nextIndex() {
		return this.i;
	}

	@Override
	public int previousIndex() {
		return this.i - 1;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public void add(T arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}
	
	@Override
	public void set(T arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}
	
}