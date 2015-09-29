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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import eu.wordnice.db.ColType;
import eu.wordnice.db.serialize.CollSerializer;
import eu.wordnice.db.serialize.SerializeException;
import eu.wordnice.db.wndb.WNDBEncoder;

public abstract class OutputAdv extends OutputStream implements Output {
	
	@Override
	public abstract void write(int byt) throws IOException;
	
	@Override
	public void write(byte[] bytes) throws IOException {
		this.write(bytes, 0, bytes.length);
	}
	
	@Override
	public abstract void write(byte[] bytes, int off, int len) throws IOException;
	
	@Override
	public int write(ByteBuffer buf) throws IOException {
		int toWrite = buf.remaining();
		if(buf.hasArray() && !buf.isReadOnly()) {
			this.write(buf.array(), buf.arrayOffset() + buf.position(), toWrite);
			buf.position(buf.position() + toWrite);
			return toWrite;
		}
		byte[] buff = new byte[Math.min(8192, toWrite)];
		if(buff.length == toWrite) {
			buf.get(buff);
			this.write(buff);
			return toWrite;
		}
		int total = 0;
		while(toWrite != total) {
			int curWrite = Math.min(buff.length, toWrite - total);
			buf.get(buff, 0, curWrite);
			this.write(buff, 0, curWrite);
			total += curWrite;
		}
		return toWrite;
	}
	
	
	@Override
	public void writeBoolean(boolean v) throws IOException {
		this.write(v ? 1 : 0);
	}
	
	@Override
	public void writeByte(int v) throws IOException {
		this.write(v);
	}
	
	@Override
	public void writeShort(int v) throws IOException {
		this.write((v >>> 0) & 0xFF);
		this.write((v >>> 8) & 0xFF);
	}
	
	@Override
	public void writeChar(int v) throws IOException {
		this.write((v >>> 0) & 0xFF);
		this.write((v >>> 8) & 0xFF);
	}
	
	@Override
	public void writeInt(int v) throws IOException {
		this.write((v >>>  0) & 0xFF);
		this.write((v >>>  8) & 0xFF);
		this.write((v >>> 16) & 0xFF);
		this.write((v >>> 24) & 0xFF);
	}
	
	@Override
	public void writeLong(long v) throws IOException {
		this.write((int) ((v >>  0) & 0xFF));
		this.write((int) ((v >>  8) & 0xFF));
		this.write((int) ((v >> 16) & 0xFF));
		this.write((int) ((v >> 24) & 0xFF));
		this.write((int) ((v >> 32) & 0xFF));
		this.write((int) ((v >> 40) & 0xFF));
		this.write((int) ((v >> 48) & 0xFF));
		this.write((int) ((v >> 56) & 0xFF));
	}

	@Override
	public void writeFloat(float v) throws IOException {
		this.writeInt(Float.floatToIntBits(v));
	}
	
	@Override
	public void writeDouble(double v) throws IOException {
		this.writeLong(Double.doubleToLongBits(v));
	}
	
	@Override
	public void writeBytes(byte[] bytes) throws IOException {
		if(bytes == null) {
			this.writeInt(-1);
			return;
		}
		int len = bytes.length;
		this.writeInt(len);
		this.write(bytes);
	}
	
	@Override
	public void writeBytes(byte[] bytes, int off, int len) throws IOException {
		if(bytes == null || len < 0) {
			this.writeInt(-1);
			return;
		}
		this.writeInt(len);
		this.write(bytes, off, len);
	}
	
	@Override
	@Deprecated
	public void writeBytes(String str) throws IOException {
		int n = str.length();
		byte[] buff = new byte[Math.min(8192, n)];
		if(buff.length == n) {
			for(int i = 0; i < n; i++) {
				buff[i] = (byte) str.charAt(i);
			}
			this.write(buff);
			return;
		}
		int strI = 0;
		while(strI < n) {
			int curWrite = Math.min(buff.length, n);
			for(int i = 0; i < curWrite;) {
				buff[i++] = (byte) str.charAt(strI++);
			}
			this.write(buff, 0, curWrite);
		}
	}
	
	@Override
	public void writeUTF(String str) throws IOException {
		if(str == null) {
			this.writeInt(-1);
			return;
		}
		int len = str.length();
		this.writeInt(len);
		this.writeChars(str, 0, len);
	}
	
	@Override
	public void writeUTF(String str, int off, int len) throws IOException {
		if(str == null || len < 0) {
			this.writeInt(-1);
			return;
		}
		this.writeInt(len);
		this.writeChars(str, off, len);
	}
	
	@Override
	public void writeUTF(char[] chars) throws IOException {
		if(chars == null) {
			this.writeInt(-1);
			return;
		}
		int len = chars.length;
		this.writeInt(len);
		this.writeChars(chars, 0, len);
	}
	
