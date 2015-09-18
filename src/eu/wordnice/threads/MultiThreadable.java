package eu.wordnice.threads;

public interface MultiThreadable extends Runnable {

	/**
	 * Run part of program
	 * 
	 * @param cur Current thread number (1 - max)
	 * @param max Maximum threads count (1 - ...)
	 */
	public void run(int cur, int max);
	
	/**
	 * Run all parts
	 * If only one thread is available in MultiThreading, this method is called
	 * directly from thread
	 * 
	 * @see {@link Runnable#run()}
	 */
	@Override
	public void run();
	
}
