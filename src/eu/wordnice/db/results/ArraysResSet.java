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

package eu.wordnice.db.results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import eu.wordnice.api.Api;
import eu.wordnice.api.Val;
import eu.wordnice.cols.ImmArray;
import eu.wordnice.cols.ImmMapPair;
import eu.wordnice.db.ColType;
import eu.wordnice.db.DatabaseException;
import eu.wordnice.db.RawUnsupportedException;
import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Limit;
import eu.wordnice.db.operator.Sort;
import eu.wordnice.db.serialize.BadResultException;
import eu.wordnice.db.serialize.SerializeException;
import eu.wordnice.db.serialize.SerializeUtils;
import eu.wordnice.db.wndb.WNDBDecoder;
import eu.wordnice.db.wndb.WNDBEncoder;
import eu.wordnice.streams.Input;
import eu.wordnice.streams.Output;

public class ArraysResSet extends ObjectResSet implements ResSetDB {

	public static final long PREFIX = 0x12435687CAFF44L;
	
	public List<Object[]> values;
	public String[] names; //Might be null, but should not
	public ListIterator<Object[]> it;
	public Object[] cur;
	public ColType[] types; //Might be null
	public int cols;
	public long nextID = 1; //currentID = nextID; nextID++;

	protected ArraysResSet() {}
	
	public ArraysResSet(String[] names, ResSet rs) throws DatabaseException, SQLException {
		this(names, null, names.length, rs);
	}
	
	public ArraysResSet(String[] names, int names_len, ResSet rs) throws DatabaseException, SQLException {
		this(names, null, names_len, rs);
	}
	
	public ArraysResSet(String[] names, ColType[] types, ResSet rs) throws DatabaseException, SQLException {
		this(names, types, names.length, rs);
	}
	
	public ArraysResSet(String[] names, ColType[] types, int names_len, ResSet rs) throws DatabaseException, SQLException {
		this.values = new ArrayList<Object[]>();
		this.names = names;
		this.types = types;
		this.cols = names.length;
		this.insertAll(rs);
		this.first();
	}
	
	public ArraysResSet(List<Object[]> values, int size) {
		this(values, null, null, size);
	}
	
	public ArraysResSet(List<Object[]> values, String[] names) {
		this(values, names, null, names.length);
	}
	
	public ArraysResSet(List<Object[]> values, String[] names, int names_len) {
		this(values, names, null, names_len);
	}
	
	public ArraysResSet(List<Object[]> values, ColType[] types, String[] names) {
		this(values, names, types, names.length);
	}
	
	public ArraysResSet(List<Object[]> values, String[] names, ColType[] types, int names_len) {
		this.values = values;
		this.names = names;
		this.types = types;
		this.cols = names_len;
		this.first();
	}
	
	/**
	 * Create empty database
	 */
	public ArraysResSet(String[] names) {
		this(new ArrayList<Object[]>(), names, names.length);
	}
	
	/**
	 * Create empty database
	 */
	public ArraysResSet(String[] names, int names_len) {
		this(new ArrayList<Object[]>(), names, names_len);
	}
	
	/**
	 * Create empty database
	 */
	public ArraysResSet(String[] names, ColType[] types) {
		this(new ArrayList<Object[]>(), names, types, names.length);
	}
	
	/**
	 * Create empty database
	 */
	public ArraysResSet(String[] names, ColType[] types, int names_len) {
		this(new ArrayList<Object[]>(), names, types, names_len);
	}
	
	
	protected void checkSet() {}
	
	protected void changed() {}
	

	
	protected Object[] getCurrent() {
		return this.cur;
	}
	
