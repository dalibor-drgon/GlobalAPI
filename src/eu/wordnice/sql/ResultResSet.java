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

package eu.wordnice.sql;

import java.sql.ResultSet;

public class ResultResSet implements ResSet {

	public ResultSet rs;

	public ResultResSet() {
	}

	public ResultResSet(ResultSet rs) {
		this.rs = rs;
	}

	@Override
	public Object getObject(String name) {
		try {
			return this.rs.getObject(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Object getObject(int in) {
		try {
			return this.rs.getObject(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public String getString(String name) {
		try {
			return this.rs.getString(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public String getString(int in) {
		try {
			return this.rs.getString(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public byte[] getBytes(String name) {
		try {
			return this.rs.getBytes(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public byte[] getBytes(int in) {
		try {
			return this.rs.getBytes(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Boolean getBoolean(String name) {
		try {
			return this.rs.getBoolean(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Boolean getBoolean(int in) {
		try {
			return this.rs.getBoolean(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Byte getByte(String name) {
		try {
			return this.rs.getByte(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Byte getByte(int in) {
		try {
			return this.rs.getByte(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Short getShort(String name) {
		try {
			return this.rs.getShort(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Short getShort(int in) {
		try {
			return this.rs.getShort(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Integer getInt(String name) {
		try {
			return this.rs.getInt(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Integer getInt(int in) {
		try {
			return this.rs.getInt(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Long getLong(String name) {
		try {
			return this.rs.getLong(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Long getLong(int in) {
		try {
			return this.rs.getLong(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Float getFloat(String name) {
		try {
			return this.rs.getFloat(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Float getFloat(int in) {
		try {
			return this.rs.getFloat(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Double getDouble(String name) {
		try {
			return this.rs.getDouble(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Double getDouble(int in) {
		try {
			return this.rs.getDouble(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public boolean first() {
		try {
			return this.rs.first();
		} catch (Throwable t) {
		}
		return false;
	}

	@Override
	public boolean next() {
		try {
			return this.rs.next();
		} catch (Throwable t) {
		}
		return false;
	}

	@Override
	public boolean close() {
		try {
			this.rs.close();
			return true;
		} catch (Throwable t) {
		}
		return false;
	}

}
