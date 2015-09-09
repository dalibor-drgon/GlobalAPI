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

package eu.wordnice.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

public enum DBType {

	BOOLEAN(1), BYTE(2), SHORT(3), INT(4), LONG(5), FLOAT(6), DOUBLE(7), STRING(8), BYTES(9),
	SET(11), MAP(12), LIST(13), ARRAY(14);

	public byte b;

	private DBType(int b) {
		this.b = (byte) b;
	}
	
	public static DBType getByByte(Byte b) {
		DBType[] wnsqlvt = DBType.values();
		for(DBType tp : wnsqlvt) {
			if(tp.b == b) {
				return tp;
			}
		}
		return null;
	}
	
	public static boolean isAssignable(DBType typ, Object o) {
		if(typ == null) {
			return false;
		}
		if(o == null) {
			return true;
		}
		Class<?> c = o.getClass();
		switch(typ) {
			case BOOLEAN:
				return Boolean.class.isAssignableFrom(c);
			case BYTE:
				return Byte.class.isAssignableFrom(c);
			case SHORT:
				return Short.class.isAssignableFrom(c);
			case INT:
				return Integer.class.isAssignableFrom(c);
			case LONG:
				return Long.class.isAssignableFrom(c);
			case FLOAT:
				return Float.class.isAssignableFrom(c);
			case DOUBLE:
				return Double.class.isAssignableFrom(c);
			case STRING:
				return String.class.isAssignableFrom(c);
			case BYTES:
				return (byte[].class.isAssignableFrom(c) || Byte[].class.isAssignableFrom(c));
			case SET:
			case LIST:
				return Iterable.class.isAssignableFrom(c);
			case ARRAY:
				return c.isArray();
			case MAP:
				return Map.class.isAssignableFrom(c);
		}
		return false;
	}
	
	public static DBType getByObject(Object o) {
		if(o == null) {
			return BYTES;
		}
		return DBType.getByClass(o.getClass());
	}
	
	public static DBType getByClass(Class<?> c) {
		if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
			return BOOLEAN;
		}
		if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
			return BYTE;
		}
		if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
			return SHORT;
		}
		if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
			return INT;
		}
		if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
			return LONG;
		}
		if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
			return FLOAT;
		}
		if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
			return DOUBLE;
		}
		if(String.class.isAssignableFrom(c)) {
			return STRING;
		}
		if(Byte[].class.isAssignableFrom(c) || byte[].class.isAssignableFrom(c)) {
			return BYTES;
		}
		if(Set.class.isAssignableFrom(c) && !List.class.isAssignableFrom(c)) {
			return SET;
		}
		if(Iterable.class.isAssignableFrom(c)) {
			return LIST;
		}
		if(c.isArray()) {
			return ARRAY;
		}
		if(Map.class.isAssignableFrom(c)) {
			return MAP;
		}
		
		return null;
	}
	
	
	public static byte[] toBytes(DBType[] set) {
		byte[] out = new byte[set.length];
		for(int i = 0; i < set.length; i++) {
			out[i] = set[i].b;
		}
		return out;
	}
	
	public static DBType[] toTypes(byte[] set) {
		DBType[] out = new DBType[set.length];
		for(int i = 0; i < set.length; i++) {
			out[i] = DBType.getByByte(set[i]);
		}
		return out;
	}

}
