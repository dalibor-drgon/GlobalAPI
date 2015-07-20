/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import eu.wordnice.sql.wndb.WNDBDecoder;
import eu.wordnice.sql.wndb.WNDBEncoder;
import eu.wordnice.sql.wndb.WNDBVarTypes;

public class Set<X> implements Jsonizable {
	
	public static final long FILE_PREFIX = 10945948982L;

	public Object[] values;
	public Handler.OneHandler<Boolean, Val.TwoVal<X, X>> set_handler;
	public int size = 0;

	public Set() {
		this.values = new Object[0];
		this.size = 0;
	}
	
	@SafeVarargs
	public Set(X... vals) {
		this.values = vals;
		this.size = vals.length;
	}

	public boolean set(int i, X value) {
		if (i < 0 || i > this.size) {
			return false;
		}
		if (i == this.size) {
			return this.add(value);
		}
		if(this.set_handler != null) {
			Val.TwoVal<X, X> vals = new Val.TwoVal<X, X>(value, this.get(i));
			if(this.set_handler.handle(vals) == true) {
				value = vals.one;
			}
		}
		this.values[i] = value;
		return true;
	}

	public boolean addWC(X val) {
		int ns = this.size + 1;
		Object[] lvalues = new Object[ns];
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		this.values = lvalues;
		this.values[this.size] = val;
		this.size++;
		return true;
	}

	public boolean add(X val) {
		int i = this.indexOf(val);
		if (i > -1) {
			this.set(i, val);
		} else {
			this.addWC(val);
		}
		return true;
	}
	
	
	public boolean addAllWC(Collection<? extends X> set) {
		return this.addAllWC(set.toArray(), set.size());
	}

	public boolean addAllWC(Set<? extends X> map) {
		if (map == null || map.size < 1) {
			return false;
		}
		return this.addAllWC(map.values, map.size);
	}
	
	public boolean addAllWC(Object[] arr, int sz) {
		if (arr == null || sz < 1) {
			return false;
		}
		int si = this.size;
		int ns = si + sz;
		Object[] lvalues = new Object[ns];
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		Api.memcpy(lvalues, si, arr, 0,  sz);
		this.values = lvalues;
		this.size = ns;
		return true;
	}
	
	public boolean addAllXWC(X[] arr) {
		if (arr == null || arr.length < 1) {
			return false;
		}
		return this.addAllXWC(arr, arr.length);
	}
	
	public boolean addAllXWC(X[] arr, int sz) {
		if (arr == null || sz < 1) {
			return false;
		}
		int si = this.size;
		int ns = si + sz;
		Object[] lvalues = new Object[ns];
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		Api.memcpy(lvalues, si,arr, 0,  sz);
		this.values = lvalues;
		this.size = ns;
		return true;
	}
	
	
	public boolean addAll(Collection<? extends X> set) {
		return this.addAll(set.toArray(), set.size());
	}
	
	public boolean addAll(Set<? extends X> map) {
		if (map == null || map.size < 1) {
			return false;
		}
		return this.addAll(map.values, map.size);
	}
	
	@SuppressWarnings("unchecked")
	public boolean addAll(Object[] arr, int sz) {
		if (arr == null || sz < 1) {
			return false;
		}
		for (int i = 0; i < sz; i++) {
			this.add((X) arr[i]);
		}
		return true;
	}
	
	public boolean addAllX(X[] arr) {
		if (arr == null || arr.length < 1) {
			return false;
		}
		for (int i = 0; i < arr.length; i++) {
			this.add((X) arr[i]);
		}
		return true;
	}
	
	public boolean addAllX(X[] arr, int sz) {
		if (arr == null || sz < 1) {
			return false;
		}
		for (int i = 0; i < sz; i++) {
			this.add((X) arr[i]);
		}
		return true;
	}
	
