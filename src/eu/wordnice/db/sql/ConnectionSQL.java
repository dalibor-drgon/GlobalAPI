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

package eu.wordnice.db.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import eu.wordnice.api.Api;
import eu.wordnice.db.ColType;
import eu.wordnice.db.operator.Sort;
import eu.wordnice.db.operator.Where;
import eu.wordnice.db.results.ResSet;
import eu.wordnice.db.results.ResultResSet;

public abstract class ConnectionSQL implements SQL {

	public Connection con;
	
	public ConnectionSQL() {}

	public ConnectionSQL(Connection con) {
		this.con = con;
	}
	
	public Statement createStatement() throws SQLException {
		this.checkConnection();
		return this.con.createStatement();
	}
	
	@Override
	public ResSet query(String query) throws SQLException {
		Statement stm = this.createStatement();
		System.out.println("Query: " + query);
		try {
			return new ResultResSet(stm.executeQuery(query), stm);
		} catch(SQLException sqle) {
			try {
				stm.close();
			} catch(Exception e) {}
			throw sqle;
		}
	}

	@Override
	public void command(String cmd) throws SQLException {
		Statement stm = this.createStatement();
		System.out.println("Command: " + cmd);
		try {
			stm.executeUpdate(cmd);
		} catch(SQLException sqle) {
			try {
				stm.close();
			} catch(Exception e) {}
			throw sqle;
		}
		try {
			stm.close();
		} catch(Exception e) {}
	}
	
	@Override
	public PreparedStatement prepare(String cmd) throws SQLException {
		this.checkConnection();
		System.out.println("Prepare: " + cmd);
		return this.con.prepareStatement(cmd);
	}

	@Override
	public void close() throws SQLException {
		if(this.con != null) {
			this.con.close();
		}
	}

	@Override
	public boolean isClosed() {
		if(this.con == null) {
			return true;
		}
		try {
			return this.con.isClosed();
		} catch (Throwable t) {}
		return true;
	}

	public void checkConnection() throws SQLException {
		if(this.isClosed()) {
			this.connect();
		}
	}

	@Override
	public abstract void connect() throws SQLException;
	
	public static String escapeJDBC(String in) {
		return Api.replace(in, "?", "\\?");
	}
	
	@Override
	public String getWhere(Where where) {
		String str = where.flag.sql;
		if(where.val instanceof Number) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					" 222", "",
					"333", "",
					"$", where.key
			});
		} else if(where.val instanceof byte[]) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					" 222", "",
					"333", ((byte[]) where.val).length,
					"$", where.key
			});
		} else if(where.val instanceof String) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					"222", (where.sens) ? "COLLATE utf8_bin" : "",
					"333", ((String) where.val).length(),
					"$", where.key
			});
		} else if(where.val == null) {
			return Api.replace(str, new Object[]{
					"111 ", "",
					" 222", "",
					"333", "0",
					"$", where.key
			});
		} else {
			throw new IllegalArgumentException("Unknown value type " + where.val.getClass().getName());
		}
	}
	
	@Override
	public String getSort(Sort sort, ColType tp) {
		if(tp == ColType.STRING) {
			return sort.key + " " + sort.type.sql_str;
		}
		return sort.key + " " + sort.type.sql;
	}

}
