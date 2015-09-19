package eu.wordnice.threads;

public interface MThreadableSizeAuto extends MThreadableSize {
	
	
	
	/**
	 * Run in given MultiThreading
	 * You can just call {@link MThreading#run(MThreadAuto)}
	 */
	public void run(MThreading mt) throws Exception;
	
	public int getOffset();
	
	public int getLength();
	
}
