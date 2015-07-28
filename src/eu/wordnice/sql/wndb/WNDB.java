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

package eu.wordnice.sql.wndb;

import java.io.File;

import eu.wordnice.api.IStream;
import eu.wordnice.api.OStream;
import eu.wordnice.api.Set;
import eu.wordnice.api.Val;
import eu.wordnice.api.Val.ThreeVal;
import eu.wordnice.sql.SetSetResSet;

public class WNDB extends SetSetResSet {

	public File file = null;
	public Set<WNDBVarTypes> types = null;
	public boolean changed = false;
	public long timeout = 2000;
	
	public WNDB() {
		//For hackers
		this.changed = false;
		this.file = null;
	}

	public WNDB(String file) {
		this(new File(file));
	}

	public WNDB(File file) {
		this.file = file;
	}
	
	
	
	public void save() throws Exception {
		if(this.file == null) {
			return;
		}
		WNDBEncoder.writeFileData(this.file, new Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>>(this.names, this.types, this.values), this.timeout);
		this.changed = false;
	}
	
	public void save(OStream ost) throws Exception {
		WNDBEncoder.writeOutputStreamData(ost, new Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>>(this.names, this.types, this.values));
		this.changed = false;
	}
	
	public void load(IStream ist) throws Exception {
		ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> vals = WNDBDecoder.readInputStreamRawData(ist);
		this.names = vals.one;
		this.types = vals.two;
		this.values = vals.three;
		this.changed = false;
	}
	
	public void saveIfChanged() throws Exception {
		if(this.changed) {
			return;
		}
		this.save();
	}
	
	public WNDBVarTypes getValueType(int i) {
		this.checkSet();
		if(this.types == null) {
			return null;
		}
		return this.types.get(i);
	}
	
	public boolean getChanged() {
		return this.changed;
	}
	
	
	/*** Override ***/
	
	@Override
	public boolean isValueOK(Object val, int i) {
		return WNDBVarTypes.isAssignable(this.getValueType(i), val);
	}
	
	@Override
	public boolean isSetOK(Set<Object> vals) {
		int sz = this.sizeOfHeader();
		if(vals == null || vals.size() < sz) {
			return false;
		}
		Object o;
		for(int n = 0; n < sz; n++) {
			o = vals.get(n);
			if(WNDBVarTypes.isAssignable(this.types.get(n), o) == false) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void changed() {
		this.changed = true;
	}
	
	@Override
	public void checkSet() {
		if(this.values == null || this.names == null || this.types == null) {
			if(this.file == null) {
				throw new NullPointerException("File is null");
			}
			try {
				if(!this.file.exists()) {
					this.file.createNewFile();
				}
				ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> vals = WNDBDecoder.readFileRawData(this.file, this.timeout);
				this.names = vals.one;
				this.types = vals.two;
				this.values = vals.three;
			} catch(Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}
	
	@Override
	public int sizeOfHeader() {
		this.checkSet();
		if(this.names != null) {
			return this.names.size();
		}
		if(this.types != null) {
			return this.types.size();
		}
		if(this.values != null) {
			Set<Object> set = this.values.get(0);
			if(set != null) {
				return set.size();
			}
		}
		return 0;
	}
	
	
	/*** Static CREATE ***/
	
	public static WNDB createWNDB_(File f, Set<String> names, Set<Byte> types, long timeout) throws Exception {
		return WNDB.createWNDB(f, names, WNDBDecoder.getBytesToVarTypes(types), timeout);
	}
	
	public static WNDB createWNDB(File f, Set<String> names, Set<WNDBVarTypes> types, long timeout) throws Exception {
		f.createNewFile();
		Set<Set<Object>> vals = new Set<Set<Object>>();
		Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> threevals = new Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>>(names, types, vals);
		WNDBEncoder.writeFileData(f, threevals, timeout);
		WNDB ret = new WNDB(f);
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		return ret;
	}

}
