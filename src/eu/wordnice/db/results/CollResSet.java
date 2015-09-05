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

package eu.wordnice.db.results;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
	
	protected Map<String, Object> getCurrent() {
		return this.cur;
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
		return this.getCurrent().get(name);
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
	public int getColumnIndex(String name) {
		return -1;
	}

	@Override
	public boolean checkRow(Object[] pair) {
		return true;
	}

	@Override
	public void update(Object[] pair) throws Exception {
		this.it.set(this.createMap(pair));
	}

	@Override
	public void insert(Object[] pair) throws Exception {
		this.list.add(this.createMap(pair));
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
		return new CollResSetSnapshot();
	}
	
	protected class CollResSetSnapshot extends CollResSet implements ResSetDBSnap {
		
		@SuppressWarnings("unchecked")
		protected CollResSetSnapshot() {
			List<Map<String, Object>> list = null;
			try {
				Class<?> c = CollResSet.this.list.getClass();
				Constructor<?> con = c.getDeclaredConstructor();
				con.setAccessible(true);
				list = (List<Map<String, Object>>) con.newInstance();
			} catch(Throwable t) {}
			if(list == null) {
				list = new ArrayList<Map<String, Object>>();
			}
			list.addAll(CollResSet.this.list);
			this.list = list;
			this.first();
		}
		
		@Override
		public ResSetDB getOriginal() {
			return CollResSet.this;
		}
		
		@Override
		public ResSetDBSnap getSnapshot() {
			return this.getOriginal().getSnapshot();
		}
		
	}


	@Override
	public void sort(Sort[] sorts) throws UnsupportedOperationException {
		// TODO Auto-generated method stub		
	}

}
