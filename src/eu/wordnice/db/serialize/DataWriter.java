package eu.wordnice.db.serialize;

import java.io.IOException;

import eu.wordnice.streams.Output;

public interface DataWriter {
	
	/**
	 * Write content to output stream
	 * 
	 * @param out Target output stream where data will be written
	 * 
	 * @throws SerializeException Serialization problem
	 * @throws IOException Error while writing
	 */
	public void write(Output out) throws SerializeException, IOException;
	
}
