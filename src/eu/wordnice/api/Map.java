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

import eu.wordnice.sql.wndb.WNDBDecoder;
import eu.wordnice.sql.wndb.WNDBEncoder;
import eu.wordnice.sql.wndb.WNDBVarTypes;

public class Map<X, Y> implements Jsonizable {
	
	public static final long FILE_PREFIX = 13945948982L;

	public Object[] names;
	public Object[] values;
	public Handler.OneHandler<Boolean, Val.FourVal<X, Y, X, Y>> set_handler;
	public int size = 0;

	public Map() {
		this.names = new Object[0];
		this.values = new Object[0];
		this.size = 0;
	}
	
	public Map(X[] nams, Y[] vals) {
		this.names = nams;
		this.values = vals;
		this.size = nams.length;
	}
	

	public boolean set(int i, X name, Y value) {
		if (i < 0 || i > this.size) {
			return false;
		}
		if (i == this.size) {
			return this.add(name, value);
		}
		if(this.set_handler != null) {
			Val.FourVal<X, Y, X, Y> vals = new Val.FourVal<X, Y, X, Y>(name, value, this.getNameI(i), this.getI(i));
			if(this.set_handler.handle(vals) == true) {
				name = vals.one;
				value = vals.two;
			}
		}
		this.names[i] = name;
		this.values[i] = value;
		return true;
	}

	public boolean addWC(X name, Y value) {
		int ns = this.size + 1;
		Object[] lnames = new Object[ns];
		Object[] lvalues = new Object[ns];
		Api.memcpy(lnames, 0, this.names, 0, this.size);
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		this.names = lnames;
		this.values = lvalues;
		this.names[this.size] = name;
		this.values[this.size] = value;
		this.size++;
		return true;
	}

	public boolean add(X name, Y value) {
		int i = this.indexOfName(name);
		if (i > -1) {
			return this.set(i, name, value);
		}
		return this.addWC(name, value);
	}
	
	public boolean addAllWC(java.util.Map<? extends X, ? extends Y> map) {
		if (map == null || map.size() < 1) {
			return false;
		}
		int msz = map.size();
		int si = this.size;
		int ns = si + msz;
		Object[] lnames = new Object[ns];
		Object[] lvalues = new Object[ns];
		Api.memcpy(lnames, 0, this.names, 0, this.size);
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		java.util.Set<? extends X> set = map.keySet();
		int ci = si;
		for(X x : set) {
			lnames[ci] = (X) x;
			lvalues[ci] = (Y) map.get(x);
			ci++;
		}
		this.names = lnames;
		this.values = lvalues;
		this.size = ns;
		return true;
	}

	public boolean addAllWC(Map<? extends X, ? extends Y> map) {
		return this.addAllWC(map.names, map.values, map.size);
	}
	
	public boolean addAllWC(Object[] names, Object[] values, int size) {
		if (names == null || values == null || size < 1) {
			return false;
		}
		int si = this.size;
		int ns = si + size;
		Object[] lnames = new Object[ns];
		Object[] lvalues = new Object[ns];
		Api.memcpy(lnames, 0, this.names, 0, this.size);
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		Api.memcpy(lnames, si, names, 0, size);
		Api.memcpy(lvalues, si, values, 0, size);
		this.names = lnames;
		this.values = lvalues;
		this.size = ns;
		return true;
	}
	
	public boolean addAllXYWC(X[] names, Y[] values) {
		if(names.length != values.length) {
			return false;
		}
		return this.addAllXYWC(names, values, names.length);
	}
	
	public boolean addAllXYWC(X[] names, Y[] values, int size) {
		if (names == null || values == null || size < 1) {
			return false;
		}
		int si = this.size;
		int ns = si + size;
		Object[] lnames = new Object[ns];
		Object[] lvalues = new Object[ns];
		Api.memcpy(lnames, 0, this.names, 0, this.size);
		Api.memcpy(lvalues, 0, this.values, 0, this.size);
		Api.memcpy(lnames, si, names, 0, size);
		Api.memcpy(lvalues, si, values, 0, size);
		this.names = lnames;
		this.values = lvalues;
		this.size = ns;
		return true;
	}
	
	public boolean addAll(java.util.Map<? extends X, ? extends Y> map) {
		if (map == null || map.size() < 1) {
			return false;
		}
		java.util.Set<? extends X> vals = map.keySet();
		for (X x : vals) {
			this.add((X) x, (Y) map.get(x));
		}
		return true;
	}
	
	public boolean addAll(Map<? extends X, ? extends Y> map) {
		return this.addAll(map.names, map.values, map.size);
	}

	@SuppressWarnings("unchecked")
	public boolean addAll(Object[] names, Object[] values, int len) {
		if (names == null || values == null || names.length < len || values.length < len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			this.add((X) names[i], (Y) values[i]);
		}
		return true;
	}
	
	public boolean addAllXY(X[] names, Y[] values) {
		if(names.length != values.length) {
			return false;
		}
		for (int i = 0; i < names.length; i++) {
			this.add((X) names[i], (Y) values[i]);
		}
		return true;
	}
	
	public boolean addAllXY(X[] names, Y[] values, int len) {
		if (names == null || values == null || names.length < len || values.length < len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			this.add((X) names[i], (Y) values[i]);
		}
		return true;
	}
	
