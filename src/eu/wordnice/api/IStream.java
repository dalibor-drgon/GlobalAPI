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

import eu.wordnice.sql.wndb.WNDBDecoder;

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

	public String readString() throws IOException {
		byte[] bytes = this.readBytes();
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}

	public byte[] readBytes() throws IOException {
		int len = this.readInt();
		if(len < 0) {
			return null;
		}
		return this.readFully(len);
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(this.readLong());
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(this.readInt());
	}
	
	public long readLong() throws IOException {
		byte[] b = this.readFully(8);
		return (long) ((((long) b[0] & 0xFF)) | (((long) b[1] & 0xFF) << 8) |
				(((long) b[2] & 0xFF) << 16) | (((long) b[3] & 0xFF) << 24) |
				(((long) b[4] & 0xFF) << 32) | (((long) b[5] & 0xFF) << 40) |
				(((long) b[6] & 0xFF) << 48) | (((long) b[7] & 0xFF) << 56));
	}

	public int readInt() throws IOException {
		byte[] b = this.readFully(4);
		return (int) ((((int) b[0] & 0xFF)) | (((int) b[1] & 0xFF) << 8) |
				(((int) b[2] & 0xFF) << 16) | (((int) b[3] & 0xFF) << 24));
	}

	public short readShort() throws IOException {
		byte[] b = this.readFully(2);
		return (short) ((((short) b[0] & 0xFF)) | (((short) b[1] & 0xFF) << 8));
	}

	public byte readByte() throws IOException {
		return (byte) this.read();
	}
	
	public boolean readBoolean() throws IOException {
		return (this.readByte() != 0);
	}
	
	
	public Set<Object> readSet() throws Exception {
		int size = this.readInt();
		if(size < 0) {
			return null;
		}
		Set<Object> set = new Set<Object>();
		int i = 0;
		for(; i < size; i++) {
			set.addWC(this.readObject(i, -1));
		}
		return set;
	}
	
	public Map<Object, Object> readMap() throws Exception {
		int size = this.readInt();
		if(size < 0) {
			return null;
		}
		Map<Object, Object> map = new Map<Object, Object>();
		int i = 0;
		for(; i < size; i++) {
			map.addWC(this.readObject(), this.readObject());
		}
		return map;
	}
	
	public Object readObject() throws Exception {
		return this.readObject(-1, -1);
	}
	
	public Object readObject(int ri, int vi) throws Exception {
		return WNDBDecoder.readObject(this, this.readByte(), ri, vi);
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
