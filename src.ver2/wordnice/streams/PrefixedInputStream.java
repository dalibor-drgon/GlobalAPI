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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import wordnice.api.Nice;

public class PrefixedInputStream 
extends FilterInputStream 
implements AutoCloseable {

	protected volatile byte[] buf;
	protected volatile int off;
	protected volatile int len;
	
	
	public PrefixedInputStream(InputStream in) {
		super(in);
		this.setPrefix(null);
	}
	
	public InputStream getRoot() {
		return super.in;
	}
	
	public PrefixedInputStream setRoot(InputStream in) {
		super.in = in;
		return this;
	}
	
	
	public byte[] getPrefix() {
		return this.buf;
	}
	
	public int getOffset() {
		return this.off;
	}
	
	public int getLength() {
		return this.len;
	}
	
	public PrefixedInputStream setPrefix(byte[] buffer) {
		if(buffer == null || buffer.length == 0) {
			this.buf = null;
			this.off = 0;
			this.len = 0;
		} else {
			this.buf = buffer;
			this.off = 0;
			this.len = buffer.length;
		}
		return this;
	}
	
	public PrefixedInputStream setPrefix(byte[] buffer, int off, int len) {
		if(buffer == null || len == 0) {
			this.buf = null;
			this.off = 0;
			this.len = 0;
		} else {
			Nice.checkBounds(buffer, off, len);
			this.buf = buffer;
			this.off = off;
			this.len = len;
		}
		return this;
	}
	
	public PrefixedInputStream trimPrefix() {
		if(this.off == 0) {
			return this;
		}
		System.arraycopy(buf, off, buf, 0, len);
		this.off = 0;
		return this;
	}

	
	@Override
	public int read() throws IOException {
		System.out.println("Read- ");
		if(this.len > 0) {
			this.len--;
			int ret = (int) this.buf[this.off++];
			if(this.len == 0) {
				this.off = 0;
				this.buf = null;
			}
			return ret;
		}
		return super.in.read();
	}
	
	@Override
	public int read(byte[] buff, int off, int len) throws IOException {
		Nice.checkBounds(buff, off, len);
		System.out.println("Read: " + off + "," + len + " / " + this.len + " / " + this.buf);
		if(this.len > 0) {
			int possible = (len > this.len) ? this.len : len;
			System.arraycopy(this.buf, this.off, buff, off, possible);
			System.out.println("--Readed from buff " + possible + " " + new String(buff, off, possible));
			this.len -= possible;
			this.off += possible;
			if(this.len == 0) {
				this.buf = null;
				this.off = 0;
			}
			return possible;
		}
		System.out.println("--Reading from in...");
		return this.in.read(buff, off, len);
	}
	
	
}
