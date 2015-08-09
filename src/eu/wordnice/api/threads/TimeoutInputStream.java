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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class TimeoutInputStream extends InputStream {
	
	public final InputStream in;
	public long timeout;
	public boolean safe;
	
	public TimeoutInputStream(InputStream in, long maxtime_read) {
		this(in, maxtime_read, true);
	}
	
	public TimeoutInputStream(InputStream in, long maxtime_read, boolean safe) {
		this.in = in;
		this.timeout = maxtime_read;
		this.safe = safe;
	}
	
	public void setTimeout(long to) {
		this.timeout = to;
	}
	

	@Override
	public int read() throws IOException {
		if(this.safe) {
			try {
				return TimeoutInputStream.read(this.in, this.timeout);
			} catch(Throwable t) {
				if(t instanceof IOException) {
					throw (IOException) t;
				}
				throw new IOException("Timed out (safe fail for " + t + ")");
			}
		}
		return TimeoutInputStream.read(this.in, this.timeout);
	}

	@Override
	public int read(final byte[] bytes) throws IOException {
		if(this.safe) {
			try {
				return TimeoutInputStream.read(this.in, this.timeout, bytes);
			} catch(Throwable t) {
				if(t instanceof IOException) {
					throw (IOException) t;
				}
				throw new IOException("Timed out (safe fail for " + t + ")");
			}
		}
		return TimeoutInputStream.read(this.in, this.timeout, bytes);
	}

	@Override
	public int read(final byte[] bytes, final int of, final int l) throws IOException {
		if(this.safe) {
			try {
				return TimeoutInputStream.read(this.in, this.timeout, bytes, of, l);
			} catch(Throwable t) {
				if(t instanceof IOException) {
					throw (IOException) t;
				}
				throw new IOException("Timed out (safe fail for " + t + ")");
			}
		}
		return TimeoutInputStream.read(this.in, this.timeout, bytes, of, l);
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
	
	
	
	/*** STATIC ***/
	
	public static int read(final InputStream in, long timeout) throws IOException {
		Object ret = null; 
		try {
			ret = TimeoutThread.run(new Callable<Object>() {
			
				@Override
				public Object call() {
					try {
						return in.read();
					} catch(IOException t) {
						return t;
					}
				}
				
			}, timeout);
		} catch(Throwable t) {
			if(t instanceof TimeoutException) {
				throw new IOException("Timed out: " + t.getMessage());
			} else {
				if(t instanceof RuntimeException) {
					throw (RuntimeException) t;
				}
				throw new RuntimeException(t);
			}
		}
		if(ret instanceof Integer) {
			return (Integer) ret;
		}
		if(ret instanceof IOException) {
			throw (IOException) ret;
		}
		throw new IllegalStateException("Unknown returned value: " 
				+ ((ret == null) ? null : ret.getClass().getName()) + " / " + ret);
	}

	public static int read(final InputStream in, long timeout ,final byte[] bytes) throws IOException {
		Object ret = null;
		try {
			TimeoutThread.run(new Runa<Object>() {
				
				@Override
				public Object call() {
					try {
						return in.read(bytes);
					} catch(IOException t) {
						return t;
					}
				};
				
			}, timeout);
		} catch(Throwable t) {
			if(t instanceof TimeoutException) {
				throw new IOException("Timed out: " + t.getMessage());
			} else {
				if(t instanceof RuntimeException) {
					throw (RuntimeException) t;
				}
				throw new RuntimeException(t);
			}
		}
		if(ret instanceof Integer) {
			return (Integer) ret;
		}
		if(ret instanceof IOException) {
			throw (IOException) ret;
		}
		throw new IllegalStateException("Unknown returned value: " 
				+ ((ret == null) ? null : ret.getClass().getName()) + " / " + ret);
	}

	public static int read(final InputStream in, long timeout, final byte[] bytes, final int of, final int l) throws IOException {
		Object ret = null;
		try {
			ret = TimeoutThread.run(new Runa<Object>() {
		
				@Override
				public Object call() {
					try {
						return in.read(bytes, of, l);
					} catch(IOException t) {
						return t;
					}
				};
				
			}, timeout);
		} catch(Throwable t) {
			if(t instanceof TimeoutException) {
				throw new IOException("Timed out: " + t.getMessage());
			} else {
				if(t instanceof RuntimeException) {
					throw (RuntimeException) t;
				}
				throw new RuntimeException(t);
			}
		}
		if(ret instanceof Integer) {
			return (Integer) ret;
		}
		if(ret instanceof IOException) {
			throw (IOException) ret;
		}
		throw new IllegalStateException("Unknown returned value: " 
				+ ((ret == null) ? null : ret.getClass().getName()) + " / " + ret);
	}
	
}
