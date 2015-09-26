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

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

import eu.wordnice.api.Api;
import eu.wordnice.api.ByteString;
import eu.wordnice.api.OnlyOnce;
import eu.wordnice.api.OnlyOnce.OnlyOnceLogger;
import eu.wordnice.cols.ImmMapArray;
import eu.wordnice.db.ColType;
import eu.wordnice.db.Database;
import eu.wordnice.db.DatabaseException;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.sql.JDBCSQL;
import eu.wordnice.db.sql.MySQL;
import eu.wordnice.db.sql.SQLite;

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
	 * @param flag {@link WType} {@link Where#flag}
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
	 * @param flag {@link WType} {@link Where#flag}
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
				return ByteString.equals(ent, b, b.length);
			} else {
				return ByteString.equalsIgnoreCase(ent, b, b.length);
			}
		} else if(val instanceof String) {
			String b = (String) val;
			String ent = rs.getString(key);
			if(ent == null || ent.length() < b.length()) {
				return false;
			}
			if(sens) {
				return Api.equals(ent, 0, b, 0, b.length());
			} else {
				return Api.equalsIgnoreCase(ent, 0, b, 0, b.length());
			}
		}
		throw new IllegalArgumentException("Unknown value type " + val.getClass().getName());
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
				return ByteString.equals(ent, ent.length - b_len, b.length, b, 0, b.length);
			} else {
				return ByteString.equalsIgnoreCase(ent, ent.length - b_len, b.length, b, 0, b.length);
			}
		} else if(val instanceof String) {
			String b = (String) val;
			String ent = rs.getString(key);
			if(ent == null || ent.length() < b.length()) {
				return false;
			}
			if(sens) {
				return Api.equals(ent, ent.length() - b.length(), b, 0, b.length());
			} else {
				return Api.equalsIgnoreCase(ent, ent.length() - b.length(), b, 0, b.length());
			}
		}
		throw new IllegalArgumentException("Unknown value type " + val.getClass().getName());
	}
	
	protected static boolean equals(ResSet rs, String key, Object val, boolean sens) {
		if(val == null) {
			return rs.getObject(key) == null;
		}
		if(val instanceof Number) {
			return rs.getDouble(key) == ((Number) val).doubleValue();
		} else if(val instanceof byte[]) {
			if(sens) {
				return ByteString.equals(rs.getBytes(key), (byte[]) val);
			}
			return ByteString.equalsIgnoreCase(rs.getBytes(key), (byte[]) val);
		} else {
			if(!sens && val instanceof String) {
				return ((String) val).equalsIgnoreCase(rs.getString(key));
			}
			return val.equals(rs.getObject(key));
		}
	}
	
	/**
	 * Test
	 */
	public static void main(String... lel_varargs) throws Throwable {
		
		OnlyOnce.debugAll(new OnlyOnceLogger() {

			@Override
			public void info(String str) {
				System.out.println(str);
			}

			@Override
			public void severe(String str) {
				System.err.println(str);
			}
			
		});
		
		//TEST!
		int type = 0;
		//0 - MySQL
		//1 - SQLite
		//2 - WNDB (ResSetDB)
		
		String table = "example4";
		Map<String, ColType> cols = new ImmMapArray<String, ColType>(new Object[] {
				"id", ColType.ID,
				"name", ColType.STRING,
				"pass", ColType.BYTES
		});
		
		Database db = null;
		if(type == 0) {
			JDBCSQL sql = new MySQL("db.mysql-01.gsp-europe.net", "sql_1040", "sql_1040", "2qZ0h1e0nURTWbfiCQpHaz50Not8yuV");
			db = new Database(sql, table, cols);
		} else if(type == 1) {
			db = new Database(new SQLite("./test.sqlite"), table, cols);
		} else {
			db = new Database(new ImmMapArray<String, Object>(new Object[] {
					"type", "wndb",
					"file", "./example.wndb"
			}), cols);
		}
		
		ResSet rs = db.sql.query("show collation where charset = 'utf8'");
		while(rs.next()) {
			System.out.println(rs.getEntries());
		}
		
		
		///
		System.out.print("\nSELECT\n");
		
		rs = db.select(new Or(
				new Where("id", null),
				new Where("id", 0),
				new Where("id", 1)
		));
		while(rs.next()) {
			System.out.println(rs.getString("id") + " / " + rs.getEntries());
		}
		
		
		///
		System.out.print("\nSELECT\n");
		Where.selectAll(db);
		
		
		
		///
		System.out.print("\nINSERT\n");
		
		db.insert(new ImmMapArray<String, Object>(new Object[] {
				"name", "DEADBEEF",
				"pass", new byte[] {1,2,3,4,5,6,7,8}
		}));
		
		
		
		///
		System.out.print("\nSELECT\n");
		Where.selectAll(db);
		
		
		
		///
		System.out.print("\nUPDATE\n");
		
		db.update(new ImmMapArray<String, Object>(new Object[] {
				"name", "DEADCAFE"
		}), new And(
				new Where("name", "DEADBeeF", WType.EQUAL, false),
				new Where("pass", new byte[] {1,2,3,4,5,6,7,8}, true)
		));
		
		
		
		///
		System.out.print("\nSELECT\n");
		Where.selectAll(db);
		
		
		
		///
		System.out.print("\nDELETE\n");
		
		db.delete(new Or(
				new Where("id", null),
				new Where("id", 1, WType.BIGGER_EQUAL)
		));
		
		
		///
		System.out.print("\nSELECT\n");
		Where.selectAll(db);
		
	}
	
	public static void selectAll(Database db) throws SQLException, DatabaseException {
		ResSet rs = db.select(new Sort[] {
				new Sort("id", SType.ASC)
		});
		while(rs.next()) {
			System.out.println(rs.getEntries());
		}
	}
	
}
