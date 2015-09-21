package eu.wordnice.streams;

import java.io.IOException;
import java.io.InputStream;

public class InputAdvStream extends InputAdv {

	public InputStream in;
	
	public InputAdvStream(InputStream in) {
		this.in = in;
	}


	@Override
	public int read() throws IOException {
		return this.in.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.in.read(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		this.in.close();
	}
	
	@Override
	public void reset() throws IOException {
		this.in.reset();
	}
	
	@Override
	public int available() throws IOException {
		return this.in.available();
	}
	
	@Override
	public boolean markSupported() {
		return this.in.markSupported();
	}
	
	@Override
	public void mark(int readlimit) {
		this.in.mark(readlimit);
	}
	
	@Override
	public long skip(long bytes) throws IOException {
		return this.in.skip(bytes);
	}


}