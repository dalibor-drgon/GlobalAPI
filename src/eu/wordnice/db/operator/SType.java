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

package eu.wordnice.db.operator;

import java.util.Comparator;

import eu.wordnice.api.ByteChar;

public enum SType {
	
	/**
	 * Ascending = From the smallest to the biggest
	 */
	ASC_IC("ASC", "COLLATE NOCASE ASC", "ASC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			if(o1 instanceof CharSequence && o2 instanceof CharSequence) {
				CharSequence s1 = (CharSequence) o1;
				CharSequence s2 = (CharSequence) o2;
				int n1 = s1.length();
				int n2 = s2.length();
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					char c1 = s1.charAt(i);
					char c2 = s2.charAt(i);
					if(c1 != c2) {
						char h1 = c1;
						char h2 = c2;
						c1 = Character.toUpperCase(c1);
						c2 = Character.toUpperCase(c2);
						if(c1 != c2) {
							c1 = Character.toLowerCase(c1);
							c2 = Character.toLowerCase(c2);
							if(c1 != c2) {
								return c1 - c2;
							}
						} else {
							return h1 - h2;
						}
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof byte[] && o2 instanceof byte[]) {
				byte[] b1 = (byte[]) o1;
				byte[] b2 = (byte[]) o2;
				int n1 = b1.length;
				int n2 = b2.length;
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					int c1 = b1[i];
					int c2 = b2[i];
					if(c1 != c2) {
						int h1 = c1;
						int h2 = c2;
						c1 = ByteChar.toLower(c1);
						c2 = ByteChar.toLower(c2);
						if(c1 != c2) {
							return c1 - c2;
						} else {
							return h1 - h2;
						}
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof Number && o2 instanceof Number) {
				double d1 = ((Number) o1).doubleValue();
				double d2 = ((Number) o2).doubleValue();
				if(d1 > d2) {
					return 1;
				} else if(d1 < d2) {
					return -1;
				} else {
					return 0;
				}
			}
			
			if(o1 != null && o2 != null) {
				return o1.hashCode() - o2.hashCode();
			} else if(o1 == null && o2 == null) {
				return 0;
			} else if(o2 == null) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}),
	
	/**
	 * Descending = From the biggest to the smallest
	 */
	DESC_IC("DESC", "COLLATE NOCASE DESC", "DESC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			return SType.ASC_IC.getComp().compare(o2, o1);
		}
		
	}),
	
	/**
	 * Case-insensitive
	 * Ascending = From the smallest to the biggest
	 */
	ASC("COLLATE utf8_bin ASC", "ASC", "ASC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			if(o1 instanceof CharSequence && o2 instanceof CharSequence) {
				CharSequence s1 = (CharSequence) o1;
				CharSequence s2 = (CharSequence) o2;
				int n1 = s1.length();
				int n2 = s2.length();
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					char c1 = s1.charAt(i);
					char c2 = s2.charAt(i);
					if(c1 != c2) {
						return c1 - c2;
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof byte[] && o2 instanceof byte[]) {
				byte[] b1 = (byte[]) o1;
				byte[] b2 = (byte[]) o2;
				int n1 = b1.length;
				int n2 = b2.length;
				int min = Math.min(n1, n2);
				for(int i = 0; i < min; i++) {
					byte c1 = b1[i];
					byte c2 = b2[i];
					if(c1 != c2) {
						return c1 - c2;
					}
				}
				return n1 - n2;
			}
			if(o1 instanceof Number && o2 instanceof Number) {
				double d1 = ((Number) o1).doubleValue();
				double d2 = ((Number) o2).doubleValue();
				if(d1 > d2) {
					return 1;
				} else if(d1 < d2) {
					return -1;
				} else {
					return 0;
				}
			}
			
			if(o1 != null && o2 != null) {
				return o1.hashCode() - o2.hashCode();
			} else if(o1 == null && o2 == null) {
				return 0;
			} else if(o2 == null) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}),
	
	/**
	 * Case-insensitive
	 * Descending = From the biggest to the smallest
	 */
	DESC("COLLATE utf8_bin DESC", "DESC", "DESC", new Comparator<Object>() {

		@Override
		public int compare(Object o1, Object o2) {
			return SType.ASC.getComp().compare(o2, o1);
		}
		
	});
	
	/**
	 * SQL ORDER BY syntax for string
	 */
	public String sql_str;
	
	/**
	 * SQLite ORDER BY syntax for string
	 */
	public String sqlite_str;
	
	/**
	 * SQL* ORDER BY syntax for any object
	 */
	public String sql;
	
	/**
	 * Comparision of values
	 */
	public Comparator<Object> comp;
	
	/**
	 * Internal creation of SType
	 * 
	 * @param sql SQL for text and numbers
	 * @param sqlite SQLite for text and numbers
	 * @param comp Comparision of values
	 */
	SType(String sql_str, String sqlite_str, String sql, Comparator<Object> comp) {
		this.sql_str = sql_str;
		this.sqlite_str = sqlite_str;
		this.sql = sql;
		this.comp = comp;
	}
	
	/**
	 * @return Comparator for values
	 */
	public Comparator<Object> getComp() {
		return this.comp;
	}
	
}
