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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eu.wordnice.api.IStream;
import eu.wordnice.api.Val;

public class WNDBDecoder { 

	public static final long STATIC_DB_PREFIX = 0xDEADCAFEBEEFBABEL;
	
	public static Byte[] getVarTypesToBytes(WNDBVarTypes[] set) {
		Byte[] out = new Byte[set.length];
		for(int i = 0; i < set.length; i++) {
			out[i] = set[i].b;
		}
		return out;
	}
	
	public static WNDBVarTypes[] getBytesToVarTypes(Byte[] set) {
		WNDBVarTypes[] out = new WNDBVarTypes[set.length];
		for(int i = 0; i < set.length; i++) {
			out[i] = WNDBVarTypes.getByByte(set[i]);
		}
		return out;
	}

	
	public static Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> readFileRawData(File f) throws Exception {
		InputStream fin = new FileInputStream(f);
		IStream in = new IStream(new BufferedInputStream(fin));
		Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> ret = WNDBDecoder.readInputStreamRawData(in);
		in.close();
		fin.close();
		return ret;
	}
	
	public static Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>> readInputStreamRawData(IStream in) throws Exception {
		long type = in.readLong();
		if (type != WNDBDecoder.STATIC_DB_PREFIX) {
			throw new Exception("Not WNDB format!");
		}

		int bt = in.readInt();
		if(bt < 1) {
			throw new NullPointerException("Invalid returned number of heads: " + bt);
		}

		String[] names = new String[bt];
		WNDBVarTypes[] types = new WNDBVarTypes[bt];
		int i = 0;
		for(i = 0; i < bt; i++) {
			names[i] = in.readString();
			types[i] = WNDBVarTypes.getByByte(in.readByte());
		}
			
		int b = 0;
		i = 0;
		List<Object[]> data = new ArrayList<Object[]>();
		while(in.readByte() == 1) {
			Object[] cur = new Object[bt];
			for(b = 0; b < bt; b++) {
				cur[b] = WNDBDecoder.readObject(in, types[b], i, b);
			}
			data.add(cur);
			i++;
		}
		
		return new Val.ThreeVal<String[], WNDBVarTypes[], List<Object[]>>(names, types, data);
	}
	
	public static Object readObject(IStream in, Byte type, int ri, int vi) throws Exception {
		return WNDBDecoder.readObject(in, WNDBVarTypes.getByByte(type), ri, vi);
	}

	public static Object readObject(IStream in, WNDBVarTypes typ, int ri, int vi) throws Exception {
		switch(typ) {
			case BOOLEAN:
				return in.readBoolean();
			case BYTE:
				return in.readByte();
			case SHORT:
				return in.readShort();
			case INT:
				return in.readInt();
			case LONG:
				return in.readLong();
			case FLOAT:
				return in.readFloat();
			case DOUBLE:
				return in.readDouble();
			case STRING:
				return in.readString();
			case BYTES:
				return in.readBytes();
			case SET:
				return in.readSet(new HashSet<Object>());
			case LIST:
				return in.readSet(new ArrayList<Object>());
			case MAP:
				return in.readMap(new HashMap<Object, Object>());
		}
		throw new Exception("Cannot read object at " + ri + ":" + vi + " - unsupported type " + typ.name());
	}
}
