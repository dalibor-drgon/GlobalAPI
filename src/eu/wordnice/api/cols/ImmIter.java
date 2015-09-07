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

package eu.wordnice.api.cols;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ImmIter<T> implements Set<T> {
	
	/**
	 * Array to iterate
	 */
	public Iterable<T> arr;
	
	/**
	 * Size
	 */
	public int size;
	
	/**
	 * Create immutable iterable randomaccess list & set
	 * @param arr Iterable
	 * @param size Size of iterable
	 */
	public ImmIter(Iterable<T> arr, int size) {
		this.arr = arr;
		this.size = size;
	}

	/**
	 * Create simple immutable iterator
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new SimpleIterator();
	}
	
	public class SimpleIterator implements Iterator<T> {
		
		/**
		 * Current index
		 */
		private int i = -1;
		
		/**
		 * Iterator
		 */
		private Iterator<T> it = ImmIter.this.arr.iterator();
		
		@Override
		public boolean hasNext() {
			this.i++;
			return (this.i < ImmIter.this.size() && this.it.hasNext());
		}

		@Override
		public T next() {
			return this.it.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Values from immutable map!");
		}
		
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return (this.size == 0);
	}

	@Override
	public boolean contains(Object o) {
		Iterator<T> it = this.arr.iterator();
		int i = 0;
		if(o == null) {
			while(i++ < this.size && it.hasNext()) {
				if(it.next() == null) {
					return true;
				}
			}
		} else {
			while(i++ < this.size && it.hasNext()) {
				if(o.equals(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object[] toArray() {
		Object[] arr = new Object[this.size];
		int i = 0;
		Iterator<T> it = this.arr.iterator();
		while(i < this.size && it.hasNext()) {
			arr[i++] = it.next();
		}
		i++;
		if(arr.length != i) {
			arr = Arrays.copyOf(arr, i);
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> U[] toArray(U[] ar) {
		if(ar == null) {
			ar = (U[]) new Object[this.size];
		} else if(ar.length < this.size) {
			ar = (U[]) Array.newInstance(ar.getClass().getComponentType(), this.size);
		}
		int i = 0;
		Iterator<T> it = this.arr.iterator();
		while(i < this.size && it.hasNext()) {
			ar[i++] = (U) it.next();
		}
		i++;
		if(ar.length != i) {
			ar = (U[]) Arrays.copyOf(ar, i, (Class<? extends U[]>)ar.getClass());
		}
		return ar;
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
	
	@Override
	public String toString() {
		int len = this.size;
		if(len == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		int i = 0;
		Iterator<T> it = this.arr.iterator();
		while(i++ < len && it.hasNext()) {
			Object key = it.next();
			if(i != 1) {
				sb.append(',').append(' ');
			}
			sb.append((key == this) ? "(this Collection)" : key);
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
			Iterator<?> it2 = this.iterator();
			int i = 0;
			while(it.hasNext()) {
				if(!it2.hasNext() || i >= this.size) {
					return false;
				}
				i++;
				Object cur = it.next();
				Object tcur = it2.next();
				if((cur == null) ? tcur != null : !cur.equals(tcur)) {
					return false;
				}
			}
			return (i == this.size);
		}
		return false;
	}
	
}