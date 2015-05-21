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
import java.io.InputStream;

public class TimeoutInputStream extends InputStream {
	
	public final InputStream in;
	public TimeoutThread<Object> thread;
	
	public TimeoutInputStream(InputStream in, long maxtime_read) {
		this.in = in;
		this.thread = new TimeoutThread<Object>(null, maxtime_read);
	}

	@Override
	public int read() throws IOException {
		Runa<Object> run = new Runa<Object>() {
			
			@Override
			public Object call() throws Exception {
				try {
					return TimeoutInputStream.this.in.read();
				} catch(IOException t) {
					return t;
				}
			};
			
		};
		thread.run = run;
		Object out = thread.run(null);
		if(out instanceof Integer) {
			return (Integer) out;
		}
		if(out instanceof IOException) {
			throw (IOException) out;
		}
		return -1;
	}

	@Override
	public int read(final byte[] bytes) throws IOException {
		Runa<Object> run = new Runa<Object>() {
			
			@Override
			public Object call() throws Exception {
				try {
					return TimeoutInputStream.this.in.read(bytes);
				} catch(IOException t) {
					return t;
				}
			};
			
		};
		thread.run = run;
		Object out = thread.run(null);
		if(out instanceof Integer) {
			return (Integer) out;
		}
		if(out instanceof IOException) {
			throw (IOException) out;
		}
		return 0;
	}

	@Override
	public int read(final byte[] bytes, final int of, final int l) throws IOException {
		Runa<Object> run = new Runa<Object>() {
			
			@Override
			public Object call() throws Exception {
				try {
					return TimeoutInputStream.this.in.read(bytes, of, l);
				} catch(IOException t) {
					return t;
				}
			};
			
		};
		thread.run = run;
		Object out = thread.run(null);
		if(out instanceof Integer) {
			return (Integer) out;
		}
		if(out instanceof IOException) {
			throw (IOException) out;
		}
		return 0;
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
