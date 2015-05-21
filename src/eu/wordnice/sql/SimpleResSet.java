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

public abstract class SimpleResSet implements ResSet {

	@Override
	public abstract Object getObject(String name);

	@Override
	public abstract Object getObject(int in);

	@Override
	public String getString(String name) {
		try {
			return (String) this.getObject(name);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public String getString(int in) {
		try {
			return (String) this.getObject(in);
		} catch (Throwable t) {
		}
		return null;
	}

	public byte[] getBytes(String name) {
		try {
			return (byte[]) this.getObject(name);
		} catch (Throwable t) {
		}
		return null;
	}

	public byte[] getBytes(int in) {
		try {
			return (byte[]) this.getObject(in);
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Boolean getBoolean(String name) {
		Object o = this.getObject(name);
		try {
			return (Boolean) o;
		} catch (Throwable t) {
		}
		try {
			return (Boolean) Boolean.parseBoolean(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Boolean getBoolean(int in) {
		Object o = this.getObject(in);
		try {
			return (Boolean) o;
		} catch (Throwable t) {
		}
		try {
			return (Boolean) Boolean.parseBoolean(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Byte getByte(String name) {
		Object o = this.getObject(name);
		try {
			return (Byte) o;
		} catch (Throwable t) {
		}
		try {
			return (Byte) Byte.parseByte(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Byte getByte(int in) {
		Object o = this.getObject(in);
		try {
			return (Byte) o;
		} catch (Throwable t) {
		}
		try {
			return (Byte) Byte.parseByte(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Short getShort(String name) {
		Object o = this.getObject(name);
		try {
			return (Short) o;
		} catch (Throwable t) {
		}
		try {
			return (Short) Short.parseShort(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Short getShort(int in) {
		Object o = this.getObject(in);
		try {
			return (Short) o;
		} catch (Throwable t) {
		}
		try {
			return (Short) Short.parseShort(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Integer getInt(String name) {
		Object o = this.getObject(name);
		try {
			return (Integer) o;
		} catch (Throwable t) {
		}
		try {
			return (Integer) Integer.parseInt(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Integer getInt(int in) {
		Object o = this.getObject(in);
		try {
			return (Integer) o;
		} catch (Throwable t) {
		}
		try {
			return (Integer) Integer.parseInt(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Long getLong(String name) {
		Object o = this.getObject(name);
		try {
			return (Long) o;
		} catch (Throwable t) {
		}
		try {
			return (Long) Long.parseLong(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Long getLong(int in) {
		Object o = this.getObject(in);
		try {
			return (Long) o;
		} catch (Throwable t) {
		}
		try {
			return (Long) Long.parseLong(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Float getFloat(String name) {
		Object o = this.getObject(name);
		try {
			return (Float) o;
		} catch (Throwable t) {
		}
		try {
			return (Float) Float.parseFloat(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Float getFloat(int in) {
		Object o = this.getObject(in);
		try {
			return (Float) o;
		} catch (Throwable t) {
		}
		try {
			return (Float) Float.parseFloat(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Double getDouble(String name) {
		Object o = this.getObject(name);
		try {
			return (Double) o;
		} catch (Throwable t) {
		}
		try {
			return (Double) Double.parseDouble(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public Double getDouble(int in) {
		Object o = this.getObject(in);
		try {
			return (Double) o;
		} catch (Throwable t) {
		}
		try {
			return (Double) Double.parseDouble(o.toString());
		} catch (Throwable t) {
		}
		return null;
	}

	@Override
	public abstract boolean first();

	@Override
	public abstract boolean next();

	@Override
	public abstract boolean close();

}
