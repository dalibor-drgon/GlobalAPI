package eu.wordnice.threads;

public class MThreadDefaultExceptionHandler implements MThreadExceptionHandler {

	public Throwable lastExc = null;
	public Thread lastThread = null;
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if(e == null) {
			return;
		}
		if(t == null) {
			t = Thread.currentThread();
		}
		this.lastExc = e;
		this.lastThread = t;
	}

	@Override
	public Throwable getLastException() {
		return this.lastExc;
	}
	
	@Override
	public Thread getLastThread() {
		return this.lastThread;
	}

}
