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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import wordnice.api.Nice;

public class ArrayOutputStreamSimple
extends ArrayOutputStream {

	/**
     * Creates a new byte array output stream, with a buffer capacity of
     * 128 bytes
     */
    public ArrayOutputStreamSimple() {
        super(0); //zero = default
    }

    /**
     * Creates a new byte array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param   size   the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
    public ArrayOutputStreamSimple(int size) {
        super(size);
    }

    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is
     * interpreted as a request for the unsatisfiably large capacity
     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    protected void ensureCapacity(int minCapacity) {
    	if (minCapacity < 0) // overflow
            throw new OutOfMemoryError(""+minCapacity);
        // overflow-conscious code
    	if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity. Should be positive
     */
    private void grow(int minCapacity) {
    	int factorSize = minCapacity + (buf.length >> 1);
    	int doubleSize = buf.length << 1;
    	int newBufferSize;
    	if(doubleSize > Nice.maxArrayLength || doubleSize < 0
    			|| factorSize > Nice.maxArrayLength || factorSize < 0) {
    		newBufferSize = Math.max(Nice.maxArrayLength, minCapacity);
    	} else {
    		newBufferSize = Math.max(doubleSize, factorSize);
    	}
    	buf = Arrays.copyOf(buf, newBufferSize);
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count += 1;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
            ((off + len) - b.length > 0)) {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf, 0, count);
    }

    @Override
    public void reset() {
        count = 0;
    }

    @Override
    public byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    @Override
    public int size() {
        return count;
    }

	@Override
	public int capacity() {
		return this.buf.length;
	}
	
	@Override
	public byte[] getBuffer() {
		return this.buf;
	}

	public void ensureSpace() {
		//double the size!
		ensureSpace(buf.length+1);
	}
	
	@Override
	public void ensureSpace(int space) {
		long nevcap = this.sizeLong() + (long)space;
		if(nevcap <= Integer.MAX_VALUE) {
			this.ensureCapacity((int) nevcap);
		}
	}

	@Override
	public int write(InputStream in) throws IOException {
		int rd = -1;
		int total = 0;
		while((rd = in.read(buf, count, this.buf.length-count)) != -1) {
			count += rd;
			total += rd;
			if(count == this.buf.length) {
				int nevspace = Math.min(Integer.MAX_VALUE-this.size(), Nice.bufferSize);
				if(nevspace == 0) {
					throw new OutOfMemoryError();
				}
				this.ensureSpace(nevspace);
			}
		}
		return total;
	}

}