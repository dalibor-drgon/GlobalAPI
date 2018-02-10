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

package wordnice.db;

import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import wordnice.api.Nice;
import wordnice.coll.CollUtils;
import wordnice.db.operator.AndOr;
import wordnice.db.operator.Limit;
import wordnice.db.operator.Sort;
import wordnice.db.results.ArraysResSet;
import wordnice.db.results.MapsResSet;
import wordnice.db.results.ResSet;
import wordnice.db.results.ResSetDB;
import wordnice.db.results.ResultResSet;
import wordnice.db.serialize.SerializeException;
import wordnice.db.sql.SQL;

/**
 * This class allows you to easily create database of any available
 * type from entered data / configuration
 * 
 * @author wordnice
 */
@SuppressWarnings("deprecation")
public class SQLDatabase implements Closeable, AutoCloseable, Database {
	
	/**
	 * Set when MySQL, SQLite or any other SQL-based database is used
	 * Pair with sql_table, columns
	 */
	public SQL sql;
	
	/**
	 * SQL table name
	 * Part with sql, columns
	 */
	public String sql_table;
	
	/**
	 * Columns {Name: Type}
	 * Pair with sql, sql_table
	 * 
	 * Private to avoid differences between SQL & ResSet databases
	 */
	private Map<String, ColType> columns;
	
	/**
	 * Create database for given connection and table
	 */
	public SQLDatabase(SQL sql, String table, Map<String, ColType> cols)
			throws SQLException {
		this.init(sql, table, cols);
	}

