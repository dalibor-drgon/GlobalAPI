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
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import eu.wordnice.api.OStream;
import eu.wordnice.api.Val;

public class WNDBEncoder {

	public static void writeFileData(File f, 
			Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> vals) throws Exception {
		if(vals == null || f == null) {
			throw new NullPointerException("File or values are null!");
		}
		OutputStream fout = new FileOutputStream(f);
		OStream out = new OStream(new BufferedOutputStream(fout));
		WNDBEncoder.writeOutputStreamData(out, vals);
		out.close();
		fout.close();
	}
	
	public static void writeOutputStreamData(OStream out, 
			Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> vals) throws Exception {
		if(out == null || vals == null || 
				vals.one == null || vals.two == null || vals.three == null ||
				vals.one.length < 1 || vals.two.length < 1 || (vals.one.length != vals.two.length)) {
			throw new NullPointerException("File or values are null, or the lengths "
					+ "of names and types do not match!");
		}
		out.writeLong(WNDBDecoder.STATIC_DB_PREFIX);
		int sz = vals.one.length;
		out.writeInt(sz);
		
		for(int b = 0; b < sz; b++) {
			out.writeString(vals.one[b]);
			out.writeByte(vals.two[b].b);
		}
		
		Iterator<Object[]> it = vals.three.iterator();
		int i = 0;
		while(it.hasNext()) {
			Object[] cur = it.next();
			out.writeBoolean(true);
			int i2 = 0;
			for(; i2 < sz; i2++) {
				WNDBEncoder.writeObject(out, cur[i2], vals.two[i2], i, i2);
			}
			i++;
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
			case LIST:
				if(obj == null) {
					out.writeSet(null);
				} else {
					out.writeSet((Collection<?>) obj);
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
