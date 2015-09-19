package eu.wordnice.threads;

import java.lang.Thread.UncaughtExceptionHandler;

public interface MThreadExceptionHandler extends UncaughtExceptionHandler {

	public Throwable getLastException();
	
	public Thread getLastThread();
	
}
