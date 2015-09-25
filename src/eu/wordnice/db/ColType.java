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

import java.util.Collection;
import java.util.Map;

public enum ColType {

	BOOLEAN(1, "TINYINT DEFAULT 0", false), BYTE(2, "TINYINT DEFAULT 0", (byte) 0),
	SHORT(3, "SMALLINT DEFAULT 0", (short) 0), INT(4, "INT DEFAULT 0", (int) 0),
	LONG(5, "BIGINT DEFAULT 0", (long) 0),
	
	FLOAT(6, "FLOAT DEFAULT 0.0", (float) 0), DOUBLE(7, "DOUBLE DEFAULT 0.0", (double) 0),
	
	STRING(8, "LONGTEXT CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL", null),
	BYTES(9, "LONGBLOB DEFAULT NULL", null),
	
	MAP(10, "LONGBLOB DEFAULT NULL", null), ARRAY(11, "LONGBLOB DEFAULT NULL", null),
	
	ID(12, "BIGINT PRIMARY KEY", (long) 0);
	
	public static String STRING_SQLITE = "LONGTEXT DEFAULT NULL";

	public byte b;
	public String sql;
	public Object def;
	
	private ColType(int b, String sql, Object def) {
		this.b = (byte) b;
		this.sql = sql;
		this.def = def;
	}
	
	public static ColType getByByte(int b) {
		ColType[] cur = ColType.values();
		if(b <= 0 || b > cur.length) {
			return null;
		}
		return cur[b - 1];
	}
	
	public static boolean isAssignable(ColType typ, Object o) {
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
			case ID:
				return Long.class.isAssignableFrom(c);
			case FLOAT:
				return Float.class.isAssignableFrom(c);
			case DOUBLE:
				return Double.class.isAssignableFrom(c);
			case STRING:
				return String.class.isAssignableFrom(c);
			case BYTES:
				return (byte[].class.isAssignableFrom(c) || Byte[].class.isAssignableFrom(c));
			case ARRAY:
				return Collection.class.isAssignableFrom(c) || c.isArray();
			case MAP:
				return Map.class.isAssignableFrom(c);
		}
		return false;
	}
	
	public static ColType getByObject(Object o) {
		if(o == null) {
			return BYTES;
		}
		return ColType.getByClass(o.getClass());
	}
	
	public static ColType getByClass(Class<?> c) {
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
		if(Collection.class.isAssignableFrom(c) || c.isArray()) {
			return ARRAY;
		}
		if(Map.class.isAssignableFrom(c)) {
			return MAP;
		}
		
		return null;
	}
	
	
	public static byte[] toBytes(ColType[] set) {
		byte[] out = new byte[set.length];
		for(int i = 0; i < set.length; i++) {
			out[i] = set[i].b;
		}
		return out;
	}
	
	public static ColType[] toTypes(byte[] set) {
		ColType[] out = new ColType[set.length];
		for(int i = 0; i < set.length; i++) {
			out[i] = ColType.getByByte(set[i]);
		}
		return out;
	}

}
