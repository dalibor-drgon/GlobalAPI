package eu.wordnice.db.operator;

public class Limit {
	
	/**
	 * Data offset
	 */
	public int off = 0;
	
	/**
	 * Data length
	 */
	public int len = Integer.MAX_VALUE;
	
	public Limit(int len) {
		this.len = len;
	}
	
	public Limit(int off, int len) {
		this.off = off;
		this.len = len;
	}
	
}
