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

package wordnice.db.results;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import wordnice.coll.ImmArray;
import wordnice.coll.ImmIter;
import wordnice.coll.ImmMapIterPair;
import wordnice.db.DatabaseException;
import wordnice.db.RawUnsupportedException;
import wordnice.db.operator.AndOr;
import wordnice.db.operator.Limit;
import wordnice.db.operator.Sort;
import wordnice.db.serialize.BadResultException;
import wordnice.db.serialize.SerializeException;
import wordnice.streams.IUtils;
import wordnice.streams.OUtils;

public class MapsResSet extends ObjectResSet implements ResSetDB {

	public List<Map<String, Object>> list;
	public ListIterator<Map<String, Object>> it;
	public Map<String, Object> cur;

	
	/**
	 * Create empty MapsResSet
	 */
	public MapsResSet() {
		this.list = new ArrayList<Map<String, Object>>();
		this.first();
	}
	
	public MapsResSet(ResSet rs) throws DatabaseException, SQLException {
		this.list = new ArrayList<Map<String, Object>>();
		this.insertAll(rs);
		this.first();
	}

	/**
	 * @param list Values
	 */
	public MapsResSet(List<Map<String, Object>> list) {
		this.list = list;
		this.first();
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
	public void update(Map<String, Object> vals) throws DatabaseException, SQLException {
		Map<String, Object> keys = new HashMap<String, Object>();
		keys.putAll(this.cur);
		keys.putAll(vals);
		this.it.set(keys);
	}
	
	@Override
	public void updateAll(Map<String, Object> vals) throws DatabaseException, SQLException {
		this.it.set(vals);
	}

	@Override
	public void insert(Map<String, Object> vals) throws DatabaseException, SQLException {
		if(this.it != null) {
			this.it.add(vals);
		} else {
			this.list.add(vals);
		}
	}
	
	@Override
	public void insertAll(Collection<Map<String, Object>> vals)
			throws DatabaseException, SQLException {
		Iterator<Map<String, Object>> it = vals.iterator();
		if(this.list instanceof ArrayList) {
			((ArrayList<?>) this.list).ensureCapacity(this.list.size() + vals.size());
		}
		while(it.hasNext()) {
			this.insert(it.next());
		}
	}

	@Override
	public void insertAll(Collection<String> columns,
			Collection<Collection<Object>> vals) throws DatabaseException, SQLException {
		Iterator<Collection<Object>> it = vals.iterator();
		List<String> cols = new ImmArray<String>(columns.toArray(new String[0]));
		if(this.list instanceof ArrayList) {
			((ArrayList<?>) this.list).ensureCapacity(this.list.size() + vals.size());
		}
		while(it.hasNext()) {
			Collection<Object> cur = it.next();
			int cursize = Math.min(cur.size(), cols.size());
			this.insert(new ImmMapIterPair<String, Object>(cols, new ImmIter<Object>(cur, cursize), cursize));
		}
	}
	
	public void insertAll(ResSet rs)
			throws DatabaseException, SQLException {
		while(rs.next()) {
			this.insert(rs.getEntries());
		}
	}

	@Override
	public void remove() throws DatabaseException, SQLException {
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
	public void updateRaw(Collection<Object> values) throws RawUnsupportedException {
		throw new RawUnsupportedException();
	}

	@Override
	public void insertRaw(Collection<Object> values) throws RawUnsupportedException {
		throw new RawUnsupportedException();
	}
	
	@Override
	public void insertRawAll(Collection<Collection<Object>> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException, SQLException {
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

	@Override
	public ResSetDBSnap getSnapshot() {
		return new MapResSetSnapshot(this);
	}
	
	/**
	 * Database snapshot
	 * 
	 * @author wordnice
	 */
	protected class MapResSetSnapshot extends MapsResSet implements ResSetDBSnap {
		
		protected MapsResSet orig;
		
		protected MapResSetSnapshot(MapsResSet orig) {
			this.orig = orig;
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			list.addAll(orig.list);
			this.list = list;
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
	public void write(OutputStream out) throws SerializeException, IOException {
		OUtils.serializeColl(out, this.list);
	}

	@Override
	public void read(InputStream in) throws SerializeException, IOException {
		this.list = (List<Map<String, Object>>) IUtils.deserializeColl(in, new ArrayList<Map<String, Object>>());
		if(this.list == null) {
			throw new BadResultException("Readed null collection!");
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

}
