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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class CollResSet extends SimpleResSet implements ResSetDB {

	public Collection<Map<String, Object>> set;
	public Iterator<Map<String, Object>> it;
	public Map<String, Object> cur;

	public CollResSet() { }

	public CollResSet(Collection<Map<String, Object>> set) {
		this.set = set;
		this.it = set.iterator();
	}
	
	public Map<String, Object> getCurrent() {
		return this.cur;
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
		return this.set.size();
	}
	
	@Override
	public void reset() {
		this.it = this.set.iterator();
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
		return 0;
	}


}
