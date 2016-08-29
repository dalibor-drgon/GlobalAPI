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

package wordnice.db.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.map.hash.THashMap;
import wordnice.api.Api;
import wordnice.streams.IUtils;
import wordnice.streams.OUtils;

public class CollSerializer {
	
	public static final int ARR_PREFIX = 0x1BABE101;
	public static final int MAP_PREFIX = 0x1BABE201;
	
	
	/*** Converting ***/
	
	public List<Map<String, Object>> resultsToList(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		String[] cols = new String[columns];
		List<Map<String, Object>> list = Api.createList();
		for(int i = 0; i < columns; i++) {           
			cols[i] = md.getColumnName(i + 1);
		}
		while(rs.next()) {
			Map<String, Object> row = Api.createMap(columns);
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
	
	public static void arrayToFile(File f, Object[] arr)
			throws SerializeException, IOException {
		try(OutputStream ost = Api.output(f)) {
			CollSerializer.arrayToStream(ost, arr, 0, arr.length);
		}
	}
	
	public static void arrayToFile(File f, Object[] arr, int off, int len)
			throws SerializeException, IOException {
		try(OutputStream ost = Api.output(f)) {
			CollSerializer.arrayToStream(ost, arr, off, len);
		}
	}
	
	public static void arrayToStream(OutputStream o, Object[] arr, int off, int len)
			throws SerializeException, IOException {
		OUtils.writeInt(o, CollSerializer.ARR_PREFIX);
		CollSerializer.arrayToStreamWithoutPrefix(o, arr, off, len);
	}
	
	public static void arrayToStreamWithoutPrefix(OutputStream o, Object[] arr, int off, int len)
			throws SerializeException, IOException {
		OUtils.writeInt(o, len);
		len += off;
		for(; off < len; off++) {
			OUtils.serializeKnownObject(o, arr[off]);
		}
	}
	
	public static <X> X[] arrayFromFile(Class<X> clz, File f)
			throws SerializeException, IOException {
		try (InputStream ins = Api.input(f)) {
			return CollSerializer.arrayFromStream(clz, ins);
		}
	}
	
	public static <X> X[] arrayFromStream(Class<X> clz, InputStream s)
			throws SerializeException, IOException {
		int l = IUtils.readInt(s);
		if(l == -1) {
			return null;
		}
		if(l != CollSerializer.ARR_PREFIX) {
			throw new BadPrefixException("Not ARRAY!");
		}
		return CollSerializer.arrayFromStreamWithoutPrefix(clz, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] arrayFromStreamWithoutPrefix(Class<X> clz, InputStream s)
			throws SerializeException, IOException {
		int len = IUtils.readInt(s);
		if(len < 0) {
			return null;
		}
		X[] arr = (X[]) Array.newInstance(clz, len);
		for(int i = 0; i < len; i++) {
			arr[i] = (X) IUtils.deserializeKnownObject(s);
		}
		return arr;
	}
	
	
	/*** Iterable & Collection ***/
	
	public static void collToFile(File f, Collection<?> col)
			throws SerializeException, IOException {
		try(OutputStream ost = Api.output(f)) {
			CollSerializer.collToStream(ost, col.iterator(), col.size());
		}
	}
	
	public static void collToFile(File f, Iterator<?> it, int size)
			throws SerializeException, IOException {
		try(OutputStream ost = Api.output(f)) {
			CollSerializer.collToStream(ost, it, size);
		}
	}
	
	public static void collToStream(OutputStream o, Iterator<?> it, int size)
			throws SerializeException, IOException {
		OUtils.writeInt(o, CollSerializer.ARR_PREFIX);
		CollSerializer.collToStreamWithoutPrefix(o, it, size);
	}
	
	public static void collToStreamWithoutPrefix(OutputStream o, Iterator<?> it, int size)
			throws SerializeException, IOException {
		OUtils.writeInt(o, size);
		if(size == 0) {
			return;
		}
		int i = 0;
		while(it.hasNext()) {
			OUtils.serializeKnownObject(o, it.next());
			i++;
			if(i == size) {
				return;
			}
		}
		while(i != size) {
			i++;
			OUtils.serializeKnownObject(o, null);
		}
	}
	
	public static <X> Collection<X> collFromFile(Collection<X> col, File f)
			throws SerializeException, IOException {
		try (InputStream ins = Api.input(f)) {
			return CollSerializer.collFromStream(col, ins);
		}
	}
	
	public static <X> Collection<X> collFromStream(Collection<X> col, InputStream s)
			throws SerializeException, IOException {
		int l = IUtils.readInt(s);
		if(l == -1) {
			return null;
		}
		if(l != CollSerializer.ARR_PREFIX) {
			throw new BadPrefixException("Not ARRAY!");
		}
		return CollSerializer.collFromStreamWithoutPrefix(col, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> Collection<X> collFromStreamWithoutPrefix(Collection<X> col, InputStream s)
			throws SerializeException, IOException {
		int len = IUtils.readInt(s);
		if(len < 0) {
			return null;
		}
		if(col == null) {
			col = Api.createList(len);
		} else if(col instanceof ArrayList) {
			((ArrayList<?>) col).ensureCapacity(col.size() + len);
		}
		for(int i = 0; i < len; i++) {
			col.add((X) IUtils.deserializeKnownObject(s, i, -1));
		}
		return col;
	}
	
	
	
	/*** Map ***/
	
	public static void mapToFile(File f, Map<?,?> map)
			throws SerializeException, IOException {
		try(OutputStream ost = Api.output(f)) {
			CollSerializer.mapToStream(ost, map);
		}
	}
	
	public static void mapToStream(OutputStream o, Map<?,?> map)
			throws SerializeException, IOException {
		OUtils.writeInt(o, CollSerializer.MAP_PREFIX);
		CollSerializer.mapToStreamWithoutPrefix(o, map);
	}
	
	public static void mapToStreamWithoutPrefix(OutputStream o, Map<?,?> map)
			throws SerializeException, IOException {
		int sz = map.size();
		OUtils.writeInt(o, sz);
		if(sz == 0) {
			return;
		}
		int i = 0;
		Iterator<? extends Entry<?,?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?,?> ent = it.next();
			OUtils.serializeKnownObject(o, ent.getKey());
			OUtils.serializeKnownObject(o, ent.getValue());
			i++;
			if(i == sz) {
				return;
			}
		}
		while(i != sz) {
			i++;
			OUtils.serializeKnownObject(o, null);
		}
	}
	
	public static <X,Y> Map<X,Y> mapFromFile(Map<X,Y> map, File f)
			throws SerializeException, IOException {
		try (InputStream ins = Api.input(f)) {
			return CollSerializer.mapFromStream(map, ins);
		}
	}
	
	public static <X,Y> Map<X,Y> mapFromStream(Map<X,Y> map, InputStream s)
			throws SerializeException, IOException {
		int l = IUtils.readInt(s);
		if(l == -1) {
			return null;
		}
		if(l != CollSerializer.MAP_PREFIX) {
			throw new BadPrefixException("Not MAP!");
		}
		return CollSerializer.mapFromStreamWithoutPrefix(map, s);
	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> Map<X,Y> mapFromStreamWithoutPrefix(Map<X,Y> map, InputStream s)
			throws SerializeException, IOException {
		int i = 0;
		int sz = IUtils.readInt(s);
		if(map == null) {
			map = new THashMap<X, Y>(sz);
		}
		while(i != sz) {
			X key = (X) IUtils.deserializeKnownObject(s, i, 0);
			Y val = (Y) IUtils.deserializeKnownObject(s, i, 1);
			map.put(key, val);
			i++;
		}
		return map;
	}
	
	
	
	/*** UTILS ***/
	
	public static byte[] serializeColl(Collection<?> col) throws SerializeException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OUtils.serializeColl(baos, col);
			return baos.toByteArray();
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static byte[] serializeColl(Iterator<?> it, int len) throws SerializeException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OUtils.serializeColl(baos, it, len);
			return baos.toByteArray();
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static byte[] serializeCollArray(Object[] arr) throws SerializeException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OUtils.serializeCollArray(baos, arr);
			return baos.toByteArray();
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static byte[] serializeCollArray(Object[] arr, int off, int len) throws SerializeException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OUtils.serializeCollArray(baos, arr, off, len);
			return baos.toByteArray();
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static byte[] serializeMap(Map<?, ?> map) throws SerializeException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OUtils.serializeMap(baos, map);
			return baos.toByteArray();
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	
	public static <X> Collection<X> deserializeColl(byte[] bytes, int off, int len) throws SerializeException {
		try {
			return IUtils.deserializeColl(new ByteArrayInputStream(bytes, off, len));
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static <X> Collection<X> deserializeColl(byte[] bytes, int off, int len, Collection<X> col) throws SerializeException {
		try {
			return IUtils.deserializeColl(new ByteArrayInputStream(bytes, off, len), col);
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static <X, Y> Map<X, Y> deserializeMap(byte[] bytes, int off, int len) throws SerializeException {
		try {
			return IUtils.deserializeMap(new ByteArrayInputStream(bytes, off, len));
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
	public static <X, Y> Map<X, Y> deserializeMap(byte[] bytes, int off, int len, Map<X, Y> map) throws SerializeException {
		try {
			return IUtils.deserializeMap(new ByteArrayInputStream(bytes, off, len), map);
		} catch(IOException exc) {
			throw new SerializeException("Unexpected IO error occured", exc);
		}
	}
	
}
