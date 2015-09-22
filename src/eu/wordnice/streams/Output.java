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
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.Map;

import eu.wordnice.db.serialize.SerializeException;

public interface Output extends DataOutput, Closeable, WritableByteChannel {
	
	/**
	 * Write byte
	 * Compatible with OutputStream
	 * 
	 * @param byt Byte to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link java.io.OutputStream#write(int)}
	 */
	public void write(int byt) throws IOException;
	
	/**
	 * Write multiple bytes
	 * Compatible with OutputStream
	 * 
	 * @param bytes Bytes to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link java.io.OutputStream#write(byte[])}
	 */
	public void write(byte[] bytes) throws IOException;
	
	/**
	 * Write multiple bytes
	 * Compatible with OutputStream
	 * 
	 * @param bytes Bytes to write
	 * @param off Offset of bytes
	 * @param len Number of bytes after offset to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link java.io.OutputStream#write(byte[], int, int)}
	 */
	public void write(byte[] bytes, int off, int len) throws IOException;
	
	/**
	 * Write full ByteBuffer to this Output
	 * 
	 * @param buf ByteBuffer with bytes to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * @return number of written bytes from {@link ByteBuffer#remaining()}
	 * 
	 * @see {@link WritableByteChannel#write(ByteBuffer)}
	 */
	@Override
	public int write(ByteBuffer buf) throws IOException;
	
	/**
	 * Write multiple bytes prefixed with length (s32)
	 * If bytes are null, only -1 is written (s32)
	 * 
	 * @param bytes Bytes to write
	 * 
	 * @throws IOException Any exception while writting occured
	 */
	public void writeBytes(byte[] bytes) throws IOException;
	
	/**
	 * Write multiple bytes prefixed with length (s32)
	 * If bytes are null, only -1 is written (s32)
	 * 
	 * @param bytes Bytes to write
	 * @param off Offset of bytes
	 * @param len Number of bytes after offset to write
	 * 
	 * @throws IOException Any exception while writting occured
	 */
	public void writeBytes(byte[] bytes, int off, int len) throws IOException;
	
	/**
	 * Write characters from given string casted to byte
	 * 
	 * @deprecated
	 * Does not write prefix with length,
	 * Confusing name (Output standard),
	 * Encoding problems
	 * 
	 * @param str String to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link java.io.DataOutput#writeBytes(String)}
	 */
	@Override
	@Deprecated
	public void writeBytes(String str) throws IOException;
	
	/**
	 * Write string prefixed with length (s32, number of characters)
	 * If given string is null, is written only -1 (s32)
	 * 
	 * @param str String to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link Output#writeUTF(char[], int, int)
	 */
	@Override
	public void writeUTF(String str) throws IOException;
	
	/**
	 * Write string prefixed with length (s32, number of characters)
	 * If given string is null, or length is under zero, is written only -1 (s32)
	 * 
	 * @param str String to write
	 * @param off Offset of string
	 * @param len Number of characters after offset to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link Output#writeUTF(char[], int, int)
	 */
	public void writeUTF(String str, int off, int len) throws IOException;
	
	/**
	 * Write string prefixed with length (s32, number of characters)
	 * If given array is null, is written only -1 (s32)
	 * 
	 * @param chars Characters to write
	 * 
	 * @throws IOException Any exception while writting occured
	 * 
	 * @see {@link Output#writeUTF(char[], int, int)
	 */
	public void writeUTF(char[] chars) throws IOException;
	public void writeUTF(char[] chars, int off, int len) throws IOException;
	
	/**
	 * 
	 * @param str
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	public void writeChars(String str, int off, int len) throws IOException;
	public void writeChars(char[] chars) throws IOException;
	public void writeChars(char[] chars, int off, int len) throws IOException;
	
	public void writeColl(Collection<?> set) throws SerializeException, IOException;
	public void writeCollArray(Object[] arr) throws SerializeException, IOException;
	public void writeCollArray(Object[] arr, int off, int len) throws SerializeException, IOException;
	public void writeMap(Map<?,?> map) throws SerializeException, IOException;
	
	public void writeObject(Object obj) throws SerializeException, IOException;
	public void writeObject(Object obj, int ri, int vi) throws SerializeException, IOException;

}
