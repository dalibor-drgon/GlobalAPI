package wordnice.coll;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ImmSkipArrayIterator<X> implements ListIterator<X> {

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
	 * Next element location
	 */
	public int every = 1;
	
	/**
	 * Current internal index
	 */
	public int index = 0;
	
	/**
	 * Create Immutable iterator for array
	 * 
	 * @param arr Array of data
	 * @param size Size of array
	 * @param start Where array starts
	 * @param every Next element location
	 * @param iterstart Normal index where iterator begin
	 */
	public ImmSkipArrayIterator(Object[] arr, int size, int start, int every, int iterstart) {
		this.arr = arr;
		this.start = start;
		this.every = every;
		this.recomputeSize(size);
		this.recomputeIndex(iterstart);
	}
	
	public void recomputeSize(int sz) {
		this.size = (sz - this.start);
		if((this.size % this.every) == 0) {
			this.size /= this.every;
		} else {
			this.size = (this.size / this.every) + 1;
		}
		this.maxi = sz;
	}
	
	public void recomputeIndex(int index) {
		if(index < 0 || index >= this.size) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		this.index = this.start + (this.every * index);
	}
	
	@Override
	public boolean hasNext() {
		return this.index < this.maxi;
	}
	
	@Override
	public boolean hasPrevious() {
		return (this.index - this.every) >= this.start;
	}

	@SuppressWarnings("unchecked")
	@Override
	public X next() {
		if(this.index >= this.maxi) {
			throw new NoSuchElementException();
		}
		X ret = (X) this.arr[this.index];
		this.index += this.every;
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public X previous() {
		if(this.index <= 0) {
			throw new NoSuchElementException();
		}
		this.index -= this.every;
		return (X) this.arr[this.index];
	}

	@Override
	public int nextIndex() {
		return (this.index - this.start) / this.every;
	}

	@Override
	public int previousIndex() {
		return (this.index - this.every - this.start) / this.every;
	}

	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Immutable iterator!");
	}

	@Override
	public void set(X e) {
		throw new UnsupportedOperationException("Immutable iterator!");
	}
	
	@Override
	public void add(X e) {
		throw new UnsupportedOperationException("Immutable iterator!");
	}

}