	public boolean remove(int i) {
		if(i < 0 || i >= this.size) {
			return false;
		}
		int sz1 = i;
		int sz2 = this.size - i - 1;
		Object[] vals = new Object[this.size - 1];
		Api.memcpy(vals, 0, this.values, 0, sz1);
		Api.memcpy(vals, 0, this.values, (sz1 + 1), sz2);
		this.values = vals;
		this.size--;
		return true;
	}

	public int size() {
		return this.size;
	}

	public boolean clear() {
		this.size = 0;
		this.values = new Object[0];
		return true;
	}

	@SuppressWarnings("unchecked")
	public X get(int i) {
		if (this.size <= i) {
			return null;
		}
		return (X) this.values[i];
	}

	public int indexOf(X val) {
		Object na;
		for (int i = 0; i < this.size; i++) {
			na = this.values[i];
			if (na.equals(val)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("{");
		for (int i = 0; i < this.size; i++) {
			s.append(this.values[i]);
			if(i != (this.size - 1)) {
				s.append(", ");
			}
		}
		s.append("}");
		return s.toString();
	}
	
	@Override
	public void toJsonString(OutputStream out) throws IOException {
		out.write('[');
		for (int i = 0; i < this.size; i++) {
			JSONEncoder.writeObject(out, this.values[i]);
			if(i != (this.size - 1)) {
				out.write(',');
			}
		}
		out.write(']');
	}
	
	public Object[] toArray() {
		return this.values;
	}
	
	public X[] toArray(X[] arr) {
		int size = this.size();
		if(size > arr.length) {
			size = arr.length;
		}
		return Api.memcpy(arr, 0, this.values, 0, size)
				? arr : null;
	}
	
	public X[] toArray(X[] arr, int pos, int len) {
		int size = this.size();
		if(size + pos > len) {
			size = len - pos;
			if(size < 0) {
				return null;
			}
		}
		return Api.memcpy(arr, pos, this.values, 0, size)
				? arr : null;
	}
	
	
	/*** Static FILE ***/
	
	public static boolean writeToFile(File f, Set<?> set) throws Exception {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		boolean ret = Set.writeToStream(ost, set);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
		return ret;
	}
	
	public static boolean writeToStream(OStream o, Set<?> set) throws Exception {
		o.writeLong(Set.FILE_PREFIX);
		Object obj;
		WNDBVarTypes t;
		for(int i = 0; i < set.size(); i++) {
			obj = set.get(i);
			if(obj != null) {
				t = WNDBVarTypes.getByObject(obj);
				if(t == null) {
					return false;
				}
				o.writeByte(t.b);
				WNDBEncoder.writeObject(o, obj, t, 0, i);
			}
		}
		o.writeByte((byte) 0);
		return true;
	}
	
	public static <X> Set<X> loadFromFile(File f) throws Exception {
		return Set.loadFromFile(new Set<X>(), f);
	}
	
	public static <X> Set<X> loadFromFile(Set<X> set, File f) throws Exception {
		if(set == null || f == null || f.isFile() == false) {
			throw new NullPointerException("File or set is null!");
		}
		InputStream in = new FileInputStream(f);
		IStream ins = new IStream(new BufferedInputStream(in));
		Set.loadFromStream(set, ins);
		ins.close();
		try {
			in.close();
		} catch(Throwable t) {}
		return set;
	}
	
	public static <X> Set<X> loadFromStream(IStream s) throws Exception {
		return Set.loadFromStream(new Set<X>(), s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> Set<X> loadFromStream(Set<X> set, IStream s) throws Exception {
		if(set == null || s == null) {
			return null;
		}
		long l = s.readLong();
		if(l != Set.FILE_PREFIX) {
			throw new Exception("Not SET format!");
		}
		byte b;
		int i = 0;
		while((b = s.readByte()) > 0) {
			set.addWC((X) WNDBDecoder.readObject(s, b, 0, i));
			i++;
		}
		
		return set;
	}

}
