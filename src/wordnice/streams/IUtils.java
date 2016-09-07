/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.streams;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.Collection;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.api.Nice.Value;
import wordnice.codings.ASCII;
import wordnice.db.serialize.CollSerializer;
import wordnice.db.serialize.SerializeException;
import wordnice.db.wndb.WNDBDecoder;

public class IUtils {
	
	public static void readFully(InputStream in, ByteBuffer buf) throws IOException {
		if(buf.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		int toRead = buf.remaining();
		int readed = 0;
		if((readed = tryToReadFully(in, buf)) != toRead) {
			throw Nice.eof(readed, toRead);
		}
	}
	
	public static int tryToReadFully(InputStream in, ByteBuffer buf) throws IOException {
		if(buf.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		int toRead = buf.remaining();
		if(buf.hasArray()) {
			byte[] arr = buf.array();
			int off = buf.arrayOffset();
			int len = buf.remaining();
			int n = 0;
			while (n < len) {
				int count = 0;
				try {
					count = in.read(arr, off + n, len - n);
				} catch(EOFException ioe) {}
				if(count < 1) {
					break;
				}
				n += count;
				if(n == len) {
					//buf.position(pos + len);
					break;
				}
			}
			return n;
		}
		byte[] buff = new byte[Math.min(8192, toRead)];
		int total = 0;
		while(total < toRead) {
			int curRead = 0;
			try {
				curRead = in.read(buff, 0, Math.min(buff.length, toRead - total));
			} catch(EOFException ioe) {}
			if(curRead < 1) {
				break;
			}
			total += curRead;
			buf.put(buff, 0, curRead);
		}
		return total;
	}
	
	
	public static byte readByte(InputStream in) throws IOException {
		int ch = in.read();
		if(ch < 0) {
			throw new EOFException();
		}
		return (byte) ch;
	}
	
	public static boolean readBoolean(InputStream in) throws IOException {
		int ch = in.read();
		if(ch < 0) {
			throw new EOFException();
		}
		return (ch != 0);
	}
	
	public static final int readUnsignedByte(InputStream in) throws IOException {
		int ch = in.read();
		if(ch < 0) {
			throw new EOFException();
		}
		return ch;
	}
	
	public static short readShort(InputStream in) throws IOException {
		int ch2 = in.read();
		int ch1 = in.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (short) ((ch1 << 8) | (ch2 << 0));
	}
	
	public static int readUnsignedShort(InputStream in) throws IOException {
		int ch2 = in.read();
		int ch1 = in.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (ch1 << 8) | (ch2 << 0);
	}
	
	public static char readChar(InputStream in) throws IOException {
		int ch2 = in.read();
		int ch1 = in.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return (char) ((ch1 << 8) | (ch2 << 0));
	}
	
	public static int readInt(InputStream in) throws IOException {
		int ch4 = in.read();
		int ch3 = in.read();
		int ch2 = in.read();
		int ch1 = in.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4 << 0));
	}
	
	public static long readLong(InputStream in) throws IOException {
		long ch8 = in.read();
		long ch7 = in.read();
		long ch6 = in.read();
		long ch5 = in.read();
		long ch4 = in.read();
		long ch3 = in.read();
		long ch2 = in.read();
		long ch1 = in.read();
		if(ch1 < 0) {
			throw new EOFException();
		}
		return ((ch1 << 56) | (ch2 << 48) | (ch3 << 40) | (ch4 << 32)
				| (ch5 << 24) | (ch6 << 16) | (ch7 << 8) | (ch8 << 0));
	}
	
	public static char readChar(byte[] bt, int off) throws IOException {
		return (char) ((char) bt[off++] | (bt[off++] << 8));
	}
	
	public static short readShort(byte[] bt, int off) throws IOException {
		return (short) ((short) bt[off++] | (bt[off++] << 8));
	}
	
	public static int readInt(byte[] bt, int off) throws IOException {
		return ((int) bt[off++] | (bt[off++] << 8) | (bt[off++] << 16) | (bt[off++] << 24));
	}
	
	public static long readLong(byte[] bt, int off) throws IOException {
		return ((long) bt[off++] | (bt[off++] << 8) | (bt[off++] << 16) | (bt[off++] << 24)
			| (bt[off++] << 32) | (bt[off++] << 40) | (bt[off++] << 48) | (bt[off++] << 56));
	}
	
	public static float readFloat(InputStream in) throws IOException {
		return Float.intBitsToFloat(readInt(in));
	}
	