	protected Object[] vals2raw(Iterator<Entry<String, Object>> it, boolean keepOriginal) throws IllegalArgumentException {
		Object[] nev = new Object[this.cols()];
		boolean[] was = new boolean[this.cols()];
		while(it.hasNext()) {
			Entry<String, Object> ent =  it.next();
			String key = ent.getKey();
			Object val = ent.getValue();
			int index = this.getColumnIndex(key);
			if(index == -1) {
				throw new IllegalArgumentException("Illegal column name '" + key + "'!");
			} else if(this.isRawValueOK(index, val)) {
				nev[index] = val;
				was[index] = true;
			} else {
				throw new IllegalArgumentException("Illegal type for column '" + key 
						+ "', value class " + ((val == null) ? null : val.getClass()) + "!");
			}
		}
		if(keepOriginal && this.cur != null) {
			for(int i = 0, n = this.cols; i < n; i++) {
				if(!was[i]) {
					nev[i] = this.cur[i];
				}
			}
		} else if(this.types != null) {
			for(int i = 0, n = this.cols; i < n; i++) {
				if(!was[i]) {
					ColType ct = this.types[i];
					if(ct == ColType.ID) {
						nev[i] = this.nextID++;
					} else {
						nev[i] = ct.def;
					}
				}
			}
		}
		return nev;
	}
	
