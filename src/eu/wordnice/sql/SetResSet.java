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

import eu.wordnice.api.Map;
import eu.wordnice.api.Set;

public class SetResSet extends SimpleResSet {

	public Set<Map<String, Object>> set;
	public Integer i;

	public SetResSet() { }

	public SetResSet(Set<Map<String, Object>> set) {
		this.set = set;
	}
	
	
	protected void checkSet() {}
	
	protected void checkIndex() {
		if(this.i == null) {
			this.i = 0;
		}
	}

	protected Map<String, ? extends Object> getMap(int i) {
		this.checkSet();
		return this.set.get(i);
	}

	@Override
	public Object getObject(String name) {
		this.checkIndex();
		Map<String, ? extends Object> map = this.getMap(this.i);
		if (map != null) {
			return map.get(name);
		}
		return null;
	}

	@Override
	public Object getObject(int in) {
		this.checkIndex();
		Map<String, ? extends Object> map = this.getMap(this.i);
		if (map != null) {
			return map.getI(in);
		}
		return null;
	}
	
	public int size() {
		return this.set.size();
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
	public boolean first() {
		return this.setIndex(0);
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
		//this.set.clear();
		return true;
	}

}
