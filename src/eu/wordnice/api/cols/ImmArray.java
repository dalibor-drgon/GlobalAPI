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
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import eu.wordnice.api.Api;

@Immutable
public class ImmArray<T> implements List<T>, Set<T>, RandomAccess {
	
	/**
	 * Array to iterate
	 */
	public Object[] arr;
	
	/**
	 * Size
	 */
	public int size;
	
	/**
	 * Create immutable iterable randomaccess list & set
	 * @param arr Array to iterate
	 */
	public ImmArray(Object[] arr) {
		this.arr = arr;
		this.size = arr.length;
	}
	
	/**
	 * Create immutable iterable randomaccess list & set
	 * @param arr Array to iterate
	 * @param size Size of array
	 */
	public ImmArray(Object[] arr, int size) {
		this.arr = arr;
		this.size = size;
	}

	/**
	 * Create simple immutable iterator
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new ImmArrayIterator<T>(this.arr, this.size, 0);
	}
	
	/**
	 * Create simple immutable list iterator
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<T> listIterator() {
		return new ImmArrayIterator<T>(this.arr, this.size, 0);
	}
	
	/**
	 * Create simple immutable list iterator
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int start) {
		return new ImmArrayIterator<T>(this.arr, this.size, start);
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
		if(o == null) {
			for(int i = 0, n = this.size; i < n; i++) {
				if(this.arr[i] == null) {
					return true;
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n; i++) {
				if(o.equals(this.arr[i])) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object[] toArray() {
		return Arrays.copyOf((Object[]) this.arr, this.size);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> U[] toArray(U[] ar) {
		if(ar == null) {
			ar = (U[]) new Object[this.size];
		} else if(ar.length < this.size) {
			ar = (U[]) Array.newInstance(ar.getClass().getComponentType(), this.size);
		}
		Api.memcpy(ar, this.arr, this.size);
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
	public void add(int arg0, T arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int index) {
		return (T) this.arr[index];
	}

	@Override
	public int indexOf(Object obj) {
		return Api.indexOf(obj, this.arr);
	}

	@Override
	public int lastIndexOf(Object obj) {
		return Api.lastIndexOf(obj, this.arr);
	}

	@Override
	public T remove(int arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public T set(int arg0, T arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		if(fromIndex < 0 || fromIndex >= this.size()) {
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if(toIndex < 0 || toIndex >= this.size()) {
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}
		if(fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex > toIndex");
		}
		return new ImmSkipArray<T>(this.arr, (toIndex - fromIndex), fromIndex, 1);
	}
	
	@Override
	public String toString() {
		int len = this.size;
		if(len == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < len;) {
			Object key = this.arr[i++];
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
		if(obj instanceof ImmArray) {
			ImmArray<?> ia = (ImmArray<?>) obj;
			return (this.size == ia.size && Api.equals(this.arr, ia.arr, this.size));
		} else if(obj instanceof Iterable) {
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
	
	
	@SafeVarargs
	public static <X> ImmArray<X> create(X... vals) {
		return new ImmArray<X>(vals);
	}
	
	public static ImmArray<Object> createObj(Object... vals) {
		return new ImmArray<Object>(vals);
	}
	
}