	public boolean checkRowRaw(Object[] vals) {
		if(vals == null || vals.length < this.cols) {
			return false;
		}
		if(this.types == null) {
			return true;
		}
		for(int i = 0, n = this.cols; i < n; i++) {
			ColType ct = this.types[i];
			Object obj = vals[i];
			if(!ColType.isAssignable(ct, obj)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isRawValueOK(int i, Object val) {
		if(i < 0 || i >= this.cols) {
			return false;
		}
		if(this.types != null) {
			return ColType.isAssignable(this.types[i], val);
		}
		return true;
	}

	public boolean isRawValueOK(String name, Object val) {
		int index = this.getColumnIndex(name);
		if(index == -1) {
			return false;
		}
		return true;
	}
	
	public boolean checkRow(Map<String, Object> vals) {
		Iterator<Entry<String, Object>> it = vals.entrySet().iterator();
		while(it.hasNext()) {
			Entry<String, Object> ent =  it.next();
			String key = ent.getKey();
			Object val = ent.getValue();
			if(!this.isRawValueOK(key, val)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void remove() {
		this.it.remove();
	}
	
	@Override
	public Object getObject(String name) {
		this.checkSet();
		if(this.names == null) {
			return null;
		}
		int index = this.getColumnIndex(name);
		if(index == -1) {
			throw new IllegalArgumentException("Illegal column name '" + name + "'!");
		}
		return this.getCurrent()[index];
	}

	@Override
	public Object getObject(int in) {
		this.checkSet();
		if(in < 0 || in >= this.cols()) {
			throw new IllegalArgumentException("Illegal column index " + in + " / " + this.cols() + "!");
		}
		return this.getCurrent()[in];
	}

	@Override
	public int cols() {
		this.checkSet();
		return this.cols;
	}
	
	@Override
	public int size() {
		this.checkSet();
		if(this.values == null) {
			return 0;
		}
		return this.values.size();
	}
	
	@Override
	public void first() {
		this.it = this.values.listIterator();
		this.cur = null;
	}
	
	@Override
	public boolean forwardOnly() {
		return false;
	}

	@Override
	public boolean next() {
		if(!this.it.hasNext()) {
			this.cur = null;
			return false;
		}
		this.cur = this.it.next();
		return true;
	}
	
	@Override
	public boolean previous() {
		if(!this.it.hasPrevious()) {
			this.cur = null;
			return false;
		}
		this.cur = this.it.previous();
		return true;
	}

	@Override
	public void close() {}

	@Override
	public boolean hasByIndex() {
		return true;
	}

	@Override
	public boolean isTable() {
		return true;
	}

	@Override
	public Collection<String> getKeys() {
		return new ImmArray<String>(this.names);
	}
	
	@Override
	public Collection<Object> getValues() {
		return new ImmArray<Object>(this.getCurrent());
	}
	
	@Override
	public Map<String, Object> getEntries() {
		return new ImmMapPair<String, Object>(this.names, this.getCurrent(), this.cols);
	}

	@Override
	public int getColumnIndex(String name) {
		if(name == null || this.names == null) {
			return -1;
		}
		this.checkSet();
		return Api.indexOf(name, this.names, 0, this.cols);
	}
	
	@Override
	public void update(Map<String, Object> vals) throws DatabaseException, SQLException {
		this.updateRaw(this.vals2raw(vals.entrySet().iterator(), true));
	}
	
	@Override
	public void updateAll(Map<String, Object> vals) throws DatabaseException, SQLException {
		this.updateRaw(this.vals2raw(vals.entrySet().iterator(), false));
	}

	@Override
	public void insert(Map<String, Object> vals) throws DatabaseException, SQLException {
		this.insertRaw(this.vals2raw(vals.entrySet().iterator(), false));
	}
	
	@Override
	public void insertAll(Collection<Map<String, Object>> vals)
			throws DatabaseException, SQLException {
		Iterator<Map<String, Object>> it = vals.iterator();
		if(this.values instanceof ArrayList) {
			((ArrayList<?>) this.values).ensureCapacity(this.values.size() + vals.size());
		}
		while(it.hasNext()) {
			this.insert(it.next());
		}
	}

	@Override
	public void insertAll(Collection<String> columns,
			Collection<Collection<Object>> vals) throws DatabaseException, SQLException {
		Iterator<Collection<Object>> it = vals.iterator();
		String[] cols = columns.toArray(new String[0]);
		int n = cols.length;
		int[] inds = new int[n];
		int[] inds2 = new int[this.cols];
		for(int i = 0; i < n; i++) {
			String key = cols[i];
			int index = this.getColumnIndex(key);
			if(index == -1) {
				throw new IllegalArgumentException("Illegal column name '" + key + "'!");
			}
			for(int i2 = 0; i2 < i; i2++) {
				if(inds[i2] == index) {
					throw new IllegalArgumentException("Duplicated column '" 
							+ key + "' at indexes " + i + " & " + i2 + "!");
				}
			}
			inds[i] = index;
			inds2[index] = i;
		}
		
		if(this.values instanceof ArrayList) {
			((ArrayList<?>) this.values).ensureCapacity(this.values.size() + vals.size());
		}
		
		while(it.hasNext()) {
			Collection<Object> cur = it.next();
			int cursz = cur.size();
			if(cursz < n) {
				throw new IllegalArgumentException("Illegal values size " + cursz);
			}
			Object[] insert_vals = new Object[n];
			Iterator<Object> cur_it = cur.iterator();
			int i = 0;
			while(i < n && cur_it.hasNext()) {
				Object val = it.next();
				int index = inds[i++];
				if(this.isRawValueOK(index, val)) {
					insert_vals[index] = val;
				} else {
					throw new IllegalArgumentException("Illegal type for column " + index 
							+ "'" + cols[i] + "', value class " 
							+ ((val == null) ? null : val.getClass()) + "!");
				}
			}
			if(i != n) {
				throw new IllegalArgumentException("Illegal values size " + i 
						+ ". We have planned to reach " + n + " from " + cursz + "!");
			}
			for(i = 0; i < this.cols; i++) {
				if(inds2[i] == -1) {
					ColType ct = this.types[i];
					if(ct == ColType.ID) {
						insert_vals[i] = this.nextID++;
					} else {
						insert_vals[i] = ct.def;
					}
				}
			}
			if(this.it != null) {
				this.it.add(insert_vals);
			} else {
				this.values.add(insert_vals);
			}
		}
	}
	
	public void insertAll(ResSet rs)
			throws DatabaseException, SQLException {
		while(rs.next()) {
			this.insert(rs.getEntries());
		}
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	public void updateRaw(Object[] values) throws IllegalStateException, DatabaseException, SQLException {
		if(!this.checkRowRaw(values)) {
			throw new IllegalArgumentException("Invalid raw values!");
		}
		this.it.set(values);
	}

	public void insertRaw(Object[] values) throws IllegalStateException, DatabaseException, SQLException {
		if(!this.checkRowRaw(values)) {
			throw new IllegalArgumentException("Invalid raw values!");
		}
		if(this.it != null) {
			this.it.add(values);
		} else {
			this.values.add(values);
		}
	}
	
	@Override
	public void updateRaw(Collection<Object> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException, SQLException {
		this.updateRaw(Api.<Object>toArray(values));
	}

	@Override
	public void insertRaw(Collection<Object> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException, SQLException {
		this.insertRaw(Api.<Object>toArray(values));
	}
	
	@Override
	public void insertRawAll(Collection<Collection<Object>> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException, SQLException {
		Iterator<Collection<Object>> it = values.iterator();
		if(this.values instanceof ArrayList) {
			((ArrayList<?>) this.values).ensureCapacity(this.values.size() + values.size());
		}
		while(it.hasNext()) {
			this.insertRaw(it.next());
		}
	}
	
	/**
	 * Optional, but implemented sort
	 * @see {@link ResSetDB#hasSort()}
	 */
	@Override
	public boolean hasSort() {
		return true;
	}
	
	/**
	 * Optional, but implemented cut
	 * @see {@link ResSetDB#hasCut()}
	 */
	@Override
	public boolean hasCut() {
		return true;
	}
	
	@Override
	public void sort(final Sort[] sorts) throws UnsupportedOperationException {
		Collections.sort(this.values, new Comparator<Object[]>() {

			@Override
			public int compare(Object[] m1, Object[] m2) {
				for(int i = 0, n = sorts.length; i < n; i++) {
					Sort cur = sorts[i];
					int index = ArraysResSet.this.getColumnIndex(cur.key);
					if(index != -1) {
						int cmp = cur.type.comp.compare(m1[index], m2[index]);
						if(cmp != 0) {
							return cmp;
						}
					}
				}
				return 0;
			}
			
		});
		this.first();
	}
	
	@Override
	public void cut(int off, int len) throws UnsupportedOperationException {
		this.values = this.values.subList(off, off + len);
		this.first();
	}

	@Override
	public ResSetDBSnap getSnapshot() {
		return new SetSetResSetSnapshot(this);
	}
	
	/**
	 * Database snapshot
	 * 
	 * @author wordnice
	 */
	protected class SetSetResSetSnapshot extends ArraysResSet implements ResSetDBSnap {
		
		protected ArraysResSet orig;
		
		protected SetSetResSetSnapshot(ArraysResSet orig) {
			this.orig = orig;
			List<Object[]> list = new ArrayList<Object[]>();
			list.addAll(orig.values);
			this.values = list;
			this.names = orig.names;
			this.cols = orig.cols;
			this.nextID = orig.nextID;
			this.types = orig.types;
			this.first();
		}
		
		@Override
		public ResSetDB getOriginal() {
			return this.orig;
		}
		
		@Override
		public ResSetDBSnap getSnapshot() {
			return this.getOriginal().getSnapshot();
		}
		
	}

	@Override
	public void write(Output out) throws SerializeException, IOException {
		if(this.types != null) {
			WNDBEncoder.writeOutputStreamData(out, this.names, this.types, this.values, this.nextID);
		} else {
			out.writeLong(ArraysResSet.PREFIX);
			out.writeInt(this.cols);
			out.writeLong(this.nextID);
			out.writeCollArray(this.names);
			out.writeColl(this.values);
		}
	}

	@Override
	public void read(Input in) throws SerializeException, IOException {
		long prefix = in.readLong();
		if(prefix == ArraysResSet.PREFIX) {
			this.cols = in.readInt();
			this.nextID = in.readLong();
			this.names = in.readColl().toArray(new String[this.cols]);
			this.values = (List<Object[]>) in.readColl(new ArrayList<Object[]>());
			if(this.values == null || this.values.isEmpty()) {
				this.nextID = 1;
				if(this.values == null) {
					throw new BadResultException("Readed null collection!");
				}
				return;
			}
			Iterator<Object[]> it = this.values.iterator();
			int i = 0;
			while(it.hasNext()) {
				Object[] arr = it.next();
				if(arr == null || arr.length <= this.cols) {
					throw new BadResultException("Broken read collection! Array at index " + i + " has " + ((arr == null) ? -1 : arr.length) + " elements!");
				}
				i++;
			}
		} else {
			Val.FourVal<String[], ColType[], List<Object[]>, Long> vals = WNDBDecoder.readInputStreamRawData(in, prefix);
			this.names = vals.one;
			this.types = vals.two;
			this.values = vals.three;
			this.cols = this.names.length;
			this.nextID = (this.values.isEmpty()) ? 1 : vals.four;
			this.first();
		}
	}

	@Override
	public boolean hasSelectDB() {
		return false;
	}

	@Override
	public ResSet selectDB(String[] columns, AndOr where, Limit limit, Sort[] sort)
			throws UnsupportedOperationException, IllegalArgumentException, DatabaseException, SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasUpdateDB() {
		return false;
	}

	@Override
	public void updateDB(Map<String, Object> nevvals, AndOr where, int limit)
			throws UnsupportedOperationException, DatabaseException, SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasDeleteDB() {
		return false;
	}

	@Override
	public void deleteDB(AndOr where, int limit)
			throws UnsupportedOperationException, DatabaseException, SQLException {
		throw new UnsupportedOperationException();
	}
	
	
	/************
	 * Utilities
	 */
	
	public static ArraysResSet loadOrCreate(File f, String[] names, ColType[] types) throws SerializeException, IOException {
		if(f.exists()) {
			File ren = null;
			try {
				ArraysResSet rs = new ArraysResSet();
				SerializeUtils.read(rs, f);
				return rs;
			} catch(Exception e) {
				ren = Api.getFreeName(f);
				System.err.println("Error occured while loading binary database ("
						+ Api.getRealPath(f) + "). Database will be renamed and marked as corruped (" + ren.getName() + "):");
				e.printStackTrace();
			}
			Api.moveFile(f, ren);
		}
		return new ArraysResSet(names, types);
	}
	
	public static ArraysResSet loadOrCreateSafe(File f, String[] names, ColType[] types) {
		if(f.exists()) {
			try {
				ArraysResSet rs = new ArraysResSet();
				SerializeUtils.read(rs, f);
				return rs;
			} catch(Exception e) {
				System.err.println("Error occured while loading WNDB database (" 
						+ Api.getRealPath(f) + "):");
				e.printStackTrace();
			}
			File ren = Api.getFreeName(f);
			try {
				ren.createNewFile();
				if(!f.renameTo(ren)) {
					byte[] buff = new byte[(int) Math.min(f.length(), 8192)];
					InputStream in = new FileInputStream(f);
					OutputStream out = new FileOutputStream(ren);
					int cur = 0;
					while((cur = in.read(buff)) > 0) {
						out.write(buff, 0, cur);
					}
					in.close();
					out.close();
				}
			} catch(Exception e) {
				System.err.println("Error occured while moving corrupted binary database (" 
						+ Api.getRealPath(f) + ") to (" + ren.getName() 
						+ "). Due this unexpected error, all saved data will lost!");
				e.printStackTrace();
				return new ArraysResSet(names, types);
			}
		}
		try {
			ArraysResSet rs = new ArraysResSet(names, types);
			SerializeUtils.write(rs, f);
			return rs;
		} catch(Exception e) {
			System.err.println("Error occured while creating binary database (" 
					+ Api.getRealPath(f) 
					+ "). Due this unexpected error, all saved data will lost!");
			e.printStackTrace();
		}
		return new ArraysResSet(names, types);
	}

}
