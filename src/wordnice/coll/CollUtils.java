/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.coll;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wordnice.api.Nice;

public class CollUtils {

	public static Map<String, String> toStringMap(Map<?, ?> map) {
		Map<String, String> ret = Nice.createMap();
		toStringMap(ret, map);
		return ret;
	}
	
	public static void toStringMap(Map<String, String> out, Map<?, ?> map) {
		Iterator<? extends Entry<?, ?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?, ?> ent = it.next();
			Object key = ent.getKey();
			Object val = ent.getValue();
			String str_key = (key == null) ? null : key.toString();
			String str_val = (val == null) ? null : val.toString();
			out.put(str_key, str_val);
		}
	}
	
	/*@SuppressWarnings("unchecked")
	public static void copyMap(Map<?, ?> out, Map<?, ?> map) {
		Iterator<? extends Entry<?, ?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?, ?> ent = it.next();
			((Map<Object, Object>)out).put(ent.getKey(), ent.getValue());
		}
	}*/
	
	public static List<String> toStringColl(Collection<?> set) {
		List<String> list = new ArrayList<String>();
		toStringColl(list, set);
		return list;
	}
	
	public static void toStringColl(Collection<String> out, Iterable<?> set) {
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			Object next = it.next();
			if(next == null) {
				out.add(null);
			} else {
				out.add(next.toString());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void copyColl(Collection<?> out, Iterable<?> set) {
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			((Collection<Object>) out).add(it.next());
		}
	}
	
	public static boolean equals(Object[] one, Object[] two, int size) {
		if(one == two) {
			return true;
		}
		while(size-- != 0) {
			Object on = one[size];
			Object tw = two[size];
			if((on == null) ? tw != null : !on.equals(tw)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean equals(Object[] one, int off1, Object[] two, int off2, int size) {
		if(one == two && off1 == off2) {
			return true;
		}
		size += off1;
		for(; off1 < size; off1++, off2++) {
			Object on = one[off1];
			Object tw = two[off2];
			if((on == null) ? tw != null : !on.equals(tw)) {
				return false;
			}
		}
		return true;
	}

	/***********
	 * INDEX OF
	 */
	
	public static int indexOf(Object o, Object[] vals) {
		return indexOf(o, vals, 0);
	}
	
	public static int indexOf(Object o, Object[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		int size = vals.length;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int indexOf(Object o, Object[] vals, int i, int size) {
		if(vals == null || size == 0) {
			return -1;
		}
		size += i;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int indexOfInsensitive(String o, String[] vals) {
		return indexOfInsensitive(o, vals, 0);
	}
	
	public static int indexOfInsensitive(String o, String[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		int size = vals.length;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equalsIgnoreCase(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int indexOfInsensitive(String o, String[] vals, int i, int size) {
		if(vals == null || size == 0) {
			return -1;
		}
		size += i;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equalsIgnoreCase(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	
	/****************
	 * LAST INDEX OF
	 */
	
	public static int lastIndexOf(Object o, Object[] vals) {
		return lastIndexOf(o, vals, 0);
	}
	
	public static int lastIndexOf(Object o, Object[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		if(i >= vals.length) {
			i = vals.length - 1;
		}
		if(o == null) {
			for(; i >= 0; i--) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i >= 0; i--) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int lastIndexOfInsensitive(String o, String[] vals) {
		return lastIndexOfInsensitive(o, vals, 0);
	}
	
	public static int lastIndexOfInsensitive(String o, String[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		if(i >= vals.length) {
			i = vals.length - 1;
		}
		if(o == null) {
			for(; i >= 0; i--) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i >= 0; i--) {
				if(o.equalsIgnoreCase(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] toArray(Collection<?> col) {
		if(col instanceof ImmArray) {
			return (X[]) ((ImmArray<X>) col).arr;
		}
		return ((Collection<X>) col).toArray((X[]) new Object[col.size()]);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] toArray(Collection<?> col, Class<?> c) {
		if(col instanceof ImmArray) {
			return (X[]) ((ImmArray<X>) col).arr;
		}
		return ((Collection<X>) col).toArray((X[]) Array.newInstance(c, col.size()));
	}
	
	/**
	 * Check if values in iterables are identical and in identical order
	 * If both are instanceof Collection, this method check sizes first
	 * Calls only once Itarable#iterator() for both iterables
	 */
	public static boolean equalsIterablesInOrder(Iterable<?> i1, Iterable<?> i2) {
		if(i1 == i2) {
			return true;
		}
		if(i1 == null) {
			if(i2 == null) {
				return true;
			}
			return false;
		} else if(i2 == null) {
			return false;
		}
		if(i1 instanceof Collection && i2 instanceof Collection) {
			if(((Collection<?>) i1).size() != ((Collection<?>) i2).size()) {
				return false;
			}
		}
		Iterator<?> it1 = i1.iterator();
		Iterator<?> it2 = i2.iterator();
		while(it1.hasNext()) {
			if(!it2.hasNext()) {
				return false;
			}
			Object c1 = it1.next();
			Object c2 = it2.next();
			if((c1 == null) ? c2 != null : !c1.equals(c2)) {
				return false;
			}
		}
		return !it2.hasNext();
	}
	
	/**
	 * Check if values in iterables are identical (in any order)
	 * This method check sizes first
	 * For first collection calls Iterable#iterator() only once, and
	 * several times for second collection (max = size of collection)
	 */
	public static boolean equalsCollectionsAnyOrder(Collection<?> i1, Collection<?> i2) {
		if(i1 == i2) {
			return true;
		}
		if(i1 == null) {
			if(i2 == null) {
				return true;
			}
			return false;
		} else if(i2 == null) {
			return false;
		}
		int size = i1.size();
		if(size != i2.size()) {
			return false;
		}
		boolean[] was = new boolean[size];
		Iterator<?> it1 = i1.iterator();
		for(int i = 0; i < size; i++) {
			if(!it1.hasNext()) {
				return false;
			}
			Object cur = it1.next();
			Iterator<?> it2 = i2.iterator();
			boolean wasCur = false;
			if(cur == null) {
				for(int seci = 0; seci < size; seci++) {
					if(!it2.hasNext()) {
						return false;
					}
					Object cur2 = it2.next();
					if(!was[seci]) {
						if(cur2 == null) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			} else {
				for(int seci = 0; seci < size; seci++) {
					if(!it2.hasNext()) {
						return false;
					}
					Object cur2 = it2.next();
					if(!was[seci]) {
						if(cur.equals(cur2)) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			}
			if(!wasCur || it2.hasNext()) {
				return false;
			}
		}
		return !it1.hasNext();
	}
	
	/**
	 * Check if values are identical (in any order)
	 */
	public static boolean equalsAnyOrder(Object[] arr1, int off1, Object[] arr2, int off2, int size) {
		if(arr1 == arr2 && off1 == off2) {
			return true;
		}
		if(arr1 == null) {
			if(arr2 == null) {
				return true;
			}
			return false;
		} else if(arr2 == null) {
			return false;
		}
		boolean[] was = new boolean[size];
		int size1 = size + off1;
		int size2 = size + off2;
		for(int i = off1; i < size1; i++) {
			Object cur = arr1[i];
			boolean wasCur = false;
			if(cur == null) {
				for(int seci = off2; seci < size2; seci++) {
					Object cur2 = arr2[seci];
					if(!was[seci]) {
						if(cur2 == null) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			} else {
				for(int seci = 0; seci < size; seci++) {
					Object cur2 = arr2[seci];
					if(!was[seci]) {
						if(cur.equals(cur2)) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			}
			if(!wasCur) {
				return false;
			}
		}
		return true;
	}
	
}
