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

package eu.wordnice.streams;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Map;

import eu.wordnice.db.serialize.SerializeException;

public interface Input extends DataInput, Closeable, ReadableByteChannel {
	
	public long skip(long bytes) throws IOException;
	
	public int read() throws IOException;
	public int read(byte[] bytes) throws IOException;
	public int read(byte[] bytes, int off, int len) throws IOException;
	
	public void readFully(ByteBuffer buf) throws IOException;
	public void readFully(char[] chars) throws IOException;
	public void readFully(char[] chars, int off, int len) throws IOException;
	
	public byte[] readBytes() throws IOException;
	public char[] readUTFChars() throws IOException;
	
	@Override
	@Deprecated
	public String readLine() throws IOException;
	
	public <X> Collection<X> readColl(Collection<X> col) throws SerializeException, IOException;
	public Collection<? extends Object> readColl() throws SerializeException, IOException;
	public <X, Y> Map<X, Y> readMap(Map<X, Y> map) throws SerializeException, IOException;
	public Map<? extends Object, ?> readMap() throws SerializeException, IOException;
	
	public Object readObject() throws SerializeException, IOException;
	public Object readObject(int ri, int vi) throws SerializeException, IOException;

}
