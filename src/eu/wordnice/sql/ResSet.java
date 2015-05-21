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

public interface ResSet {

	public Object getObject(String name);

	public Object getObject(int in);

	public String getString(String name);

	public String getString(int in);

	public byte[] getBytes(String name);

	public byte[] getBytes(int in);

	public Boolean getBoolean(String name);

	public Boolean getBoolean(int in);

	public Byte getByte(String name);

	public Byte getByte(int in);

	public Short getShort(String name);

	public Short getShort(int in);

	public Integer getInt(String name);

	public Integer getInt(int in);

	public Long getLong(String name);

	public Long getLong(int in);

	public Float getFloat(String name);

	public Float getFloat(int in);

	public Double getDouble(String name);

	public Double getDouble(int in);

	public boolean first();

	public boolean next();

	public boolean close();

}