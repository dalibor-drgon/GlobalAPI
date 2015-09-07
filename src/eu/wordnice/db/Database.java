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

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import eu.wordnice.api.Api;
import eu.wordnice.api.Val;
import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Limit;
import eu.wordnice.db.operator.Sort;
import eu.wordnice.db.results.MapsResSet;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.results.ResSetDB;
import eu.wordnice.db.results.ResSetDBAdvanced;
import eu.wordnice.db.results.ResultResSet;
import eu.wordnice.db.results.ArraysResSet;
import eu.wordnice.db.sql.MySQL;
import eu.wordnice.db.sql.SQL;
import eu.wordnice.db.sql.SQLite;
import eu.wordnice.db.wndb.WNDB;

/**
 * This class allows you to easily create database of any available
 * type from entered data / configuration
 * 
 * @author wordnice
 */
public class Database {
	
	/**
	 * Set when MySQL, SQLite or any other SQL-based database is used
	 * Pair with sql_table
	 */
	public SQL sql;
	
	/**
	 * SQL table name
	 * Part with sql
	 */
	public String sql_table;
	
	/**
	 * Set when WNDB, FLATFILE, JSON or any other ResSet-based database is used
	 */
	public ResSetDB rs;
	
	/**
	 * @see {@link Database#init(Map)}
	 */
	public Database(Map<String, String> data) throws IllegalArgumentException, Exception {
		this.init(data);
	}
	
	/**
	 * @see {@link Database#init(SQL)}
	 */
	public Database(SQL sql, String table) throws SQLException {
		this.init(sql, table);
	}
	
	/**
	 * @see {@link Database#init(ResSet)}
	 */
	public Database(ResSetDB rs) {
		this.init(rs);
	}
	
	/**
	 * Create database for given type
	 * 
	 * Formats:
	 * - type: sqlite
	 * - file: ./test.sql
	 * - table: test_table
	 * 
	 * - type: wndb
	 * - file: ./test.wndb
	 * 
	 * - type: flatfile (todo)
	 * - file: ./test.txt
	 * 
	 * - type: yaml (todo)
	 * - file: ./test.yml
	 * 
	 * - type: json
	 * - file: ./test.json
	 * 
	 * - type: mysql
	 * - host: localhost:3306
	 * - db: testdb
	 * - user: admin
	 * - pass: Passw0rd!
	 * - table: test_table
	 * 
	 * 
	 * @param data Info
	 * 
	 * @throws IllegalArgumentException when not enought information were provided
	 *                                  or entered database type does not exist
	 * @throws Exception If error occured while parsing / connecting to database
	 */
	public void init(Map<String, String> data) throws IllegalArgumentException, Exception {
		String type = data.get("type");
		if(type == null) {
			throw new IllegalArgumentException("Entered type is null!");
		}
		type = type.toLowerCase();
		if(type.equals("sqlite")) {
			String file = data.get("file");
			if(file == null) {
				throw new IllegalArgumentException("SQLite file is null!");
			}
			String table = data.get("table");
			if(table == null) {
				throw new IllegalArgumentException("SQLite table is null!");
			}
			this.init(new SQLite(Api.getRealPath(new File(file))), table);
			this.sql.connect();
		} else if(type.equals("mysql")) {
			String host = data.get("host");
			if(host == null) {
				throw new IllegalArgumentException("MySQL host is null!");
			}
			String db = data.get("db");
			if(db == null) {
				throw new IllegalArgumentException("MySQL database name is null!");
			}
			String user = data.get("user");
			if(user == null) {
				throw new IllegalArgumentException("MySQL user is null!");
			}
			String pass = data.get("pass");
			if(pass == null) {
				throw new IllegalArgumentException("MySQL pass is null!");
			}
			String table = data.get("table");
			if(table == null) {
				throw new IllegalArgumentException("MySQL table is null!");
			}

			this.init(new MySQL(host, db, user, pass), table);
			this.sql.connect();
		} else if(type.equals("wndb")) {
			String file = data.get("file");
			if(file == null) {
				throw new IllegalArgumentException("SQLite file is null!");
			}
			this.init(new WNDB(new File(file)));
		}
	}
	
	/**
	 * Set database type to SQL
	 * 
	 * @param sql SQL instance
	 * @param Table name
	 */
	public void init(SQL sql, String table) throws SQLException {
		this.rs = null;
		this.sql = sql;
		this.sql_table = table;
	}
	
