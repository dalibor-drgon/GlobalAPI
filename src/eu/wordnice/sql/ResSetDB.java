package eu.wordnice.sql;

import java.util.Collection;

public interface ResSetDB extends ResSet {
	
	/**
	 * @return Number of rows
	 */
	public int size();
	
	/**
	 * @return If table-based database, return number of columns, otherwise 0
	 */
	public int cols();
	
	/**
	 * @return Current keys
	 */
	public Collection<String> getKeys();
	
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
	public void update(Object[] pair) throws RuntimeException, Exception;
	
	/**
	 * Insert values
	 * 
	 * @param pair {key, value, key2, value2, ...}
	 * 
	 * @throws Exception Implementation specific exception
	 */
	public void insert(Object[] pair) throws RuntimeException, Exception;
	
	
	/**
	 * @return `true` if can be accessed with Raw methods
	 */
	public boolean isRaw();
	
	/**
	 * @return If {@link ResSetDB#isRaw()} returns `true`, this method returns
	 *         current Object[]s
	 */
	public Collection<Object> getRawValue();
	
	/**
	 * Check if value could be inserted into given column
	 * 
	 * @param val Value to check
	 * @param i Index, Column number (starting from 0)
	 * 
	 * 
	 * @return `true` If value could be inserted into given column and
	 *         if {@link ResSet#hasByIndex()} and {@link ResSetDB#isRaw()} returns `true`
	 */
	public boolean isRawValueOK(Object val, int i);
	
	/**
	 * Check if value could be inserted into given column
	 * 
	 * @param val Value to check
	 * @param name Key for value to check
	 * 
	 * @return `true` If value could be inserted into given column and
	 *         if {@link ResSet#hasByName()} and {@link ResSetDB#isRaw()} return `true`
	 */
	public boolean isRawValueOK(Object val, String name);
	
	/**
	 * Update current values
	 * 
	 * @param values {value, value2, ...}
	 * 
	 * @throws IllegalStateException When raw is not supported
	 * @throws Exception Implementation specific exception
	 */
	public void updateRaw(Object[] values) throws IllegalStateException, RuntimeException, Exception;
	
	/**
	 * Insert values
	 * 
	 * @param values {value, value2, ...}
	 * 
	 * @throws IllegalStateException When raw is not supported
	 * @throws Exception Implementation specific exception
	 */
	public void insertRaw(Object[] values) throws IllegalStateException, RuntimeException, Exception;
	
}
