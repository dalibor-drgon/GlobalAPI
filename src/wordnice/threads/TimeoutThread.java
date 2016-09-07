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

package wordnice.threads;

/*** TimeoutThread.java ***/
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import wordnice.api.Nice;

public class TimeoutThread<X> {
	
	public static class Result<X> {
		protected boolean started = false;
		protected boolean timedOut = false;
		protected Throwable exception = null;
		protected X result = null;
		
		public Result() {}

		public boolean hasStarted() {
			return started;
		}

		public Result<X> setStarted(boolean started) {
			this.started = started;
			return this;
		}

		public boolean hasTimedOut() {
			return timedOut;
		}

		public Result<X> setTimedOut(boolean timedOut) {
			this.timedOut = timedOut;
			return this;
		}

		public Throwable getException() {
			return exception;
		}

		public Result<X> setException(Throwable exception) {
			this.exception = exception;
			return this;
		}
		
		public X getResult() {
			return result;
		}

		public Result<X> setResult(X result) {
			this.result = result;
			return this;
		}
		
		public boolean isExitOK() {
			return this.started && !this.timedOut && this.exception == null;
		}
	}

	protected Callable<X> callable = null;
	protected long timeout = 100L;

	public TimeoutThread(Runnable runit, X ret, long timeout) {
		this(new Runa<X>(runit, ret), timeout);
	}

	public TimeoutThread(Callable<X> runit, long timeout) {
		this.callable = runit;
		if(timeout < 1L) {
			throw new IllegalArgumentException("Timeout ("+timeout+") < 1");
		}
		this.timeout = timeout;
	}
	
	public X run() throws Exception {
		return run(this.callable, this.timeout);
	}
	
	public void runSafe(Result<X> out) {
		runSafe(out, this.callable, this.timeout);
	}
	
	public Result<X> runSafe() {
		return runSafe(this.callable, this.timeout);
	}
	
	
	/*** STATIC ***/
	public static <Y> Y run(Callable<Y> callable, long timeout) throws Exception  {
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
	
	public static <Y> Result<Y> runSafe(Callable<Y> callable, long timeout) {
		Result<Y> out = new Result<Y>();
		runSafe(out, callable, timeout);
		return out;
	}
	
	public static <Y> void runSafe(Result<Y> out, Callable<Y> callable, long timeout) {
		try {
			out.setStarted(true);
			out.setTimedOut(false);
			out.setResult(run(callable, timeout));
		} catch(Throwable t) {
			out.setException(t);
			Nice.checkError(t);
			if(t instanceof TimeoutException)
				out.setTimedOut(true);
		}
	}

}