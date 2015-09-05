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

import java.util.Map;

import eu.wordnice.db.RawUnsupportedException;
import eu.wordnice.db.operator.Sort;

public interface ResSetDB extends ResSet {
	
	/**
	 * @return Number of rows
	 */
	public int size();
	
	/**
	 * @param name
	 * @return If {@link ResSetDB#hasByIndex()} and {@link ResSetDB#hasByName()} returns `true`,
	 *         this method may return index of requested column, otherwise or if not found `-1`
	 */
	public int getColumnIndex(String name);
	
	/**
	 * Check if values can be inserted into database
	 * 
	 * @param vals Values
	 * 
	 * @return `true` if data are valid
	 */
	public boolean checkRow(Map<String, Object> vals);
	
	/**
	 * Update current values
	 * 
	 * @param vals Values
	 * 
	 * @throws Exception Implementation specific exception
	 */
	public void update(Map<String, Object> vals) throws Exception;
	
	/**
	 * Insert values
	 * 
	 * @param vals Values
	 * 
	 * @throws Exception Implementation specific exception
	 */
	public void insert(Map<String, Object> vals) throws Exception;
	
	/**
	 * Get current database snapshot
	 * All inserts and updates must be ignored by snapshot,
	 * but inserted into original database
	 * 
	 * @return If calling for ResSetDBSnap, should return another copy
	 *         of original database
	 */
	public ResSetDBSnap getSnapshot();
	
	/**
	 * @return `true` when methods {@link ResSetDB#sort(Sort[])} and
	 *         {@link ResSetDB#cut(int, int)} are accessible
	 */
	public boolean hasSort();
	
	/**
	 * Sort values by given schema
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
	 * Check if raw values could be inserted
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @param vals raw values to check
	 * 
	 * @return `true` if Raw value array can be inserted
	 */
	public boolean checkRowRaw(Object[] vals) throws RawUnsupportedException;
	
	/**
	 * Check if value could be inserted into given column
	 * 
	 * @param i Index, Column number (starting from 0)
	 * @param val Value to check
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @return `true` If value could be inserted into given column and
	 *         if {@link ResSet#hasByIndex()} returns `true`
	 */
	public boolean isRawValueOK(int i, Object val) throws RawUnsupportedException;
	
	/**
	 * Check if value could be inserted into given column
	 * 
	 * @param name Key for value to check
	 * @param val Value to check
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @return `true` If value could be inserted into given column and
	 *         if {@link ResSet#hasByName()} returns `true`
	 */
	public boolean isRawValueOK(String name, Object val) throws RawUnsupportedException;
	
	/**
	 * Update current values
	 * 
	 * @param values {value, value2, ...}
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @throws IllegalArgumentException When {@link ResSetDB#checkRowRaw(Object[])} returns `false`
	 * @throws Exception Implementation specific exception
	 */
	public void updateRaw(Object[] values) throws RawUnsupportedException, IllegalArgumentException, Exception;
	
	/**
	 * Insert values
	 * 
	 * @param values {value, value2, ...}
	 * 
	 * @throws RawUnsupportedException If raw is not supported
	 * @throws IllegalArgumentException When {@link ResSetDB#checkRowRaw(Object[])} returns `false`
	 * @throws Exception Implementation specific exception
	 */
	public void insertRaw(Object[] values) throws RawUnsupportedException, IllegalArgumentException, Exception;
	
}
