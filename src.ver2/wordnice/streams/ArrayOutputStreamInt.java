
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
 * Modifications made by wordnice:
 * - Synchronized blocks and usage of ArrayList were removed for better
 * performance. Usable only by one thread at a time!
 * - All original private fields and private methods are protected now
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
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /** The list of buffers, which grows and never reduces. */
    private byte[][] buffers = new byte[24][];
    protected int buffersSize;
    /** The index of the current buffer. */
    protected int currentBufferIndex;
    /** The total count of bytes in all the filled buffers. */
    protected int filledBufferSum;
    /** The current buffer. */
    protected byte[] currentBuffer;
    /** The total count of bytes written. */
    protected int count;

    /**
     * Creates a new byte array output stream.
     */
    public ArrayOutputStreamInt() {
        this(Nice.builderSize);
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of 
     * the specified size, in bytes. 
     *
     * @param size  the initial size
     * @throws IllegalArgumentException if size is negative
     */
    public ArrayOutputStreamInt(int size) {
    	super(size);
    	this.currentBuffer = super.buf;
    	super.buf = null;
    	this.addBufferToList(this.currentBuffer);
    }
    
    /**
     * Add newly allocated buffer to internal buffer list
     * @param buf newly allocated buffer which will be addded 
     * 			to internal buffer list
     */
    protected void addBufferToList(byte[] buf) {
    	currentBufferIndex++;
        if(buffers.length == buffersSize) {
        	byte[][] nevbuffers = new byte[buffers.length << 1][];
        	System.arraycopy(buffers, 0, nevbuffers, 0, buffers.length);
        	buffers = nevbuffers;
        }
        buffers[buffersSize++] = buf;
    }

    /**
     * Makes a new buffer available either by allocating
     * a new one or re-cycling an existing one.
     *
     * @param newcount  the size of the buffer if one is created
     */
    protected void needNewBuffer(int newcount) {
        if (currentBufferIndex < buffersSize - 1) {
            //Recycling old buffer
            filledBufferSum += currentBuffer.length;
            
            currentBufferIndex++;
            currentBuffer = buffers[currentBufferIndex];
        } else {
            //Creating new buffer
            int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newcount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(
                    currentBuffer.length << 1, 
                    newcount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }
            
            currentBufferIndex++;
            currentBuffer = new byte[newBufferSize];
            addBufferToList(currentBuffer);
        }
    }

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
        int newcount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newcount);
                inBufferPos = 0;
            }
        }
        count = newcount;
    }

    public void write(int b) {
        int inBufferPos = count - filledBufferSum;
        if (inBufferPos == currentBuffer.length) {
            needNewBuffer(count + 1);
            inBufferPos = 0;
        }
        currentBuffer[inBufferPos] = (byte) b;
        count++;
    }

    public int write(InputStream in) throws IOException {
        int readCount = 0;
        int inBufferPos = count - filledBufferSum;
        int n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != -1) {
            readCount += n;
            inBufferPos += n;
            count += n;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public void reset() {
        count = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        currentBuffer = buffers[currentBufferIndex];
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        int remaining = count;
        for (int i = 0; i < buffersSize; i++) {
            byte[] buf = buffers[i];
            int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

    @Override
    public byte[] toByteArray() {
        int remaining = count;
        if (remaining == 0) {
            return EMPTY_BYTE_ARRAY; 
        }
        byte newbuf[] = new byte[remaining];
        int pos = 0;
        for (int i = 0; i < buffersSize; i++) {
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
    
	@Override
	public void ensureSpace(int space) {}
    
}
