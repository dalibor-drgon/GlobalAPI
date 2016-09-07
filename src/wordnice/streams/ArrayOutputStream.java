/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Dalibor Drgoň <emptychannelmc@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public abstract class ArrayOutputStream
extends ByteArrayOutputStream {
	
	public ArrayOutputStream(int size) {
		super((size < 0 || size > 128) ? size : 128);
	}

	/**
	 * Closing a <tt>ArrayOutputStream</tt> has no effect
	 * @see java.io.ByteArrayOutputStream#close()
	 */
	@Override
	public void close() {}
	
	/**
	 * Flushing a <tt>ArrayOutputStream</tt> has no effect
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() {}

	/**
	 * @see java.io.ByteArrayOutputStream#reset()
	 */
	@Override
	public abstract void reset();
	
	/**
	 * Return true if this array supports more than 2GiB of data
	 * Please note, some methods may throw OutOfMemoryError
	 * to avoid integer truncation or when stream does not support
	 * more than 2GiB of data
	 * @return true if this array supports more than 2GiB of data
	 */
	public boolean isLong() {
		return false;
	}
	
	/**
	 * @see java.io.ByteArrayOutputStream#size()
	 * @throws OutOfMemoryError if isLong() == true and sizeLong() > int
	 */
	@Override
	public abstract int size();
	
	/**
	 * Return allocated capacity
	 * @return Allocated capacity
	 * @throws OutOfMemoryError if isLong() == true and capacityLong() > int
	 */
	public abstract int capacity();
	
	/**
	 * 
	 * @see java.io.ByteArrayOutputStream#size()
	 */
	public long sizeLong() {
		return this.size();
	}
	
	/**
	 * Return allocated capacity
	 * @return Allocated capacity
	 */
	public long capacityLong() {
		return this.capacity();
	}

	/**
	 * @see wordnice.streams.ArrayOutputStream#toByteArray()
	 */
	@Override
	public abstract byte[] toByteArray();
	
	/**
	 * Return internal byte array if possible or null
	 * @return internal byte array if possible or null
	 */
	public byte[] getBuffer() {
		return null;
	}
	
	/**
	 * Return internal byte array offset if possible or 0
	 * @return internal byte array offset if possible or 0
	 */
	public int getBufferOffset() {
		return 0;
	}
	
	/**
	 * Return internal byte array offset+"off" if possible or "off"
	 * @param off Offset relative to offset of this buffer
	 * @return internal byte array offset+"off" if possible or "off"
	 */
	public int getBufferOffset(int off) {
		return getBufferOffset()+off;
	}

	/**
	 * @see wordnice.streams.ArrayOutputStream#toString()
	 */
    @Override
    public String toString() {
    	byte[] intn = getBuffer();
    	if(intn != null) return new String(intn, getBufferOffset(), size());
        return new String(toByteArray());
    }

    /**
	 * @see wordnice.streams.ArrayOutputStream#toString(java.lang.String)
	 */
    @Override
    public String toString(String enc) throws UnsupportedEncodingException {
    	byte[] intn = getBuffer();
    	if(intn != null) return new String(intn, getBufferOffset(), size(), enc);
        return new String(toByteArray(), enc);
    }
    
    /**
     * Creates a newly allocated string. They bytes from this
     * stream are decoded by given charset and string is created from.
     * 
     * @param enc Charset to decode bytes with
     * @return String decoded with given charset
     */
	public String toString(Charset enc) {
		byte[] intn = getBuffer();
    	if(intn != null) return new String(intn, getBufferOffset(), size(), enc);
        return new String(toByteArray(), enc);
    }
    
	/**
	 * Creates a newly allocated string.
	 * 
	 * @deprecated This method does not properly convert bytes into characters.
	 * 		Use toString(), toString(String) or toString(Charset) instead
	 * @see java.io.ByteArrayOutputStream#toString(int)
	 */
    @Override
    @Deprecated
	public String toString(int hibyte) {
    	byte[] intn = getBuffer();
    	if(intn != null) return new String(intn, getBufferOffset(), size(), hibyte);
		return new String(toByteArray(), hibyte);
	}

	@Override
	public abstract void write(byte[] b, int off, int len);
	
	@Override
	public void write(byte[] b) {
		this.write(b, 0, b.length);
	}

	@Override
	public abstract void write(int b);

	/**
	 * @see java.io.ByteArrayOutputStream#writeTo(java.io.OutputStream)
	 */
	@Override
	public abstract void writeTo(OutputStream out) throws IOException;
	
	/**
	 * Make sure this stream's free space is at least big as "space" argument
	 * May do nothing on some streams
	 */
	public abstract void ensureSpace(int space);
	
	/**
	 * Make sure this stream's free space is at least big as "space" argument
	 * May do nothing on some streams
	 */
	public void ensureSpace(long space) {
		if(space > Integer.MAX_VALUE)
			throw new OutOfMemoryError(""+(this.capacityLong() + space));
		this.ensureSpace((int) space);
	}

	/**
     * Writes the entire contents of the specified input stream to this
     * byte stream. Bytes from the input stream are usually read directly 
     * into the internal buffers of this streams.
     *
     * @param in the input stream to read from
     * @return total number of bytes read from the input stream
     *         (and written to this stream).
     *         Returns 0 if !isLong() && current size() == Integer.MAX_VALUE
     *         Return maximum is Integer.MAX_VALUE
     * @throws IOException if an I/O error occurs while reading the input stream
     */
	public abstract int write(InputStream in) throws IOException;
	
	/**
	 * Write whole input stream, ussualy directly to internal buffers
	 * without copying
	 * 
	 * @param in Input stream to read from
	 * @return Bytes readed
	 * @throws IOException when IO exception is thrown by input stream
	 * @throws OutOfMemoryError if isLong() == false and size reached 2GiB
	 */
	public long writeLong(InputStream in) throws IOException {
		return write(in);
	}
	
}
