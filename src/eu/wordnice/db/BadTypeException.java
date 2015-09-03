package eu.wordnice.db;

public class BadTypeException extends Exception {
	
	private static final long serialVersionUID = -9098826783241103869L;

	public BadTypeException() {
		super("Types do not match!");
	}
	
	public BadTypeException(String str) {
		super(str);
	}
	
	public BadTypeException(Throwable thr) {
		super(thr);
	}
	
}
