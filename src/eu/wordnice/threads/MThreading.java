package eu.wordnice.threads;

import java.util.Random;

import eu.wordnice.api.Handler;

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
	protected Handler.OneVoidHandler<Thread> hand = null;
	
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
	
	public void setThreadHandler(Handler.OneVoidHandler<Thread> hand) {
		this.hand = hand;
	}
	
	public Handler.OneVoidHandler<Thread> getThreadHandler() {
		return this.hand;
	}
	
	public void setCount(int count) {
		if(count < 1) {
			throw new IllegalArgumentException("Illegal thread count: " + count);
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
	 * @param len Length of data after offset (end index = off + len)
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
	
	
	
	
	
	
	public static void main(String[] blah) throws Exception {
		System.out.println("Allocating...");
		final int[] arr = new int[4 * 1024 * 1024];
		System.out.println("Allocated " + arr.length + " integers...");
		
		MThreadableSize mts = new MThreadableSize() {
			
			@Override
			public void run(int i, int n) {
				n += i;
				int sum = 0;
				Random rd = new Random();
				for(; i < n; i++) {
					sum = arr[i] + 1;
					sum += sum;
					sum += Math.round(rd.nextDouble() * rd.nextDouble() * rd.nextDouble()
							* rd.nextDouble() * rd.nextDouble() * rd.nextDouble());
					sum *= Math.round(rd.nextDouble() * rd.nextDouble() * rd.nextDouble()
							* rd.nextDouble() * rd.nextDouble() * rd.nextDouble());
					sum -= rd.nextInt();
					arr[i] = sum;
				}
			}
			
		};
		
		long start = System.nanoTime();
		mts.run(0, arr.length);
		System.out.println("(main thread) in " + (System.nanoTime() - start) + " ns");
		
		
		start = System.nanoTime();
		MThreading mt = new MThreading(1);
		mt.run(mts, 0, arr.length);
		mt.joinAll();
		System.out.println("(single thread) in " + (System.nanoTime() - start) + " ns");
	
		
		start = System.nanoTime();
		mt = new MThreading();
		mt.run(mts, 0, arr.length);
		mt.joinAll();
		System.out.println("(auto, " + mt.getLastCount() + " threads) in " + (System.nanoTime() - start) + " ns");
	}
	
}