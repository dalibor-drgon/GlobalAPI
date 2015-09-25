/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 22201115, Dalibor Drgoň <emptychannelmc@gmail.com>
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

package eu.wordnice.db.operator;

public enum WType {
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is too same as entered
	 */
	EQUAL("$ = 111 ? 222"),

	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is different from entered
	 */
	NOT_EQUAL("$ != 111 ? 222"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database starts with entered string
	 */
	START("SUBSTR($, 1, 333) = 111 ? 222"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database ends with entered string
	 */
	END("SUBSTR($, -333, 333) = 111 ? 222"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database does not start with entered string
	 */
	NOT_START("SUBSTR($, 1, 333) != 111 ? 222"),
	
	/**
	 * For: String, byte[]
	 * 
	 * Check if value in database does not end with entered string
	 */
	NOT_END("SUBSTR($, -333, 333) != 111 ? 222"),
	
	/**
	 * For: String
	 * 
	 * Match values from database with regex
	 */
	REGEX("$ REGEXP ?"),
	
	/**
	 * For: String
	 * 
	 * Get values from database which does not match with entered regex
	 */
	NOT_REGEX("$ NOT_REGEXP 111 ? 222"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is bigger than entered
	 * 
	 * Cannot be combined with anything else
	 */
	BIGGER("$ > 111 ? 222"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is bigger than / too same as entered
	 * 
	 * Cannot be combined with anything else
	 */
	BIGGER_EQUAL("$ >= 111 ? 222"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is smaller than entered
	 * 
	 * Cannot be combined with anything else
	 */
	SMALLER("$ < 111 ? 222"),
	
	/**
	 * For: String, byte[], Number
	 * 
	 * Check if value in database is smaller than / too same as entered
	 * 
	 * Cannot be combined with anything else
	 */
	SMALLER_EQUAL("$ <= 111 ? 222");
	
	public String sql;
	
	WType(String sql) {
		this.sql = sql;
	}
	
}
