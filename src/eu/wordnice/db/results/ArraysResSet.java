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

import java.io.IOException;
import java.lang.reflect.Constructor;
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
import eu.wordnice.api.IStream;
import eu.wordnice.api.OStream;
import eu.wordnice.api.cols.ImmArray;
import eu.wordnice.api.cols.ImmMapPair;
import eu.wordnice.api.serialize.BadResultException;
import eu.wordnice.api.serialize.SerializeException;
import eu.wordnice.db.DatabaseException;
import eu.wordnice.db.RawUnsupportedException;
import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Limit;
import eu.wordnice.db.operator.Sort;

public class ArraysResSet extends ObjectResSet implements ResSetDB {

	public List<Object[]> values;
	public String[] names;
	public ListIterator<Object[]> it;
	public Object[] cur;
	public int cols;

	public ArraysResSet() {}

	public ArraysResSet(List<Object[]> values, int size) {
		this(values, null, size);
	}
	
	public ArraysResSet(List<Object[]> values, String[] names) {
		this(values, names, names.length);
	}
	
	public ArraysResSet(List<Object[]> values, String[] names, int cols) {
		this.values = values;
		this.names = names;
		this.cols = cols;
		this.first();
	}
	
	public ArraysResSet(String[] names) {
		this(new ArrayList<Object[]>(), names, names.length);
	}
	
	public ArraysResSet(String[] names, int cols) {
		this(new ArrayList<Object[]>(), names, cols);
	}
	
	
	protected void checkSet() {}
	
	protected void changed() {}
	

	
	protected Object[] getCurrent() {
		return this.cur;
	}
	
	protected Object[] vals2raw(Map<String, Object> vals, boolean keepOriginal) throws IllegalArgumentException {
		Object[] nev = new Object[this.cols()];
		boolean[] was = new boolean[this.cols()];
		Iterator<Entry<String, Object>> it = vals.entrySet().iterator();
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
		if(keepOriginal) {
			for(int i = 0; i < this.cols; i++) {
				if(!was[i]) {
					nev[i] = this.cur[i];
				}
			}
		}
		return nev;
	}
	
	public boolean checkRowRaw(Object[] vals) {
		return (vals != null && vals.length >= this.cols());
	}
	
	public boolean isRawValueOK(int i, Object val) {
		if(i < 0 || i >= this.cols) {
			return false;
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
		return this.getCurrent()[this.getColumnIndex(name)];
	}

	@Override
	public Object getObject(int in) {
		this.checkSet();
		if(in < 0 || in >= this.cols()) {
			return null;
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
	public boolean next() {
		if(!this.it.hasNext()) {
			this.cur = null;
			return false;
		}
		this.cur = this.it.next();
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
		return Api.indexOf(name, this.names);
	}
	
	@Override
	public void update(Map<String, Object> vals) throws DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().update(vals);
			return;
		}
		
		this.updateRaw(this.vals2raw(vals, true));
	}
	
	@Override
	public void updateAll(Map<String, Object> vals) throws DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().update(vals);
			return;
		}
		
		this.updateRaw(this.vals2raw(vals, false));
	}

	@Override
	public void insert(Map<String, Object> vals) throws DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().insert(vals);
			return;
		}
		
		this.insertRaw(this.vals2raw(vals, false));
	}
	
	@Override
	public void insertAll(Collection<Map<String, Object>> vals)
			throws DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().insertAll(vals);
			return;
		}
		
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
			Collection<Collection<Object>> vals) throws DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().insertAll(columns, vals);
			return;
		}
		
		Iterator<Collection<Object>> it = vals.iterator();
		String[] cols = columns.toArray(new String[0]);
		int n = cols.length;
		int[] inds = new int[n];
		for(int i = 0; i < n; i++) {
			String key = cols[i];
			int index = this.getColumnIndex(key);
			if(index == -1) {
				throw new IllegalArgumentException("Illegal column name '" + key + "'!");
			}
			inds[i] = index;
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
			if(this.it != null) {
				this.it.add(insert_vals);
			} else {
				this.values.add(insert_vals);
			}
		}
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	public void updateRaw(Object[] values) throws IllegalStateException, DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ArraysResSet) ((ResSetDBSnap) this).getOriginal()).updateRaw(values);
			return;
		}
		
		if(!this.checkRowRaw(values)) {
			throw new IllegalArgumentException("Invalid raw values!");
		}
		this.it.set(values);
	}

	public void insertRaw(Object[] values) throws IllegalStateException, DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ArraysResSet) ((ResSetDBSnap) this).getOriginal()).insertRaw(values);
			return;
		}
		
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
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().updateRaw(values);
			return;
		}
		
		this.updateRaw(Api.<Object>toArray(values));
	}

	@Override
	public void insertRaw(Collection<Object> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().insertRaw(values);
			return;
		}
		
		this.insertRaw(Api.<Object>toArray(values));
	}
	
	@Override
	public void insertRawAll(Collection<Collection<Object>> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException {
		if(this instanceof ResSetDBSnap) {
			((ResSetDBSnap) this).getOriginal().insertRawAll(values);
			return;
		}
		
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
	public boolean hasSortCut() {
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
		
		@SuppressWarnings("unchecked")
		protected SetSetResSetSnapshot(ArraysResSet orig) {
			this.orig = orig;
			List<Object[]> list = null;
			try {
				Class<?> c = orig.values.getClass();
				Constructor<?> con = c.getDeclaredConstructor();
				con.setAccessible(true);
				list = (List<Object[]>) con.newInstance();
			} catch(Throwable t) {}
			if(list == null) {
				list = new ArrayList<Object[]>();
			}
			list.addAll(orig.values);
			this.values = list;
			this.names = orig.names;
			this.cols = orig.cols;
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
	public void write(OStream out) throws SerializeException, IOException {
		out.writeColl(this.values);
	}

	@Override
	public void read(IStream in) throws SerializeException, IOException {
		this.values = (List<Object[]>) in.readColl(new ArrayList<Object[]>());
		if(this.values == null) {
			throw new BadResultException("Readed null collection!");
		}
		ListIterator<Object[]> it = this.values.listIterator();
		int i = 0;
		while(it.hasNext()) {
			Object[] arr = it.next();
			if(arr == null || arr.length <= this.cols) {
				throw new BadResultException("Bad readed collection! Array at index " + i + " has " + ((arr == null) ? -1 : arr.length) + " elements!");
			}
			i++;
		}
	}

	@Override
	public boolean hasGet() {
		return false;
	}

	@Override
	public ResSet get(String[] columns, AndOr where, Limit limit, Sort[] sort)
			throws UnsupportedOperationException, IllegalArgumentException, Exception {
		throw new UnsupportedOperationException();
	}

}
