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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import eu.wordnice.api.Api;
import eu.wordnice.api.Array;

public class SetSetResSet extends SimpleResSet implements ResSetDB {

	public List<Object[]> values;
	public String[] names;
	public ListIterator<Object[]> it;
	public Object[] cur;
	public int cols;

	public SetSetResSet() {}

	public SetSetResSet(List<Object[]> values, int size) {
		this(values, null, size);
	}
	
	public SetSetResSet(List<Object[]> values, String[] names) {
		this(values, names, names.length);
	}
	
	public SetSetResSet(List<Object[]> values, String[] names, int cols) {
		this.values = values;
		this.names = names;
		this.cols = cols;
		this.reset();
	}
	
	
	protected void checkSet() {}
	
	protected void changed() {}
	

	
	protected Object[] getCurrent() {
		return this.cur;
	}
	
	protected Object[] pair2raw(Object[] pair) throws IllegalArgumentException {
		Object[] nev = new Object[this.cols()];
		for(int i = 0, n = pair.length; i < n; i++) {
			String key = (String) pair[i++];
			Object val = pair[i++];
			int index = this.getColumnIndex(key);
			if(index != -1 && this.isRawValueOK(key, index)) {
				nev[index] = val;
			} else if(index == -1) {
				throw new IllegalArgumentException("Illegal column name '" + key + "'!");
			} else {
				throw new IllegalArgumentException("Illegal type for column '" + key 
						+ "', value class " + ((val == null) ? null : val.getClass()) + "!");
			}
		}
		return nev;
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
	public void reset() {
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
	public boolean hasByName() {
		return this.names != null;
	}

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
		return new Array<String>(this.names);
	}
	
	@Override
	public Collection<Object> getValues() {
		return new Array<Object>(this.getCurrent());
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
	public boolean checkRow(Object[] pair) {
		for(int i = 0, n = pair.length; i < n; i++) {
			String key = (String) pair[i++];
			Object val = pair[i++];
			if(!this.isRawValueOK(key, val)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void update(Object[] pair) throws Exception {
		this.updateRaw(this.pair2raw(pair));
	}

	@Override
	public void insert(Object[] pair) throws Exception {
		this.insertRaw(this.pair2raw(pair));
	}

	@Override
	public boolean isRaw() {
		return true;
	}

	@Override
	public boolean checkRowRaw(Object[] vals) {
		return (vals != null && vals.length >= this.cols());
	}
	
	@Override
	public boolean isRawValueOK(int i, Object val) {
		if(i < 0 || i >= this.cols) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isRawValueOK(String name, Object val) {
		int index = this.getColumnIndex(name);
		if(index == -1) {
			return false;
		}
		return true;
	}

	@Override
	public void updateRaw(Object[] values) throws IllegalStateException, Exception {
		if(!this.checkRowRaw(values)) {
			throw new IllegalArgumentException("Invalid raw values!");
		}
		this.it.set(values);
	}

	@Override
	public void insertRaw(Object[] values) throws IllegalStateException, Exception {
		if(!this.checkRowRaw(values)) {
			throw new IllegalArgumentException("Invalid raw values!");
		}
		this.values.add(values);
	}

	@Override
	public ResSetDBSnap getSnapshot() {
		return new SetSetResSetSnapshot();
	}
	
	protected class SetSetResSetSnapshot extends SetSetResSet implements ResSetDBSnap {
		
		@SuppressWarnings("unchecked")
		protected SetSetResSetSnapshot() {
			List<Object[]> list = null;
			try {
				Class<?> c = SetSetResSet.this.values.getClass();
				Constructor<?> con = c.getDeclaredConstructor();
				con.setAccessible(true);
				list = (List<Object[]>) con.newInstance();
			} catch(Throwable t) {}
			if(list == null) {
				list = new ArrayList<Object[]>();
			}
			list.addAll(SetSetResSet.this.values);
			this.values = list;
			this.reset();
		}
		
		@Override
		public ResSetDB getOriginal() {
			return SetSetResSet.this;
		}
		
		@Override
		public ResSetDBSnap getSnapshot() {
			return this;
		}
		
	}

}
