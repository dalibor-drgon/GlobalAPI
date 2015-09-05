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
import java.io.InputStream;
import java.io.OutputStream;
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

public class WNSerializer {
	
	public static final int SET_PREFIX = 0xBABE0989;
	public static final int MAP_PREFIX = 0xBABE1989;
	
	
	/*** Converting ***/
	
	public List<Map<String, Object>> resultsToList(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		while (rs.next()){
			Map<String, Object> row = new HashMap<String, Object>(columns);
			int i = 1;
			for(; i <= columns; i++) {           
				row.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(row);
		}
		return list;
	}
	
	
	
	/*** Collection ***/
	
	public static void coll2file(File f, Iterable<?> set) throws Exception {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		WNSerializer.coll2stream(ost, set);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
	}
	
	public static void coll2stream(OStream o, Iterable<?> set) throws Exception {
		o.writeInt(WNSerializer.SET_PREFIX);
		WNSerializer.coll2streamWithoutPrefix(o, set);
	}
	
	public static void coll2streamWithoutPrefix(OStream o, Iterable<?> set) throws Exception {
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			o.writeObject(it.next());
		}
		o.writeByte((byte) 0);
	}
	
	public static <X> void file2coll(Collection<X> set, File f) throws Exception {
		InputStream in = new FileInputStream(f);
		IStream ins = new IStream(new BufferedInputStream(in));
		WNSerializer.stream2coll(set, ins);
		ins.close();
		try {
			in.close();
		} catch(Throwable t) {}
	}
	
	public static <X> void stream2coll(Collection<X> set, IStream s) throws Exception {
		int l = s.readInt();
		if(l != WNSerializer.SET_PREFIX) {
			throw new Exception("Not SET format!");
		}
		WNSerializer.stream2collWithoutPrefix(set, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> void stream2collWithoutPrefix(Collection<X> set, IStream s) throws Exception {
		byte b;
		int i = 0;
		while((b = s.readByte()) > 0) {
			set.add((X) WNDBDecoder.readObject(s, b, i, 0));
			i++;
		}
	}
	
	
	
	/*** Map ***/
	
	public static void map2file(File f, Map<?,?> map) throws Exception {
		OutputStream out = new FileOutputStream(f);
		OStream ost = new OStream(new BufferedOutputStream(out));
		WNSerializer.map2stream(ost, map);
		ost.close();
		try {
			out.close();
		} catch(Throwable t) {}
	}
	
	public static void map2stream(OStream o, Map<?,?> map) throws Exception {
		o.writeInt(WNSerializer.MAP_PREFIX);
		WNSerializer.map2streamWithoutPrefix(o, map);
	}
	
	public static void map2streamWithoutPrefix(OStream o, Map<?,?> map) throws Exception {
		Iterator<? extends Entry<?,?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?,?> ent = it.next();
			o.writeObject(ent.getKey());
			o.writeObject(ent.getValue());
		}
		o.writeByte((byte) 0);
	}
	
	public static <X,Y> void file2map(Map<X,Y> map, File f) throws Exception {
		InputStream in = new FileInputStream(f);
		IStream ins = new IStream(new BufferedInputStream(in));
		WNSerializer.stream2map(map, ins);
		ins.close();
		try {
			in.close();
		} catch(Throwable t) {}
	}
	
	public static <X,Y> void stream2map(Map<X,Y> map, IStream s) throws Exception {
		int l = s.readInt();
		if(l != WNSerializer.MAP_PREFIX) {
			throw new Exception("Not MAP format!");
		}
		WNSerializer.stream2mapWithoutPrefix(map, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> void stream2mapWithoutPrefix(Map<X,Y> map, IStream s) throws Exception {
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
