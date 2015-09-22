package eu.wordnice.streams;

import java.io.IOException;
import java.io.OutputStream;

public class OutputAdvStream extends OutputAdv implements AutoCloseable {

	public OutputStream out;
	public boolean closed = false;
	
	public OutputAdvStream(OutputStream out) {
		this.out = out;
	}
	
	
	@Override
	public void write(int byt) throws IOException {
		this.out.write(byt);
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		this.out.write(bytes, off, len);
	}
	
	@Override
	public void close() throws IOException {
		this.closed = true;
		this.out.close();
	}
	
	@Override
	public void flush() throws IOException {
		this.out.flush();
	}


	@Override
	public boolean isOpen() {
		if(this.closed) {
			return false;
		}
		try {
			this.out.write(new byte[0]);
			return true;
		} catch(Exception exc) {
			return false;
		}
	}

	

}
