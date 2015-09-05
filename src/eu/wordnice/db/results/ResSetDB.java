package eu.wordnice.db.results;

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
	 * @param pair {key, value, key2, value2, ...}
	 * 
	 * @return `true` if data are valid
	 */
	public boolean checkRow(Object[] pair);
	
	/**
	 * Update current values
	 * 
	 * @param pair {key, value, key2, value2, ...}
	 * 
	 * @throws Exception Implementation specific exception
	 */
	public void update(Object[] pair) throws Exception;
	
	/**
	 * Insert values
	 * 
	 * @param pair {key, value, key2, value2, ...}
	 * 
	 * @throws Exception Implementation specific exception
	 */
	public void insert(Object[] pair) throws Exception;
	
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
	 * Sort values by given schema
	 * 
	 * @param sorts Schema for sorting
	 * 
	 * @throws UnsupportedOperationException
	 *         If this operation is not supported. In this case
	 *         there is got snapshot from this database, and data are
	 *         copyied to SetSetResSet or CollResSet, depending
	 *         on {@link ResSetDB#isTable()}                            
	 */
	public void sort(Sort[] sorts) throws UnsupportedOperationException;
	
	
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
