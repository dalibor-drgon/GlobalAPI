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

package wordnice.db.operator;

import java.util.regex.Pattern;

import wordnice.api.Nice;
import wordnice.codings.ASCII;
import wordnice.db.results.ResSet;
import wordnice.utils.NiceStringUtils;

public class Where {
	
	/**
	 * Column name / key for val
	 */
	public String key;
	
	/**
	 * Value to find
	 */
	public Object val;
	
	/**
	 * Compare type
	 */
	public WType flag;
	
	/**
	 * Compare case-sensitive
	 */
	public boolean sens;
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 */
	public Where(String key, Object val) {
		this.key = key;
		this.val = val;
		this.flag = WType.EQUAL;
		this.sens = true;
	}
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 * @param flag Where flag {@link WType} {@link Where#flag}
	 */
	public Where(String key, Object val, WType flag) {
		this.key = key;
		this.val = val;
		this.flag = (flag == null) ? WType.EQUAL : flag;
		this.sens = true;
	}
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 * @param sens Compare case-sensitive {@link Where#sens}
	 */
	public Where(String key, Object val, boolean sens) {
		this.key = key;
		this.val = val;
		this.flag = WType.EQUAL;
		this.sens = sens;
	}
	
	/**
	 * Create where comparator
	 * 
	 * @param key Column name / key for val {@link Where#key}
	 * @param val Value to find {@link Where#val}
	 * @param flag Where flag {@link WType} {@link Where#flag}
	 * @param sens Compare case-sensitive {@link Where#sens}
	 */
	public Where(String key, Object val, WType flag, boolean sens) {
		this.key = key;
		this.val = val;
		this.flag = (flag == null) ? WType.EQUAL : flag;
		this.sens = sens;
	}
	
	/**
	 * @param rs ResSet with values to compare
	 * @return `true` if values match with this AndOr
	 */
	public boolean match(ResSet rs) {
		switch(this.flag) {
			case BIGGER:
				if(this.sens) {
					return SType.ASC.comp.compare(rs.getObject(this.key), this.val) > 0;
				}
				return SType.ASC_IC.comp.compare(rs.getObject(this.key), this.val) > 0;
				
			case BIGGER_EQUAL:
				if(this.sens) {
					return SType.ASC.comp.compare(rs.getObject(this.key), this.val) >= 0;
				}
				return SType.ASC_IC.comp.compare(rs.getObject(this.key), this.val) >= 0;
				
			case SMALLER:
				if(this.sens) {
					return SType.ASC.comp.compare(rs.getObject(this.key), this.val) < 0;
				}
				return SType.ASC_IC.comp.compare(rs.getObject(this.key), this.val) < 0;
				
			case SMALLER_EQUAL:
				if(this.sens) {
					return SType.ASC.comp.compare(rs.getObject(this.key), this.val) <= 0;
				}
				return SType.ASC_IC.comp.compare(rs.getObject(this.key), this.val) <= 0;
				
			case START:
				return Where.start(rs, this.key, this.val, this.sens);
				
			case NOT_START:
				return !Where.start(rs, this.key, this.val, this.sens);
				
			case END:
				return Where.end(rs, this.key, this.val, this.sens);
				
			case NOT_END:
				return !Where.end(rs, this.key, this.val, this.sens);
				
			case REGEX:
				return Where.regex(rs, this.key, this.val, this.sens);
				
			case NOT_REGEX:
				return !Where.regex(rs, this.key, this.val, this.sens);
				
			case NOT_EQUAL:
				return !Where.equals(rs, this.key, this.val, this.sens);
				
			case EQUAL:
			default:
				return Where.equals(rs, this.key, this.val, this.sens);
		}
	}
	
	protected static boolean regex(ResSet rs, String key, Object val, boolean sens) {
		String str = rs.getString(key);
		if(val == null) {
			return str == null;
		} else if(str == null) {
			return false;
		}
		if(sens) {
			return Pattern.compile((String) val).matcher(str).find();
		} else {
			return Pattern.compile((String) val, Pattern.CASE_INSENSITIVE).matcher(str).find();
		}
	}
	
	protected static boolean start(ResSet rs, String key, Object val, boolean sens) {
		if(val == null) {
			return rs.getObject(key) == null;
		}
		if(val instanceof byte[]) {
			byte[] b = (byte[]) val;
			byte[] ent = rs.getBytes(key);
			if(ent == null || ent.length < b.length) {
				return false;
			}
			if(sens) {
				return ASCII.equals(ent, b, b.length);
			} else {
				return ASCII.equalsIgnoreCase(ent, b, b.length);
			}
		} else if(val instanceof String) {
			String b = (String) val;
			String ent = rs.getString(key);
			if(ent == null || ent.length() < b.length()) {
				return false;
			}
			if(sens) {
				return NiceStringUtils.equals(ent, 0, b, 0, b.length());
			} else {
				return NiceStringUtils.equalsIgnoreCase(ent, 0, b, 0, b.length());
			}
		}
		throw Nice.illegal("Unknown value type " + val.getClass().getName());
	}
	
	protected static boolean end(ResSet rs, String key, Object val, boolean sens) {
		if(val == null) {
			return rs.getObject(key) == null;
		}
		if(val instanceof byte[]) {
			byte[] b = (byte[]) val;
			byte[] ent = rs.getBytes(key);
			int b_len = b.length;
			if(ent == null || ent.length < b.length) {
				return false;
			}
			if(sens) {
				return ASCII.equals(ent, ent.length - b_len, b.length, b, 0, b.length);
			} else {
				return ASCII.equalsIgnoreCase(ent, ent.length - b_len, b.length, b, 0, b.length);
			}
		} else if(val instanceof String) {
			String b = (String) val;
			String ent = rs.getString(key);
			if(ent == null || ent.length() < b.length()) {
				return false;
			}
			if(sens) {
				return NiceStringUtils.equals(ent, ent.length() - b.length(), b, 0, b.length());
			} else {
				return NiceStringUtils.equalsIgnoreCase(ent, ent.length() - b.length(), b, 0, b.length());
			}
		}
		throw Nice.illegal("Unknown value type " + val.getClass().getName());
	}
	
	protected static boolean equals(ResSet rs, String key, Object val, boolean sens) {
		if(val == null) {
			return rs.getObject(key) == null;
		}
		if(val instanceof Number) {
			return rs.getDouble(key) == ((Number) val).doubleValue();
		} else if(val instanceof byte[]) {
			if(sens) {
				return ASCII.equals(rs.getBytes(key), (byte[]) val);
			}
			return ASCII.equalsIgnoreCase(rs.getBytes(key), (byte[]) val);
		} else {
			if(!sens && val instanceof String) {
				return ((String) val).equalsIgnoreCase(rs.getString(key));
			}
			return val.equals(rs.getObject(key));
		}
	}
	
}
