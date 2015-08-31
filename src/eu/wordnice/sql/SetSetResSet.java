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

package eu.wordnice.sql;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import eu.wordnice.api.Api;

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
		this.it = values.listIterator();
		this.cur = null;
		this.cols = cols;
	}
	
	
	protected void checkSet() {}
	
	protected void changed() {}
	
	public boolean isValueOK(Object val, int i) {
		return true;
	}
	
	public boolean isEntryOK(Object[] vals) {
		return (vals != null && vals.length >= this.cols());
	}
	

	
	private Object[] getCurrent() {
		return this.cur;
	}
	
	@Override
	public void remove() {
		this.it.remove();
	}
	
	public boolean update(Object[] vals) throws Exception {
		this.checkSet();
		if(!this.isEntryOK(vals)) {
			return false;
		}
		this.it.set(vals);
		return true;
	}
	
	public boolean insert(Object[] vals) {
		this.checkSet();
		if(!this.isEntryOK(vals)) {
			return false;
		}
		this.it.add(vals);
		return true;
	}
	
	public boolean insert(String[] names, Object[] data) {
		Object[] nev = new Object[this.cols()];
		int i = 0;
		for(; i < names.length; i++) {
			String name = this.names[i];
			int ind = this.indexOfCol(name);
			if(ind > -1) {
				nev[ind] = data[i];
			}
		}
		return this.insert(nev);
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
		this.it = values.listIterator();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getColumnIndex(String name) {
		if(name == null || this.names == null) {
			return -1;
		}
		this.checkSet();
		return Api.indexOfSafe(name, this.names);
	}

	@Override
	public boolean checkRow(Object[] pair) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRaw() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Object> getRawValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRawValueOK(Object val, int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRawValueOK(Object val, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateRaw(Object[] values) throws IllegalStateException,
			RuntimeException, Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertRaw(Object[] values) throws IllegalStateException,
			RuntimeException, Exception {
		throw new IllegalStateException("Unsupported")
	}

}
