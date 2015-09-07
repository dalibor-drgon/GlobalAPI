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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import eu.wordnice.api.Api;

@Immutable
public class ImmMapArray<X, Y> implements Map<X, Y> {

	/**
	 * Array of entries {key1, value1, key2, value2 ...}
	 */
	public Object[] objs;
	
	/**
	 * Pair with {@link ImmMapArray#objs}
	 * Size divideable by 2
	 */
	public int size;
	
	/**
	 * Create Map for array of entries
	 * @param objs array of entries {@link ImmMapArray#objs}
	 */
	public ImmMapArray(Object... objs) {
		this.objs = objs;
		this.size = objs.length;
		if((this.size & 0x01) == 0x01) {
			throw new IllegalArgumentException("Array size must be divideable by 2!");
		}
	}
	
	/**
	 * Create Map for array of entries
	 * @param objs array of entries {@link ImmMapArray#objs}
	 * @param size Number of entries divideable by 2 {@link ImmMapArray#size}
	 */
	public ImmMapArray(Object[] objs, int size) {
		this.objs = objs;
		this.size = size;
		if((this.size & 0x01) == 0x01) {
			throw new IllegalArgumentException("Array size must be divideable by 2!");
		}
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Immutable map");
	}

	@Override
	public boolean containsKey(Object key) {
		if(key == null) {
			for(int i = 0, n = this.size; i < n; i += 2) {
				if(this.objs[i] == null) {
					return true;
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n; i += 2) {
				if(key.equals(this.objs[i])) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(Object val) {
		if(val == null) {
			for(int i = 0, n = this.size; i < n;) {
				i++;
				if(this.objs[i++] == null) {
					return true;
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n;) {
				i++;
				if(val.equals(this.objs[i++])) {
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
			for(int i = 0, n = this.size; i < n; i += 2) {
				if(this.objs[i] == null) {
					return (Y) this.objs[i + 1];
				}
			}
		} else {
			for(int i = 0, n = this.size; i < n; i += 2) {
				if(key.equals(this.objs[i])) {
					return (Y) this.objs[i + 1];
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
		return this.size / 2;
	}

	
	@Override
	public Set<X> keySet() {
		return new KeySet();
	}
	
	public class KeySet implements Set<X> {

		@Override
		public boolean add(X arg0) {
			throw new UnsupportedOperationException("Keys from immutable map!");
		}

		@Override
		public boolean addAll(Collection<? extends X> arg0) {
			throw new UnsupportedOperationException("Keys from immutable map!");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("Keys from immutable map!");
		}

		@Override
		public boolean contains(Object key) {
			return ImmMapArray.this.containsKey(key);
		}

		@Override
		public boolean containsAll(Collection<?> col) {
			Iterator<?> it = col.iterator();
			while(it.hasNext()) {
				if(!ImmMapArray.this.containsKey(it.next())) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return ImmMapArray.this.isEmpty();
		}

		@Override
		public Iterator<X> iterator() {
			return new KeySetIterator();
		}
		
		public class KeySetIterator implements Iterator<X> {

			public int i = -1;
			
			@Override
			public boolean hasNext() {
				this.i++;
				return this.i < ImmMapArray.this.size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public X next() {
				return (X) ImmMapArray.this.objs[this.i * 2];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Keys from immutable map!");
			}
			
		}

		@Override
		public boolean remove(Object arg0) {
			throw new UnsupportedOperationException("Keys from immutable map!");
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			throw new UnsupportedOperationException("Keys from immutable map!");
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			throw new UnsupportedOperationException("Keys from immutable map!");
		}

		@Override
		public int size() {
			return ImmMapArray.this.size();
		}

		@Override
		public Object[] toArray() {
			Object[] objs = ImmMapArray.this.objs;
			int len = ImmMapArray.this.size / 2;
			Object[] arr = new Object[len];
			for(int i = 0; i < len; i++) {
				arr[i] = objs[i * 2];
			}
			return arr;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] arr) {
			Object[] objs = ImmMapArray.this.objs;
			int len = ImmMapArray.this.size / 2;
			if(arr == null) {
				arr = (T[]) new Object[len];
			} else if(arr.length < len) {
				arr = (T[]) Array.newInstance(arr.getClass().getComponentType(), len);
			}
			for(int i = 0; i < len; i++) {
				arr[i] = (T) objs[i * 2];
			}
			return arr;
		}
		
		@Override
		public String toString() {
			int len = ImmMapArray.this.size;
			if(len == 0) {
				return "[]";
			}

			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for(int i = 0; i < len; i += 2) {
				Object key = ImmMapArray.this.objs[i];
				if(i != 0) {
					sb.append(',').append(' ');
				}
				sb.append(key);
			}
			sb.append(']');
			return sb.toString();
		}
		
	}
	
	@Override
	public Collection<Y> values() {
		return new ValueCollection();
	}
	
	public class ValueCollection implements Collection<Y> {

		@Override
		public boolean add(Y arg0) {
			throw new UnsupportedOperationException("Values from immutable map!");
		}

		@Override
		public boolean addAll(Collection<? extends Y> arg0) {
			throw new UnsupportedOperationException("Values from immutable map!");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("Values from immutable map!");
		}

		@Override
		public boolean contains(Object val) {
			return ImmMapArray.this.containsValue(val);
		}

		@Override
		public boolean containsAll(Collection<?> col) {
			Iterator<?> it = col.iterator();
			while(it.hasNext()) {
				if(!ImmMapArray.this.containsValue(it.next())) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isEmpty() {
			return ImmMapArray.this.isEmpty();
		}

		@Override
		public Iterator<Y> iterator() {
			return new ValueCollectionIterator();
		}
		
		public class ValueCollectionIterator implements Iterator<Y> {

			public int i = -1;
			
			@Override
			public boolean hasNext() {
				this.i++;
				return this.i < ImmMapArray.this.size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public Y next() {
				return (Y) ImmMapArray.this.objs[(this.i * 2) + 1];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Values from immutable map!");
			}
			
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Values from immutable map!");
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("Values from immutable map!");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("Values from immutable map!");
		}

		@Override
		public int size() {
			return ImmMapArray.this.size();
		}

		@Override
		public Object[] toArray() {
			Object[] objs = ImmMapArray.this.objs;
			int len = ImmMapArray.this.size / 2;
			Object[] arr = new Object[len];
			for(int i = 0; i < len; i++) {
				arr[i] = objs[(i * 2) + 1];
			}
			return arr;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] arr) {
			Object[] objs = ImmMapArray.this.objs;
			int len = ImmMapArray.this.size / 2;
			if(arr == null) {
				arr = (T[]) new Object[len];
			} else if(arr.length < len) {
				arr = (T[]) Array.newInstance(arr.getClass().getComponentType(), len);
			}
			for(int i = 0; i < len; i++) {
				arr[i] = (T) objs[(i * 2) + 1];
			}
			return arr;
		}
		
		@Override
		public String toString() {
			int len = ImmMapArray.this.size;
			if(len == 0) {
				return "[]";
			}

			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for(int i = 0; i < len;) {
				i++;
				Object key = ImmMapArray.this.objs[i++];
				if(i != 2) {
					sb.append(',').append(' ');
				}
				sb.append(key);
			}
			sb.append(']');
			return sb.toString();
		}
		
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
			int len = ImmMapArray.this.size;
			for(int i = 0; i < len;) {
				Object key = ImmMapArray.this.objs[i++];
				Object val = ImmMapArray.this.objs[i++];
				
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
			return ImmMapArray.this.isEmpty();
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
				return this.i < ImmMapArray.this.size();
			}

			@SuppressWarnings("unchecked")
			@Override
			public Entry<X, Y> next() {
				return new ImmEntry<X, Y>((X) ImmMapArray.this.objs[this.i * 2],
						(Y) ImmMapArray.this.objs[(this.i * 2) + 1]);
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
			return ImmMapArray.this.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] toArray() {
			Object[] objs = ImmMapArray.this.objs;
			int len = ImmMapArray.this.size / 2;
			Object[] arr = new ImmEntry[len];
			for(int i = 0; i < len; i++) {
				arr[i] = new ImmEntry<X, Y>((X) objs[i * 2], (Y) objs[(i * 2) + 1]);
			}
			return arr;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] toArray(T[] arr) {
			Object[] objs = ImmMapArray.this.objs;
			int len = ImmMapArray.this.size / 2;
			if(arr == null || arr.length < len) {
				arr = (T[]) new ImmEntry[len];
			}
			for(int i = 0; i < len; i++) {
				arr[i] = (T) new ImmEntry<X, Y>((X) objs[i * 2], (Y) objs[(i * 2) + 1]);
			}
			return arr;
		}
		
		@Override
		public String toString() {
			return ImmMapArray.this.toString();
		}
		
	}
	
	@Override
	public String toString() {
		int len = this.size;
		if(len == 0) {
			return "{}";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for(int i = 0; i < len;) {
			Object key = this.objs[i++];
			Object val = this.objs[i++];
			
			if(i != 2) {
				sb.append(',').append(' ');
			}
			sb.append((key == this) ? "(this Map)" : key);
			sb.append('=');
			sb.append((val == this) ? "(this Map)" : val);
		}
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ImmMapArray) {
			ImmMapArray<?,?> ia = (ImmMapArray<?,?>) obj;
			return (this.size == ia.size && Api.equals(this.objs, ia.objs, this.size));
		} else if(obj instanceof Map) {
			if(obj instanceof Collection) {
				if(this.size != ((Collection<?>) obj).size()) {
					return false;
				}
				if(obj instanceof ImmIter) {
					obj = ((ImmIter<?>) obj).arr;
					if(obj == null) {
						return false;
					}
				}
			}
			Iterator<?> it = ((Map<?, ?>) obj).entrySet().iterator();
			Iterator<?> it2 = this.entrySet().iterator();
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
