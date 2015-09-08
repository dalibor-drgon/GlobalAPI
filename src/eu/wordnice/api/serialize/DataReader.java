package eu.wordnice.api.serialize;

import java.io.IOException;

import eu.wordnice.api.IStream;

public interface DataReader {

	/**
	 * Read and parse data from input stream
	 * 
	 * @param in Input stream to read
	 * 
	 * @throws SerializeException Serialization problem
	 * @throws IOException Error while reading
	 */
	public void read(IStream in) throws SerializeException, IOException;
	
}
