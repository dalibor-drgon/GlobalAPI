package wordnice.coll;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class ImmEntryArray<X, Y> implements Set<Entry<X, Y>> {
	
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
	 * Create entry set for array
	 * 
	 * @param objs array of entries
	 */
	public ImmEntryArray(Object[] objs) {
		this.objs = objs;
		this.size = objs.length;
		if((this.size & 0x01) == 0x01) {
			this.size--;
		}
	}
	
	/**
	 * Create entry set for array
	 * 
	 * @param objs array of entries
	 * @param size Number of entries divideable by 2
	 */
	public ImmEntryArray(Object[] objs, int size) {
		this.objs = objs;
		this.size = size;
		if((this.size & 0x01) == 0x01) {
			this.size--;
		}
	}

	@Override
	public boolean add(java.util.Map.Entry<X, Y> e) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean addAll(Collection<? extends java.util.Map.Entry<X, Y>> c) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean contains(Object o) {
		if(!(o instanceof Entry)) {
			return false;
		}
		Entry<?, ?> en = (Entry<?, ?>) o;
		Object e_key = en.getKey();
		Object e_val = en.getValue();
		for(int i = 0; i < this.size;) {
			Object key = this.objs[i++];
			Object val = this.objs[i++];
			
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
		return (this.size == 0);
	}

	@Override
	public Iterator<Entry<X, Y>> iterator() {
		return new ImmEntryArrayIterator<X, Y>(this.objs, this.size);
	}
	
	@Override
	public boolean remove(Object o) {
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
	public int size() {
		return this.size / 2;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] toArray() {
		Object[] objs = this.objs;
		int len = this.size / 2;
		Object[] arr = new ImmEntry[len];
		for(int i = 0; i < len; i++) {
			arr[i] = new ImmEntry<X, Y>((X) objs[i * 2], (Y) objs[(i * 2) + 1]);
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arr) {
		Object[] objs = this.objs;
		int len = this.size / 2;
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
		int len = this.size;
		if(len == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(int i = 0; i < len;) {
			Object key = this.objs[i++];
			Object val = this.objs[i++];
			if(i != 2) {
				sb.append(',').append(' ');
			}
			sb.append(key);
			sb.append('=');
			sb.append(val);
		}
		sb.append(']');
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ImmEntryArray) {
			ImmEntryArray<?,?> ia = (ImmEntryArray<?,?>) obj;
			return (this.size == ia.size && CollUtils.equals(this.objs, ia.objs, this.size));
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
				if(!(cur instanceof Entry)) {
					return false;
				}
				Entry<?, ?> en = (Entry<?, ?>) cur;
				Object key = this.objs[i++];
				Object val = this.objs[i++];
				if(!((key == null) 
						? (en.getKey() == null)
						: (key.equals(en.getKey()))
					&& (val == null) 
						? (en.getValue() == null)
						: (val.equals(en.getValue())))) {
					return false;
				}
			}
			return (i == this.size);
		}
		return false;
	}
	
}