	public boolean remove(X name) {
		return this.removeByIndex(this.indexOfName(name));
	}
	
	public boolean removeByValue(Y value) {
		return this.removeByIndex(this.indexOfValue(value));
	}
	
	public boolean removeByIndex(int i) {
		if(i < 0 || i >= this.size) {
			return false;
		}
		int nsz = this.size - 1;
		int sz1 = i;
		int sz2 = this.size - i - 1;
		Object[] vals = new Object[nsz];
		Object[] names = new Object[nsz];
		Api.memcpy(vals, 0, this.values, 0, sz1);
		Api.memcpy(vals, 0, this.values, (sz1 + 1), sz2);
		Api.memcpy(names, 0, this.names, 0, sz1);
		Api.memcpy(names, 0, this.names, (sz1 + 1), sz2);
		this.values = vals;
		this.names = names;
		this.size--;
		return true;
	}

	public int size() {
		return this.size;
	}

	public void clear() {
		this.size = 0;
		this.values = new Object[0];
		this.names = new Object[0];
	}

	@SuppressWarnings("unchecked")
	public Y getI(int i) {
		if (this.size <= i) {
			return null;
		}
		return (Y) this.values[i];
	}

	@SuppressWarnings("unchecked")
	public X getNameI(int i) {
		if (this.size <= i) {
			return null;
		}
		return (X) this.names[i];
	}

	@SuppressWarnings("unchecked")
	public Y get(X name) {
		Object na;
		for (int i = 0; i < this.size; i++) {
			na = this.names[i];
			if (na.equals(name)) {
				return (Y) this.values[i];
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public X getName(Y val) {
		Object na;
		for (int i = 0; i < this.size; i++) {
			na = this.values[i];
			if (na.equals(val)) {
				return (X) this.names[i];
			}
		}
		return null;
	}

	public int indexOfName(X name) {
		Object na;
		for (int i = 0; i < this.size; i++) {
			na = this.names[i];
			if (na.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	public int indexOfValue(Y val) {
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
			s.append(this.names[i]);
			s.append(':');
			s.append(this.values[i]);
			if(i != (this.size - 1)) {
				s.append(',');
			}
		}
		s.append('}');
		return s.toString();
	}
	
	@Override
	public void toJsonString(OutputStream out) throws IOException {
		out.write('{');
		for (int i = 0; i < this.size; i++) {
			JSONEncoder.writeObject(out, this.names[i]);
			out.write(':');
			JSONEncoder.writeObject(out, this.values[i]);
			if(i != (this.size - 1)) {
				out.write(',');
			}
		}
		out.write('}');
	}
	
	
	/*** Static FILE ***/
	
	public static boolean writeToFile(File f, Map<?,?> set) throws Exception {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		boolean ret = Map.writeToStream(ost, set);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
		return ret;
	}
	
	public static boolean writeToStream(OStream o, Map<?,?> set) throws Exception {
		o.writeLong(Map.FILE_PREFIX);
		Object obj1;
		Object obj2;
		WNDBVarTypes t1;
		WNDBVarTypes t2;
		for(int i = 0; i < set.size(); i++) {
			obj1 = set.getNameI(i);
			obj2 = set.getI(i);
			if(obj1 != null && obj2 != null) {
				t1 = WNDBVarTypes.getByObject(obj1);
				t2 = WNDBVarTypes.getByObject(obj2);
				if(t1 == null || t2 == null) {
					return false;
				}
				o.writeByte(t1.b);
				o.writeByte(t2.b);
				WNDBEncoder.writeObject(o, obj1, t1, 0, i);
				WNDBEncoder.writeObject(o, obj2, t2, 0, i);
			}
		}
		o.writeBoolean(false);
		return true;
	}
	
	public static <X,Y> Map<X,Y> loadFromFile(File f) {
		return Map.loadFromFile(new Map<X,Y>(), f);
	}
	
	public static <X,Y> Map<X,Y> loadFromFile(Map<X,Y> map, File f) {
		if(map == null || f == null || f.isFile() == false) {
			return null;
		}
		try {
			InputStream in = new FileInputStream(f);
			IStream ins = new IStream(new BufferedInputStream(in));
			Map.loadFromStream(map, ins);
			ins.close();
			try {
				in.close();
			} catch(Throwable t) {}
		} catch(Throwable t) {}
		return map;
	}
	
	public static <X,Y> Map<X,Y> loadFromStream(IStream s) throws Exception {
		return Map.loadFromStream(new Map<X,Y>(), s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> Map<X,Y> loadFromStream(Map<X,Y> map, IStream s) throws Exception {
		if(map == null || s == null) {
			return null;
		}
		long l = s.readLong();
		if(l != Map.FILE_PREFIX) {
			throw new Exception("Not MAP format!");
		}
		byte b;
		byte b2;
		Object o1, o2;
		int i = 0;
		while((b = s.readByte()) > 0) {
			b2 = s.readByte();
			o1 = WNDBDecoder.readObject(s, b, 0, i);
			o2 = WNDBDecoder.readObject(s, b2, 0, i);
			map.addWC((X) o1, (Y) o2);
			i++;
		}
		
		return map;
	}

}