	/**
	 * Set database type to ResSet
	 * 
	 * @param rs ResSet instance
	 */
	public void init(ResSetDB rs) {
		this.sql = null;
		this.sql_table = null;
		this.rs = rs;
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 * @return ResSet with all values
	 */
	public ResSet get() throws IllegalArgumentException, Exception {
		return this.get(null, null, null, null);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(AndOr where) throws IllegalArgumentException, Exception {
		return this.get(null, where, null, null);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(String[] columns, AndOr where) throws IllegalArgumentException, Exception {
		return this.get(columns, where, null, null);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(String[] columns, AndOr where, Sort[] sort) throws IllegalArgumentException, Exception {
		return this.get(columns, where, sort, null);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(String[] columns, AndOr where, Limit limit) throws IllegalArgumentException, Exception {
		return this.get(columns, where, null, limit);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(AndOr where, Sort[] sort) throws IllegalArgumentException, Exception {
		return this.get(null, where, sort, null);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(AndOr where, Limit limit) throws IllegalArgumentException, Exception {
		return this.get(null, where, null, limit);
	}
	
	/**
	 * @see {@link Database#get(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet get(AndOr where, Sort[] sort, Limit limit) throws IllegalArgumentException, Exception {
		return this.get(null, where, sort, limit);
	}
	
	/**
	 * @param columns Columns to get. Returned value can contain more or all
	 *                available columns.
	 *                If null, then there are selected all available columns
	 * @param where Filter values
	 * @param sort Sort by
	 * @param limit Offset + Limit
	 * 
	 * @throws IllegalArgumentException When limit != null and (limit.off < 0 or limit.len <= 0)
	 * @throws Exception Implementation specific exception
	 * @return Results
	 */
	public ResSet get(String[] columns, AndOr where, Sort[] sort, Limit limit) throws IllegalArgumentException, Exception {
		if(this.sql != null) {
			StringBuilder suf = new StringBuilder();
			if(sort != null && sort.length != 0) {
				suf.append(" ORDER BY ");
				for(int i = 0, n = sort.length; i < n; i++) {
					if(i != 0) {
						suf.append(", ");
					}
					suf.append(sort[i].toSQL());
				}
			}
			if(limit != null) {
				if(limit.len <= 0) {
					throw new IllegalArgumentException("Invalid limit " + limit);
				} else {
					suf.append(" LIMIT ");
					suf.append(limit.len);
				}
					
				if(limit.off < 0) {
					throw new IllegalArgumentException("Invalid offset " + limit.off);
				} else if(limit.off > 0) {
					suf.append(" OFFSET ");
					suf.append(limit.off);
				}
			}
			if(columns == null && where == null) {
				return sql.query("SELECT * FROM " + this.sql_table + suf.toString());
			}
			
			String cols = null;
			if(columns == null) {
				cols = "*";
			} else {
				cols = Api.join(columns, ", ");
			}
			String cmd = "SELECT " + cols + " FROM " + this.sql_table;
			if(where == null) {
				return this.sql.query(cmd + suf.toString());
			}
			
			Val.TwoVal<String, List<Object>> whproc = where.toSQL();
			PreparedStatement ps = this.sql.prepare(cmd + " WHERE " + whproc.one + suf.toString());
			List<Object> list = whproc.two;
			for(int i = 0, n = list.size(); i < n;) {
				Object v = list.get(i++);
				ps.setObject(i, v);
			}
			return new ResultResSet(ps.executeQuery());
		} else {
			if(this.rs instanceof ResSetDBAdvanced) {
				return ((ResSetDBAdvanced) this.rs).get(columns, where, limit, sort);
			}
			ResSetDB rs = this.rs.getSnapshot();
			if(where != null) {
				while(rs.next()) {
					if(where.match(rs) == false) {
						rs.remove();
					}
				}
			}
			if(sort != null) {
				if(rs.hasSortCut() == false) {
					rs = Database.copy(rs);
				}
				rs.sort(sort);
			}
			if(limit != null) {
				if(limit.len <= 0) {
					throw new IllegalArgumentException("Invalid limit " + limit);
				}
				if(limit.off < 0) {
					throw new IllegalArgumentException("Invalid offset " + limit.off);
				}
				if(rs.hasSortCut() == false) {
					rs = Database.copy(rs);
				}
				rs.cut(limit.off, limit.len);
			}
			return rs;
		}
	}
	
	
	/**
	 * Create copy of Entered ResSet with supported sort() and cut()
	 * 
	 * @param rs ResSet to copy
	 * 
	 * @return copy of Entered ResSet with supported sort() and cut()
	 * 
	 * @throws IllegalArgumentException
	 * @throws Exception
	 * 
	 * @see {@link ResSetDB#insert(Map)}
	 * @see {@link ResSetDB#insertRaw(java.util.Collection)}
	 */
	public static ResSetDB copy(ResSet rs) throws IllegalArgumentException, Exception {
		if(rs.isTable()) {
			ResSetDB nev = new ArraysResSet(Api.<String>toArray(rs.getKeys(), String.class));
			while(rs.next()) {
				nev.insertRaw(rs.getValues());
			}
			return nev;
		}
		ResSetDB nev = new MapsResSet();
		while(rs.next()) {
			nev.insert(rs.getEntries());
		}
		return nev;
	}
	
	/**
	 * Copy values from second to first argument
	 * 
	 * @param out Destination
	 * @param rs Source
	 * 
	 * @throws IllegalArgumentException
	 * @throws Exception
	 * 
	 * @see {@link ResSetDB#insert(Map)}
	 * @see {@link ResSetDB#insertRaw(java.util.Collection)}
	 */
	public static void copy(ResSetDB out, ResSet rs) throws IllegalArgumentException, Exception {
		if(rs.isTable() && out.isTable() && out.isRaw() && out.getKeys().equals(rs.getKeys())) {
			while(rs.next()) {
				out.insertRaw(rs.getValues());
			}
		} else {
			while(rs.next()) {
				out.insert(rs.getEntries());
			}
		}
	}
	
}
