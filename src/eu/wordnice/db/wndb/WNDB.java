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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import eu.wordnice.api.Api;
import eu.wordnice.api.Val;
import eu.wordnice.db.ColType;
import eu.wordnice.db.results.ResSetDB;
import eu.wordnice.db.results.ResSetDBSnap;
import eu.wordnice.db.serialize.SerializeException;
import eu.wordnice.streams.Input;
import eu.wordnice.streams.Output;
import eu.wordnice.db.results.ArraysResSet;

public class WNDB extends ArraysResSet {

	public File file = null;
	public ColType[] types = null;
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
	public WNDB(Input in) throws SerializeException, IOException {
		this.changed = false;
		this.file = null;
		this.read(in);
	}

	/**
	 * Create & load database for entered file
	 * 
	 * @param file File where is database saved
	 */
	public WNDB(File file) throws SerializeException, IOException {
		this.changed = false;
		this.file = file;
		this.load();
	}	
	
	
	public void save() throws SerializeException, IOException {
		if(this.file == null) {
			return;
		}
		WNDBEncoder.writeFileData(this.file, this.names, this.types, this.values);
		this.changed = false;
	}
	
	@Override
	public void write(Output ost) throws SerializeException, IOException {
		WNDBEncoder.writeOutputStreamData(ost, this.names, this.types, this.values);
		this.changed = false;
	}
	
	
	@Override
	public void read(Input ist) throws SerializeException, IOException {
		Val.ThreeVal<String[], ColType[], List<Object[]>> vals = WNDBDecoder.readInputStreamRawData(ist);
		this.names = vals.one;
		this.types = vals.two;
		this.values = vals.three;
		this.changed = false;
		this.cols = this.names.length;
		this.first();
	}
	
	public void saveIfChanged() throws SerializeException, IOException {
		if(!this.changed) {
			return;
		}
		this.save();
	}
	
	public ColType getValueType(int i) {
		this.checkSet();
		return this.types[i];
	}
	
	public boolean getChanged() {
		return this.changed;
	}
	
	
	/*** Override ***/
	
	@Override
	public boolean isRawValueOK(int i, Object val) {
		return ColType.isAssignable(this.getValueType(i), val);
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
			if(ColType.isAssignable(this.types[n], o) == false) {
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
	
	public void load() throws SerializeException, IOException {
		if(this.file == null) {
			throw new NullPointerException("File is null");
		}
		Val.ThreeVal<String[], ColType[], List<Object[]>> vals = WNDBDecoder.readFileRawData(this.file);
		this.names = vals.one;
		this.types = vals.two;
		this.values = vals.three;
		this.cols = this.names.length;
		this.first();
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
		
		protected WNDBSnapshot(WNDB orig) {
			this.orig = orig;
			List<Object[]> list = new ArrayList<Object[]>();
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
	
	public static WNDB loadOrCreateWNDB(File f, String[] names, ColType[] types) throws SerializeException, IOException {
		if(f.exists()) {
			try {
				return new WNDB(f);
			} catch(Exception e) {
				System.err.println("Error occured while loading WNDB database ("
						+ Api.getRealPath(f) + "):");
				e.printStackTrace();
			}
			File ren = Api.getFreeName(f);
			f.renameTo(ren);
		}
		return WNDB.createWNDB(f, names, types);
	}
	
	public static WNDB loadOrCreateWNDBSafe(File f, String[] names, ColType[] types) {
		if(f.exists()) {
			try {
				return new WNDB(f);
			} catch(Exception e) {
				System.err.println("Error occured while loading WNDB database (" 
						+ Api.getRealPath(f) + "):");
				e.printStackTrace();
			}
			File ren = Api.getFreeName(f);
			try {
				ren.createNewFile();
				if(!f.renameTo(ren)) {
					byte[] buff = new byte[(int) Math.min(f.length(), 8192)];
					InputStream in = new FileInputStream(f);
					OutputStream out = new FileOutputStream(ren);
					int cur = 0;
					while((cur = in.read(buff)) > 0) {
						out.write(buff, 0, cur);
					}
					in.close();
					out.close();
				}
			} catch(Exception e) {
				System.err.println("Error occured while moving corrupted WNDB database (" 
						+ Api.getRealPath(f) + ") to (" + ren.getName() 
						+ "). Due this unexpected error, all saved data will lost!");
				e.printStackTrace();
				return WNDB.createEmptyWNDB(names, types);
			}
		}
		try {
			return WNDB.createWNDB(f, names, types);
		} catch(Exception e) {
			System.err.println("Error occured while creating WNDB database (" 
					+ Api.getRealPath(f) 
					+ "). Due this unexpected error, all saved data will lost!");
			e.printStackTrace();
		}
		return WNDB.createEmptyWNDB(names, types);
	}
	
	public static WNDB createWNDB(File f, String[] names, ColType[] types) throws SerializeException, IOException {
		f.createNewFile();
		List<Object[]> vals = new ArrayList<Object[]>();
		WNDBEncoder.writeFileData(f, names, types, vals);
		WNDB ret = new WNDB();
		ret.file = f;
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		ret.cols = names.length;
		ret.first();
		return ret;
	}
	
	public static WNDB createEmptyWNDB(Output out, String[] names, ColType[] types) throws SerializeException, IOException {
		List<Object[]> vals = new ArrayList<Object[]>();
		WNDBEncoder.writeOutputStreamData(out, names, types, vals);
		WNDB ret = new WNDB();
		ret.file = null;
		ret.names = names;
		ret.types = types;
		ret.values = vals;
		ret.cols = names.length;
		ret.first();
		return ret;
	}
	
	public static WNDB createEmptyWNDB(String[] names, ColType[] types) {
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
