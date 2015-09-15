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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ImmMapIterPair<X, Y> implements Map<X, Y> {

	/**
	 * Keys
	 * Pair with {@link ImmMapIterPair#vals}
	 */
	public Iterable<X> keys;
	
	/**
	 * Values
	 * Pair with {@link ImmMapIterPair#keys}
	 */
	public Iterable<Y> vals;
	
	/**
	 * Size
	 */
	public int size;
	
	/**
	 * Create immutable map from two iterables with given size
	 * 
	 * @param keys Keys
	 * @param vals Values
	 * @param size Maximum size (real size can be smaller if
	 *             any iterator on #hasNext() returns false)
	 */
	public ImmMapIterPair(Iterable<X> keys, Iterable<Y> vals, int size) {
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
		Iterator<X> it = this.keys.iterator();
		int i = 0;
		if(key == null) {
			while(i++ < this.size && it.hasNext()) {
				if(it.next() == null) {
					return true;
				}
			}
		} else {
			while(i++ < this.size && it.hasNext()) {
				if(key.equals(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(Object val) {
		Iterator<Y> it = this.vals.iterator();
		int i = 0;
		if(val == null) {
			while(i++ < this.size && it.hasNext()) {
				if(it.next() == null) {
					return true;
				}
			}
		} else {
			while(i++ < this.size && it.hasNext()) {
				if(val.equals(it.next())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Y get(Object key) {
		Iterator<X> it = this.keys.iterator();
		Iterator<Y> it2 = this.vals.iterator();
		int i = 0;
		if(key == null) {
			while(i++ < this.size && it.hasNext() && it2.hasNext()) {
				if(it.next() == null) {
					return it2.next();
				}
			}
		} else {
			while(i++ < this.size && it.hasNext() && it2.hasNext()) {
				if(key.equals(it.next())) {
					return it2.next();
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
		return new ImmIter<X>(this.keys, this.size);
	}

	@Override
	public Collection<Y> values() {
		return new ImmIter<Y>(this.vals, this.size);
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
			int len = ImmMapIterPair.this.size;
			int i = 0;
			Iterator<X> it = ImmMapIterPair.this.keys.iterator();
			Iterator<Y> it2 = ImmMapIterPair.this.vals.iterator();
			while(i++ < len && it.hasNext() && it2.hasNext()) {
				Object key = it.next();
				Object val = it2.next();
				
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
			return ImmMapIterPair.this.isEmpty();
		}

		@Override
		public Iterator<Entry<X, Y>> iterator() {
			return new EntrySetIterator();
		}

		public class EntrySetIterator implements Iterator<Entry<X, Y>> {

			public int i = -1;
			
			/**
			 * Key Iterator
			 */
			private Iterator<X> it = ImmMapIterPair.this.keys.iterator();
			
			/**
			 * Value Iterator
			 */
			private Iterator<Y> it2 = ImmMapIterPair.this.vals.iterator();
			
			@Override
			public boolean hasNext() {
				this.i++;
				return (this.i < ImmMapIterPair.this.size() && this.it.hasNext() && this.it2.hasNext());
			}

			@Override
			public Entry<X, Y> next() {
				return new ImmEntry<X, Y>(this.it.next(), this.it2.next());
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
			return ImmMapIterPair.this.size();
		}

		@Override
		public Object[] toArray() {
			int len = ImmMapIterPair.this.size;
			Object[] arr = new ImmEntry[len];
			int i = 0;
			Iterator<X> it = ImmMapIterPair.this.keys.iterator();
			Iterator<Y> it2 = ImmMapIterPair.this.vals.iterator();
			while(i++ < len && it.hasNext() && it2.hasNext()) {
				X key = it.next();
				Y val = it2.next();
				arr[i] = new ImmEntry<X, Y>(key, val);
			}
			return arr;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] arr) {
			int len = ImmMapIterPair.this.size;
			if(arr == null || arr.length < len) {
				arr = (T[]) new ImmEntry[len];
			}
			int i = 0;
			Iterator<X> it = ImmMapIterPair.this.keys.iterator();
			Iterator<Y> it2 = ImmMapIterPair.this.vals.iterator();
			while(i++ < len && it.hasNext() && it2.hasNext()) {
				X key = it.next();
				Y val = it2.next();
				arr[i] = (T) new ImmEntry<X, Y>(key, val);
			}
			return arr;
		}
		
		@Override
		public String toString() {
			return ImmMapIterPair.this.toString('[', ']');
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
		int i = 0;
		Iterator<X> it = ImmMapIterPair.this.keys.iterator();
		Iterator<Y> it2 = ImmMapIterPair.this.vals.iterator();
		while(i++ < len && it.hasNext() && it2.hasNext()) {
			Object key = it.next();
			Object val = it2.next();
			
			if(i != 1) {
				sb.append(',').append(' ');
			}
			sb.append(key);
			sb.append('=');
			sb.append(val);
		}
		sb.append(end);
		return sb.toString();
	}
	
	
}