	public static double readDouble(InputStream in) throws IOException {
		return Double.longBitsToDouble(readLong(in));
	}
	
	public static int tryToReadFully(InputStream in, byte[] b) throws IOException {
		return tryToReadFully(in, b, 0, b.length);
	}
	
	public static int tryToReadFully(InputStream in, byte[] b, int off, int len) throws IOException {
		Nice.checkBounds(b, off, len);
		int n = 0;
		while (n < len) {
			int count = -1;
			try {
				count = in.read(b, off + n, len - n);
			} catch(EOFException eof) {}
			if (count < 1) {
				return n;
			}
			n += count;
		}
		return n;
	}
	
	public static void readFully(InputStream in, byte[] b) throws IOException {
		readFully(in, b, 0, b.length);
	}
	
	public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
		int readed = 0;
		if((readed = tryToReadFully(in, b, off, len)) != len) {
			throw Nice.eof(readed, len);
		}
	}
	
	protected int readFullyInitSize() {
		return 8*1024;
	}
	
	protected int readFullyBufferSize() {
		return 8*1024;
	}
	
	public static ByteArrayOutputStream readFully(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(Nice.bufferSize);
		readFully(in, baos);
		return baos;
	}
	
	public static int readFully(InputStream in, OutputStream out) throws IOException {
		byte[] buff = new byte[Nice.bufferSize];
		int total = 0;
		while(true) {
			int readed = -1;
			try {
				readed = in.read(buff);
			} catch(EOFException eof) {}
			if(readed < 1) {
				break;
			}
			total += readed;
			out.write(buff, 0, readed);
		}
		return total;
	}
	
	public static byte[] deserializeBytes(InputStream in) throws IOException {
		int len = readInt(in);
		if(len < 0) {
			return null;
		}
		byte[] bytes = new byte[len];
		readFully(in, bytes);
		return bytes;
	}
	
	public static int tryToReadFully(InputStream in, char[] chars) throws IOException {
		if(in == null || chars == null) {
			throw Nice.illegal("InputStream stream or array is null!");
		}
		return tryToReadFully(in, chars, 0, chars.length);
	}
	
	public static int tryToReadFully(InputStream in, char[] chars, int off, int len) throws IOException {
		Nice.checkBounds(chars, off, len);
		if(len == 0) {
			return 0;
		}
		int clen = len;
		len *= 2;
		if(len < 0) {
			throw new IOException("Char[] exceeds limit 2GB (one milion characters)!");
		}
		byte[] buff = new byte[Math.min(8192, len)];
		int nextread = buff.length;
		int bufoff = 0;
		int charIndex = 0;
		while(true) {
			int cur = -1;
			try {
				cur = in.read(buff, bufoff, nextread);
			} catch(EOFException eof) {}
			if(cur < 1) {
				break;
			}
			cur = cur + bufoff;
			cur--;
			int i = 0;
			for(i = 0; i < cur;) {
				chars[off + charIndex++] = (char)((buff[i++] & 0xff) | (buff[i++] << 8));
			}
			nextread = Math.min(buff.length, (clen - charIndex) * 2);
			if(nextread == 0) {
				if((cur & 0x01) != 1) {
					throw new InternalError("Internal decoder error! Chunk Norris?!"); //impossibru
				}
				break;
			}
			if((cur & 0x01) == 0) {
				bufoff = 1;
				buff[0] = buff[cur];
				nextread--;
			} else {
				bufoff = 0;
			}
			
		}
		return charIndex;
	}
	
	public static void readFully(InputStream in, char[] chars) throws IOException {
		readFully(in, chars, 0, chars.length);
	}
	
	public static void readFully(InputStream in, char[] chars, int off, int len) throws IOException {
		int readed = 0;
		if((readed = tryToReadFully(in, chars, off, len)) != len) {
			throw Nice.eof(readed, len);
		}
	}
	
	public static char[] deserializeUTFChars(InputStream in) throws IOException {
		int len = readInt(in);
		if(len < 0) {
			return null;
		}
		char[] chars = new char[len];
		readFully(in, chars);
		return chars;
	}
	
	public static String deserializeUTF(InputStream in) throws IOException {
		char[] chars = deserializeUTFChars(in);
		if(chars == null) {
			return null;
		}
		return String.copyValueOf(chars);
	}
	
	public static boolean readLine(InputStream in, OutputStream out) throws IOException {
		boolean hasWrite = false;
		while(true) {
			int ch = -1;
			try {
				ch = in.read();
			} catch(EOFException eof) {}
			if(ch == -1) {
				if(!hasWrite) {
					return false;
				}
				break;
			}
			hasWrite = true;
			if(ch == '\n') {
				break;
			} else if(ch == '\r') {
				try {
					ch = in.read();
				} catch(EOFException eof) {}
				if(ch == '\n' || ch == -1) {
					break;
				}
				out.write('\r');
				out.write(ch);
			}
			out.write((byte) ch);
		}
		return true;
	}
	
	public static boolean readLineStrict(InputStream in, OutputStream out) throws IOException {
		boolean hasWrite = false;
		while(true) {
			int ch = -1;
			try {
				ch = in.read();
			} catch(EOFException eof) {}
			if(ch == -1) {
				if(!hasWrite) {
					return false;
				}
				break;
			}
			hasWrite = true;
			if(ch == '\n') {
				break;
			} else if(ch == '\r') {
				try {
					ch = in.read();
				} catch(EOFException eof) {}
				if(ch == '\n') {
					break;
				}
				throw new IOException("\\R not followed by \\N!");
			}
			out.write((byte) ch);
		}
		return true;
	}
	
	public static String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream baos = Nice.createArrayOutput();
		return readLine(in, baos) ? baos.toString() : null;
	}
	
	public static String readLineStrict(InputStream in) throws IOException {
		ByteArrayOutputStream baos = Nice.createArrayOutput();
		return readLineStrict(in, baos) ? baos.toString() : null;
	}
	
	
	public static <X> Collection<X> deserializeColl(InputStream in, Collection<X> col) throws SerializeException, IOException {
		return CollSerializer.collFromStream(col, in);
	}
	
	public static <X, Y> Map<X, Y> deserializeMap(InputStream in, Map<X, Y> map) throws SerializeException, IOException {
		return CollSerializer.mapFromStream(map, in);
	}
	
	public static <X> Collection<X> deserializeColl(InputStream in) throws SerializeException, IOException {
		return deserializeColl(in, (Collection<X>) null);
	}
	
	public static <X, Y> Map<X, Y> deserializeMap(InputStream in) throws SerializeException, IOException {
		return deserializeMap(in, (Map<X, Y>) null);
	}
	
	public static Object deserializeKnownObject(InputStream in) throws SerializeException, IOException {
		return deserializeKnownObject(in, -1, -1);
	}
	
	public static Object deserializeKnownObject(InputStream in, int ri, int vi) throws SerializeException, IOException {
		return WNDBDecoder.deserializeKnownObject(in, readByte(in), ri, vi);
	}

	
	public static void readUntil(InputStream in, OutputStream out, byte[] bytes) throws IOException, EOFException {
		if(bytes == null) {
			throw Nice.illegal("Bytes = null");
		}
		readUntil(in, out, bytes, 0, bytes.length);
	}
	
	public static void readUntil(InputStream in, OutputStream out, byte[] bytes, int off, int len) 
			throws IOException, EOFException {
		readUntil(in, out, bytes, off, len, new byte[len*5]);
	}
	
	public static void readFullyNowhere(InputStream in) throws IOException {
		while(in.read(Nice.loopBuffer) > 0);
	}
	
	public static void readUntil(InputStream in, OutputStream out, 
			byte[] bytes, int off, int len, byte[] buffer) 
			throws IOException, EOFException {
		Nice.checkBounds(bytes, off, len);
		if(buffer == null || buffer.length < len*2) {
			buffer = new byte[len*2];
		}
		int maxbf = buffer.length-len;
		int bf = 0;
		readFully(in, buffer, 0, len);
		while(true) {
			if(ASCII.equals(bytes, off, len, buffer, bf, len)) {
				out.write(buffer, 0, bf);
				return;
			}
			//out.write(buffer[bf]);
			int c = in.read();
			if(c < 0) {
				throw new EOFException("Stream probably closed or got EOF!");
			}
			buffer[len + bf++] = (byte) c;
			if(bf == maxbf) {
				out.write(buffer, 0, maxbf);
				System.arraycopy(buffer, bf, buffer, 0, len);
				bf = 0;
			}
		}
	}
	
	public static void readUntilEOF(InputStream in, OutputStream out, byte[] bytes) throws IOException, EOFException {
		if(bytes == null) {
			throw Nice.illegal("Bytes = null");
		}
		readUntilEOF(in, out, bytes, 0, bytes.length);
	}
	
	public static void readUntilEOF(InputStream in, OutputStream out, byte[] bytes, int off, int len) 
			throws IOException, EOFException {
		readUntilEOF(in, out, bytes, off, len, new byte[len*5], null);
	}
	
	//IN DEV
	public static void readUntilEOF(InputStream in, OutputStream out, 
			byte[] bytes, int off, int len, byte[] buffer, Value<Integer> bufferDataLength) 
			throws IOException, EOFException {
		Nice.checkBounds(bytes, off, len);
		if(buffer == null || buffer.length < len*2) {
			buffer = new byte[len*3];
		}
		int readed = tryToReadFully(in,buffer);
		int maxbf = buffer.length-len+1;
		int bf = len-1;
		if(readed < len) {
			throw new EOFException("Stream probably closed or got EOF!");
		}
		readed -= bf;
		while(true) {
			int index = ASCII.indexOf(buffer, 0, readed, bytes, off, len);
			if(readed != maxbf) {
				if(index < 0) {
					throw new EOFException("Stream probably closed or got EOF!"); //Nothing found, and we are at EOF
				}
			}
			if(index >= 0) {
				if(bufferDataLength != null) {
					int indexEnd = index + len;
					int afterLength = readed - indexEnd;
					bufferDataLength.setValue(afterLength);
					System.arraycopy(buffer, indexEnd, buffer, 0, afterLength);
				}
				return;
			}
			System.arraycopy(buffer, maxbf, buffer, 0, bf);
			readed = tryToReadFully(in,buffer, bf, maxbf);
		}
	}
	
	/**
	 * Fast read until
	 * Two buffers, could be any size (size of smaller <= bufferlen)
	 * One of buffers should be as prefix for given PrefixedInputStream
	 * These buffers are *usualy* swappend with call of readUntilEOF
	 * EOFException is throws when zero or -1 is returned from in.read(bytes)
	 * 
	 * NOTE: If prefixedinputstream returns null prefix, buffer1 is used for it
	 */
	//IN DEV
	public static void readUntilEOF(PrefixedInputStream in, OutputStream out, 
			byte[] bytes, int off, int len, byte[] buffer1, byte[] buffer2, int bufferlen) 
			throws IOException, EOFException {
		Nice.checkBounds(bytes, off, len);
		if(buffer1 == null || buffer2 == null) {
			throw Nice.illegal("Buffer = null!");
		}
		byte[] buffer = (in.getPrefix() == buffer1) ? buffer2 : buffer1; 
		byte[] inbuff = (buffer == buffer2) ? buffer1 : buffer2;
			//when prefix null, buf2 is used for local reading (buffer)
			//and buf1 for PrefixedInputStream (inbuff)
		int readed = tryToReadFully(in,buffer, 0, bufferlen);
		int maxbf = bufferlen-len+1;
		int bf = len-1;
		if(readed < len) {
			throw new EOFException("Stream probably closed or got EOF!");
		}
		readed -= bf;
		while(true) {
			int index = ASCII.indexOf(buffer, 0, readed, bytes, off, len);
			System.out.println("Readed " + readed + " / " + maxbf);
			if(readed != maxbf) {
				if(index < 0) {
					throw new EOFException("Stream probably closed or got EOF!"); //Nothing found, and we are at EOF
				}
			}
			if(index >= 0) {
				int indexEnd = index + len;
				int afterLength = readed - indexEnd;
				if(in.getPrefix() == null) {
					System.arraycopy(buffer, indexEnd, inbuff, 0, afterLength);
					in.setPrefix(inbuff, 0, afterLength);
					//in.setPrefix(buffer, indexEnd, afterLength);
				} else {
					byte[] prefix = in.trimPrefix().getPrefix();
					int preflen = in.getLength();
					if(preflen+afterLength > bufferlen) {
						throw new InternalError("Impossibru!");
					}
					System.arraycopy(buffer, indexEnd, prefix, preflen, afterLength);
					in.setPrefix(prefix, 0, preflen+afterLength);
				}
				return;
			}
			System.arraycopy(buffer, maxbf, buffer, 0, bf);
			System.out.println("Reading... " + bf + " / " + maxbf);
			readed = tryToReadFully(in,buffer, bf, maxbf);
			System.out.println("--Readed " + readed);
		}
		
	}
	
}
