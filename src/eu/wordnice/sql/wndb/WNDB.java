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
import java.util.ArrayList;
import java.util.List;

import eu.wordnice.api.IStream;
import eu.wordnice.api.OStream;
import eu.wordnice.api.Val;
import eu.wordnice.sql.SetSetResSet;

public class WNDB extends SetSetResSet {

	public File file = null;
	public WNDBVarTypes[] types = null;
	public boolean changed = false;
	
	/**
	 * For hackers
	 */
	public WNDB() {
		this.changed = false;
		this.file = null;
	}

	/**
	 * Create & load database for entered file
	 * 
	 * @param file File where is database saved
	 * 
	 * @throws Exception When error occured while reading or parsing file
	 */
	public WNDB(File file) throws Exception {
		this.file = file;
		this.load();
	}
	
	
	
	public void save() throws Exception {
		if(this.file == null) {
			return;
		}
		WNDBEncoder.writeFileData(this.file, new Val.ThreeVal<String[], WNDBVarTypes[], Iterable<Object[]>>(this.names, this.types, this.values));
		this.changed = false;
	}
	
	public void save(OStream ost) throws Exception {
		WNDBEncoder.writeOutputStreamData(ost, new Val.ThreeVal<String[], WNDBVarTypes[], Iterable<Object[]>>(this.names, this.types, this.values));
		this.changed = false;
	}
	
	public void load(IStream ist) throws Exception {
		Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> vals = WNDBDecoder.readInputStreamRawData(ist);
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
		return this.types[i];
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
	public boolean isEntryOK(Object[] vals) {
		int sz = this.sizeOfHeader();
		if(vals == null || vals.length < sz) {
			return false;
		}
		int n = 0;
		for(; n < sz; n++) {
			Object o = vals[n];
			if(WNDBVarTypes.isAssignable(this.types[n], o) == false) {
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
	public void checkSet() {}
	
	public void load() throws Exception {
		if(this.values == null || this.names == null || this.types == null) {
			if(this.file == null) {
				throw new NullPointerException("File is null");
			}
			Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> vals = WNDBDecoder.readFileRawData(this.file);
			this.names = vals.one;
			this.types = vals.two;
			this.values = vals.three;
			this.reset();
		}
	}
	
	@Override
	public int cols() {
		this.checkSet();
		if(this.names != null) {
			return this.names.length;
		}
		return this.types.length;
	}
	
	
	/*** Static CREATE ***/
	
	public static WNDB createWNDB_(File f, String[] names, Byte[] types) throws Exception {
		return WNDB.createWNDB(f, names, WNDBDecoder.getBytesToVarTypes(types));
	}
	
	public static WNDB createWNDB(File f, String[] names, WNDBVarTypes[] types) throws Exception {
		f.createNewFile();
		List<Object[]> vals = new ArrayList<Object[]>();
		Val.ThreeVal<String[], WNDBVarTypes[], Iterable<Object[]>> threevals = new Val.ThreeVal<String[], WNDBVarTypes[], Iterable<Object[]>>(names, types, vals);
		WNDBEncoder.writeFileData(f, threevals);
		WNDB ret = new WNDB(f);
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		ret.reset();
		return ret;
	}
	
	
	public static WNDB createWNDB_(OStream out, String[] names, Byte[] types) throws Exception {
		return WNDB.createWNDB(out, names, WNDBDecoder.getBytesToVarTypes(types));
	}
	
	public static WNDB createWNDB(OStream out, String[] names, WNDBVarTypes[] types) throws Exception {
		List<Object[]> vals = new ArrayList<Object[]>();
		Val.ThreeVal<String[], WNDBVarTypes[], Iterable<Object[]>> threevals = new Val.ThreeVal<String[], WNDBVarTypes[], Iterable<Object[]>>(names, types, vals);
		WNDBEncoder.writeOutputStreamData(out, threevals);
		WNDB ret = new WNDB();
		ret.file = null;
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		ret.reset();
		return ret;
	}
	
	public static WNDB createEmptyWNDB_(String[] names, Byte[] types) {
		return WNDB.createEmptyWNDB(names, WNDBDecoder.getBytesToVarTypes(types));
	}
	
	public static WNDB createEmptyWNDB(String[] names, WNDBVarTypes[] types) {
		WNDB ret = new WNDB();
		ret.file = null;
		ret.names = names;
		ret.types = types;
		ret.values = new ArrayList<Object[]>();
		ret.reset();
		return ret;
	}

}
