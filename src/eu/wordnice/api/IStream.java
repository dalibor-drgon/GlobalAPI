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

package eu.wordnice.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IStream extends InputStream {

	public InputStream in;

	public IStream() {
	}

	public IStream(InputStream in) {
		this.in = in;
	}

	/*** Special reading ***/

	public byte[] readFully(int all) throws IOException {
		if (all == 0) {
			return new byte[0];
		}
		if (all < 0) {
			return null;
		}
		int lene = 1024 * 4;
		int len = (lene > all) ? all : lene;
		byte[] buffer = new byte[len];
		// try {

		int cur = this.in.read(buffer);
		if (cur == 0) {
			return null;
		}
		if (cur == all) {
			return buffer;
		}

		int total = cur;
		int left = all - total;
		if (left > lene) {
			left = lene;
		}
		int c_total;
		ByteArrayOutputStream write = new ByteArrayOutputStream();

		while (left > 0 && (cur = this.in.read(buffer, 0, left)) > 0) {
			total = total + cur;
			c_total = total + lene;
			if (c_total > all) {
				left = c_total - all;
				if (left > lene) {
					left = lene;
				}
			}
			write.write(buffer, 0, cur);
		}
		return write.toByteArray();

	}

	/*
	 * public static int byteArrayToInt(byte[] b) { int value = 0; for (int i =
	 * 0; i < 4; i++) { int shift = (4 - 1 - i) * 8; value += (b[i] &
	 * 0x000000FF) << shift; } return value; }
	 */

	public String readString() throws IOException {
		return new String(this.readBytes());
	}

	public byte[] readBytes() throws IOException {
		int len = this.readInt();
		return this.readFully(len);
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(this.readLong());
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(this.readInt());
	}
	
	public long readuLong() throws IOException {
		return (long) (this.readLong() - Long.MIN_VALUE);
	}

	public long readLong() throws IOException {
		byte[] b = this.readFully(8);
		//long value = 0;
		/*value += b[0] << 24;
		value += b[1] << 16;
		value += b[2] << 8;
		value += b[3];*/
		/*value = ((long)b[0] << 56) + ((long)b[1] << 48) + ((long)b[2] << 40) + ((long)b[3] << 32) +
				((long)b[4] << 24) + ((long)b[5] << 16) + ((long)b[6] << 8) + ((long)b[7]);*/
		return (long) (((long) b[0] & 0xffL) << 56) + (((long) b[1] & 0xffL) << 48) +
				(((long) b[2] & 0xffL) << 40) + (((long) b[3] & 0xffL) << 32) + 
				(((long) b[4] & 0xffL) << 24) + (((long) b[5] & 0xffL) << 16) +
				(((long) b[6] & 0xffL) << 8) + ((long) b[7] & 0xffL);
		//System.out.println("Read long: " + value);
		//return value;
	}
	
	public long readuInt() throws IOException {
		return ((long) this.readInt() - Integer.MIN_VALUE);
	}

	public int readInt() throws IOException {
		byte[] b = this.readFully(4);
		int value = 0;
		/*for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}*/
		value = (b[0] << 24) + (b[1] << 16) + (b[2] << 8) + (b[3]);
		return value;
	}
	
	public int readuShort() throws IOException {
		return ((int) this.readShort() - Short.MIN_VALUE);
	}

	public short readShort() throws IOException {
		byte[] b = this.readFully(2);
		short value = 0;
		/*for (int i = 0; i < 2; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i] & 0x000000FF) << shift;
		}*/
		value = (short) ((b[0] << 8) + (b[1]));
		return value;
	}
	
	public short readuByte() throws IOException {
		return (short) ((short) this.readByte() - Byte.MIN_VALUE);
	}

	public byte readByte() throws IOException {
		return (byte) this.read();
	}
	
	public boolean readBoolean() throws IOException {
		return (this.readByte() != 0);
	}

	/*** Override ***/

	@Override
	public int read() throws IOException {
		return this.in.read();
	}

	@Override
	public int read(byte[] bytes) throws IOException {
		return this.in.read(bytes);
	}

	@Override
	public int read(byte[] bytes, int of, int l) throws IOException {
		return this.in.read(bytes, of, l);
	}

	@Override
	public void close() throws IOException {
		this.in.close();
	}

	@Override
	public int available() throws IOException {
		return this.in.available();
	}

	@Override
	public void mark(int i) {
		this.in.mark(i);
	}

	@Override
	public long skip(long i) throws IOException {
		return this.in.skip(i);
	}

	@Override
	public boolean markSupported() {
		return this.in.markSupported();
	}

	@Override
	public void reset() throws IOException {
		this.in.reset();
	}

}