	@Override
	public void writeUTF(char[] chars, int off, int clen) throws IOException {
		if(chars == null || clen < 0) {
			this.writeInt(-1);
			return;
		}
		this.writeInt(clen);
		this.writeChars(chars, off, clen);
	}
	
	@Override
	public void writeChars(char[] chars) throws IOException {
		this.writeChars(chars, 0, chars.length);
	}
	
	@Override
	public void writeChars(char[] chars, int off, int clen) throws IOException {
		int len = clen * 2;
		if(len < 0) {
			throw new IOException("Char[] exceeds limit 2GB (one milion characters)!");
		}
		byte[] buff = new byte[Math.min(8192, len)];
		clen += off;
		while(off < clen) {
			int curChars = Math.min((clen - off) * 2, buff.length);
			for(int i = 0; i < curChars; off++) {
				char cur = chars[off];
				buff[i++] = (byte) (cur & 0xFF);
				buff[i++] = (byte) (cur >> 8);
			}
			this.write(buff, 0, curChars);
		}
	}
	
	@Override
	public void writeChars(String str) throws IOException {
		this.writeChars(str, 0, str.length());
	}
	
	@Override
	public void writeChars(String str, int off, int clen) throws IOException {
		int len = clen * 2;
		if(len < 0) {
			throw new IOException("Char[] exceeds limit 2GB (one milion characters)!");
		}
		byte[] buff = new byte[Math.min(8192, len)];
		clen += off;
		while(off < clen) {
			int curChars = Math.min((clen - off) * 2, buff.length);
			for(int i = 0; i < curChars; off++) {
				char cur = str.charAt(off);
				buff[i++] = (byte) (cur & 0xFF);
				buff[i++] = (byte) (cur >> 8);
			}
			this.write(buff, 0, curChars);
		}
	}
	
	
	@Override
	public void writeColl(Collection<?> set) throws SerializeException, IOException {
		if(set == null) {
			this.writeInt(-1);
			return;
		}
		this.writeColl(set.iterator(), set.size());
	}
	
	@Override
	public void writeColl(Iterator<?> it, int size) throws SerializeException, IOException {
		CollSerializer.coll2stream(this, it, size);
	}
	
	@Override
	public void writeMap(Map<?,?> map) throws SerializeException, IOException {
		if(map == null) {
			this.writeInt(-1);
			return;
		}
		CollSerializer.map2stream(this, map);
	}
	
	@Override
	public void writeCollArray(Object[] arr) throws SerializeException, IOException {
		if(arr == null) {
			this.writeInt(-1);
			return;
		}
		CollSerializer.collarray2stream(this, arr, 0, arr.length);
	}
	
	@Override
	public void writeCollArray(Object[] arr, int off, int len) throws SerializeException, IOException {
		if(arr == null || len < 0) {
			this.writeInt(-1);
			return;
		}
		CollSerializer.collarray2stream(this, arr, off, len);
	}
	
	@Override
	public void writeObject(Object obj) throws SerializeException, IOException {
		this.writeObject(obj, -1, -1);
	}
	
	@Override
	public void writeObject(Object obj, int ri, int vi) throws SerializeException, IOException {
		if(obj == null) {
			this.writeByte(ColType.BYTES.b);
			this.writeInt(-1);
		} else {
			ColType typ = ColType.getByObject(obj);
			this.writeByte(typ.b);
			WNDBEncoder.writeObject(this, obj, typ, ri, vi);
		}
	}
	
	
	
	/**
	 * @param file File, for which will be output stream created
	 * 
	 * @return Output stream for file
	 * @throws FileNotFoundException File does not exist
	 * 
	 * @see {@link OutputAdv#forStreamBuff(OutputStream)}
	 */
	public static OutputAdv forFile(File file) throws FileNotFoundException {
		return OutputAdv.forStreamBuff(new FileOutputStream(file));
	}
	
	/**
	 * @param sock Socket, for which will be output stream created
	 * 
	 * @return
	 * @throws IOException See {@link Socket#getOutputStream()}
	 * 
	 * @see {@link OutputAdv#forStreamBuff(OutputStream)}
	 */
	public static OutputAdv forSocket(Socket sock) throws IOException {
		return OutputAdv.forStreamBuff(sock.getOutputStream());
	}
	
	/**
	 * @param in Output stream, for which will be created buffered stream
	 * @return Buffered Output stream
	 * 
	 * @see {@link OutputAdv#forStream(OutputStream)}
	 */
	public static OutputAdv forStreamBuff(OutputStream out) {
		return OutputAdv.forStream(new BufferedOutputStream(out));
	}
	
	/**
	 * @param in Output stream, for which will be OutputAdv created
	 * @return OutputAdv for given stream
	 */
	public static OutputAdv forStream(OutputStream out) {
		return new OutputAdvStream(out);
	}

}
