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

import java.sql.PreparedStatement;
import java.util.regex.Pattern;

import eu.wordnice.api.Api;
import eu.wordnice.api.ByteString;
import eu.wordnice.api.cols.ImmArray;
import eu.wordnice.db.DBType;
import eu.wordnice.db.Database;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.results.ResSetDB;
import eu.wordnice.db.results.ResultResSet;
import eu.wordnice.db.sql.MySQL;
import eu.wordnice.db.sql.SQL;
import eu.wordnice.db.wndb.WNDB;

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
	 * @return SQL format
	 */
	public String toSQL() {
		String str = this.flag.sql;
		if(this.val instanceof Number) {
			return Api.replace(str, new Object[]{
					"1 ", "",
					" 2", "",
					"$", this.key
			});
		}
		if(this.val instanceof byte[]) {
			return Api.replace(str, new Object[]{
					"1", (this.sens) ? "BINARY" : "",
					" 2", "",
					"$", this.key
			});
		} else {
			return Api.replace(str, new Object[]{
					"1 ", "",
					"2", (this.sens) ? "COLLATE utf8_bin" : "",
					"$", this.key
			});
		}
	}
	
	/**
	 * @param rs ResSet with values to compare
	 * @return `true` if values match with this AndOr
	 */
	public boolean match(ResSet rs) {
		switch(this.flag) {
			case BIGGER:
				return rs.getDouble(this.key) > ((Number) this.val).doubleValue();
				
			case BIGGER_EQUAL:
				return rs.getDouble(this.key) >= ((Number) this.val).doubleValue();
				
			case SMALLER:
				return rs.getDouble(this.key) < ((Number) this.val).doubleValue();
				
			case SMALLER_EQUAL:
				return rs.getDouble(this.key) <= ((Number) this.val).doubleValue();
				
			case START:
				return Where.start(rs, this.key, rs, this.sens);
				
			case NOT_START:
				return !Where.start(rs, this.key, rs, this.sens);
				
			case END:
				return Where.end(rs, this.key, rs, this.sens);
				
			case NOT_END:
				return !Where.end(rs, this.key, rs, this.sens);
				
			case REGEX:
				return Where.regex(rs, this.key, rs, this.sens);
				
			case NOT_REGEX:
				return !Where.regex(rs, this.key, rs, this.sens);
				
			case NOT_EQUAL:
				return !Where.equals(rs, this.key, rs, this.sens);
				
			case EQUAL:
			default:
				return Where.equals(rs, this.key, rs, this.sens);
		}
	}
	
	protected static boolean regex(ResSet rs, String key, Object val, boolean sens) {
		String str = rs.getString(key);
		if(str == null) {
			if(val == null) {
				return true;
			}
			return false;
		}
		if(sens) {
			return Pattern.compile((String) val).matcher("" + rs.getObject(key)).find();
		} else {
			return Pattern.compile((String) val, Pattern.CASE_INSENSITIVE).matcher("" + rs.getObject(key)).find();
		}
	}
	
	protected static boolean start(ResSet rs, String key, Object val, boolean sens) {
		if(val instanceof byte[]) {
			byte[] b = (byte[]) val;
			byte[] ent = rs.getBytes(key);
			if(ent.length < b.length) {
				return false;
			}
			if(sens) {
				return ByteString.equals(ent, b, b.length);
			} else {
				return ByteString.equalsIgnoreCase(ent, b, b.length);
			}
		} else {
			String b = (String) val;
			String ent = rs.getString(key);
			if(ent.length() < b.length()) {
				return false;
			}
			if(sens) {
				return Api.equals(ent, 0, b, 0, b.length());
			} else {
				return Api.equalsIgnoreCase(ent, 0, b, 0, b.length());
			}
		}
	}
	
	protected static boolean end(ResSet rs, String key, Object val, boolean sens) {
		if(val instanceof byte[]) {
			byte[] b = (byte[]) val;
			byte[] ent = rs.getBytes(key);
			int b_len = b.length;
			if(ent.length < b.length) {
				return false;
			}
			if(sens) {
				return ByteString.equals(ent, ent.length - b_len, b.length, b, 0, b.length);
			} else {
				return ByteString.equalsIgnoreCase(ent, ent.length - b_len, b.length, b, 0, b.length);
			}
		} else {
			String b = (String) val;
			String ent = rs.getString(key);
			if(ent.length() < b.length()) {
				return false;
			}
			if(sens) {
				return Api.equals(ent, ent.length() - b.length(), b, 0, b.length());
			} else {
				return Api.equalsIgnoreCase(ent, ent.length() - b.length(), b, 0, b.length());
			}
		}
	}
	
	protected static boolean equals(ResSet rs, String key, Object val, boolean sens) {
		if(val instanceof Number) {
			return rs.getDouble(key) == ((Number) val).doubleValue();
		} else if(val instanceof byte[]) {
			if(sens) {
				return ByteString.equals(rs.getBytes(key), (byte[]) val);
			}
			return ByteString.equalsIgnoreCase(rs.getBytes(key), (byte[]) val);
		} else if(val == null) {
			return rs.getObject(key) == null;
		} else {
			if(!sens == val instanceof String) {
				return ((String) val).equalsIgnoreCase(rs.getString(key));
			}
			return val.equals(rs.getObject(key));
		}
	}
	
	/**
	 * Test
	 */
	public static void main(String... lel_varargs) throws Throwable {
		
		//TODO Test me!
		boolean use_sql = true;
		
		Database db = null;
		if(use_sql) {
			SQL sql = new MySQL("db.mysql-01.gsp-europe.net", "sql_1040", "sql_1040", "2qZ0h1e0nURTWbfiCQpHaz50Not8yuV");
			sql.connect();
			db = new Database(sql, "shets");
			
			PreparedStatement ps = sql.prepare("SELECT * FROM shets WHERE rekts LIKE ? '%'");
			ps.setString(1, "SHRE%a");
			ResSet rs = new ResultResSet(ps.executeQuery());
			while(rs.next()) {
				System.out.println(rs.getEntries());
			}
		} else {
			ResSetDB wdb = WNDB.createEmptyWNDB(new String[] {"rekts", "rektd", "rektb"}, new DBType[] {DBType.STRING, DBType.DOUBLE, DBType.BYTES});
			wdb.insertRaw(new ImmArray<Object>(new Object[] { "SHREKTB", 23.42, new byte[] {} }));
			wdb.insertRaw(new ImmArray<Object>(new Object[] { "SHREKTa", 23.42, new byte[] {} }));
			wdb.insertRaw(new ImmArray<Object>(new Object[] { "SHREKTA", 23.42, new byte[] {} }));
			db = new Database(Database.copy(Database.copy(wdb.getSnapshot()).getSnapshot()));
		}
		
		ResSet rs = db.get(new And(
				new Where("rekts", "SHREKT", WType.NOT_EQUAL),
				new Where("rekts", "SHREKTy", WType.NOT_EQUAL, true),
				new Where("rekts", "SHREKTa", WType.EQUAL, false),
					//[UP] change to true will display only SHREKTa, otherwise SHREKTA too
				new Where("rektb", new byte[] {}, WType.EQUAL, false),
				new Where("rektd", 23.43, WType.SMALLER, false)
		), new Sort[] {
				new Sort("rekts", SType.ASC, false)
		});
		while(rs.next()) {
			System.out.println(rs.getString("rekts"));
		}
	}
	
}
