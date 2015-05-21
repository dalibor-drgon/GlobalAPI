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

public enum WNDBVarTypes {

	BOOLEAN(1), BYTE(2), SHORT(3), INT(4), LONG(5), FLOAT(6), DOUBLE(7), STRING(8), BYTES(9);

	public byte b;

	private WNDBVarTypes(int b) {
		this.b = (byte) b;
	}
	
	public static WNDBVarTypes getByByte(Byte b) {
		//System.out.println("Getting  WNDBVarTypes by byte " + b);
		WNDBVarTypes[] wnsqlvt = WNDBVarTypes.values();
		for(WNDBVarTypes tp : wnsqlvt) {
			if(tp.b == b) {
				return tp;
			}
		}
		//System.out.println("Got null!");
		return null;
	}
	
	public static boolean isAssignable(WNDBVarTypes typ, Object o) {
		if(typ == null) {
			return false;
		}
		if(o == null) {
			return true;
		}
		Class<?> c = o.getClass();
		switch(typ) {
			case BOOLEAN:
				return (c.isAssignableFrom(Boolean.class) || c.isAssignableFrom(boolean.class));
			case BYTE:
				return (c.isAssignableFrom(Byte.class) || c.isAssignableFrom(byte.class));
			case SHORT:
				return (c.isAssignableFrom(Short.class) || c.isAssignableFrom(short.class));
			case INT:
				return (c.isAssignableFrom(Integer.class) || c.isAssignableFrom(int.class));
			case LONG:
				return (c.isAssignableFrom(Long.class) || c.isAssignableFrom(long.class));
			case FLOAT:
				return (c.isAssignableFrom(Float.class) || c.isAssignableFrom(float.class));
			case DOUBLE:
				return (c.isAssignableFrom(Double.class) || c.isAssignableFrom(double.class));
			case STRING:
				return (c.isAssignableFrom(String.class));
			case BYTES:
				return (c.isAssignableFrom(byte[].class) || c.isAssignableFrom(Byte[].class));
		}
		return false;
	}
	
	public static WNDBVarTypes getByObject(Object o) {
		if(o == null) {
			return null;
		}
		return WNDBVarTypes.getByClass(o.getClass());
	}
	
	public static WNDBVarTypes getByClass(Class<?> c) {
		if((c.isAssignableFrom(Boolean.class) || c.isAssignableFrom(boolean.class))) {
			return BOOLEAN;
		}
		
		if((c.isAssignableFrom(Byte.class) || c.isAssignableFrom(byte.class))) {
			return BYTE;
		}
		if((c.isAssignableFrom(Short.class) || c.isAssignableFrom(short.class))) {
			return SHORT;
		}
		if((c.isAssignableFrom(Integer.class) || c.isAssignableFrom(int.class))) {
			return INT;
		}
		if((c.isAssignableFrom(Long.class) || c.isAssignableFrom(long.class))) {
			return LONG;
		}
		if((c.isAssignableFrom(Float.class) || c.isAssignableFrom(float.class))) {
			return FLOAT;
		}
		if((c.isAssignableFrom(Double.class) || c.isAssignableFrom(double.class))) {
			return DOUBLE;
		}
		if((c.isAssignableFrom(String.class))) {
			return STRING;
		}
		if((c.isAssignableFrom(byte[].class) || c.isAssignableFrom(Byte[].class))) {
			return BYTES;
		}
		
		return null;
	}

}
