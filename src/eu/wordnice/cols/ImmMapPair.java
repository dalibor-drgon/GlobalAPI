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

package eu.wordnice.cols;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import eu.wordnice.api.Api;

@Immutable
public class ImmMapPair<X, Y> implements Map<X, Y> {

	/**
	 * Keys
	 * Pair with {@link ImmMapPair#vals}
	 */
	public Object[] keys;
	
	/**
	 * Values
	 * Pair with {@link ImmMapPair#keys}
	 */
	public Object[] vals;
	
	/**
	 * Size
	 */
	public int size;
	
	public ImmMapPair(Object[] keys, Object[] vals) {
		this.keys = keys;
		this.vals = vals;
		this.size = Math.min(this.keys.length, this.vals.length);
	}
	
	public ImmMapPair(Object[] keys, Object[] vals, int size) {
		this.keys = keys;
		this.vals = vals;
		this.size = size;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Immutable map");
	}

	@Override
	public boolean containsKey(Object key) {
		if(key == null) {
			for(int i = 0, n = this.size; i < n; i++) {
				if(this.keys[i] == null) {
					return true;
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n; i++) {
				if(key.equals(this.keys[i])) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(Object val) {
		if(val == null) {
			for(int i = 0, n = this.size; i < n; i++) {
				if(this.vals[i] == null) {
					return true;
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n; i++) {
				if(val.equals(this.vals[i])) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Y get(Object key) {
		if(key == null) {
			for(int i = 0, n = this.size; i < n; i++) {
				if(this.keys[i] == null) {
					return (Y) this.vals[i];
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n; i++) {
				if(key.equals(this.keys[i])) {
					return (Y) this.vals[i];
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public Y put(X arg0, Y arg1) {
		throw new UnsupportedOperationException("Immutable map");
	}

	@Override
	public void putAll(Map<? extends X, ? extends Y> arg0) {
		throw new UnsupportedOperationException("Immutable map");
	}

	@Override
	public Y remove(Object arg0) {
		throw new UnsupportedOperationException("Immutable map");
	}

	@Override
	public int size() {
		return this.size;
	}
	
	@Override
	public Set<X> keySet() {
		return new ImmArray<X>(this.keys, this.size);
	}

	@Override
	public Collection<Y> values() {
		return new ImmArray<Y>(this.vals, this.size);
	}
	
	@Override
	public Set<Entry<X, Y>> entrySet() {
		return new EntrySet();
	}
	
	public class EntrySet implements Set<Entry<X, Y>> {

		@Override
		public boolean add(java.util.Map.Entry<X, Y> e) {
			throw new UnsupportedOperationException("Immutable map!");
		}

		@Override
		public boolean addAll(Collection<? extends java.util.Map.Entry<X, Y>> c) {
			throw new UnsupportedOperationException("Immutable map!");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("Immutable map!");
		}

		@Override
		public boolean contains(Object o) {
			if(!(o instanceof Entry)) {
				return false;
			}
			Entry<?, ?> en = (Entry<?, ?>) o;
			Object e_key = en.getKey();
			Object e_val = en.getValue();
			int len = ImmMapPair.this.size;
			for(int i = 0; i < len; i++) {
				Object key = ImmMapPair.this.keys[i];
				Object val = ImmMapPair.this.vals[i];
				
				if((key == null) 
						? (e_key == null)
						: (key.equals(e_key))
					&& (val == null) 
						? (e_val == null)
						: (val.equals(e_val))) {
					return true;
				}
			}
			return false;
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
		public boolean isEmpty() {
			return ImmMapPair.this.isEmpty();
		}

		@Override
		public Iterator<Entry<X, Y>> iterator() {
			return new EntrySetIterator();
		}

		public class EntrySetIterator implements Iterator<Entry<X, Y>> {

			public int i = -1;
			
			@Override
			public boolean hasNext() {
				this.i++;
				return this.i < ImmMapPair.this.size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public Entry<X, Y> next() {
				return new ImmEntry<X, Y>((X) ImmMapPair.this.keys[this.i],
						(Y) ImmMapPair.this.vals[this.i]);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Values from immutable map!");
			}
			
		}
		
		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Immutable map!");
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("Immutable map!");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("Immutable map!");
		}

		@Override
		public int size() {
			return ImmMapPair.this.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] toArray() {
			int len = ImmMapPair.this.size;
			Object[] arr = new ImmEntry[len];
			for(int i = 0; i < len; i++) {
				arr[i] = new ImmEntry<X, Y>((X) ImmMapPair.this.keys[i], (Y) ImmMapPair.this.vals[i]);
			}
			return arr;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] arr) {
			int len = ImmMapPair.this.size;
			if(arr == null || arr.length < len) {
				arr = (T[]) new ImmEntry[len];
			}
			for(int i = 0; i < len; i++) {
				arr[i] = (T) new ImmEntry<X, Y>((X) ImmMapPair.this.keys[i], (Y) ImmMapPair.this.vals[i]);
			}
			return arr;
		}
		
		@Override
		public String toString() {
			return ImmMapPair.this.toString('[', ']');
		}
		
	}
	
	@Override
	public String toString() {
		return this.toString('{', '}');
	}
	
	public String toString(char start, char end) {
		int len = this.size;
		if(len == 0) {
			return start + "" + end;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(start);
		for(int i = 0; i < len; i++) {
			Object key = this.keys[i];
			Object val = this.vals[i];
			
			if(i != 0) {
				sb.append(',').append(' ');
			}
			sb.append(key);
			sb.append('=');
			sb.append(val);
		}
		sb.append(end);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Map)) {
			return false;
		}
		return Api.equalsCollections(this.entrySet(), ((Map<?, ?>) obj).entrySet());
	}
	
}
