
package wordnice.streams;


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file was modified by wordnice as it is part of NiceAPI 
 * library which is under MIT license. See LICENSE.txt in root folder.
 * 
 * Synchronized blocks and usage of ArrayList were removed for better
 * performance. Usable only by one thread at a time!
 * Default starting capacity is 512bytes, minimal was set to 128 bytes.
 * By original implementation, every new buffer is at least 2x bigger 
 * than previous. That also means we can fill max. 26x byte[][] 
 * buffer-storing arrays to get more than 2gb (3.9gb) of ram usage 
 * which is in most cases mean OutOfMemory error.
 * Implementation was also updated and does not go beyond Java limits
 * and keeps every new buffer size under Nice.maxArrayLength.
 * 
 * This stream should not be used in functions which expect this
 * to be ByteArrayOutputStream compatible and uses reflections to access 
 * fields - fields "buf" and "count" are not really used and are null/0
 */
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import wordnice.api.Nice;

/**
 * This class implements an output stream in which the data is 
 * written into a byte array. The buffer automatically grows as data 
 * is written to it.
 * <p> 
 * The data can be retrieved using <code>toByteArray()</code> and
 * <code>toString()</code>.
 * <p>
 * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
 * this class can be called after the stream has been closed without
 * generating an <tt>IOException</tt>.
 * <p>
 * This is an alternative implementation of the java.io.ByteArrayOutputStream
 * class. The original implementation only allocates 32 bytes at the beginning.
 * As this class is designed for heavy duty it starts at 1024 bytes. In contrast
 * to the original it doesn't reallocate the whole memory block but allocates
 * additional buffers. This way no buffers need to be garbage collected and
 * the contents don't have to be copied to the new buffer. This class is
 * designed to behave exactly like the original. The only exception is the
 * deprecated toString(int) method that has been ignored.
 * 
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @author Holger Hoffstatte
 * @version $Id: ByteArrayOutputStream.java 610010 2008-01-08 14:50:59Z niallp $
 */
