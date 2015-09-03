/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.db.results;

import java.sql.SQLException;

public interface ResSet {
	
	/**
	 * @return `true` if results are from table-based database
	 */
	public boolean isTable();
	
	/**
	 * @see {@link ResSet#getObject(String)}
	 * @return `true` if getting by name is supported
	 */
	public boolean hasByName();
	
	/**
	 * @see {@link ResSet#getObject(int)}
	 * @return `true` if getting by index is supported
	 */
	public boolean hasByIndex();

	/**
	 * Get object by name
	 * Always check if getting by name is supported by {@link ResSet#hasByName()}
	 * 
	 * @param name Key for requested value
	 * 
	 * @return Object, or `null`
	 */
	public Object getObject(String name);

	/**
	 * Get object by name
	 * Always check if getting by index is supported by {@link ResSet#hasByIndex()}
	 * 
	 * @param in Index for requested value
	 * 
	 * @return Object, or `null`
	 */
	public Object getObject(int in);

	public String getString(String name);

	public String getString(int in);

	public byte[] getBytes(String name);

	public byte[] getBytes(int in);

	public boolean getBoolean(String name);

	public boolean getBoolean(int in);

	public byte getByte(String name);

	public byte getByte(int in);

	public short getShort(String name);

	public short getShort(int in);

	public int getInt(String name);

	public int getInt(int in);

	public long getLong(String name);

	public long getLong(int in);

	public float getFloat(String name);

	public float getFloat(int in);

	public double getDouble(String name);

	public double getDouble(int in);

	/**
	 * Go to first value
	 */
	public void reset();

	/**
	 * @return `true` If got next entry, otherwise `false`
	 */
	public boolean next();
	
	/**
	 * Remove current entry
	 * 
	 * @throws SQLException When error occured
	 */
	public void remove() throws SQLException;

	/**
	 * Close & destroy this result set
	 */
	public void close() throws SQLException;

}