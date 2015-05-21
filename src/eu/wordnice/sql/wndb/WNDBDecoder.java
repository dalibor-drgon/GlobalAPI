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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import eu.wordnice.api.IStream;
import eu.wordnice.api.Map;
import eu.wordnice.api.Set;
import eu.wordnice.api.Val;

public class WNDBDecoder { 

	public static final long STATIC_DB_PREFIX = 1957294757399235L;
	
	public static Set<Byte> getVarTypesToBytes(Set<WNDBVarTypes> set) {
		if(set == null) {
			return null;
		}
		Set<Byte> ret = new Set<Byte>();
		if(set.size() < 1) {
			return ret;
		}
		
		for(int i = 0; i < set.size(); i++) {
			ret.add(set.get(i).b);
		}
		
		return ret;
	}
	
	public static Set<WNDBVarTypes> getBytesToVarTypes(Set<Byte> set) {
		if(set == null) {
			return null;
		}
		Set<WNDBVarTypes> ret = new Set<WNDBVarTypes>();
		if(set.size() < 1) {
			return ret;
		}
		
		for(int i = 0; i < set.size(); i++) {
			ret.add(WNDBVarTypes.getByByte(set.get(i)));
		}
		
		return ret;
	}

	public static Set<Map<String, Object>> readFileData(File f) {
		try {
			InputStream fin = new FileInputStream(f);
			IStream in = new IStream(fin);
			Set<Map<String, Object>> ret = WNDBDecoder.readInputStreamData(in);
			fin.close();
			return ret;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static Val.TwoVal<Set<String>, Set<Byte>> readInputStreamTypes(IStream in) {
		try {
			long type = in.readLong();
			if (type != WNDBDecoder.STATIC_DB_PREFIX) {
				throw new Exception("Isnt WNSQL stream!");
			}

			byte bt = in.readByte();
			if (bt < 1) {
				return null;
			}

			Set<String> names = new Set<String>();
			Set<Byte> types = new Set<Byte>();
			for (byte i = 0; i < bt; i++) {
				names.addWC(in.readString());
				types.addWC(in.readByte());
			}
			return new Val.TwoVal<Set<String>, Set<Byte>>(names, types);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> readFileRawData(File f) {
		try {
			InputStream fin = new FileInputStream(f);
			IStream in = new IStream(fin);
			Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> ret = WNDBDecoder.readInputStreamRawData(in);
			fin.close();
			return ret;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>> readInputStreamRawData(IStream in) {
		try {
			long type = in.readLong();
			if (type != WNDBDecoder.STATIC_DB_PREFIX) {
				throw new Exception("Isnt WNSQL stream!");
			}

			byte bt = in.readByte();
			if (bt < 1) {
				return null;
			}

			Set<String> names = new Set<String>();
			Set<WNDBVarTypes> types = new Set<WNDBVarTypes>();
			for (byte i = 0; i < bt; i++) {
				names.addWC(in.readString());
				types.addWC(WNDBVarTypes.getByByte(in.readByte()));
			}
			
			byte b;
			Set<Set<Object>> data = new Set<Set<Object>>();
			Set<Object> curset;
			while (in.readByte() == 1) {
				curset = new Set<Object>();
				for(b = 0; b < bt; b++) {
					curset.addWC(WNDBDecoder.readObject(in, types.get(b)));
				}
				data.addWC(curset);
			}
			
			return new Val.ThreeVal<Set<String>, Set<WNDBVarTypes>, Set<Set<Object>>>(names, types, data);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static Set<Set<Object>> readInputStreamValues(IStream in) {
		try {
			long type = in.readLong();
			if (type != WNDBDecoder.STATIC_DB_PREFIX) {
				throw new Exception("Isnt WNSQL stream!");
			}

			byte bt = in.readByte();
			if (bt < 1) {
				return null;
			}

			//Set<String> names = new Set<String>();
			Set<WNDBVarTypes> types = new Set<WNDBVarTypes>();
			for (byte i = 0; i < bt; i++) {
				in.readString();
				types.addWC(WNDBVarTypes.getByByte(in.readByte()));
			}
			
			byte b;
			Set<Set<Object>> data = new Set<Set<Object>>();
			Set<Object> curset;
			while (in.readByte() == 1) {
				curset = new Set<Object>();
				for(b = 0; b < bt; b++) {
					curset.addWC(WNDBDecoder.readObject(in, types.get(b)));
				}
				data.addWC(curset);
			}
			
			return data;
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public static Set<Map<String, Object>> readInputStreamData(IStream in) {
		try {
			long type = in.readLong();
			if (type != WNDBDecoder.STATIC_DB_PREFIX) {
				throw new Exception("Isnt WNSQL stream!");
			}

			byte bt = in.readByte();
			if (bt < 1) {
				return null;
			}

			Set<String> names = new Set<String>();
			Set<WNDBVarTypes> types = new Set<WNDBVarTypes>();
			for (byte i = 0; i < bt; i++) {
				names.addWC(in.readString());
				types.addWC(WNDBVarTypes.getByByte(in.readByte()));
			}

			byte b;
			Set<Map<String, Object>> data = new Set<Map<String, Object>>();
			Map<String, Object> curmap;
			while (in.readByte() == 1) {
				curmap = new Map<String, Object>();
				for(b = 0; b < bt; b++) {
					curmap.addWC(names.get(b), WNDBDecoder.readObject(in, types.get(b)));
				}
				data.addWC(curmap);
			}
			return data;
			
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}
	
	public static Object readObject(IStream in, Byte type) throws IOException {
		return WNDBDecoder.readObject(in, WNDBVarTypes.getByByte(type));
	}

	public static Object readObject(IStream in, WNDBVarTypes typ) throws IOException {
		//WNDBVarTypes typ = WNDBVarTypes.getByByte(type);
		//System.out.println("TYP: " + typ);
		if(typ == null) {}
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
		}
		return null;
	}
}
