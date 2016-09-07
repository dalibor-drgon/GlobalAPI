
package wordnice.streams;

import java.io.IOException;
import java.io.InputStream;

public class ArrayOutputStreamLong
extends ArrayOutputStreamInt {

    /**
     * Create output stream with size of 1KiB
     */
    public ArrayOutputStreamLong() {
        this(1024);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of 
     * the specified size, in bytes. 
     *
     * @param size  the initial size. If negative, exception is thrown,
     * 		otherwise new array is allocated with length at least 128
     * @throws IllegalArgumentException if size is negative
     */
    public ArrayOutputStreamLong(int size) {
        super(size);
    }
    
    /**
     * We support over 2GB of data!
     */
    @Override
    public boolean isLong() {
    	return true;
    }
   
    /**
     * Return the current size of the byte array.
     * @return the current size of the byte array
     * 
     * @deprecated use only with streams under 2gigs
     * 		If possible, use sizeLong() instead
     */
    @Override
    @Deprecated
    public int size() {
    	return super.size();
    }
    
    /**
     * Return the current size of the byte array.
     * @return the current size of the byte array
     * 
     * @deprecated use only with streams under 2gigs
     * 		If possible, use capacityLong() instead
     */
    @Override
    @Deprecated
    public int capacity() {
    	return super.capacity();
    }

    
    /**
     * @see wordnice.streams.ArrayOutputStreamInt#write(java.io.InputStream)
     */
	public long writeLong(InputStream in) throws IOException {
        long readCount = 0;
        int inBufferPos = (int)(countReal - filledBufferSum);
        long n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != -1) {
            readCount += n;
            inBufferPos += n;
            countReal += n;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

}
