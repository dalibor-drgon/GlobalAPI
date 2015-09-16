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

package eu.wordnice.db.results;

import java.util.Collection;
import java.util.Map;

import eu.wordnice.api.serialize.DataReader;
import eu.wordnice.api.serialize.DataWriter;
import eu.wordnice.db.DatabaseException;
import eu.wordnice.db.RawUnsupportedException;
import eu.wordnice.db.operator.AndOr;
import eu.wordnice.db.operator.Limit;
import eu.wordnice.db.operator.Sort;

public interface ResSetDB extends ResSet, DataWriter, DataReader {
	
	/**
	 * @return Number of rows
	 */
	public int size();
	
	/**
	 * @param name
	 * @return If {@link ResSetDB#hasByIndex()} returns `true`,
	 *         this method may return index of requested column, otherwise or if not found `-1`
	 */
	public int getColumnIndex(String name);
	
	/**
	 * Change entered values and keep other unchanged
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param vals Values
	 * 
	 * @throws DatabaseException Implementation specific exception
	 */
	public void update(Map<String, Object> vals) throws DatabaseException;
	
	/**
	 * Replace current values with entered
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param vals Values
	 * 
	 * @throws DatabaseException Implementation specific exception
	 */
	public void updateAll(Map<String, Object> vals) throws DatabaseException;
	
	/**
	 * Insert values
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param vals Values
	 * 
	 * @throws DatabaseException Implementation specific exception
	 */
	public void insert(Map<String, Object> vals) throws DatabaseException;
	
	/**
	 * Insert values
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param vals Multiple values (entries) to insert
	 * 
	 * @throws DatabaseException Implementation specific exception
	 */
	public void insertAll(Collection<Map<String, Object>> vals) throws DatabaseException;
	
	/**
	 * Insert values
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param columns Column names for given values
	 * @param vals Multiple values (entries) to insert
	 * 
	 * @throws DatabaseException Implementation specific exception
	 */
	public void insertAll(Collection<String> columns, Collection<Collection<Object>> vals) throws DatabaseException;
	
	/**
	 * Get current database snapshot
	 * All inserts and updates must be ignored by snapshot,
	 * but send into original database
	 * 
	 * @return If calling for ResSetDBSnap, should return another copy
	 *         of original database
	 */
	public ResSetDBSnap getSnapshot();
	
	/**
	 * @return `true` when methods {@link ResSetDB#sort(Sort[])} and
	 *         {@link ResSetDB#cut(int, int)} are accessible
	 */
	public boolean hasSortCut();
	
	/**
	 * Sort values by given schema
	 * Modify only this ResSet (if instanceof ResSetDBSnap, do not touch original ResSet)
	 * This action also calls {@link ResSetDB#first()}
	 * 
	 * @param sorts Schema for sorting
	 * 
	 * @see {@link java.util.Arrays#sort(Object[], java.util.Comparator)}
	 * @see {@link java.util.Collections#sort(java.util.List, java.util.Comparator)}
	 * 
	 * @throws UnsupportedOperationException
	 *         If {@link ResSetDB#hasSort()} returns `false`                        
	 */
	public void sort(Sort[] sorts) throws UnsupportedOperationException;
	
	/**
	 * Cut results
	 * Modify only this ResSet (if instanceof ResSetDBSnap, do not touch original ResSet)
	 * This action also calls {@link ResSetDB#first()}
	 * 
	 * @param off Offset
	 * @param len Length of new results
	 * 
	 * @throws UnsupportedOperationException
	 *         If {@link ResSetDB#hasSort()} returns `false` 
	 */
	public void cut(int off, int len) throws UnsupportedOperationException;
	
	
	/**
	 * @return `true` if can be accessed with Raw methods
	 */
	public boolean isRaw();
	
	/**
	 * Update current values
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param values Values to insert {value, value2, ...}
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @throws IllegalArgumentException When values to insert are invalid / instances of invalid type
	 * @throws DatabaseException Implementation specific exception
	 */
	public void updateRaw(Collection<Object> values) throws RawUnsupportedException, IllegalArgumentException, DatabaseException;
	
	/**
	 * Insert values
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param values Values to insert {value, value2, ...}
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @throws IllegalArgumentException When values to insert are invalid / instances of invalid type
	 * @throws DatabaseException Implementation specific exception
	 */
	public void insertRaw(Collection<Object> values) throws RawUnsupportedException, IllegalArgumentException, DatabaseException;
	
	/**
	 * Insert values
	 * If instanceof ResSetDBSnap, modify only original ResSetDB
	 * 
	 * @param values Multiple values (entries) to insert [{value, value2, ...}, ...]
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @throws IllegalArgumentException When values to insert are invalid / instances of invalid type
	 * @throws DatabaseException Implementation specific exception
	 */
	public void insertRawAll(Collection<Collection<Object>> values)
			throws RawUnsupportedException, IllegalArgumentException, DatabaseException;
	
	
	
	
	
	/**
	 * @return `true` if got implemented {@link ResSetDB#get(String[], AndOr, Limit, Sort[])}
	 */
	public boolean hasGet();
	
	/**
	 * @see {@link eu.wordnice.db.Database#get(String[], AndOr, Limit, Sort[])}
	 * 
	 * @throws UnsupportedOperationException When {@link ResSetDB#hasGet()} returns `false`
	 */
	public ResSet get(String[] columns, AndOr where, Limit limit, Sort[] sort)
			throws UnsupportedOperationException, IllegalArgumentException, Exception;
}
