package eu.wordnice.threads;

public interface MThreadableSize {

	/**
	 * Run part of program
	 * 
	 * @param off Offset
	 * @param len Length of data after given offset (end index = off + len)
	 */
	public void run(int off, int len) throws Exception;
	
}