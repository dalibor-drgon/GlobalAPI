package eu.wordnice.api.serialize;

import java.io.IOException;

import eu.wordnice.api.OStream;

public interface DataWriter {
	
	/**
	 * Write content to output stream
	 * 
	 * @param out Target output stream where
	 * 
	 * @throws SerializeException Serialization problem
	 * @throws IOException Error while writing
	 */
	public void write(OStream out) throws SerializeException, IOException;
	
}
