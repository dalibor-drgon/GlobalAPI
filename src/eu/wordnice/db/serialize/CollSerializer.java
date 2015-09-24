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

package eu.wordnice.db.serialize;

import java.io.File;
import java.io.IOException;
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

import eu.wordnice.streams.Input;
import eu.wordnice.streams.Output;
import eu.wordnice.streams.InputAdv;
import eu.wordnice.streams.OutputAdv;
import gnu.trove.map.hash.THashMap;

public class CollSerializer {
	
	public static final int ARR_PREFIX = 0x1BABE101;
	public static final int MAP_PREFIX = 0x1BABE201;
	
	
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
	
	public static void collarray2file(File f, Object[] arr)
			throws SerializeException, IOException {
		Output ost = OutputAdv.forFile(f);
		CollSerializer.collarray2stream(ost, arr, 0, arr.length);
		ost.close();
	}
	
	public static void collarray2file(File f, Object[] arr, int off, int len)
			throws SerializeException, IOException {
		Output ost = OutputAdv.forFile(f);
		CollSerializer.collarray2stream(ost, arr, off, len);
		ost.close();
	}
	
	public static void collarray2stream(Output o, Object[] arr, int off, int len)
			throws SerializeException, IOException {
		o.writeInt(CollSerializer.ARR_PREFIX);
		CollSerializer.collarray2streamWithoutPrefix(o, arr, off, len);
	}
	
	public static void collarray2streamWithoutPrefix(Output o, Object[] arr, int off, int len)
			throws SerializeException, IOException {
		o.writeInt(len);
		len += off;
		for(; off < len; off++) {
			o.writeObject(arr[off]);
		}
	}
	
	public static <X> X[] file2array(Class<X> clz, File f)
			throws SerializeException, IOException {
		Input ins = InputAdv.forFile(f);
		X[] arr = CollSerializer.stream2array(clz, ins);
		ins.close();
		return arr;
	}
	
	public static <X> X[] stream2array(Class<X> clz, Input s)
			throws SerializeException, IOException {
		int l = s.readInt();
		if(l == -1) {
			return null;
		}
		if(l != CollSerializer.ARR_PREFIX) {
			throw new BadFilePrefixException("Not ARRAY!");
		}
		return CollSerializer.stream2arrayWithoutPrefix(clz, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] stream2arrayWithoutPrefix(Class<X> clz, Input s)
			throws SerializeException, IOException {
		int len = s.readInt();
		if(len < 0) {
			return null;
		}
		X[] arr = (X[]) Array.newInstance(clz, len);
		for(int i = 0; i < len; i++) {
			arr[i] = (X) s.readObject();
		}
		return arr;
	}
	
	
	/*** Iterable & Collection ***/
	
	public static void coll2file(File f, Collection<?> col)
			throws SerializeException, IOException {
		Output ost = OutputAdv.forFile(f);
		CollSerializer.coll2stream(ost, col.iterator(), col.size());
		ost.close();
	}
	
	public static void coll2file(File f, Iterator<?> it, int size)
			throws SerializeException, IOException {
		Output ost = OutputAdv.forFile(f);
		CollSerializer.coll2stream(ost, it, size);
		ost.close();
	}
	
	public static void coll2stream(Output o, Iterator<?> it, int size)
			throws SerializeException, IOException {
		o.writeInt(CollSerializer.ARR_PREFIX);
		CollSerializer.coll2streamWithoutPrefix(o, it, size);
	}
	
	public static void coll2streamWithoutPrefix(Output o, Iterator<?> it, int size)
			throws SerializeException, IOException {
		o.writeInt(size);
		if(size == 0) {
			return;
		}
		int i = 0;
		while(it.hasNext()) {
			o.writeObject(it.next());
			i++;
			if(i == size) {
				return;
			}
		}
		while(i != size) {
			i++;
			o.writeObject(null);
		}
	}
	
	public static <X> Collection<X> file2coll(Collection<X> col, File f)
			throws SerializeException, IOException {
		Input ins = InputAdv.forFile(f);
		col = CollSerializer.stream2coll(col, ins);
		ins.close();
		return col;
	}
	
	public static <X> Collection<X> stream2coll(Collection<X> col, Input s)
			throws SerializeException, IOException {
		int l = s.readInt();
		if(l == -1) {
			return null;
		}
		if(l != CollSerializer.ARR_PREFIX) {
			throw new BadFilePrefixException("Not ARRAY!");
		}
		return CollSerializer.stream2collWithoutPrefix(col, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> Collection<X> stream2collWithoutPrefix(Collection<X> col, Input s)
			throws SerializeException, IOException {
		int len = s.readInt();
		if(len < 0) {
			return null;
		}
		if(col == null) {
			col = new ArrayList<X>(len);
		} else if(col instanceof ArrayList) {
			((ArrayList<?>) col).ensureCapacity(col.size() + len);
		}
		for(int i = 0; i < len; i++) {
			col.add((X) s.readObject(i, -1));
		}
		return col;
	}
	
	
	
	/*** Map ***/
	
	public static void map2file(File f, Map<?,?> map)
			throws SerializeException, IOException {
		Output ost = OutputAdv.forFile(f);
		CollSerializer.map2stream(ost, map);
		ost.close();
	}
	
	public static void map2stream(Output o, Map<?,?> map)
			throws SerializeException, IOException {
		o.writeInt(CollSerializer.MAP_PREFIX);
		CollSerializer.map2streamWithoutPrefix(o, map);
	}
	
	public static void map2streamWithoutPrefix(Output o, Map<?,?> map)
			throws SerializeException, IOException {
		int sz = map.size();
		o.writeInt(sz);
		if(sz == 0) {
			return;
		}
		int i = 0;
		Iterator<? extends Entry<?,?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?,?> ent = it.next();
			o.writeObject(ent.getKey());
			o.writeObject(ent.getValue());
			i++;
			if(i == sz) {
				return;
			}
		}
		while(i != sz) {
			i++;
			o.writeObject(null);
		}
	}
	
	public static <X,Y> Map<X,Y> file2map(Map<X,Y> map, File f)
			throws SerializeException, IOException {
		Input ins = InputAdv.forFile(f);
		Map<X,Y> ret = CollSerializer.stream2map(map, ins);
		ins.close();
		return ret;
	}
	
	public static <X,Y> Map<X,Y> stream2map(Map<X,Y> map, Input s)
			throws SerializeException, IOException {
		int l = s.readInt();
		if(l == -1) {
			return null;
		}
		if(l != CollSerializer.MAP_PREFIX) {
			throw new BadFilePrefixException("Not MAP!");
		}
		return CollSerializer.stream2mapWithoutPrefix(map, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> Map<X,Y> stream2mapWithoutPrefix(Map<X,Y> map, Input s)
			throws SerializeException, IOException {
		int i = 0;
		int sz = s.readInt();
		if(map == null) {
			map = new THashMap<X, Y>(sz);
		}
		while(i != sz) {
			X key = (X) s.readObject(i, 0);
			Y val = (Y) s.readObject(i, 1);
			map.put(key, val);
			i++;
		}
		return map;
	}
}
