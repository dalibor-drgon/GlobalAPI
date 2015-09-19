package eu.wordnice.db.serialize;

import java.io.IOException;

import eu.wordnice.streams.Input;

public interface DataReader {

	/**
	 * Read and parse data from input stream
	 * 
	 * @param in Input stream to read
	 * 
	 * @throws SerializeException Serialization problem
	 * @throws IOException Error while reading
	 */
	public void read(Input in) throws SerializeException, IOException;
	
}
