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

package eu.wordnice.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import eu.wordnice.api.serialize.BadFilePrefixException;
import eu.wordnice.api.serialize.CollSerializer;
import eu.wordnice.api.serialize.SerializeException;
import eu.wordnice.db.wndb.WNDBDecoder;

public class IStream extends InputStream {

	public InputStream in;
	
	public IStream() {}
	
	public IStream(InputStream in) {
		this.in = in;
	}

	/*** Special reading ***/

	public byte[] readFully(int all) throws IOException {
		if (all == 0) {
			return new byte[0];
		}
		if(all < 0) {
			throw new java.lang.IllegalArgumentException("readFully request with invalid size " + all);
		}
		byte[] ret = new byte[all];
		int left = all;
		int cur = 0;
		int total = 0;
		while((cur = this.read(ret, total, left)) > 0) {
			left -= cur;
			total += cur;
			if(left == 0) {
				break;
			}
		}
		if(left != 0) {
			throw new IOException("EOF, readFully readed " + total + " / " + all +" bytes!");
		}
		return ret;
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
	
	
	public <X> Collection<X> readColl(Collection<X> col) throws SerializeException, IOException {
		int ch = this.readInt();
		if(ch == -1) {
			return null;
		}
		if(ch != CollSerializer.SET_PREFIX) {
			throw new BadFilePrefixException("Not SET!");
		}
		CollSerializer.stream2collWithoutPrefix(col, this);
		return col;
	}
	
	public <X, Y> Map<X, Y> readMap(Map<X, Y> map) throws SerializeException, IOException {
		int ch = this.readInt();
		if(ch == -1) {
			return null;
		}
		if(ch != CollSerializer.MAP_PREFIX) {
			throw new BadFilePrefixException("Not MAP!");
		}
		CollSerializer.stream2mapWithoutPrefix(map, this);
		return map;
	}
	
	public Object readObject() throws SerializeException, IOException {
		return this.readObject(-1, -1);
	}
	
	public Object readObject(int ri, int vi) throws SerializeException, IOException {
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
