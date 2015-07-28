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

import eu.wordnice.api.Api;
import eu.wordnice.api.Set;

public class SetSetResSet extends SimpleResSet {

	public Set<Set<Object>> values;
	public Set<String> names;
	public Integer i = null;

	public SetSetResSet() { }

	public SetSetResSet(Set<Set<Object>> values) {
		this.values = values;
	}
	
	public SetSetResSet(Set<Set<Object>> values, Set<String> names) {
		this.values = values;
		this.names = names;
	}
	
	
	protected void checkSet() {}
	
	protected void changed() {}
	
	public boolean isValueOK(Object val, int i) {
		return true;
	}
	
	public boolean isSetOK(Set<Object> vals) {
		int sz = this.sizeOfHeader();
		if(vals == null || vals.size() < sz) {
			return false;
		}
		return true;
	}

	
	
	public int indexOfHeader(String s) {
		if(s == null) {
			return -1;
		}
		this.checkSet();
		if(this.names == null) {
			return -1;
		}
		return this.names.indexOf(s);
	}
	
	public Set<Object> getEntry() {
		this.checkIndex();
		return this.getEntry(this.i);
	}
	
	public Set<Object> getEntry(int i) {
		if(i < 0) {
			return null;
		}
		this.checkSet();
		if(this.values == null) {
			return null;
		}
		int so = this.size();
		if(i >= so) {
			return null;
		}
		return this.values.get(i);
	}
	
	public boolean delEntry() {
		this.checkIndex();
		return this.delEntry(this.i);
	}
	
	public boolean delEntry(int i) {
		if(i < 0) {
			return false;
		}
		this.checkSet();
		if(this.values == null) {
			return false;
		}
		int so = this.size();
		if(i >= so) {
			return false;
		}
		
		boolean ret = this.values.remove(i);
		if(ret == true) {
			this.changed();
		}
		return ret;
	}
	
	public boolean setEntry(Object... vals) {
		this.checkIndex();
		return this.setEntry(this.i, Api.toSetO(vals));
	}
	
	public boolean setEntry(Set<Object> set) {
		this.checkIndex();
		return this.setEntry(this.i, set);
	}
	
	public boolean setEntry(int i, Object... vals) {
		return this.setEntry(i, Api.toSetO(vals));
	}
	
	public boolean setEntry(int i, Set<Object> set) {
		if(set == null || set.size() < 1 || i < 0) {
			return false;
		}
		this.checkSet();
		if(this.values == null) {
			return false;
		}
		if(this.isSetOK(set) == false) {
			return false;
		}
		int so = this.size();
		if(i == so) {
			return this.insert(set);
		}
		if(i > so) {
			return false;
		}
		
		boolean ret = this.values.set(i, set);
		if(ret == true) {
			this.changed();
		}
		return ret;
	}
	
	public boolean insert(Object... vals) {
		return this.insert((Set<Object>) Api.toSetO(vals));
	}
	
	public boolean insert(Set<Object> set) {
		if(set == null || set.size() < 1) {
			return false;
		}
		this.checkSet();
		if(this.values == null) {
			return false;
		}
		if(this.isSetOK(set) == false) {
			return false;
		}
		
		boolean ret = this.values.addWC(set);
		if(ret == true) {
			this.changed();
		}
		return ret;
	}
	
	public boolean insert(SetSetResSet ssrs, int i) {
		if(ssrs == null || i < 0 || i >= ssrs.size()) {
			return false;
		}
		return this.insert(ssrs.getEntry(i), ssrs.names);
	}
	
	public boolean insert(Set<Object> data, Set<String> names) {
		if(data == null || names == null || data.size() < 1 || data.size() != names.size()) {
			return false;
		}
		
		Object[] nev = new Object[this.sizeOfHeader()];
		String name;
		int ih;
		for(int i = 0; i < names.size(); i++) {
			name = names.get(i);
			ih = this.indexOfHeader(name);
			if(ih > -1) {
				nev[ih] = data.get(i);
			}
		}
		return this.insert(new Set<Object>(nev));
	}
	