public class ArrayOutputStreamInt
extends ArrayOutputStream {

    /** A singleton empty byte array. */
	protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /** The list of buffers, which grows and never reduces. */
    protected byte[][] buffers = new byte[32][];
    protected int buffers_size = 0;
    /** The index of the current buffer. */
    protected int currentBufferIndex;
    /** The total count of bytes in all the filled buffers. */
    protected long filledBufferSum;
    /** The current buffer. */
    protected byte[] currentBuffer;
    /** current size **/
    protected long countReal = 0;


    /**
     * Creates a new byte array output stream. The buffer capacity is 
     * initially 512 bytes, though its size increases if necessary. 
     */
    public ArrayOutputStreamInt() {
        this(512);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of 
     * the specified size, in bytes. 
     *
     * @param size  the initial size. If negative, exception is thrown,
     * 		otherwise new array is allocated with length at least 128
     * @throws IllegalArgumentException if size is negative
     */
    public ArrayOutputStreamInt(int size) {
        super((size < 0 || size >= 128) ? size : 128);
        this.currentBuffer = super.buf;
        super.buf = null;
        addBufferToList(currentBuffer);
    }
    
    /**
     * @return true if this stream supports more than 2gb of data
     */
    public boolean isLong() {
    	return false;
    }

    /**
     * Makes a new buffer available either by allocating
     * a new one or re-cycling an existing one.
     *
     * @param newcount  the size of the buffer if one is created
     */
    protected void needNewBuffer(long newcount) {
    	if(!isLong()) {
    		//>= instead of > because of possible loop in write(InputStream)
    		if(newcount >= Integer.MAX_VALUE || newcount < 0) {
	    		throw new OutOfMemoryError(
	        			"This stream does not support more than 2G (new size "
	        			+ ((double) newcount / 1024. / 1024.) +" MiB)");
	    	}
    	}
    	//No need to check (newcount-count) > Integer.MAX_VALUE
    	//because of integer only parameters in write functions 
    	//which call this method
        if (currentBufferIndex < buffers_size - 1) {
            //Recycling old buffer
            filledBufferSum += currentBuffer.length;
            
            currentBufferIndex++;
            currentBuffer = buffers[currentBufferIndex];
        } else {
            //Creating new buffer
            int newBufferSize;
            if (currentBuffer == null) {
            	//Called on start
                newBufferSize = (int) ((newcount >= 128) ? newcount : 128);
                filledBufferSum = 0;
            } else {
            	int doubleSize = currentBuffer.length << 1;
            	int factorSize = (int) (newcount - filledBufferSum);
            	if(doubleSize > Nice.maxArrayLength || doubleSize < 0
            		|| factorSize > Nice.maxArrayLength || factorSize < 0) {
            		newBufferSize = Nice.maxArrayLength;
            	} else {
            		newBufferSize = Math.max(doubleSize, factorSize);
            	}
            	filledBufferSum += currentBuffer.length;
            }
            //first check if request length i
            if(!isLong() && (newBufferSize + filledBufferSum) > Integer.MAX_VALUE) {
            	newBufferSize = (int) (Integer.MAX_VALUE - filledBufferSum);
            }
            currentBuffer = new byte[newBufferSize];
            addBufferToList(currentBuffer);
        }
    }
    
    protected void addBufferToList(byte[] buf) {
    	currentBufferIndex++;
        if(buffers.length == buffers_size) {
        	//if !isLong():
        	//practically impossible to get array bigger than 22 elements due to
        	//every new buffer is at least 2x bigger than previous, but just in 
        	//case, resize it
        	byte[][] nevbuffers = new byte[buffers.length << 1][];
        	System.arraycopy(buffers, 0, nevbuffers, 0, buffers.length);
        	buffers = nevbuffers;
        }
        buffers[buffers_size++] = buf;
    }

    /**
     * Write the bytes to byte array.
     * @param b the bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     */
    @Override
    public void write(byte[] b, int off, int len) {
        if ((off < 0) 
                || (off > b.length) 
                || (len < 0) 
                || ((off + len) > b.length) 
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        long newcount = countReal + len;
        int remaining = len;
        int inBufferPos = (int) (countReal - filledBufferSum);
        while (remaining > 0) {
            int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newcount);
                inBufferPos = 0;
            }
        }
        countReal = newcount;
    }

    /**
     * Write a byte to byte array.
     * @param b the byte to write
     */
    @Override
    public void write(int b) {
    	int inBufferPos = (int) (countReal - filledBufferSum);
        if (inBufferPos == currentBuffer.length) {
            needNewBuffer(countReal + 1);
            inBufferPos = 0;
        }
        currentBuffer[inBufferPos] = (byte) b;
        countReal++;
    }

    /**
     * Writes the entire contents of the specified input stream to this
     * byte stream. Bytes from the input stream are usually read directly 
     * into the internal buffers of this streams.
     *
     * @param in the input stream to read from
     * @return total number of bytes read from the input stream
     *         (and written to this stream).
     *         Returns 0 if current size() == Integer.MAX_VALUE
     * @throws IOException if an I/O error occurs while reading the input stream
     */
    public int write(InputStream in) throws IOException {
    	int readCount = 0;
        int inBufferPos = (int)(countReal - filledBufferSum);
        int nextRead = Math.min(Integer.MAX_VALUE - ((isLong()) ? readCount : (int)filledBufferSum), 
        		currentBuffer.length - inBufferPos);
        if(nextRead == 0) return 0;
        int n = in.read(currentBuffer, inBufferPos, nextRead);
        while (n != -1) {
            readCount += n;
            inBufferPos += n;
            countReal += n;
            if (inBufferPos == currentBuffer.length) {
            	if(!isLong() && countReal == Integer.MAX_VALUE) {
            		throw new OutOfMemoryError();
            	}
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            nextRead = Math.min(Integer.MAX_VALUE - ((isLong()) ? readCount : (int)filledBufferSum), 
            		currentBuffer.length - inBufferPos);
            if(nextRead == 0) throw new OutOfMemoryError();
            n = in.read(currentBuffer, inBufferPos, nextRead);
        }
        return readCount;
    }
    
    @Override
	public long writeLong(InputStream in) throws IOException {
        long readCount = 0;
        int inBufferPos = (int)(countReal - filledBufferSum);
        long n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != -1) {
            readCount += n;
            inBufferPos += n;
            countReal += n;
            if (inBufferPos == currentBuffer.length) {
            	if(!isLong() && countReal == Integer.MAX_VALUE) {
            		throw new OutOfMemoryError();
            	}
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

    /**
     * Return the current size of the byte array.
     * @return the current size of the byte array
     */
    @Override
    public int size() {
    	//check which may fail if isLong()
    	if(countReal > Integer.MAX_VALUE) throw new OutOfMemoryError(""+countReal);
    	return (int) countReal;
    }
    
    /**
     * Return the current size of the byte array.
     * @return the current size of the byte array
     */
    public long sizeLong() {
    	return countReal;
    }
    
    /**
     * Return current capacity
     * @return current capacity
     */
    public int capacity() {
    	//check which may fail if isLong()
    	long cap = this.capacityLong();
    	if(cap > Integer.MAX_VALUE) throw new OutOfMemoryError(""+cap);
    	return (int) cap;
    }
    
    /**
     * Return current capacity
     * @return current capacity
     */
    public long capacityLong() {
    	return filledBufferSum + currentBuffer.length;
    }

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    @Override
    public void reset() {
        countReal = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        currentBuffer = buffers[currentBufferIndex];
    }

    /**
     * Writes the entire contents of this byte stream to the
     * specified output stream.
     *
     * @param out  the output stream to write to
     * @throws IOException if an I/O error occurs, such as if the stream is closed
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    @Override
    public void writeTo(OutputStream out) throws IOException {
        long remaining = countReal;
        for (int i = 0; i < buffers_size; i++) {
            byte[] buf = buffers[i];
            int c = (int)Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

    /**
     * Gets the curent contents of this byte stream as a byte array.
     * The result is independent of this stream.
     *
     * @return the current contents of this output stream, as a byte array
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    @Override
    public byte[] toByteArray() {
    	long remainingL = countReal;
        if (remainingL == 0) {
            return EMPTY_BYTE_ARRAY; 
        }
        if(remainingL > Integer.MAX_VALUE) {
        	throw new OutOfMemoryError(""+remainingL);
        }
        int remaining = (int) remainingL;
        byte newbuf[] = new byte[remaining];
        int pos = 0;
        for (int i = 0; i < buffers_size; i++) {
            byte[] buf = buffers[i];
            int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        return newbuf;
    }
    
    /*public static void main(String...strings) throws Throwable {
    	byte[] buff = new byte[1024*1024];
    	long start = System.currentTimeMillis();
    	Gen.get().genBytes(buff);
    	System.out.println("Gen in " + (System.currentTimeMillis()-start) + " ms!");
    	ArrayOutputStream bao = new ArrayOutputStreamLong(buff.length);
    	while(true) {
    		bao.write(buff);
    		System.out.println((bao.sizeLong()/1024.0/1024/1024) + " / " 
    				+ (bao.capacityLong()/1024.0/1024/1024));
    	}
    }*/

    //TODO check needNewBuffer and calls, does not seem legit
    
	@Override
	public void ensureSpace(int space) {
		//TODO add variable used when buffer resizes
	}
	
	@Override
	public void ensureSpace(long space) {
		//TODO add variable used when buffer resizes
	}
    
}
