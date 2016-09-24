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
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.db.operator.AndOr;
import wordnice.db.operator.Limit;
import wordnice.db.operator.Sort;
import wordnice.db.results.ResSet;
import wordnice.db.results.ResSetDB;
import wordnice.db.serialize.SerializeException;
import wordnice.utils.SerializeUtils;

/**
 * This class allows you to easily create database of any available
 * type from entered data / configuration
 * 
 * @author wordnice
 */
@SuppressWarnings("deprecation")
public class ResSetDatabase implements Closeable, AutoCloseable, Database {
	
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
	 * @see {@link Database#init(ResSetDB, File)}
	 */
	public ResSetDatabase(ResSetDB rs, File file) {
		this.init(rs, file);
	}
	
	public void init(ResSetDB rs, File file) {
		this.rs = rs;
		this.file = file;
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#save()
	 */
	@Override
	public void save() throws SerializeException, IOException {
		SerializeUtils.write(this.rs, this.file);
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
			if(rs.hasSort() == false) {
				rs = DatabaseUtils.copy(rs);
			}
			rs.sort(sort);
		}
		if(limit != null && rs.size() != 0 && (limit.off != 0 || limit.len < rs.size())) {
			if(limit.len <= 0) {
				throw Nice.illegal("Invalid limit " + limit);
			}
			if(limit.off < 0) {
				throw Nice.illegal("Invalid offset " + limit.off);
			}
			if(rs.hasCut() == false) {
				rs = DatabaseUtils.copy(rs);
			}
			rs.cut(limit.off, limit.len);
		}
		return rs;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#insert(java.util.Map)
	 */
	@Override
	public void insert(Map<String, Object> vals) throws SQLException, DatabaseException {
		if(vals == null) {
			throw new NullPointerException("Map<String, Object> vals");
		}
		this.rs.insert(vals);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.db.IDatabase#insertAll(java.util.Collection)
	 */
	@Override
	public void insertAll(Collection<Map<String, Object>> vals) throws SQLException, DatabaseException {
		if(vals == null) {
			throw new NullPointerException("Collection<Map<String, Object>> vals");
		}
		this.rs.insertAll(vals);
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
		this.rs.insertAll(names, vals);
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
