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

import java.util.List;
import java.util.ListIterator;

import eu.wordnice.api.Api;

public class SetSetResSet extends SimpleResSet {

	public List<Object[]> values;
	public String[] names;
	public ListIterator<Object[]> it;
	public Object[] cur;

	public SetSetResSet() {}

	public SetSetResSet(List<Object[]> values) {
		this(values, null);
	}
	
	public SetSetResSet(List<Object[]> values, String[] names) {
		this.values = values;
		this.names = names;
		this.it = values.listIterator();
		this.cur = null;
	}
	
	
	protected void checkSet() {}
	
	protected void changed() {}
	
	public boolean isValueOK(Object val, int i) {
		return true;
	}
	
	public boolean isEntryOK(Object[] vals) {
		return (vals != null && vals.length >= this.sizeOfHeader());
	}
	
	
	public int indexOfHeader(String s) {
		if(s == null) {
			return -1;
		}
		this.checkSet();
		return Api.indexOfSafe(s, this.names);
	}
	
	public Object[] getCurrent() {
		return this.cur;
	}
	
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
	
	public boolean insert(Object... vals) {
		this.checkSet();
		if(!this.isEntryOK(vals)) {
			return false;
		}
		this.it.add(vals);
		return true;
	}
	
	public boolean insert(String[] names, Object[] data) {
		Object[] nev = new Object[this.sizeOfHeader()];
		int i = 0;
		for(; i < names.length; i++) {
			String name = this.names[i];
			int ind = this.indexOfHeader(name);
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
		return this.getCurrent()[this.indexOfHeader(name)];
	}

	@Override
	public Object getObject(int in) {
		this.checkSet();
		return this.getCurrent()[in];
	}

	public int sizeOfHeader() {
		this.checkSet();
		if(this.names != null) {
			return this.names.length;
		}
		return this.values.get(0).length;
	}
	
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
	public boolean close() {
		return true;
	}

}
