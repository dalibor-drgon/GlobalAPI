package eu.wordnice.threads;

import java.util.Random;

public class MultiThreading {
	
	/**
	 * Threads count
	 */
	protected int count = 0;
	
	/**
	 * Last threads
	 */
	protected Thread[] thrs = null;
	
	/**
	 * @param count Number of threads
	 */
	public MultiThreading(int count) {
		this.setCount(count);
	}
	
	public MultiThreading() {
		int count = Runtime.getRuntime().availableProcessors();
		if(count < 2) {
			count = 2;
		}
		this.setCount(count);
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
	
	
	public void joinAll() {
		if(this.thrs == null) {
			return;
		}
		for(int i = 0, n = this.thrs.length; i < n; i++) {
			Thread rt = this.thrs[i];
			try {
				rt.join();
			} catch(InterruptedException ie) {}
		}
	}
	
	
	protected interface MultiThreadingRunnable extends Runnable {}
	
	/**
	 * Run given instance in multiple threads
	 * 
	 * @param mt Calls handler
	 */
	public void run(final MultiThreadable mt) {
		this.thrs = new Thread[this.count];
		if(this.thrs.length == 1) {
			this.thrs[0] = new Thread(mt);
			this.thrs[0].start();
		} else {
			final int n = this.thrs.length;
			for(int i = 0; i < n; i++) {
				final int threadNumber = i + 1;
				Thread thr = new Thread(new MultiThreadingRunnable() {
					@Override
					public void run() {
						mt.run(threadNumber, n);
					}
				});
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
	public void run(final MultiThreadableSizeable mt, final int off, final int len) {
		if(len == 0) {
			this.thrs = new Thread[0];
			return;
		}
		if(this.count == 1) {
			this.thrs = new Thread[1];
			Thread thr = new Thread(new MultiThreadingRunnable() {
				@Override
				public void run() {
					mt.run(off, len);
				}
			});
			thr.start();
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
				Thread thr = new Thread(new MultiThreadingRunnable() {
					@Override
					public void run() {
						mt.run(fCurOff, fCurSize);
					}
				});
				thr.start();
				this.thrs[i] = thr;
				curoff += cursize;
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] blah) {
		System.out.println("Allocating...");
		final int[] arr = new int[4 * 1024 * 1024];
		System.out.println("Allocated " + arr.length + " integers...");
		
		MultiThreadableSizeable mts = new MultiThreadableSizeable() {
			
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
		MultiThreading mt = new MultiThreading(1);
		mt.run(mts, 0, arr.length);
		mt.joinAll();
		System.out.println("(single thread) in " + (System.nanoTime() - start) + " ns");
	
		
		start = System.nanoTime();
		mt = new MultiThreading();
		mt.run(mts, 0, arr.length);
		mt.joinAll();
		System.out.println("(auto, " + mt.getLastCount() + " threads) in " + (System.nanoTime() - start) + " ns");
	}
	
}
