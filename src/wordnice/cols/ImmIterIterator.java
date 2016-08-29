package wordnice.cols;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ImmIterIterator<T> implements Iterator<T> {
	
	/**
	 * Current index
	 */
	public int i = 0;
	
	/**
	 * Size
	 */
	public int size = 0;
	
	/**
	 * Iterator
	 */
	public Iterator<T> it = null;
	
	public ImmIterIterator(Iterator<T> it, int size) {
		this.it = it;
		this.size = size;
		this.i = 0;
	}
	
	public ImmIterIterator(Iterator<T> it, int size, int curi) {
		this.it = it;
		this.size = size;
		this.i = curi;
	}
	
	@Override
	public boolean hasNext() {
		return (this.i != this.size) && this.it.hasNext();
	}

	@Override
	public T next() {
		if(!this.hasNext()) {
			throw new NoSuchElementException();
		}
		this.i++;
		return this.it.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Immutable iterator!");
	}
	
}