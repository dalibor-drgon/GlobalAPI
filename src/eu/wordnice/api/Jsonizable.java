package eu.wordnice.api;

import java.io.IOException;
import java.io.OutputStream;

public interface Jsonizable {
	
	public void toJsonString(OutputStream out) throws IOException;
	
}
