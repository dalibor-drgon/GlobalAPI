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

package eu.wordnice.api.serialize;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.wordnice.api.IStream;
import eu.wordnice.api.OStream;
import eu.wordnice.db.wndb.WNDBDecoder;

public class CollSerializer {
	
	public static final int ARRAY_PREFIX = 0xBABE0889;
	public static final int SET_PREFIX = 0xBABE0989;
	public static final int MAP_PREFIX = 0xBABE1989;
	
	
	/*** Converting ***/
	
	public List<Map<String, Object>> resultsToList(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		String[] cols = new String[columns];
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(int i = 0; i < columns; i++) {           
			cols[i] = md.getColumnName(i + 1);
		}
		while(rs.next()) {
			Map<String, Object> row = new HashMap<String, Object>(columns);
			for(int i = 0; i < columns;) {
				String col = cols[i];
				i++;
				row.put(col, rs.getObject(i));
			}
			list.add(row);
		}
		return list;
	}
	
	
	
	/*** Arrays ***/
	
	public static void array2file(File f, Object[] arr) throws SerializeException, IOException {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		CollSerializer.array2stream(ost, arr, ((arr == null) ? 0 : arr.length));
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
	}
	
	public static void array2file(File f, Object[] arr, int len) throws SerializeException, IOException {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		CollSerializer.array2stream(ost, arr, len);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
	}
	
	public static void array2stream(OStream o, Object[] arr, int len) throws SerializeException, IOException {
		o.writeInt(CollSerializer.ARRAY_PREFIX);
		CollSerializer.array2streamWithoutPrefix(o, arr, len);
	}
	
	public static void array2streamWithoutPrefix(OStream o, Object[] arr, int len) throws SerializeException, IOException {
		o.writeInt(len);
		for(int i = 0; i < len; i++) {
			o.writeObject(arr[i]);
		}
	}
	
	public static <X> X[] file2array(Class<X> clz, File f) throws SerializeException, IOException {
		InputStream in = new FileInputStream(f);
		IStream ins = new IStream(new BufferedInputStream(in));
		X[] arr = CollSerializer.stream2array(clz, ins);
		ins.close();
		try {
			in.close();
		} catch(Throwable t) {}
		return arr;
	}
	
	public static <X> X[] stream2array(Class<X> clz, IStream s) throws SerializeException, IOException {
		int l = s.readInt();
		if(l != CollSerializer.ARRAY_PREFIX) {
			throw new BadFilePrefixException("Not ARRAY format!");
		}
		return CollSerializer.stream2arrayWithoutPrefix(clz, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] stream2arrayWithoutPrefix(Class<X> clz, IStream s) throws SerializeException, IOException {
		int len = s.readInt();
		if(len == -1) {
			return null;
		}
		X[] arr = (X[]) Array.newInstance(clz, len);
		for(int i = 0; i < len; i++) {
			arr[i] = (X) s.readObject();
		}
		return arr;
	}
	
	
	/*** Iterable & Collection ***/
	
	public static void coll2file(File f, Iterable<?> set) throws SerializeException, IOException {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		CollSerializer.coll2stream(ost, set);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
	}
	
	public static void coll2stream(OStream o, Iterable<?> set) throws SerializeException, IOException {
		o.writeInt(CollSerializer.SET_PREFIX);
		CollSerializer.coll2streamWithoutPrefix(o, set);
	}
	
	public static void coll2streamWithoutPrefix(OStream o, Iterable<?> set) throws SerializeException, IOException {
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			o.writeObject(it.next());
		}
		o.writeByte((byte) 0);
	}
	
	public static <X> void file2coll(Collection<X> set, File f) throws SerializeException, IOException {
		InputStream in = new FileInputStream(f);
		IStream ins = new IStream(new BufferedInputStream(in));
		CollSerializer.stream2coll(set, ins);
		ins.close();
		try {
			in.close();
		} catch(Throwable t) {}
	}
	
	public static <X> void stream2coll(Collection<X> set, IStream s) throws SerializeException, IOException {
		int l = s.readInt();
		if(l != CollSerializer.SET_PREFIX) {
			throw new BadFilePrefixException("Not SET format!");
		}
		CollSerializer.stream2collWithoutPrefix(set, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> void stream2collWithoutPrefix(Collection<X> set, IStream s) throws SerializeException, IOException {
		byte b;
		int i = 0;
		while((b = s.readByte()) > 0) {
			set.add((X) WNDBDecoder.readObject(s, b, i, 0));
			i++;
		}
	}
	
	
	
	/*** Map ***/
	
	public static void map2file(File f, Map<?,?> map) throws SerializeException, IOException {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		CollSerializer.map2stream(ost, map);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
	}
	
	public static void map2stream(OStream o, Map<?,?> map) throws SerializeException, IOException {
		o.writeInt(CollSerializer.MAP_PREFIX);
		CollSerializer.map2streamWithoutPrefix(o, map);
	}
	
	public static void map2streamWithoutPrefix(OStream o, Map<?,?> map) throws SerializeException, IOException {
		Iterator<? extends Entry<?,?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?,?> ent = it.next();
			o.writeObject(ent.getKey());
			o.writeObject(ent.getValue());
		}
		o.writeByte((byte) 0);
	}
	
	public static <X,Y> void file2map(Map<X,Y> map, File f) throws SerializeException, IOException {
		InputStream in = new FileInputStream(f);
		IStream ins = new IStream(new BufferedInputStream(in));
		CollSerializer.stream2map(map, ins);
		ins.close();
		try {
			in.close();
		} catch(Throwable t) {}
	}
	
	public static <X,Y> void stream2map(Map<X,Y> map, IStream s) throws SerializeException, IOException {
		int l = s.readInt();
		if(l != CollSerializer.MAP_PREFIX) {
			throw new BadFilePrefixException("Not MAP format!");
		}
		CollSerializer.stream2mapWithoutPrefix(map, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> void stream2mapWithoutPrefix(Map<X,Y> map, IStream s) throws SerializeException, IOException {
		byte b;
		int i = 0;
		while((b = s.readByte()) > 0) {
			Object key = WNDBDecoder.readObject(s, b, i, 0);
			b = s.readByte();
			Object val = WNDBDecoder.readObject(s, b, i, 1);
			map.put((X) key, (Y) val);
			i++;
		}
	}
}
