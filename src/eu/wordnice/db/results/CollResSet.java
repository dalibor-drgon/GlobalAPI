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

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import eu.wordnice.db.RawUnsupportedException;
import eu.wordnice.db.operator.Sort;

public class CollResSet extends SimpleResSet implements ResSetDB {

	public List<Map<String, Object>> list;
	public ListIterator<Map<String, Object>> it;
	public Map<String, Object> cur;
	public MapFactory factory;

	public CollResSet() { }

	/**
	 * @param factory MapFactory for creating maps
	 * @param list List with Map, which won't throw ConcurencyException
	 */
	public CollResSet(MapFactory factory, List<Map<String, Object>> list) {
		this.factory = factory;
		this.list = list;
		this.first();
	}
	
	protected Map<String, Object> createMap(Object[] pair) {
		Map<String, Object> vals = this.factory.createMap(pair);
		if(vals != null) {
			return vals;
		}
		vals = this.factory.createMap();
		for(int i = 0, n = pair.length; i < n;) {
			String key = (String) pair[i++];
			Object val = pair[i++];
			vals.put(key, val);
		}
		return vals;
	}

	@Override
	public Object getObject(String name) {
		return this.getEntries().get(name);
	}

	@Override
	public Object getObject(int in) {
		throw new RuntimeException("Indexing is not allowed!");
	}
	
	@Override
	public int size() {
		return this.list.size();
	}
	
	@Override
	public void first() {
		this.it = this.list.listIterator();
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
	public boolean hasByName() {
		return true;
	}

	@Override
	public boolean hasByIndex() {
		return false;
	}

	@Override
	public int cols() {
		return this.getValues().size();
	}

	@Override
	public Collection<String> getKeys() {
		return this.cur.keySet();
	}
	
	@Override
	public Collection<Object> getValues() {
		return this.cur.values();
	}
	
	@Override
	public Map<String, Object> getEntries() {
		return this.cur;
	}

	@Override
	public int getColumnIndex(String name) {
		return -1;
	}

	@Override
	public boolean checkRow(Map<String, Object> vals) {
		return vals != null;
	}

	@Override
	public void update(Map<String, Object> vals) throws Exception {
		this.it.set(vals);
	}

	@Override
	public void insert(Map<String, Object> vals) throws Exception {
		this.list.add(vals);
	}

	@Override
	public void remove() throws SQLException {
		this.it.remove();
	}

	@Override
	public boolean isTable() {
		return false;
	}
	
	
	/**
	 * Raw is unsupported by CollResSet
	 */
	@Override
	public boolean isRaw() {
		return false;
	}

	@Override
	public boolean checkRowRaw(Object[] vals) throws RawUnsupportedException {
		throw new RawUnsupportedException();
	}

	@Override
	public boolean isRawValueOK(int i, Object val)
			throws RawUnsupportedException {
		throw new RawUnsupportedException();
	}

	@Override
	public boolean isRawValueOK(String name, Object val)
			throws RawUnsupportedException {
		throw new RawUnsupportedException();
	}

	@Override
	public void updateRaw(Object[] values) throws RawUnsupportedException,
			IllegalArgumentException, Exception {
		throw new RawUnsupportedException();
	}

	@Override
	public void insertRaw(Object[] values) throws RawUnsupportedException,
			IllegalArgumentException, Exception {
		throw new RawUnsupportedException();
	}
	
	/**
	 * Optional, but implemented sort
	 * @see {@link ResSetDB#hasSort()}
	 */
	@Override
	public boolean hasSort() {
		return true;
	}
	
	@Override
	public void sort(final Sort[] sorts) throws UnsupportedOperationException {
		Collections.sort(this.list, new Comparator<Map<String, Object>>() {

			@Override
			public int compare(Map<String, Object> m1, Map<String, Object> m2) {
				for(int i = 0, n = sorts.length; i < n; i++) {
					Sort cur = sorts[i];
					int cmp = cur.type.comp.compare(m1.get(cur.key), m2.get(cur.key));
					if(cmp != 0) {
						return cmp;
					}
				}
				return 0;
			}
			
		});
		this.first();
	}

	@Override
	public void cut(int off, int len) throws UnsupportedOperationException {
		this.list = this.list.subList(off, off + len);
		this.first();
	}
	

	/**
	 * Map Factory
	 * 
	 * @author wordnice
	 */
	public interface MapFactory {
		
		/**
		 * @return Empty map
		 */
		public Map<String, Object> createMap();
		
		/**
		 * @param pair Keys & Values to insert
		 * 
		 * @return You can return `null` - we will additionaly call {@link #createMap()}
		 *         and put values there manually
		 */
		public Map<String, Object> createMap(Object[] pair);
		
	}


	@Override
	public ResSetDBSnap getSnapshot() {
		return new CollResSetSnapshot(this);
	}
	
	/**
	 * Database snapshot
	 * 
	 * @author wordnice
	 */
	protected class CollResSetSnapshot extends CollResSet implements ResSetDBSnap {
		
		protected CollResSet orig;
		
		@SuppressWarnings("unchecked")
		protected CollResSetSnapshot(CollResSet orig) {
			this.orig = orig;
			List<Map<String, Object>> list = null;
			try {
				Class<?> c = orig.list.getClass();
				Constructor<?> con = c.getDeclaredConstructor();
				con.setAccessible(true);
				list = (List<Map<String, Object>>) con.newInstance();
			} catch(Throwable t) {}
			if(list == null) {
				list = new ArrayList<Map<String, Object>>();
			}
			list.addAll(orig.list);
			this.list = list;
			this.factory = orig.factory;
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

}
