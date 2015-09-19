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
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import eu.wordnice.db.serialize.SerializeException;

public interface Output extends DataOutput, Closeable {
	
	public void write(int byt) throws IOException;
	public void write(byte[] bytes) throws IOException;
	public void write(byte[] bytes, int off, int len) throws IOException;
	public void write(ByteBuffer buf) throws IOException;
	
	public void writeBytes(byte[] bytes) throws IOException;
	public void writeBytes(byte[] bytes, int off, int len) throws IOException;
	
	@Override
	@Deprecated
	public void writeBytes(String str) throws IOException;
	
	public void writeUTF(String str, int off, int len) throws IOException;
	public void writeUTF(char[] chars) throws IOException;
	public void writeUTF(char[] chars, int off, int len) throws IOException;
	public void writeChars(String str, int off, int len) throws IOException;
	public void writeChars(char[] chars) throws IOException;
	public void writeChars(char[] chars, int off, int len) throws IOException;
	
	public void writeColl(Iterable<?> set) throws SerializeException, IOException;
	public void writeMap(Map<?,?> map) throws SerializeException, IOException;
	
	public void writeArray(Object[] arr) throws SerializeException, IOException;
	public void writeArray(Object[] arr, int off, int len) throws SerializeException, IOException;
	
	public void writeObject(Object obj) throws SerializeException, IOException;
	public void writeObject(Object obj, int ri, int vi) throws SerializeException, IOException;

}
