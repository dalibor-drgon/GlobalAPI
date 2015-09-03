package eu.wordnice.api.codings;

public class InvalidSyntaxException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public int i1 = -1;
	public int i2 = -1;
	
	public InvalidSyntaxException(String msg) {
		super(msg);
	}
	
	public InvalidSyntaxException(int i) {
		super("Invalid syntax at index " + i);
		this.i1 = i;
	}
	
	public InvalidSyntaxException(int i, boolean at) {
		super("Invalid syntax " + (at ? "at" : "near") +" index " + i);
		this.i1 = i;
	}
	
	public InvalidSyntaxException(int i1, int i2) {
		super("Invalid syntax between indexes " + i1 + " - " + i2);
		this.i1 = i1;
		this.i2 = i2;
	}
	
}
