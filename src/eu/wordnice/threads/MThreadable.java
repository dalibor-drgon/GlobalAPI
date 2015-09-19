package eu.wordnice.threads;

public interface MThreadable {

	/**
	 * Run part of program
	 * 
	 * @param cur Current thread number (1 - max)
	 * @param max Maximum threads count (1 - ...)
	 */
	public void run(int cur, int max) throws Exception;
	
}
