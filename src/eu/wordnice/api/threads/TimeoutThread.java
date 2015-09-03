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

/*** TimeoutThread.java ***/
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutThread<X> {

	public Callable<X> callable = null;
	public long timeout = 100L;
	private boolean cancelled = false;
	private boolean ran = false;
	
	public void exc(Throwable t, boolean fatal) {}

	public TimeoutThread(Runnable runit, X ret, long timeout) {
		this(new Runa<X>(runit, ret), timeout);
	}

	public TimeoutThread(Callable<X> runit, long timeout) {
		this.callable = runit;
		if(timeout < 1L) {
			timeout = 10L;
		}
		this.timeout = timeout;
	}
	
	public X run() throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<X> future = executor.submit(this.callable);
		executor.shutdown();
		try {
			return future.get(this.timeout, TimeUnit.MILLISECONDS);
		} catch(TimeoutException e) {
			future.cancel(true);
			throw e;
		} catch(ExecutionException e) {
			Throwable t = e.getCause();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new IllegalStateException(t);
			}
		}
	}

	public boolean wasRan() {
		return this.ran;
	}

	public boolean wasCancelled() {
		return this.cancelled;
	}
	
	
	/*** STATIC ***/
	
	public static <Y> Y run(Callable<Y> callable, long timeout) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Y> future = executor.submit(callable);
		executor.shutdown();
		try {
			return future.get(timeout, TimeUnit.MILLISECONDS);
		} catch(TimeoutException e) {
			future.cancel(true);
			throw e;
		} catch(ExecutionException e) {
			Throwable t = e.getCause();
			if (t instanceof Error) {
				throw (Error) t;
			} else if (t instanceof Exception) {
				throw (Exception) e;
			} else {
				throw new IllegalStateException(t);
			}
		}
	}

}