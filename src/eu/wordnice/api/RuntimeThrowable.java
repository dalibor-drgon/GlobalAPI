package eu.wordnice.api;

import java.io.PrintStream;
import java.io.PrintWriter;

public class RuntimeThrowable extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public Throwable exc = null;
	
	public RuntimeThrowable(Throwable t) {
		this.exc = t;
	}
	
	@Override
	public void setStackTrace(StackTraceElement[] st) {
		this.exc.setStackTrace(st);
	}
	
	@Override
	public StackTraceElement[] getStackTrace() {
		return this.exc.getStackTrace();
	}
	
	@Override
	public Throwable fillInStackTrace() {
		return this.exc.fillInStackTrace();
	}
	
	@Override
	public void printStackTrace() {
		this.exc.printStackTrace();
	}
	
	@Override
	public void printStackTrace(PrintStream ps) {
		this.exc.printStackTrace(ps);
	}
	
	@Override
	public void printStackTrace(PrintWriter pw) {
		this.exc.printStackTrace(pw);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return this.exc.equals(obj);
	}
	
	@Override
	public String toString() {
		return this.exc.toString();
	}
	
}