	public int insertMulti(SetSetResSet ssrs) {
		if(ssrs == null) {
			return 0;
		}
		return this.insertMulti(ssrs.values, ssrs.names);
	}
	
	public int insertMulti(Set<Set<Object>> vals, Set<String> names) {
		if(vals == null || names == null || vals.size() < 1 || vals.size() != names.size()) {
			return 0;
		}
		
		int ents = vals.size();
		Object[] nev;
		String name;
		int ih;
		int ret = 0;
		Set<Object> data;
		
		for(int ie = 0; ie < ents; ie++) {
			data = vals.get(ie);
			if(data != null) {
				nev = new Object[this.sizeOfHeader()];
				for(int i = 0; i < names.size(); i++) {
					name = names.get(i);
					ih = this.indexOfHeader(name);
					if(ih > -1) {
						nev[ih] = data.get(i);
					}
				}
				if(this.insert(new Set<Object>(nev))) {
					ret++;
				}
			}
		}
		if(ret > 0) {
			this.changed();
		}
		return ret;
	}
	
	public int insertMulti(@SuppressWarnings("unchecked") Set<Object>... vals) {
		return this.insertMulti((Set<Set<Object>>) Api.toSet(vals));
	}
	
	public int insertMulti(Set<Set<Object>> set) {
		if(set == null || set.size() < 1) {
			return 0;
		}
		this.checkSet();
		if(this.values == null) {
			return 0;
		}
		int ret = 0;
		//int se = this.sizeOfHeader();
		Set<Object> cur;
		for(int i = 0; i < set.size(); i++) {
			cur = set.get(i);
			if(cur != null && this.isSetOK(cur)) {
				if(this.values.addWC(cur)) {
					ret++;
				}
			}
		}
		this.changed();
		return ret;
	}
	
	public Set<Set<Object>> getEntriesWhere(String name, Object o) {
		return this.getEntriesWhere(this.indexOfHeader(name), o);
	}
	
	public Set<Set<Object>> getEntriesWhere(int i, Object o) {
		if(i < 0) {
			return null;
		}
		this.checkSet();
		if(this.values == null || this.values.size() < 1 || i >= this.sizeOfHeader()) {
			return null;
		}
		Set<Set<Object>> ret = new Set<Set<Object>>();
		Set<Object> cur;
		Object curo;
		for(int ci = 0; ci < this.values.size(); ci++) {
			cur = this.values.get(ci);
			if(cur != null) {
				curo = cur.get(i);
				if(curo == o || (curo != null && curo.equals(o))) {
					ret.addWC(cur);
				} else if(o instanceof String && curo instanceof String) {
					if(((String) o).equalsIgnoreCase((String) curo)) {
						ret.addWC(cur);
					}
				} else if(o instanceof Number && curo instanceof Number) {
					Number a1 = (Number) o;
					Number a2 = (Number) curo;
					if(a1.longValue() == a2.longValue()) {
						ret.addWC(cur);
					}
				}
			}
		}
		return ret;
	}
	
	public Set<Object> getEntryWhere(String name, Object o) {
		return this.getEntryWhere(this.indexOfHeader(name), o);
	}
	
	public Set<Object> getEntryWhere(int i, Object o) {
		if(i < 0) {
			return null;
		}
		this.checkSet();
		if(this.values == null || this.values.size() < 1 || i >= this.sizeOfHeader()) {
			return null;
		}
		Set<Object> cur;
		Object curo;
		for(int ci = 0; ci < this.values.size(); ci++) {
			cur = this.values.get(ci);
			if(cur != null) {
				curo = cur.get(i);
				if(curo == o || (curo != null && curo.equals(o))) {
					return cur;
				} else if(o instanceof String && curo instanceof String) {
					if(((String) o).equalsIgnoreCase((String) curo)) {
						return cur;
					}
				} else if(o instanceof Number && curo instanceof Number) {
					Number a1 = (Number) o;
					Number a2 = (Number) curo;
					if(a1.longValue() == a2.longValue()) {
						return cur;
					}
				}
			}
		}
		return null;
	}
	
