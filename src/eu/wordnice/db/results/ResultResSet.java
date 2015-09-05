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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;

import eu.wordnice.api.ImmArray;

public class ResultResSet implements ResSet {

	public String[] keys;
	public ResultSet rs;
	public boolean pendingFirst = false;

	public ResultResSet() {}

	public ResultResSet(ResultSet rs) throws SQLException {
		this.rs = rs;
		ResultSetMetaData md = rs.getMetaData();
		int cols = md.getColumnCount();
		this.keys = new String[cols];
		for(int i = 0; i < cols; i++) {
			this.keys[i] = md.getColumnName(i + 1);
		}
	}
	
	@Override
	public Object getObject(String name) {
		try {
			return this.rs.getObject(name);
		} catch (Throwable t) {}
		return null;
	}

	@Override
	public Object getObject(int in) {
		try {
			return this.rs.getObject(in);
		} catch (Throwable t) {}
		return null;
	}

	@Override
	public String getString(String name) {
		try {
			return this.rs.getString(name);
		} catch (Throwable t) {}
		return null;
	}

	@Override
	public String getString(int in) {
		try {
			return this.rs.getString(in);
		} catch (Throwable t) {}
		return null;
	}

	@Override
	public byte[] getBytes(String name) {
		try {
			return this.rs.getBytes(name);
		} catch (Throwable t) {}
		return null;
	}

	@Override
	public byte[] getBytes(int in) {
		try {
			return this.rs.getBytes(in);
		} catch (Throwable t) {}
		return null;
	}

	@Override
	public boolean getBoolean(String name) {
		try {
			return this.rs.getBoolean(name);
		} catch (Throwable t) {}
		return false;
	}

	@Override
	public boolean getBoolean(int in) {
		try {
			return this.rs.getBoolean(in);
		} catch (Throwable t) {}
		return false;
	}

	@Override
	public byte getByte(String name) {
		try {
			return this.rs.getByte(name);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public byte getByte(int in) {
		try {
			return this.rs.getByte(in);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public short getShort(String name) {
		try {
			return this.rs.getShort(name);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public short getShort(int in) {
		try {
			return this.rs.getShort(in);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public int getInt(String name) {
		try {
			return this.rs.getInt(name);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public int getInt(int in) {
		try {
			return this.rs.getInt(in);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public long getLong(String name) {
		try {
			return this.rs.getLong(name);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public long getLong(int in) {
		try {
			return this.rs.getLong(in);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public float getFloat(String name) {
		try {
			return this.rs.getFloat(name);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public float getFloat(int in) {
		try {
			return this.rs.getFloat(in);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public double getDouble(String name) {
		try {
			return this.rs.getDouble(name);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public double getDouble(int in) {
		try {
			return this.rs.getDouble(in);
		} catch (Throwable t) {}
		return 0;
	}

	@Override
	public void first() {
		this.pendingFirst = true;
	}

	@Override
	public boolean next() {
		try {
			if(this.pendingFirst) {
				this.pendingFirst = false;
				return this.rs.first();
			}
			return this.rs.next();
		} catch (Throwable t) {}
		return false;
	}

	@Override
	public void close() throws SQLException {
		this.rs.close();
	}
	
	
	public ResultSet getResultSet() {
		return this.rs;
	}

	@Override
	public boolean hasByName() {
		return true;
	}

	@Override
	public boolean hasByIndex() {
		return true;
	}

	@Override
	public void remove() throws SQLException {
		this.rs.deleteRow();
	}

	@Override
	public boolean isTable() {
		return true;
	}

	@Override
	public int cols() {
		return this.keys.length;
	}

	@Override
	public Collection<String> getKeys() {
		return new ImmArray<String>(this.keys);
	}

	@Override
	public Collection<Object> getValues() {
		int n = this.cols();
		Object[] vals = new Object[this.cols()];
		try {
			for(int i = 0; i < n; i++) {
				vals[i] = this.rs.getObject(i + 1);
			}
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
		return new ImmArray<Object>(vals);
	}

}
