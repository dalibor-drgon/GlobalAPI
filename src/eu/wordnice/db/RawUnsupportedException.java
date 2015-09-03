package eu.wordnice.db;

public class RawUnsupportedException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public RawUnsupportedException() {
		super("Raw calls are not supported by this database!");
	}
	
	public RawUnsupportedException(String msg) {
		super(msg);
	}
	
	public RawUnsupportedException(Throwable th) {
		super(th);
	}
	
}
