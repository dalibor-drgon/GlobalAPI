package eu.wordnice.cols;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

public class ImmEntryArrayIterator<X, Y> implements Iterator<Entry<X, Y>> {

	/**
	 * Array of entries {key1, value1, key2, value2 ...}
	 */
	public Object[] objs;
	
	/**
	 * Pair with
	 * Size divideable by 2
	 */
	public int size;
	
	/**
	 * Current index
	 */
	public int index = 0;
	
	/**
	 * Create Iterator for entries
	 * 
	 * @param objs array of entries
	 * @param size Number of entries divideable by 2
	 */
	public ImmEntryArrayIterator(Object[] objs, int size) {
		this.objs = objs;
		this.size = size;
		if((this.size & 0x01) == 0x01) {
			this.size--;
		}
		this.index = 0;
	}
	
	/**
	 * Create Iterator for entries
	 * 
	 * @param objs array of entries
	 * @param size Number of entries divideable by 2
	 * @param index Index where start
	 */
	public ImmEntryArrayIterator(Object[] objs, int size, int index) {
		this.objs = objs;
		this.size = size;
		if((this.size & 0x01) == 0x01) {
			this.size--;
		}
		if(index < 0 || index >= (this.size / 2)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		this.index = index;
	}
	
	@Override
	public boolean hasNext() {
		return this.index != (this.size / 2);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entry<X, Y> next() {
		if(this.index >= (this.size / 2)) {
			throw new NoSuchElementException();
		}
		Entry<X, Y> ent = new ImmEntry<X, Y>((X) this.objs[this.index * 2],
				(Y) this.objs[(this.index * 2) + 1]);
		this.index++;
		return ent;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Immutable map!");
	}
	
}