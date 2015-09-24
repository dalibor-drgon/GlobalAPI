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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Collection;
import java.util.Map;

import eu.wordnice.db.serialize.CollSerializer;
import eu.wordnice.db.serialize.SerializeException;
import eu.wordnice.db.wndb.WNDBDecoder;

public abstract class InputAdv extends InputStream implements Input {
	
	@Override
	public abstract long skip(long bytes) throws IOException;
	
	@Override
	public int skipBytes(int n) throws IOException {
		int total = 0;
		int cur = 0;
		while((total < n) && ((cur = (int) this.skip(n - total)) > 0)) {
			total += cur;
		}
		return total;
	}
	
	@Override
	public abstract int read() throws IOException;
	
	@Override
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}
	
	@Override
	public abstract int read(byte[] b, int off, int len) throws IOException;
	
	@Override
	public int read(ByteBuffer buf) throws IOException {
		if(buf.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		int toRead = buf.remaining();
		if(buf.hasArray()) {
			int readed = this.read(buf.array(), buf.arrayOffset() + buf.position(), toRead);
			if(readed > 0) {
				buf.position(buf.position() + readed);
			}
			return readed;
		}
		byte[] buff = new byte[Math.min(16 * 1024, toRead)];
		int curRead = this.read(buff, 0, buff.length);
		buf.put(buff, 0, curRead);
		return curRead;
	}
	
	@Override
	public void readFully(ByteBuffer buf) throws IOException {
		if(buf.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		int toRead = buf.remaining();
		if(buf.hasArray()) {
			byte[] arr = buf.array();
			int off = buf.arrayOffset();
			int len = buf.remaining();
			int pos = buf.position();
			int n = 0;
			while (n < len) {
				int count = 0;
				try {
					count = this.read(arr, off + n, len - n);
				} catch(IOException ioe) {
					buf.position(pos);
					throw ioe;
				}
				if(count <= 0) {
					buf.position(pos);
					throw new EOFException();
				}
				n += count;
				if(n == len) {
					buf.position(pos + len);
				}
			}
			return;
		}
		byte[] buff = new byte[Math.min(8192, toRead)];
		int total = 0;
		int pos = buf.position();
		while(total < toRead) {
			int curRead = 0;
			try {
				curRead = this.read(buff, 0, Math.min(buff.length, toRead - total));
			} catch(IOException ioe) {
				buf.position(pos);
				throw ioe;
			}
			if(curRead <= 0) {
				buf.position(pos);
				throw new EOFException();
			}
			buf.put(buff, 0, curRead);
		}
		return;
	}
	
	
	@Override
	public byte readByte() throws IOException {
		int ch = this.read();
		if(ch < 0) {
			throw new EOFException();
		}
		return (byte) ch;
	}
	
	@Override
	public boolean readBoolean() throws IOException {
		int ch = this.read();
		if(ch < 0) {
			throw new EOFException();
		}
		return (ch != 0);
	}
	
	@Override
	public final int readUnsignedByte() throws IOException {
		int ch = this.read();
		if(ch < 0) {
			throw new EOFException();
		}
		return ch;
	}
	
	@Override
	public short readShort() throws IOException {
		int ch2 = this.read();
		int ch1 = this.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (short) ((ch1 << 8) | (ch2 << 0));
	}
	
	@Override
	public int readUnsignedShort() throws IOException {
		int ch2 = this.read();
		int ch1 = this.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (ch1 << 8) | (ch2 << 0);
	}
	
	@Override
	public char readChar() throws IOException {
		int ch2 = this.read();
		int ch1 = this.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (char) ((ch1 << 8) | (ch2 << 0));
	}
	
	@Override
	public int readInt() throws IOException {
		int ch4 = this.read();
		int ch3 = this.read();
		int ch2 = this.read();
		int ch1 = this.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
	}
	
	@Override
	public long readLong() throws IOException {
		int ch8 = this.read();
		int ch7 = this.read();
		int ch6 = this.read();
		int ch5 = this.read();
		int ch4 = this.read();
		int ch3 = this.read();
		int ch2 = this.read();
		int ch1 = this.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (((long) ch1 << 56) | ((long)ch2 << 48) | ((long)ch3 << 40) | ((long)ch4 << 32)
				| (ch5 << 24) | (ch6 << 16) | (ch7 << 8) | (ch8 << 0));
	}
	
	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(this.readInt());
	}
	
	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(this.readLong());
	}
	
	
	
	@Override
	public void readFully(byte[] b) throws IOException {
		this.readFully(b, 0, b.length);
	}
	
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		if(len < 0) {
			throw new IndexOutOfBoundsException("Length: " + len);
		}
		int n = 0;
		while (n < len) {
			int count = this.read(b, off + n, len - n);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}
	
	@Override
	public void readFully(char[] chars) throws IOException {
		this.readFully(chars, 0, chars.length);
	}
	
	@Override
	public void readFully(char[] chars, int off, int len) throws IOException {
		if(len < 0) {
			throw new IllegalArgumentException("Length: " + len);
		}
		if(len == 0) {
			return;
		}
		int clen = len;
		len *= 2;
		if(len < 0) {
			throw new IOException("Char[] exceeds limit 2GB (one milion characters)!");
		}
		int cur = 0;
		byte[] buff = new byte[Math.min(8192, len)];
		int nextread = buff.length;
		int bufoff = 0;
		int charIndex = 0;
		while((cur = this.read(buff, bufoff, nextread)) > 0) {
			cur = cur + bufoff;
			cur--;
			for(int i = 0; i < cur;) {
				chars[off + charIndex++] = (char) ((buff[i++] << 0) | (buff[i++] << 8));
			}
			nextread = Math.min(buff.length, (clen - charIndex) * 2);
			if(nextread == 0) {
				if((cur & 0x01) != 1) {
					throw new IOException("Internal decoder error! Chunk Norris?!");
				}
				break;
			}
			if((cur & 0x01) == 0) {
				bufoff = 1;
				buff[0] = buff[cur];
				nextread--;
			}
			
		}
		if(charIndex != clen) {
			throw new EOFException("Remaining " + (clen - charIndex) + " characters to read!");
		}
	}
	
	@Override
	public byte[] readBytes() throws IOException {
		int len = this.readInt();
		if(len < 0) {
			return null;
		}
		byte[] bytes = new byte[len];
		this.readFully(bytes);
		return bytes;
	}
	
	@Override
	public String readUTF() throws IOException {
		char[] chars = this.readUTFChars();
		if(chars == null) {
			return null;
		}
		return new String(chars);
	}
	
	@Override
	public char[] readUTFChars() throws IOException {
		int len = this.readInt();
		if(len < 0) {
			return null;
		}
		char[] chars = new char[len];
		this.readFully(chars);
		return chars;
	}
	
	@Override
	@Deprecated
	public String readLine() throws IOException {
		StringBuilder sb = new StringBuilder();
		while(true) {
			int ch2 = -1;
			int ch1 = -1;
			try {
				ch2 = this.read();
				ch1 = this.read();
			} catch(IOException io) {}
			if(ch1 < 0) {
				break;
			}
			char cur = (char) ((ch1 << 8) | (ch2 << 0));
			if(cur == '\n') {
				break;
			}
			sb.append(cur);
		}
		return sb.toString();
	}
	
	
	@Override
	public <X> Collection<X> readColl(Collection<X> col) throws SerializeException, IOException {
		return CollSerializer.stream2coll(col, this);
	}
	
	@Override
	public <X, Y> Map<X, Y> readMap(Map<X, Y> map) throws SerializeException, IOException {
		return CollSerializer.stream2map(map, this);
	}
	
	@Override
	public <X> Collection<X> readColl() throws SerializeException, IOException {
		return this.readColl((Collection<X>) null);
	}
	
	@Override
	public <X, Y> Map<X, Y> readMap() throws SerializeException, IOException {
		return this.readMap((Map<X, Y>) null);
	}
	
	@Override
	public Object readObject() throws SerializeException, IOException {
		return this.readObject(-1, -1);
	}
	
	@Override
	public Object readObject(int ri, int vi) throws SerializeException, IOException {
		return WNDBDecoder.readObject(this, this.readByte(), ri, vi);
	}

	
	
	
	/**
	 * @param file File, for which will be input stream created
	 * 
	 * @return Input stream for file
	 * @throws FileNotFoundException File does not exist
	 * 
	 * @see {@link InputAdv#forStreamBuff(InputStream)}
	 */
	public static InputAdv forFile(File file) throws FileNotFoundException {
		return InputAdv.forStreamBuff(new FileInputStream(file));
	}
	
	/**
	 * @param sock Socket, for which will be input stream created
	 * 
	 * @return
	 * @throws IOException See {@link Socket#getInputStream()}
	 * 
	 * @see {@link InputAdv#forStreamBuff(InputStream)}
	 */
	public static InputAdv forSocket(Socket sock) throws IOException {
		return InputAdv.forStreamBuff(sock.getInputStream());
	}
	
	/**
	 * @param in Input stream, for which will be created buffered stream
	 * @return Buffered input stream
	 * 
	 * @see {@link InputAdv#forStream(InputStream)}
	 */
	public static InputAdv forStreamBuff(InputStream in) {
		return InputAdv.forStream(new BufferedInputStream(in));
	}
	
	/**
	 * @param in Input stream, for which will be InputAdv created
	 * @return InputAdv for given stream
	 */
	public static InputAdv forStream(InputStream in) {
		return new InputAdvStream(in);
	}
}
