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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TimeoutThread<X> {

	public Runa<X> run = null;
	public ExecutorService executor = null;
	public long timeout = 100L;
	private boolean canceled = false;
	private boolean runed = false;
	
	public void exc(Throwable t, boolean fatal) {}

	public TimeoutThread(Runnable runit, X ret, long timeout) {
		this(new Runa<X>(runit, ret), timeout);
	}

	public TimeoutThread(Runa<X> runit, long timeout) {
		this.run = runit;
		if(timeout < 1L) {
			timeout = 10L;
		}
		this.timeout = timeout;
	}


	public X run() {
		return this.run(null);
	}

	public X run(X defaulte) {
		this.runed = true;
		List<Future<X>> list = null;
		this.executor = Executors.newCachedThreadPool();
		try {
			list = executor.invokeAll(Arrays.asList(this.run), this.timeout, TimeUnit.MILLISECONDS);
		} catch(Throwable t) {
			this.canceled = true;
			this.exc(t, true);
		}
		executor.shutdown();

		if(list == null || list.size() != 1) {
			return defaulte;
		}

		try {
			Future<X> f = list.get(0);
			try {
				return f.get();
			} catch(Throwable t2) {
				this.canceled = true;
				this.exc(t2, false);
			}
		} catch(Throwable t) {
			this.exc(t, false);
		}
		return defaulte;
	}

	public boolean wasRunned() {
		return this.runed;
	}

	public boolean wasCanceled() {
		return this.canceled;
	}
	
	
	/*** STATIC ***/
	
	public static <X> X run(Runa<X> rn, X defaulte, long timeout) {
		List<Future<X>> list = null;
		ExecutorService exec = Executors.newCachedThreadPool();
		try {
			list = exec.invokeAll(Arrays.asList(rn), timeout, TimeUnit.MILLISECONDS);
		} catch(Throwable t) {}
		try {
			exec.shutdown();
		} catch(Throwable t) {}

		if(list == null || list.size() != 1) {
			return defaulte;
		}

		try {
			Future<X> f = list.get(0);
			return f.get();
		} catch(Throwable t) {}
		return defaulte;
	}

}