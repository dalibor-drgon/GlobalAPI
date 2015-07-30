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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import eu.wordnice.api.Map;
import eu.wordnice.api.OStream;
import eu.wordnice.api.Set;
import eu.wordnice.api.Val;
import eu.wordnice.api.threads.TimeoutOutputStream;

public class WNDBEncoder {

	public static void writeFileData(File f, 
			Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> vals, long timeout) throws Exception {
		if(vals == null || f == null) {
			throw new NullPointerException("File or values are null!");
		}
		OutputStream fout = new FileOutputStream(f);
		OStream out = new OStream(new TimeoutOutputStream(new BufferedOutputStream(fout), timeout));
		WNDBEncoder.writeOutputStreamData(out, vals);
		out.close();
		fout.close();
	}
	
	public static void writeOutputStreamData(OStream out, 
			Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> vals) throws Exception {
		if(out == null || vals == null || 
				vals.one == null || vals.two == null || vals.three == null ||
				vals.one.size() < 1 || vals.two.size() < 1 || (vals.one.size() != vals.two.size())) {
			throw new NullPointerException("File or values are null!");
		}
		out.writeLong(WNDBDecoder.STATIC_DB_PREFIX);
		int sz = vals.one.size();
		out.writeInt(sz);
		
		for(int b = 0; b < sz; b++) {
			out.writeString(vals.one.get(b));
			out.writeByte(vals.two.get(b).b);
		}
			
		int maxi = vals.three.size();
		Set<Object> curset;
		int i = 0;
		int i2 = 0;
		for(; i < maxi; i++) {
			curset = vals.three.get(i);
			if(curset == null || curset.size() < sz) {
				throw new Exception("value set at " + i + " is null or has invalid size");
			}
			out.writeBoolean(true);
			for(i2 = 0; i2 < curset.size(); i2++) {
				WNDBEncoder.writeObject(out, curset.get(i2), vals.two.get(i2), i, i2);
			}
		}
		out.writeBoolean(false);
	}
	
	
	public static void writeObject(OStream out, Object obj, Byte type, int ri, int vi) throws Exception {
		WNDBEncoder.writeObject(out, obj, WNDBVarTypes.getByByte(type), ri, vi);
	}

	public static void writeObject(OStream out, Object obj, WNDBVarTypes typ, int ri, int vi) throws Exception {
		if(obj != null && !WNDBVarTypes.isAssignable(typ, obj)) {
			throw new Exception("Cannot write object at " + ri + ":" + vi + ", class " + obj.getClass().getName() + " - is not assignable for type " + typ.name());
		}
		switch(typ) {
			case BOOLEAN:
				if(obj == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean((Boolean) obj);
				}
				return;
			case BYTE:
				if(obj == null) {
					out.writeByte((byte) 0);
				} else {
					out.writeByte((Byte) obj);
				}
				return;
			case SHORT:
				if(obj == null) {
					out.writeShort((short) 0);
				} else {
					out.writeShort((Short) obj);
				}
				return;
			case INT:
				if(obj == null) {
					out.writeInt((int) 0);
				} else {
					out.writeInt((Integer) obj);
				}
				return;
			case LONG:
				if(obj == null) {
					out.writeLong((long) 0);
				} else {
					out.writeLong((Long) obj);
				}
				return;
			case FLOAT:
				if(obj == null) {
					out.writeFloat((float) 0);
				} else {
					out.writeFloat((Float) obj);
				}
				return;
			case DOUBLE:
				if(obj == null) {
					out.writeDouble((double) 0);
				} else {
					out.writeDouble((Double) obj);
				}
				return;
			case STRING:
				if(obj == null) {
					out.writeString(null);
				} else {
					out.writeString((String) obj.toString());
				}
				return;
			case BYTES:
				if(obj == null) {
					out.writeBytes(null);
				} else {
					out.writeBytes((byte[]) obj);
				}
				return;
			case SET:
				if(obj == null) {
					out.writeSet(null);
				} else {
					out.writeSet((Set<?>) obj);
				}
				return;
			case MAP:
				if(obj == null) {
					out.writeMap(null);
				} else {
					out.writeMap((Map<?,?>) obj);
				}
				return;
		}
		throw new Exception("Cannot write object at " + ri + ":" + vi + ", class " + obj.getClass().getName() + " - unsupported type " + typ.name());
	}
	
}
