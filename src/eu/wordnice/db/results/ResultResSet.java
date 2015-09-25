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

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;

import eu.wordnice.cols.ImmArray;
import eu.wordnice.cols.ImmMapPair;
import eu.wordnice.db.DatabaseException;
import eu.wordnice.streams.InputAdv;

public class ResultResSet implements ResSet {

	public String[] keys;
	public Object[] last;
	public boolean hasLast = false;
	public ResultSet rs;
	public Statement sm;
	public boolean pendingFirst = false;

	public ResultResSet() {}

	public ResultResSet(ResultSet rs) throws SQLException {
		this(rs, null);
	}
	
	public ResultResSet(ResultSet rs, Statement sm) throws SQLException {
		this.rs = rs;
		ResultSetMetaData md = rs.getMetaData();
		int cols = md.getColumnCount();
		this.keys = new String[cols];
		for(int i = 0; i < cols; i++) {
			this.keys[i] = md.getColumnName(i + 1);
		}
		this.last = new Object[cols];
		this.sm = sm;
	}
	
	@Override
	public Object getObject(String name) {
		try {
			return this.rs.getObject(name);
		} catch(Exception t) {}
		return null;
	}

	@Override
	public Object getObject(int in) {
		try {
			return this.rs.getObject(in);
		} catch(Exception t) {}
		return null;
	}

	@Override
	public String getString(String name) {
		try {
			return this.rs.getString(name);
		} catch(Exception t) {}
		return null;
	}

	@Override
	public String getString(int in) {
		try {
			return this.rs.getString(in);
		} catch(Exception t) {}
		return null;
	}

	@Override
	public byte[] getBytes(String name) {
		try {
			return this.rs.getBytes(name);
		} catch(Exception t) {}
		return null;
	}

	@Override
	public byte[] getBytes(int in) {
		try {
			return this.rs.getBytes(in);
		} catch(Exception t) {}
		return null;
	}

	@Override
	public boolean getBoolean(String name) {
		try {
			return this.rs.getBoolean(name);
		} catch(Exception t) {}
		return false;
	}

	@Override
	public boolean getBoolean(int in) {
		try {
			return this.rs.getBoolean(in);
		} catch(Exception t) {}
		return false;
	}

	@Override
	public byte getByte(String name) {
		try {
			return this.rs.getByte(name);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public byte getByte(int in) {
		try {
			return this.rs.getByte(in);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public short getShort(String name) {
		try {
			return this.rs.getShort(name);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public short getShort(int in) {
		try {
			return this.rs.getShort(in);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public int getInt(String name) {
		try {
			return this.rs.getInt(name);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public int getInt(int in) {
		try {
			return this.rs.getInt(in);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public long getLong(String name) {
		try {
			return this.rs.getLong(name);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public long getLong(int in) {
		try {
			return this.rs.getLong(in);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public float getFloat(String name) {
		try {
			return this.rs.getFloat(name);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public float getFloat(int in) {
		try {
			return this.rs.getFloat(in);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public double getDouble(String name) {
		try {
			return this.rs.getDouble(name);
		} catch(Exception t) {}
		return 0;
	}

	@Override
	public double getDouble(int in) {
		try {
			return this.rs.getDouble(in);
		} catch(Exception t) {}
		return 0;
	}
	
	@Override
	public <X> Collection<X> getColl(String name) {
		byte[] bytes = this.getBytes(name);
		if(bytes != null) {
			try {
				return InputAdv.forStream(new ByteArrayInputStream(bytes)).readColl();
			} catch(Exception e) {}
		}
		return null;
	}
	
	@Override
	public <X> Collection<X> getColl(int in) {
		byte[] bytes = this.getBytes(in);
		if(bytes != null) {
			try {
				return InputAdv.forStream(new ByteArrayInputStream(bytes)).readColl();
			} catch(Exception e) {}
		}
		return null;
	}
	
	@Override
	public <X, Y> Map<X, Y> getMap(String name) {
		byte[] bytes = this.getBytes(name);
		if(bytes != null) {
			try {
				return InputAdv.forStream(new ByteArrayInputStream(bytes)).readMap();
			} catch(Exception e) {}
		}
		return null;
	}
	
	@Override
	public <X, Y> Map<X, Y> getMap(int in) {
		byte[] bytes = this.getBytes(in);
		if(bytes != null) {
			try {
				return InputAdv.forStream(new ByteArrayInputStream(bytes)).readMap();
			} catch(Exception e) {}
		}
		return null;
	}
	
	@Override
	public boolean next() {
		this.hasLast = false;
		try {
			if(this.pendingFirst) {
				this.pendingFirst = false;
				return this.rs.first();
			}
			return this.rs.next();
		} catch(Exception t) {}
		return false;
	}
	
	@Override
	public boolean forwardOnly() {
		return true;
	}
	
	@Override
	public boolean previous() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Forward only!");
	}
	
	@Override
	public void first() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Forward only!");
	}

	@Override
	public void close() throws SQLException, DatabaseException {
		this.hasLast = false;
		if(this.sm == null) {
			this.rs.close();
			return;
		}
		try {
			this.rs.close();
		} catch(SQLException sqle) {}
		this.sm.close();
	}
	
	@Override
	public void finalize() {
		this.hasLast = false;
		try {
			this.rs.close();
		} catch(Exception e) {}
		try {
			this.sm.close();
		} catch(Exception e) {}
	}
	
	
	public ResultSet getResultSet() {
		return this.rs;
	}

	@Override
	public boolean hasByIndex() {
		return true;
	}

	@Override
	public void remove() throws SQLException, DatabaseException {
		this.rs.deleteRow();
		this.hasLast = false;
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
	
	protected Object[] getValuesArray() {
		if(this.hasLast == false) {
			int n = this.cols();
			try {
				for(int i = 0; i < n; i++) {
					this.last[i] = this.rs.getObject(i + 1);
				}
				this.hasLast = true;
			} catch(SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return this.last;
	}

	@Override
	public Collection<Object> getValues() {
		return new ImmArray<Object>(this.getValuesArray());
	}

	@Override
	public Map<String, Object> getEntries() {
		return new ImmMapPair<String, Object>(this.keys, this.getValuesArray(), this.keys.length);
	}

}
