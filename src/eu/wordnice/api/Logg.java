package eu.wordnice.api;

public interface Logg {

	public void debug(String str);
	public void info(String str);
	public void warn(String str);
	public void severe(String str);
	public void severe(Throwable t);
	public void severe(String str, Throwable t);
	
}
