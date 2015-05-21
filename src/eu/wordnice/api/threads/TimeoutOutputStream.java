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

package eu.wordnice.api.threads;

import java.io.IOException;
import java.io.OutputStream;

public class TimeoutOutputStream extends OutputStream {
	
	public final OutputStream out;
	public TimeoutThread<Object> thread;
	
	@Deprecated
	public TimeoutOutputStream(OutputStream stream, long maxtime_read) {
		this.out = stream;
		this.thread = new TimeoutThread<Object>(null, maxtime_read);
		throw new RuntimeException("//TODO");
	}
	
	@Override
	public void write(int obyte) throws IOException {
		this.out.write(obyte);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		this.out.write(bytes);
	}

	@Override
	public void write(byte[] bytes, int off, int l) throws IOException {
		this.out.write(bytes, off, l);
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}
	
}