	private void init(SQL sql, String table, Map<String, ColType> cols)
			throws SQLException {
		this.sql = sql;
		this.sql_table = table;
		this.columns = cols;
		SQLDatabase.createTable(cols, sql, table);
	}

	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#save()
	 */
	@Override
	public void save() throws SerializeException, IOException {
		//
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			this.save();
		} catch(Exception e) {
			throw new IOException("Cannot save database!", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#select()
	 */
	@Override
	public ResSet select() throws SQLException, DatabaseException {
		return this.select(null, null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#select(java.lang.String[])
	 */
	@Override
	public ResSet select(String[] names) throws SQLException, DatabaseException {
		return this.select(names, null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#select(eu.wordnice.db.operator.Sort[])
	 */
	@Override
	public ResSet select(Sort[] sort) throws SQLException, DatabaseException {
		return this.select(null, null, sort, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#select(java.lang.Object)
	 */
	@Override
	public ResSet select(Object... vals) throws IllegalArgumentException, SQLException, DatabaseException {
		String[] columns = null;
		AndOr where = null;
		Sort[] sort = null;
		Limit limit = null;
		for(int i = 0, n = vals.length; i < n; i++) {
			Object cur = vals[i];
			if(cur instanceof String[]) {
				if(columns != null) {
					throw Nice.illegal("Duplicated String[] names argument.");
				}
				columns = (String[]) cur;
			} else if(cur instanceof AndOr) {
				if(where != null) {
					throw Nice.illegal("Duplicated AndOr where argument.");
				}
				where = (AndOr) cur;
			} else if(cur instanceof Sort[]) {
				if(sort != null) {
					throw Nice.illegal("Duplicated Sort[] sort argument.");
				}
				sort = (Sort[]) cur;
			} else if(cur instanceof Sort[]) {
				if(limit != null) {
					throw Nice.illegal("Duplicated Limit limit argument.");
				}
				limit = (Limit) cur;
			} else {
				throw Nice.illegal("Unknown argument type " + ((cur == null) ? null : cur.getClass().getName()));
			}
		}
		return this.select(columns, where, sort, limit);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#select(java.lang.String[], eu.wordnice.db.operator.AndOr, eu.wordnice.db.operator.Sort[], eu.wordnice.db.operator.Limit)
	 */
	@Override
	public ResSet select(String[] columns, AndOr where, Sort[] sort, Limit limit) throws SQLException, DatabaseException {
		StringBuilder suf = new StringBuilder();
		if(sort != null && sort.length != 0) {
			suf.append(" ORDER BY ");
			for(int i = 0, n = sort.length; i < n; i++) {
				if(i != 0) {
					suf.append(',').append(' ');
				}
				suf.append(sql.getSort(sort[i], this.columns.get(sort[i].key)));
			}
		}
		if(limit != null) {
			if(limit.len <= 0) {
				throw Nice.illegal("Invalid limit " + limit);
			} else {
				suf.append(" LIMIT ");
				suf.append(limit.len);
			}
				
			if(limit.off < 0) {
				throw Nice.illegal("Invalid offset " + limit.off);
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
			cols = StringUtils.join(columns, ", ");
		}
		String cmd = "SELECT " + cols + " FROM " + this.sql_table;
		if(where == null) {
			return this.sql.query(cmd + suf.toString());
		}
		
		Entry<String, List<Object>> whproc = where.toSQL(this.sql);
		List<Object> list = whproc.getValue();
		cmd = cmd + " WHERE " + whproc.getKey() + suf.toString();
		if(list == null || list.size() == 0) {
			return this.sql.query(cmd);
		}
		PreparedStatement ps = this.sql.prepare(cmd);
		try {
			for(int i = 0, n = list.size(); i < n;) {
				Object v = DatabaseUtils.toSQLObject(list.get(i));
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
	}
	
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#insert(java.util.Map)
	 */
	@Override
	public void insert(Map<String, Object> vals) throws SQLException, DatabaseException {
		if(vals == null) {
			throw new NullPointerException("Map<String, Object> vals");
		}
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
				ps.setObject(cursize, DatabaseUtils.toSQLObject(it.next().getValue()));
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
			throw Nice.illegal("Map results mismatch! After first iteration got " + size + " elements, after second " + cursize + "!");
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
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#insertAll(java.util.Collection)
	 */
	@Override
	public void insertAll(Collection<Map<String, Object>> vals) throws SQLException, DatabaseException {
		if(vals == null) {
			throw new NullPointerException("Collection<Map<String, Object>> vals");
		}
		Iterator<Map<String, Object>> it = vals.iterator();
		while(it.hasNext()) {
			this.insert(it.next());
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#insertAll(java.util.Collection, java.util.Collection)
	 */
	@Override
	public void insertAll(Collection<String> names, Collection<Collection<Object>> vals) throws SQLException, DatabaseException {
		if(names == null) {
			throw new NullPointerException("Collection<String> names");
		}
		if(vals == null) {
			throw new NullPointerException("Collection<Collection<Object>> vals");
		}
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
					ps.setObject(cur_size, DatabaseUtils.toSQLObject(it.next()));
				}
				/*if(cur_size != size) {
					throw Api.illegal("Values set at index " + i + " has less values than expected: " + cur_size + " / " + size);
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
	}
	
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#update(java.util.Map)
	 */
	@Override
	public void update(Map<String, Object> nevvals) throws DatabaseException, SQLException {
		this.update(nevvals, null, 0);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#update(java.util.Map, int)
	 */
	@Override
	public void update(Map<String, Object> nevvals, int limit) throws DatabaseException, SQLException {
		this.update(nevvals, null, limit);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#update(java.util.Map, eu.wordnice.db.operator.AndOr)
	 */
	@Override
	public void update(Map<String, Object> nevvals, AndOr where) throws DatabaseException, SQLException {
		this.update(nevvals, where, 0);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#update(java.util.Map, eu.wordnice.db.operator.AndOr, int)
	 */
	@Override
	public void update(Map<String, Object> nevvals, AndOr where, int limit) throws DatabaseException, SQLException {
		if(nevvals == null) {
			throw new NullPointerException("Map<String, Object> nevvals");
		}
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
				Entry<String, List<Object>> whproc = where.toSQL(this.sql);
				List<Object> list = whproc.getValue();
				ps = this.sql.prepare(cmd + " WHERE " + whproc.getKey() + suf);
				for(int i = 0, n = list.size(); i < n; ) {
					Object v = list.get(i);
					i++;
					ps.setObject(size + i, DatabaseUtils.toSQLObject(v));
				}
			} else {
				ps = this.sql.prepare(cmd + suf);
			}
			
			it = nevvals.entrySet().iterator();
			int secsize = 0;
			while(it.hasNext() && secsize < size) {
				secsize++;
				ps.setObject(secsize, DatabaseUtils.toSQLObject(it.next().getValue()));
			}
			if(secsize != size) {
				throw Nice.illegal("Map results mismatch! After first iteration got " + size + " elements, after second " + secsize + "!");
			}
			ps.executeUpdate();
		} catch(SQLException sqle) {
			try {
				ps.close();
			} catch(Exception e) {}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#delete()
	 */
	@Override
	public void delete() throws DatabaseException, SQLException {
		this.delete(null, 0);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#delete(int)
	 */
	@Override
	public void delete(int limit) throws DatabaseException, SQLException {
		this.delete(null, limit);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#delete(eu.wordnice.db.operator.AndOr)
	 */
	@Override
	public void delete(AndOr where) throws DatabaseException, SQLException {
		this.delete(where, 0);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#delete(eu.wordnice.db.operator.AndOr, int)
	 */
	@Override
	public void delete(AndOr where, int limit) throws DatabaseException, SQLException {
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
				Entry<String, List<Object>> whproc = where.toSQL(this.sql);
				ps = this.sql.prepare(cmd + " WHERE " + whproc.getKey() + suf);
				List<Object> list = whproc.getValue();
				for(int i = 0, n = list.size(); i < n; ) {
					Object v = DatabaseUtils.toSQLObject(list.get(i));
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
			ResSetDB nev = new ArraysResSet(CollUtils.<String>toArray(rs.getKeys(), String.class));
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
	
	
	/**
	 * CREATE TABLE IF NOT EXISTS [table]
	 * 
	 * @param map Map with column names and types
	 * @param sql SQL connection
	 * @param table Table name
	 * 
	 * @throws SQLException Exception while processing command
	 */
	public static void createTable(Map<String, ColType> map, SQL sql, String table) throws SQLException {
		if(!sql.useSQLiteSyntax()) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS ");
			sb.append(table);
			sb.append(" (");
			
			Iterator<java.util.Map.Entry<String, ColType>> it = map.entrySet().iterator();
			int i = 0;
			while(it.hasNext()) {
				if(i++ != 0) {
					sb.append(',').append(' ');
				}
				java.util.Map.Entry<String, ColType> ent = it.next();
				//sb.append('`');
				sb.append(ent.getKey());
				sb.append(' ');
				sb.append(ent.getValue().sql);
			}
			
			sb.append(") DEFAULT CHARSET=utf8 DEFAULT COLLATE utf8_unicode_ci");
			
			sql.command(sb.toString());
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE IF NOT EXISTS ");
			sb.append(table);
			sb.append(" (");
			
			Iterator<java.util.Map.Entry<String, ColType>> it = map.entrySet().iterator();
			int i = 0;
			while(it.hasNext()) {
				if(i++ != 0) {
					sb.append(',').append(' ');
				}
				java.util.Map.Entry<String, ColType> ent = it.next();
				sb.append('`');
				sb.append(ent.getKey());
				sb.append('`').append(' ');
				sb.append(ent.getValue().sql_sqlite);
			}
			
			sb.append(')');
			
			sql.command(sb.toString());
		}
	}
	
}
