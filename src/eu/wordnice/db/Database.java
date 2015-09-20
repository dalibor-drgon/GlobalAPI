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
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.wordnice.api.Api;
import eu.wordnice.api.Val;
import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Limit;
import eu.wordnice.db.operator.Sort;
import eu.wordnice.db.results.MapsResSet;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.results.ResSetDB;
import eu.wordnice.db.results.ResultResSet;
import eu.wordnice.db.serialize.SerializeException;
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
	 * Pair with File
	 */
	public ResSetDB rs;
	
	/**
	 * File, where data will be saved and from which will be data loaded
	 * Pair with rs
	 */
	public File file;
	
	/**
	 * @see {@link Database#init(Map)}
	 */
	public Database(Map<String, String> data, Map<String, Object> cols)
			throws IllegalArgumentException, SerializeException, IOException, SQLException {
		this.init(data, cols);
	}
	
	/**
	 * @see {@link Database#init(SQL)}
	 */
	public Database(SQL sql, String table, Map<String, Object> cols)
			throws SQLException {
		this.init(sql, table, cols);
	}
	
	/**
	 * @see {@link Database#init(ResSet)}
	 */
	public Database(ResSetDB rs, File file, Map<String, Object> cols) {
		this.init(rs, file, cols);
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
	 * - type: json (todo)
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
	 * @param data Map
	 * 
	 * @throws IllegalArgumentException when not enought information were provided
	 *                                  or entered database type does not exist
	 * @throws SerializeException Error while parsing file-based database
	 * @throws IOException Error while reading file-based database
	 * @throws SQLException Error while connecting or quering SQL-based database
	 */
	public void init(Map<String, String> data, Map<String, Object> cols)
			throws IllegalArgumentException, SerializeException, IOException, SQLException {
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
			File fl = new File(file);
			this.init(new WNDB(fl), fl);
		} else {
			throw new IllegalArgumentException("Unknown database type " + type);
		}
		
	}
	
	/**
	 * Set database type to SQL
	 * 
	 * @param sql SQL instance
	 * @param Table name
	 */
	public void init(SQL sql, String table)
			throws SQLException {
		this.rs = null;
		this.file = null;
		this.sql = sql;
		this.sql_table = table;
	}
	
	/**
	 * Set database type to ResSet
	 * 
	 * @param rs ResSet instance
	 */
	public void init(ResSetDB rs, File file) {
		this.sql = null;
		this.sql_table = null;
		this.rs = rs;
		this.file = file;
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 * @return ResSet with all values
	 */
	public ResSet select() throws SQLException, DatabaseException {
		return this.select(null, null, null, null);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(AndOr where) throws SQLException, DatabaseException {
		return this.select(null, where, null, null);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(String[] columns, AndOr where) throws SQLException, DatabaseException {
		return this.select(columns, where, null, null);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(String[] columns, AndOr where, Sort[] sort) throws SQLException, DatabaseException {
		return this.select(columns, where, sort, null);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(String[] columns, AndOr where, Limit limit) throws SQLException, DatabaseException {
		return this.select(columns, where, null, limit);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(AndOr where, Sort[] sort) throws SQLException, DatabaseException {
		return this.select(null, where, sort, null);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(AndOr where, Limit limit) throws SQLException, DatabaseException {
		return this.select(null, where, null, limit);
	}
	
	/**
	 * @see {@link Database#select(String[], AndOr, Limit, Sort[])}
	 */
	public ResSet select(AndOr where, Sort[] sort, Limit limit) throws SQLException, DatabaseException {
		return this.select(null, where, sort, limit);
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
	 * @throws DatabaseException Implementation specific exception
	 * @throws SQLException Exception from JDBC
	 * @return Results
	 */
	public ResSet select(String[] columns, AndOr where, Sort[] sort, Limit limit) throws SQLException, DatabaseException {
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
			try {
				List<Object> list = whproc.two;
				for(int i = 0, n = list.size(); i < n;) {
					Object v = list.get(i);
					i++;
					ps.setObject(i, v);
				}
			} catch(SQLException sqle) {
				try {
					ps.close();
				} catch(Exception e) {}
				throw sqle;
			}
			ResSet ret = null;
			try {
				ret = new ResultResSet(ps.executeQuery());
			} catch(SQLException sqle) {
				try {
					ps.close();
				} catch(Exception e) {}
				throw sqle;
			}
			try {
				ps.close();
			} catch(Exception e) {}
			return ret;
		} else {
			if(this.rs.hasSelectDB()) {
				return this.rs.selectDB(columns, where, limit, sort);
			}
			ResSetDB rs = this.rs.getSnapshot();
			if(where != null) {
				while(rs.next()) {
					if(where.match(rs) == false) {
						rs.remove();
					}
				}
			}
			if(sort != null && sort.length != 0) {
				if(rs.hasSortCut() == false) {
					rs = Database.copy(rs);
				}
				rs.sort(sort);
			}
			if(limit != null && rs.size() != 0 && (limit.off != 0 || limit.len < rs.size())) {
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
	 * Insert one row into database
	 * 
	 * @param vals Row to insert
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	public void insert(Map<String, Object> vals) throws SQLException, DatabaseException {
		if(this.sql != null) {
			StringBuilder sb = new StringBuilder();
			StringBuilder sb_vals = new StringBuilder();
			Iterator<Entry<String, Object>> it = vals.entrySet().iterator();
			
			int size = 0;
			sb.append('(');
			sb_vals.append('(');
			while(it.hasNext()) {
				Entry<String, Object> cur = it.next();
				if(size != 0) {
					sb.append(',');
					sb_vals.append(',');
				}
				sb.append(cur.getKey());
				sb_vals.append('?');
				size++;
			}
			sb.append(')');
			sb_vals.append(')');
			
			PreparedStatement ps = this.sql.prepare("INSERT INTO " + this.sql_table + " " + sb.toString() + " VALUES " + sb_vals.toString());
			
			int cursize = 0;
			try {
				it = vals.entrySet().iterator();
				while(it.hasNext()) {
					cursize++;
					ps.setObject(cursize, it.next().getValue());
				}
			} catch(SQLException sql) {
				try {
					ps.close();
				} catch(Exception t) {}
				throw sql;
			}
			
			if(cursize != size) {
				try {
					ps.close();
				} catch(Exception t) {}
				throw new IllegalArgumentException("Map results mismatch! After first iteration got " + size + " elements, after second " + cursize + "!");
			}
			try {
				ps.executeUpdate();
			} catch(SQLException sqle) {
				try {
					ps.close();
				} catch(Exception t) {}
				throw sqle;
			}
			try {
				ps.close();
			} catch(Exception t) {}
		} else {
			this.rs.insert(vals);
		}
	}
	
	/**
	 * Insert multiple rows into database
	 * 
	 * @param vals Multiple rows to insert
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	public void insertAll(Collection<Map<String, Object>> vals) throws SQLException, DatabaseException {
		if(this.sql != null) {
			Iterator<Map<String, Object>> it = vals.iterator();
			while(it.hasNext()) {
				this.insert(it.next());
			}
		} else {
			this.rs.insertAll(vals);
		}
	}
	
	/**
	 * Fast insert multiple rows into database
	 * 
	 * @param names Column names, pair with second argument
	 * @param vals Multiple rows to insert
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	public void insertAll(Collection<String> names, Collection<Collection<Object>> vals) throws SQLException, DatabaseException {
		if(this.sql != null) {
			StringBuilder sb = new StringBuilder();
			StringBuilder sb_vals = new StringBuilder();
			Iterator<String> names_it = names.iterator();
			
			int size = 0;
			sb.append('(');
			sb_vals.append('(');
			while(names_it.hasNext()) {
				if(size != 0) {
					sb.append(',');
					sb_vals.append(',');
				}
				sb.append(names_it.next());
				sb_vals.append('?');
				size++;
			}
			sb.append(')');
			sb_vals.append(')');
			
			PreparedStatement ps = this.sql.prepare("INSERT INTO " + this.sql_table + " " + sb.toString() + " VALUES " + sb_vals.toString());
			
			try {
				int i = 0;
				Iterator<Collection<Object>> vals_it = vals.iterator();
				while(vals_it.hasNext()) {
					Collection<Object> cur = vals_it.next();
					Iterator<Object> it = cur.iterator();
					int cur_size = 0;
					while(it.hasNext() && cur_size < size) {
						cur_size++;
						ps.setObject(cur_size, it.next());
					}
					/*if(cur_size != size) {
						throw new IllegalArgumentException("Values set at index " + i + " has less values than expected: " + cur_size + " / " + size);
					}
					ps.addBatch();*/
					if(cur_size == size) {
						ps.addBatch();
					}
					i++;
					if(i == 500) {
						ps.executeBatch();
						i = 0;
					}
				}
				if(i != 0) {
					ps.executeBatch();
				}
			} catch(SQLException sql) {
				try {
					ps.close();
				} catch(Exception t) {}
				throw sql;
			}
		} else {
			this.rs.insertAll(names, vals);
		}
	}
	
	
	/**
	 * @see {@link Database#update(Map, AndOr, int)}
	 */
	public void update(Map<String, Object> nevvals) throws DatabaseException, SQLException {
		this.update(nevvals, null, 0);
	}
	
	/**
	 * @see {@link Database#update(Map, AndOr, int)}
	 */
	public void update(Map<String, Object> nevvals, int limit) throws DatabaseException, SQLException {
		this.update(nevvals, null, limit);
	}
	
	/**
	 * @see {@link Database#update(Map, AndOr, int)}
	 */
	public void update(Map<String, Object> nevvals, AndOr where) throws DatabaseException, SQLException {
		this.update(nevvals, where, 0);
	}
	
	/**
	 * Update entries in database
	 * 
	 * @param nevvals New values to change
	 * @param where Where clause. If null, is ignored
	 * @param limit Maximum count of updates. Zero or lower mean all possibles
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	public void update(Map<String, Object> nevvals, AndOr where, int limit) throws DatabaseException, SQLException {
		if(this.sql != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<Entry<String, Object>> it = nevvals.entrySet().iterator();
			String suf = null;
			if(limit >= 1) {
				suf = " LIMIT " + limit;
			} else {
				suf = "";
			}
			
			final char[] app = new char[] {' ', '=', ' ', '?'};
			
			int size = 0;
			while(it.hasNext()) {
				if(size != 0) {
					sb.append(',');
				}
				sb.append(it.next().getKey());
				sb.append(app);
				size++;
			}
			
			String cmd = "UPDATE " + this.sql_table + " SET " + sb.toString();
			PreparedStatement ps = null;
			
			try {
				if(where != null) {
					Val.TwoVal<String, List<Object>> whproc = where.toSQL();
					ps = this.sql.prepare(cmd + " WHERE " + whproc.one + suf);
					List<Object> list = whproc.two;
					for(int i = 0, n = list.size(); i < n; ) {
						Object v = list.get(i);
						i++;
						ps.setObject(size + i, v);
					}
				} else {
					ps = this.sql.prepare(cmd + suf);
				}
				
				it = nevvals.entrySet().iterator();
				int secsize = 0;
				while(it.hasNext()) {
					secsize++;
					ps.setObject(secsize, it.next().getValue());
				}
				if(secsize != size) {
					throw new IllegalArgumentException("Map results mismatch! After first iteration got " + size + " elements, after second " + secsize + "!");
				}
				ps.executeUpdate();
			} catch(SQLException sqle) {
				try {
					ps.close();
				} catch(Exception e) {}
			}
		} else {
			if(this.rs.hasUpdateDB()) {
				this.rs.updateDB(nevvals, where, limit);
				return;
			}
			this.rs.first();
			ResSetDB curs = this.rs;
			if(where != null) {
				while(curs.next()) {
					if(where.match(curs)) {
						curs.update(nevvals);
						limit--;
						if(limit == 0) {
							break;
						}
					}
				}
			} else {
				while(curs.next()) {
					curs.update(nevvals);
					limit--;
					if(limit == 0) {
						break;
					}
				}
			}
		}
	}
	
	
	/**
	 * Drop all entries
	 * 
	 * @see {@link Database#delete(AndOr, int)}
	 */
	public void delete() throws DatabaseException, SQLException {
		this.delete(null, 0);
	}
	
	/**
	 * @see {@link Database#delete(AndOr, int)}
	 */
	public void delete(int limit) throws DatabaseException, SQLException {
		this.delete(null, limit);
	}
	
	/**
	 * @see {@link Database#delete(AndOr, int)}
	 */
	public void delete(AndOr where) throws DatabaseException, SQLException {
		this.delete(where, 0);
	}
	
	/**
	 * Delete entries in database
	 * 
	 * @param where Where clause. If null, is ignored
	 * @param limit Maximum count of deletes. Zero or lower mean all possibles
	 * 
	 * @throws SQLException When working with sql-based database and 
	 * error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	public void delete(AndOr where, int limit) throws DatabaseException, SQLException {
		if(this.sql != null) {
			String suf = null;
			if(limit >= 1) {
				suf = " LIMIT " + limit;
			} else {
				suf = "";
			}
						
			String cmd = "DELETE FROM " + this.sql_table;
			PreparedStatement ps = null;
			
			try {
				if(where != null) {
					Val.TwoVal<String, List<Object>> whproc = where.toSQL();
					ps = this.sql.prepare(cmd + " WHERE " + whproc.one + suf);
					List<Object> list = whproc.two;
					for(int i = 0, n = list.size(); i < n; ) {
						Object v = list.get(i);
						i++;
						ps.setObject(i, v);
					}
				} else {
					ps = this.sql.prepare(cmd + suf);
				}
				ps.executeUpdate();
			} catch(SQLException sqle) {
				try {
					ps.close();
				} catch(Exception e) {}
			}
			
		} else {
			if(this.rs.hasDeleteDB()) {
				this.rs.deleteDB(where, limit);
				return;
			}
			this.rs.first();
			ResSetDB curs = this.rs;
			if(where != null) {
				while(curs.next()) {
					if(where.match(curs)) {
						curs.remove();
						limit--;
						if(limit == 0) {
							break;
						}
					}
				}
			} else {
				while(curs.next()) {
					curs.remove();
					limit--;
					if(limit == 0) {
						break;
					}
				}
			}
		}
	}
	
	
	
	
	/**
	 * Create copy of Entered ResSet with supported sort() and cut()
	 * 
	 * @param rs ResSet to copy
	 * 
	 * @return copy of Entered ResSet with supported sort() and cut()
	 * 
	 * @throws DatabaseException Implementation specific exception
	 * @throws SQLException Exception from JDBC
	 * 
	 * @see {@link ResSetDB#insert(Map)}
	 * @see {@link ResSetDB#insertRaw(java.util.Collection)}
	 */
	public static ResSetDB copy(ResSet rs) throws SQLException, DatabaseException {
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
	 * @throws DatabaseException Implementation specific exception
	 * @throws SQLException Exception from JDBC
	 * 
	 * @see {@link ResSetDB#insert(Map)}
	 * @see {@link ResSetDB#insertRaw(java.util.Collection)}
	 */
	public static void copy(ResSetDB out, ResSet rs) throws SQLException, DatabaseException {
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
