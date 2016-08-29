/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.db;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import wordnice.db.operator.AndOr;
import wordnice.db.operator.Limit;
import wordnice.db.operator.Sort;
import wordnice.db.results.ResSet;
import wordnice.db.results.ResSetDB;
import wordnice.db.serialize.SerializeException;

public interface Database {

	/**
	 * Save the database if needed (no action on sql-based databases, but required)
	 * 
	 * @see {@link ResSetDB#write(eu.wordnice.streams.Output)}
	 * @see {@link OutputAdv#forFile(File)}
	 */
	void save() throws SerializeException, IOException;
	
	/**
	 * @see java.io.Closeable#close()
	 * @see save(
	 */
	void close() throws IOException;

	/**
	 * @see {@link Database#select(String[], AndOr, Sort[], Limit)}
	 * @return ResSet with all values
	 */
	ResSet select() throws SQLException, DatabaseException;

	/**
	 * @see {@link Database#select(String[], AndOr, Sort[], Limit)}
	 */
	ResSet select(String[] names) throws SQLException, DatabaseException;

	/**
	 * @see {@link Database#select(String[], AndOr, Sort[], Limit)}
	 */
	ResSet select(Sort[] sort) throws SQLException, DatabaseException;

	/**
	 * @param vals Any parameter(s) from {@link Database#select(String[], AndOr, Sort[], Limit)}
	 * @see {@link Database#select(String[], AndOr, Sort[], Limit)}
	 */
	ResSet select(Object... vals) throws IllegalArgumentException, SQLException, DatabaseException;

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
	ResSet select(String[] columns, AndOr where, Sort[] sort, Limit limit) throws SQLException, DatabaseException;

	/**
	 * Insert one row into database
	 * 
	 * @param vals Row to insert
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	void insert(Map<String, Object> vals) throws SQLException, DatabaseException;

	/**
	 * Insert multiple rows into database
	 * 
	 * @param vals Multiple rows to insert
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	void insertAll(Collection<Map<String, Object>> vals) throws SQLException, DatabaseException;

	/**
	 * Fast insert multiple rows into database
	 * 
	 * @param names Column names, pair with second argument
	 * @param vals Multiple rows to insert
	 * 
	 * @throws SQLException When working with sql-based database and error while connecting was thrown
	 * @throws DatabaseException Any error with reading or writing file-based database
	 */
	void insertAll(Collection<String> names, Collection<Collection<Object>> vals)
			throws SQLException, DatabaseException;

	/**
	 * @see {@link Database#update(Map, AndOr, int)}
	 */
	void update(Map<String, Object> nevvals) throws DatabaseException, SQLException;

	/**
	 * @see {@link Database#update(Map, AndOr, int)}
	 */
	void update(Map<String, Object> nevvals, int limit) throws DatabaseException, SQLException;

	/**
	 * @see {@link Database#update(Map, AndOr, int)}
	 */
	void update(Map<String, Object> nevvals, AndOr where) throws DatabaseException, SQLException;

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
	void update(Map<String, Object> nevvals, AndOr where, int limit) throws DatabaseException, SQLException;

	/**
	 * Drop all entries
	 * 
	 * @see {@link Database#delete(AndOr, int)}
	 */
	void delete() throws DatabaseException, SQLException;

	/**
	 * @see {@link Database#delete(AndOr, int)}
	 */
	void delete(int limit) throws DatabaseException, SQLException;

	/**
	 * @see {@link Database#delete(AndOr, int)}
	 */
	void delete(AndOr where) throws DatabaseException, SQLException;

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
	void delete(AndOr where, int limit) throws DatabaseException, SQLException;

}