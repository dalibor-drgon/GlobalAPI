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

package eu.wordnice.db.wndb;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import eu.wordnice.api.IStream;
import eu.wordnice.api.OStream;
import eu.wordnice.api.Val;
import eu.wordnice.db.DBType;
import eu.wordnice.db.results.ResSetDB;
import eu.wordnice.db.results.ResSetDBSnap;
import eu.wordnice.db.results.ArraysResSet;

public class WNDB extends ArraysResSet {

	public File file = null;
	public DBType[] types = null;
	public boolean changed = false;
	
	/**
	 * For hackers
	 */
	public WNDB() {
		this.changed = false;
		this.file = null;
	}
	
	/**
	 * For hackers
	 */
	public WNDB(IStream in) throws Exception {
		this.changed = false;
		this.file = null;
		this.load(in);
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
		WNDBEncoder.writeFileData(this.file, new Val.ThreeVal<String[], DBType[], Iterable<Object[]>>(this.names, this.types, this.values));
		this.changed = false;
	}
	
	public void save(OStream ost) throws Exception {
		WNDBEncoder.writeOutputStreamData(ost, new Val.ThreeVal<String[], DBType[], Iterable<Object[]>>(this.names, this.types, this.values));
		this.changed = false;
	}
	
	public void load(IStream ist) throws Exception {
		Val.ThreeVal<String[], DBType[], List<Object[]>> vals = WNDBDecoder.readInputStreamRawData(ist);
		this.names = vals.one;
		this.types = vals.two;
		this.values = vals.three;
		this.changed = false;
		this.cols = this.names.length;
	}
	
	public void saveIfChanged() throws Exception {
		if(!this.changed) {
			return;
		}
		this.save();
	}
	
	public DBType getValueType(int i) {
		this.checkSet();
		return this.types[i];
	}
	
	public boolean getChanged() {
		return this.changed;
	}
	
	
	/*** Override ***/
	
	@Override
	public boolean isRawValueOK(int i, Object val) {
		return DBType.isAssignable(this.getValueType(i), val);
	}
	
	@Override
	public boolean checkRowRaw(Object[] vals) {
		int sz = this.cols();
		if(vals == null || vals.length < sz) {
			return false;
		}
		int n = 0;
		for(; n < sz; n++) {
			Object o = vals[n];
			if(DBType.isAssignable(this.types[n], o) == false) {
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
			Val.ThreeVal<String[], DBType[], List<Object[]>> vals = WNDBDecoder.readFileRawData(this.file);
			this.names = vals.one;
			this.types = vals.two;
			this.values = vals.three;
			this.first();
		}
	}
	
	@Override
	public ResSetDBSnap getSnapshot() {
		return new WNDBSnapshot(this);
	}
	
	/**
	 * Database snapshot
	 * 
	 * @author wordnice
	 */
	protected class WNDBSnapshot extends WNDB implements ResSetDBSnap {
		
		protected WNDB orig;
		
		@SuppressWarnings("unchecked")
		protected WNDBSnapshot(WNDB orig) {
			this.orig = orig;
			List<Object[]> list = null;
			try {
				Class<?> c = orig.values.getClass();
				Constructor<?> con = c.getDeclaredConstructor();
				con.setAccessible(true);
				list = (List<Object[]>) con.newInstance();
			} catch(Throwable t) {}
			if(list == null) {
				list = new ArrayList<Object[]>();
			}
			list.addAll(orig.values);
			this.values = list;
			this.names = orig.names;
			this.cols = orig.cols;
			this.changed = false;
			this.types = orig.types;
			this.file = null;
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
	
	/*** Static CREATE ***/
	
	public static WNDB createWNDB(File f, String[] names, DBType[] types) throws Exception {
		f.createNewFile();
		List<Object[]> vals = new ArrayList<Object[]>();
		Val.ThreeVal<String[], DBType[], Iterable<Object[]>> threevals = new Val.ThreeVal<String[], DBType[], Iterable<Object[]>>(names, types, vals);
		WNDBEncoder.writeFileData(f, threevals);
		WNDB ret = new WNDB(f);
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		ret.cols = names.length;
		ret.first();
		return ret;
	}
	
	public static WNDB createWNDB(OStream out, String[] names, DBType[] types) throws Exception {
		List<Object[]> vals = new ArrayList<Object[]>();
		Val.ThreeVal<String[], DBType[], Iterable<Object[]>> threevals = new Val.ThreeVal<String[], DBType[], Iterable<Object[]>>(names, types, vals);
		WNDBEncoder.writeOutputStreamData(out, threevals);
		WNDB ret = new WNDB();
		ret.file = null;
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		ret.cols = names.length;
		ret.first();
		return ret;
	}
	
	public static WNDB createEmptyWNDB(String[] names, DBType[] types) {
		WNDB ret = new WNDB();
		ret.file = null;
		ret.names = names;
		ret.types = types;
		ret.values = new ArrayList<Object[]>();
		ret.cols = names.length;
		ret.first();
		return ret;
	}

}
