package eu.wordnice.streams;

public interface Seekable {
	
	/**
	 * @return Current position
	 */
	public long position();
	
	/**
	 * @param nev New position, to which will be jumped
	 */
	public void position(long nev);
	
}
