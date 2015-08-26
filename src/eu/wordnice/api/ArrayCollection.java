/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.wordnice.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayCollection<T> implements Collection<T> {
	
	/**
	 * Array to iterate
	 */
	private T[] arr;
	
	/**
	 * Create immutable iterable
	 * @param arr Array to iterate
	 */
	public ArrayCollection(T[] arr) {
		this.arr = arr;
	}

	/**
	 * Create simple immutable iterator
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new SimpleIterator();
	}
	
	class SimpleIterator implements Iterator<T> {
		
		/**
		 * Current index
		 */
		private int i = -1;
		
		/**
		 * Create iterator
		 */
		protected SimpleIterator() {}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			int index = this.i + 1;
			if(index < ArrayCollection.this.arr.length) {
				this.i++;
				return true;
			}
			return false;
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			int index = this.i;
			if(index == -1) {
				index = 0;
			}
			if(index >= ArrayCollection.this.arr.length) {
				throw new NoSuchElementException();
			}
			return ArrayCollection.this.arr[index];
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Immutable collection!");
		}
		
	}

	@Override
	public int size() {
		return this.arr.length;
	}

	@Override
	public boolean isEmpty() {
		return (this.arr.length == 0);
	}

	@Override
	public boolean contains(Object o) {
		if(o == null) {
			for(int i = 0, n = this.arr.length; i < n; i++) {
				if(this.arr[i] == null) {
					return true;
				}
			}
		} else {
			for(int i = 0, n = this.arr.length; i < n; i++) {
				if(o.equals(this.arr[i])) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object[] toArray() {
		return Arrays.copyOf((Object[]) this.arr, this.arr.length);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> U[] toArray(U[] a) {
		return (U[]) Arrays.copyOf(this.arr, this.arr.length);
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		Iterator<?> it = c.iterator();
		while(it.hasNext()) {
			if(!this.contains(it.next())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Immutable collection!");
	}
	
	
}