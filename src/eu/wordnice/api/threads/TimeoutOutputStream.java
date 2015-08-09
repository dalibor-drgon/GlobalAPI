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
import java.util.concurrent.TimeoutException;

public class TimeoutOutputStream extends OutputStream {
	
	public final OutputStream out;
	public long timeout;
	public boolean safe;
	
	public TimeoutOutputStream(OutputStream stream, long maxtime_write) {
		this(stream, maxtime_write, true);
	}
	
	public TimeoutOutputStream(OutputStream stream, long maxtime_write, boolean safe) {
		this.out = stream;
		this.timeout = maxtime_write;
		this.safe = safe;
	}
	
	public void setTimeout(long tm) {
		this.timeout = tm;
	}
	
	@Override
	public void write(final int obyte) throws IOException {
		if(this.safe) {
			try {
				TimeoutOutputStream.write(this.out, this.timeout, obyte);
				return;
			} catch(Throwable t) {
				if(t instanceof IOException) {
					throw (IOException) t;
				}
				throw new IOException("Timed out (safe fail for " + t + ")");
			}
		}
		TimeoutOutputStream.write(this.out, this.timeout, obyte);
	}

	@Override
	public void write(final byte[] bytes) throws IOException {
		if(this.safe) {
			try {
				TimeoutOutputStream.write(this.out, this.timeout, bytes);
				return;
			} catch(Throwable t) {
				if(t instanceof IOException) {
					throw (IOException) t;
				}
				throw new IOException("Timed out (safe fail for " + t + ")");
			}
		}
		TimeoutOutputStream.write(this.out, this.timeout, bytes);
	}

	@Override
	public void write(final byte[] bytes, final int off, final int l) throws IOException {
		if(this.safe) {
			try {
				TimeoutOutputStream.write(this.out, this.timeout, bytes, off, l);
				return;
			} catch(Throwable t) {
				if(t instanceof IOException) {
					throw (IOException) t;
				}
				throw new IOException("Timed out (safe fail for " + t + ")");
			}
		}
		TimeoutOutputStream.write(this.out, this.timeout, bytes, off, l);
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}

	@Override
	public void flush() throws IOException {
		this.out.flush();
	}
	
	
	/*** STATIC ***/
	
	public static void write(final OutputStream out, final long timeout, final int obyte) throws IOException {
		Object ret = null;
		try {
			TimeoutThread.run(new Runa<Object>() {
				
				@Override
				public Object call() {
					try {
						out.write(obyte);
						return Boolean.TRUE;
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
		if(ret instanceof IOException) {
			throw (IOException) ret;
		}
		if(ret == Boolean.TRUE) {
			return;
		}
		throw new IllegalStateException("Unknown returned value: " 
				+ ((ret == null) ? null : ret.getClass().getName()) + " / " + ret);
	}

	public static void write(final OutputStream out, final long timeout, final byte[] byts) throws IOException {
		Object ret = null;
		try {
			TimeoutThread.run(new Runa<Object>() {
				
				@Override
				public Object call() {
					try {
						out.write(byts);
						return Boolean.TRUE;
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
		if(ret instanceof IOException) {
			throw (IOException) ret;
		}
		if(ret == Boolean.TRUE) {
			return;
		}
		throw new IllegalStateException("Unknown returned value: " 
				+ ((ret == null) ? null : ret.getClass().getName()) + " / " + ret);
	}

	public static void write(final OutputStream out, final long timeout, final byte[] byts, final int off, final int len) throws IOException {
		Object ret = null;
		try {
			TimeoutThread.run(new Runa<Object>() {
				
				@Override
				public Object call() {
					try {
						out.write(byts, off, len);
						return Boolean.TRUE;
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
		if(ret instanceof IOException) {
			throw (IOException) ret;
		}
		if(ret == Boolean.TRUE) {
			return;
		}
		throw new IllegalStateException("Unknown returned value: " 
				+ ((ret == null) ? null : ret.getClass().getName()) + " / " + ret);
	}
	
}
