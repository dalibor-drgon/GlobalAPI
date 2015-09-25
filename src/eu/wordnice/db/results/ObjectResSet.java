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

import java.util.Collection;
import java.util.Map;

public abstract class ObjectResSet implements ResSet {
	
	@Override
	public String getString(String name) {
		Object obj = this.getObject(name);
		if(obj instanceof CharSequence || obj instanceof Number) {
			return obj.toString();
		}
		if(obj instanceof byte[]) {
			return new String((byte[]) obj);
		}
		return null;
	}

	@Override
	public String getString(int in) {
		Object obj = this.getObject(in);
		if(obj instanceof CharSequence || obj instanceof Number) {
			return obj.toString();
		}
		if(obj instanceof byte[]) {
			return new String((byte[]) obj);
		}
		return null;
	}

	public byte[] getBytes(String name) {
		Object obj = this.getObject(name);
		if(obj instanceof CharSequence || obj instanceof Number) {
			return obj.toString().getBytes();
		}
		if(obj instanceof byte[]) {
			return (byte[]) obj;
		}
		return null;
	}

	public byte[] getBytes(int in) {
		Object obj = this.getObject(in);
		if(obj instanceof CharSequence || obj instanceof Number) {
			return obj.toString().getBytes();
		}
		if(obj instanceof byte[]) {
			return (byte[]) obj;
		}
		return null;
	}

	@Override
	public boolean getBoolean(String name) {
		Object o = this.getObject(name);
		try {
			return (boolean) o;
		} catch(Exception e) {}
		try {
			return Boolean.getBoolean(o.toString());
		} catch(Exception e) {}
		return false;
	}

	@Override
	public boolean getBoolean(int in) {
		Object o = this.getObject(in);
		try {
			return (boolean) o;
		} catch(Exception e) {}
		try {
			return Boolean.getBoolean(o.toString());
		} catch(Exception e) {}
		return false;
	}

	@Override
	public byte getByte(String name) {
		Object o = this.getObject(name);
		try {
			return (byte) o;
		} catch(Exception e) {}
		try {
			return Byte.parseByte(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public byte getByte(int in) {
		Object o = this.getObject(in);
		try {
			return (byte) o;
		} catch(Exception e) {}
		try {
			return Byte.parseByte(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public short getShort(String name) {
		Object o = this.getObject(name);
		try {
			return (short) o;
		} catch(Exception e) {}
		try {
			return Short.parseShort(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public short getShort(int in) {
		Object o = this.getObject(in);
		try {
			return (short) o;
		} catch(Exception e) {}
		try {
			return Short.parseShort(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public int getInt(String name) {
		Object o = this.getObject(name);
		try {
			return (int) o;
		} catch(Exception e) {}
		try {
			return Integer.parseInt(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public int getInt(int in) {
		Object o = this.getObject(in);
		try {
			return (int) o;
		} catch(Exception e) {}
		try {
			return Integer.parseInt(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public long getLong(String name) {
		Object o = this.getObject(name);
		try {
			return (long) o;
		} catch(Exception e) {}
		try {
			return Long.parseLong(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public long getLong(int in) {
		Object o = this.getObject(in);
		try {
			return (long) o;
		} catch(Exception e) {}
		try {
			return Long.parseLong(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public float getFloat(String name) {
		Object o = this.getObject(name);
		try {
			return (float) o;
		} catch(Exception e) {}
		try {
			return Float.parseFloat(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public float getFloat(int in) {
		Object o = this.getObject(in);
		try {
			return (float) o;
		} catch(Exception e) {}
		try {
			return Float.parseFloat(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public double getDouble(String name) {
		Object o = this.getObject(name);
		try {
			return (double) o;
		} catch(Exception e) {}
		try {
			return Double.parseDouble(o.toString());
		} catch(Exception e) {}
		return 0;
	}

	@Override
	public double getDouble(int in) {
		Object o = this.getObject(in);
		try {
			return (double) o;
		} catch(Exception e) {}
		try {
			return Double.parseDouble(o.toString());
		} catch(Exception e) {}
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> Collection<X> getColl(String name) {
		Object o = this.getObject(name);
		try {
			return (Collection<X>) o;
		} catch(Exception e) {}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X> Collection<X> getColl(int in) {
		Object o = this.getObject(in);
		try {
			return (Collection<X>) o;
		} catch(Exception e) {}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X, Y> Map<X, Y> getMap(String name) {
		Object o = this.getObject(name);
		try {
			return (Map<X, Y>) o;
		} catch(Exception e) {}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <X, Y> Map<X, Y> getMap(int in) {
		Object o = this.getObject(in);
		try {
			return (Map<X, Y>) o;
		} catch(Exception e) {}
		return null;
	}

}
