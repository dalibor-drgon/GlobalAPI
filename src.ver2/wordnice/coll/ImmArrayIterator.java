package wordnice.coll;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ImmArrayIterator<T> implements ListIterator<T> {
	
	/**
	 * Current index
	 * If 0 - !hasPrevious, hasNext == size>0
	 * If size - !hasNext, hasPrevious == size>0
	 */
	public int cursor = 0;
	
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
		this(arr, size, 0);
	}
	
	/**
	 * Create iterator with current index
	 */
	protected ImmArrayIterator(Object[] arr, int size, int i) {
		if(i < 0 || i > size) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		this.arr = arr;
		this.cursor = i;
		this.size = size;
	}

	
	@Override
	public boolean hasNext() {
		return this.cursor != this.size;
	}
	
	@Override
	public boolean hasPrevious() {
		return this.cursor != 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if(this.cursor >= this.size) {
			throw new NoSuchElementException();
		}
		T ret = (T) this.arr[this.cursor];
		this.cursor++;
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T previous() {
		if(this.cursor <= 0) {
			throw new NoSuchElementException();
		}
		this.cursor--;
		return (T) this.arr[this.cursor];
	}

	@Override
	public int nextIndex() {
		return this.cursor;
	}

	@Override
	public int previousIndex() {
		return this.cursor - 1;
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
	
	/*public static void main(String...strings) {
		Object[] arr = new Object[] {"One", "Two", "Three", "Oh thats four!"};
		//arr = new Object[]{};
		//arr = new Object[]{"One"};
		ImmArrayIterator<String> it = new ImmArrayIterator<String>(arr, arr.length, arr.length);
		while(it.hasNext()) {
			System.out.println("+ Next: " + it.next());
		}
		while(it.hasPrevious()) {
			System.out.println("+ Previous: " + it.previous());
		}
		while(it.hasNext()) {
			System.out.println("+ Next: " + it.next());
		}
		while(it.hasPrevious()) {
			System.out.println("+ Previous: " + it.previous());
		}
		while(it.hasNext()) {
			System.out.println("+ Next: " + it.next());
		}
	}*/
	
}