	public Object getValue(String name) {
		int i2 = this.names.indexOf(name);
		return this.getValue(i2);
	}
	
	public Object getValue(int i, String name) {
		int i2 = this.names.indexOf(name);
		return this.getValue(i, i2);
	}
	
	public Object getValue(int i2) {
		this.checkIndex();
		return this.getValue(this.i, i2);
	}
	
	//i = entry number (1...2...3...4..), i2 field number (1 Name, 2 Pass, 3 IP)
	public Object getValue(int i, int i2) {
		if(i < 0 || i2 < 0) {
			return null;
		}
		this.checkSet();
		int sz1 = this.size();
		int sz2 = this.sizeOfHeader();
		if(this.values == null) {
			return null;
		}
		if(i >= sz1 || i2 >= sz2) {
			return null;
		}
		Set<Object> set = this.values.get(i);
		if(set == null) {
			return null;
		}
		return set.get(i2);
	}
	
	public boolean setValue(String name, Object val) {
		this.checkIndex();
		return this.setValue(this.i, this.indexOfHeader(name), val);
	}
	
	public boolean setValue(int i, String name, Object val) {
		return this.setValue(i, this.indexOfHeader(name), val);
	}
	
	public boolean setValue(int i2, Object val) {
		this.checkIndex();
		return this.setValue(this.i, i2, val);
	}
	
	public boolean setValue(int i, int i2, Object val) {
		if(i < 0 || i2 < 0) {
			return false;
		}
		this.checkSet();
		if(this.isValueOK(val, i2) == false) {
			return false;
		}
		int sz1 = this.size();
		int sz2 = this.sizeOfHeader();
		if(i >= sz1 || i2 >= sz2) {
			return false;
		}
		if(this.values == null) {
			return false;
		}
		Set<Object> set = this.values.get(i);
		if(set == null) {
			return false;
		}
		boolean bol = set.set(i2, val);
		if(bol == true) {
			this.changed();
		}
		return bol;
	}
	
	@Override
	public Object getObject(String name) {
		this.checkSet();
		if(this.names == null) {
			return null;
		}
		int i = this.names.indexOf(name);
		return this.getObject(i);
	}

	@Override
	public Object getObject(int in) {
		this.checkSet();
		if(this.values == null) {
			return null;
		}
		this.checkIndex();
		Set<Object> anotherset = this.values.get(this.i);
		if(anotherset != null) {
			return anotherset.get(in);
		}
		return null;
	}

	public int sizeOfHeader() {
		this.checkSet();
		if(this.names != null) {
			return this.names.size();
		}
		if(this.values != null) {
			Set<Object> set = this.values.get(0);
			if(set != null) {
				return set.size();
			}
		}
		return 0;
	}
	
	public int size() {
		this.checkSet();
		if(this.values == null) {
			return 0;
		}
		return this.values.size();
	}
	
	protected void checkIndex() {
		if(this.i == null) {
			this.i = 0;
		}
	}
	
	@Override
	public boolean first() {
		/*this.checkSet();
		if (this.sizeEntries() < 1) {
			return false;
		}
		this.i = 0;
		return true;*/
		return this.setIndex(0);
	}
	
	public boolean setIndex(int i) {
		this.checkSet();
		if(i < 0 || i >= this.size()) {
			return false;
		}
		this.i = i;
		return true;
	}

	@Override
	public boolean next() {
		if(this.i == null) {
			return this.first();
		}
		return this.setIndex(this.i + 1);
	}

	@Override
	public boolean close() {
		return true;
	}

}
