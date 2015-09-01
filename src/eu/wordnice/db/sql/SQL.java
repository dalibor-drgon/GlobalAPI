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

package eu.wordnice.db.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import eu.wordnice.db.results.ResSet;

public interface SQL {

	/**
	 * Query database (SELECT)
	 * 
	 * @param query String to query
	 * 
	 * @return ResSet with results
	 * @throws SQLException
	 */
	public ResSet query(String query) throws SQLException;
	
	/**
	 * Query database (SELECT)
	 * 
	 * @param query Query string
	 * 
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement prepareQuery(String query) throws SQLException;

	/**
	 * Update database (INSERT, UPDATE, CREATE ...)
	 * 
	 * @param cmd Update command
	 * 
	 * @throws SQLException
	 */
	public void command(String cmd) throws SQLException;
	
	/**
	 * Update database (INSERT, UPDATE, CREATE ...)
	 * 
	 * @param cmd Update command
	 * 
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	public PreparedStatement prepareCommand(String cmd) throws SQLException;

	/**
	 * @return `true` If database was closed
	 */
	public boolean isClosed();

	/**
	 * Connect to dabase (and allow auto-connect when connection is lost)
	 * 
	 * @throws SQLException
	 */
	public void connect() throws SQLException;
	
	/**
	 * Close database and disable auto-connect
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException;

}
