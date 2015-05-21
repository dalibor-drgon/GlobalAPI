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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import eu.wordnice.api.OStream;
import eu.wordnice.api.Set;
import eu.wordnice.api.Val;

public class WNDBEncoder {

	public static boolean writeFileData(File f, 
			Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> vals) {
		if(vals == null || f == null) {
			return false;
		}
		try {
			OutputStream fout = new FileOutputStream(f);
			OStream out = new OStream(fout);
			boolean ret = WNDBEncoder.writeOutputStreamData(out, vals);
			fout.close();
			return ret;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}
	
	public static boolean writeOutputStreamData(OStream out, 
			Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> vals) {
		if(vals == null) {
			//System.out.println("VALS == NULL!");
			return false;
		}
		//System.out.println("ARGS: " + vals.one + "\n" + vals.two + "\n" + vals.three);
		if(out == null || vals == null || 
				vals.one == null || vals.two == null || vals.three == null ||
				vals.one.size() < 1 || vals.two.size() < 1 || (vals.one.size() != vals.two.size())) {
			return false;
		}
		try {
			out.writeLong(WNDBDecoder.STATIC_DB_PREFIX);
			byte sz = (byte) vals.one.size();
			out.writeByte(sz);
			
			for(byte b = 0; b < sz; b++) {
				out.writeString(vals.one.get(b));
				out.writeByte(vals.two.get(b).b);
			}
			
			int maxi = vals.three.size();
			Set<Object> curset;
			for(int i = 0; i < maxi; i++) {
				curset = vals.three.get(i);
				if(curset == null || curset.size() < sz) {
					throw new Exception("curSet == NULL or has invalid size (is smaller than required)");
				}
				out.writeBoolean(true);
				for(byte b = 0; b < sz; b++) {
					WNDBEncoder.writeObject(out, curset.get(b), vals.two.get(b));
				}
			}
			out.writeBoolean(false);
			
			return true;
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return false;
	}
	
	
	public static void writeObject(OStream out, Object obj, Byte type) throws IOException {
		WNDBEncoder.writeObject(out, obj, WNDBVarTypes.getByByte(type));
	}

	public static void writeObject(OStream out, Object obj, WNDBVarTypes typ) throws IOException {
		//WNDBVarTypes typ = WNDBVarTypes.getByByte(type);
		boolean has = (obj == null || WNDBVarTypes.isAssignable(typ, obj));
		if(has == false) {
			throw new IOException("OBJECT - class\"" + obj.getClass() + "\" - is not assignable for type " + typ.name());
		}
		switch(typ) {
			case BOOLEAN:
				if(obj == null) {
					obj = (Boolean) false;
				}
				out.writeBoolean((Boolean) obj);
				return;
			case BYTE:
				if(obj == null) {
					obj = (Byte) Byte.MIN_VALUE;
				}
				out.writeByte((Byte) obj);
				return;
			case SHORT:
				if(obj == null) {
					obj = (Short) Short.MIN_VALUE;
				}
				out.writeShort((Short) obj);
				return;
			case INT:
				if(obj == null) {
					obj = (Integer) Integer.MIN_VALUE;
				}
				out.writeInt((Integer) obj);
				return;
			case LONG:
				if(obj == null) {
					obj = (Long) Long.MIN_VALUE;
				}
				out.writeLong((Long) obj);
				return;
			case FLOAT:
				if(obj == null) {
					obj = (Float) Float.MIN_VALUE;
				}
				out.writeFloat((Float) obj);
				return;
			case DOUBLE:
				if(obj == null) {
					obj = (Double) Double.MIN_VALUE;
				}
				out.writeDouble((Double) obj);
				return;
			case STRING:
				if(obj == null) {
					obj = (String) "";
				}
				out.writeString((String) obj.toString());
				return;
			case BYTES:
				if(obj == null) {
					obj = (byte[]) new byte[0];
				}
				out.writeBytes((byte[]) obj);
				return;
		}
	}
	
}
