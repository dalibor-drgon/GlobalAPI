/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 22201115, Dalibor Drgoň <emptychannelmc@gmail.com>
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

package wordnice.threads;

import wordnice.api.Nice;
import wordnice.api.Nice.VHandler;

public class MThreading {
	
	/**
	 * Threads count
	 */
	protected int count = 0;
	
	/**
	 * Last threads
	 */
	protected Thread[] thrs = null;
	
	/**
	 * Thread handler
	 * If any, called to modify thread properties
	 */
	protected VHandler<Thread> hand = null;
	
	/**
	 * Thread exception handler
	 */
	protected MThreadExceptionHandler exc_handler = null;
	
	/**
	 * @param count Number of threads
	 */
	public MThreading(int count) {
		this.setCount(count);
		this.initDefaults();
	}
	
	public MThreading() {
		int count = Runtime.getRuntime().availableProcessors();
		if(count < 2) {
			count = 2;
		}
		this.setCount(count);
		this.initDefaults();
	}
	
	public void initDefaults() {
		this.exc_handler = new MThreadDefaultExceptionHandler();
	}
	
	
	
	public void setExceptionHandler(MThreadExceptionHandler mte) {
		this.exc_handler = mte;
	}
	
	public MThreadExceptionHandler getExceptionHandler() {
		return this.exc_handler;
	}
	
	public void setThreadHandler(VHandler<Thread> hand) {
		this.hand = hand;
	}
	
	public VHandler<Thread> getThreadHandler() {
		return this.hand;
	}
	
	public void setCount(int count) {
		if(count < 1) {
			throw Nice.illegal("Illegal thread count: " + count);
		}
		this.count = count;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public int getLastCount() {
		if(this.thrs != null) {
			return this.thrs.length;
		}
		return -1;
	}
	
	
	public void joinAll() throws Exception {
		if(this.thrs == null) {
			return;
		}
		for(int i = 0, n = this.thrs.length; i < n; i++) {
			Thread rt = this.thrs[i];
			try {
				rt.join();
			} catch(InterruptedException ie) {}
			MThreadExceptionHandler teh = this.exc_handler;
			if(teh != null) {
				Thread teh_thr = teh.getLastThread();
				Throwable teh_exc = teh.getLastException();
				if(rt.equals(teh_thr) && teh_exc instanceof Exception) {
					throw ((Exception) teh_exc);
				}
			}
		}
	}
	
	public void handle(Thread thr) {
		if(this.hand != null) {
			this.hand.handle(thr);
		}
	}
	
	
	protected interface MThreadingRunnable extends Runnable {}
	
	/**
	 * Run given instance in multiple threads
	 * 
	 * @param mt Calls handler
	 */
	public void run(final MThreadable mt) {
		this.thrs = new Thread[this.count];
		if(this.thrs.length == 1) {
			Thread thr = new Thread(new MThreadingRunnable() {
				@Override
				public void run() {
					try {
						mt.run(1, 1);
					} catch(Exception exc) {
						MThreading.this.exc_handler.uncaughtException(null, exc);
					}
				}
			});
			this.handle(thr);
			thr.start();
			this.thrs[0] = thr;
		} else {
			final int n = this.thrs.length;
			for(int i = 0; i < n; i++) {
				final int threadNumber = i + 1;
				Thread thr = new Thread(new MThreadingRunnable() {
					@Override
					public void run() {
						try {
							mt.run(threadNumber, n);
						} catch(Exception exc) {
							MThreading.this.exc_handler.uncaughtException(null, exc);
						}
					}
				});
				this.handle(thr);
				thr.start();
				this.thrs[i] = thr;
			}
		}
	}
	
	/**
	 * Run given instance in multiple threads
	 * 
	 * @param mt Calls handler
	 * @param off Offset start
	 * @param count Length of data after offset (end index = off + len)
	 */
	public void run(MThreadableSizeAuto mt) {
		this.run(mt, mt.getOffset(), mt.getLength());
	}
	
	/**
	 * Run given instance in multiple threads
	 * 
	 * @param mt Calls handler
	 * @param off Offset start
	 * @param len Length of data after offset (end index = off + len)
	 */
	public void run(final MThreadableSize mt, final int off, final int len) {
		if(len == 0) {
			this.thrs = new Thread[0];
			return;
		}
		if(this.count == 1) {
			this.thrs = new Thread[1];
			Thread thr = new Thread(new MThreadingRunnable() {
				@Override
				public void run() {
					try {
						mt.run(off, len);
					} catch(Exception exc) {
						MThreading.this.exc_handler.uncaughtException(null, exc);
					}
				}
			});
			this.handle(thr);
			thr.start();
			this.handle(thr);
			this.thrs[0] = thr;
		} else {
			int addpt = len / this.count;
			int lastAdd = 0;
			if(addpt == 0) {
				addpt = 1;
				this.thrs = new Thread[len];
			} else {
				this.thrs = new Thread[this.count];
				lastAdd = len - ((int) (len / this.count) * this.count);
			}
			int curoff = off;
			for(int i = 0, n = this.thrs.length; i < n; i++) {
				int cursize = addpt;
				if(i == (n - 1)) {
					cursize += lastAdd;
				}
				final int fCurSize = cursize;
				final int fCurOff = curoff;
				Thread thr = new Thread(new MThreadingRunnable() {
					@Override
					public void run() {
						try {
							mt.run(fCurOff, fCurSize);
						} catch(Exception exc) {
							MThreading.this.exc_handler.uncaughtException(null, exc);
						}
					}
				});
				this.handle(thr);
				thr.start();
				this.handle(thr);
				this.thrs[i] = thr;
				curoff += cursize;
			}
		}
	}
	
}
