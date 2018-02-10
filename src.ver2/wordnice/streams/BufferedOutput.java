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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import wordnice.api.Nice;

/**
 * For Single thread only!
 */
public class BufferedOutput
extends BufferedOutputStream
implements AutoCloseable {
	
	/**
	 * Minimal length of byte array to be redicted to root output stream
	 */
	protected int minimalWrite;
	
	/**
	 * Buffer length
	 * Useful for later appending
	 */
	protected int bufferLength;

    public BufferedOutput(OutputStream out) {
        this(out, Nice.BufferSize, (int) (Nice.BufferSize*0.85));
    }

    public BufferedOutput(OutputStream out, int size) {
        this(out, size, getMinWrite(size));
    }
    
    public BufferedOutput(OutputStream out, int size, int minimalWrite) {
        super(out);
        if (size <= 0) {
            throw Nice.illegal("Buffer size <= 0");
        }
        setBuffer(new byte[size]);
        setMinimalWrite(minimalWrite);
    }
    
    public BufferedOutput(OutputStream out, byte[] buffer) {
    	this(out, buffer, getMinWrite(buffer.length));
    }
    
    public BufferedOutput(OutputStream out, byte[] buffer, int minimalWrite) {
    	super(out);
    	setBuffer(buffer);
    	setMinimalWrite(minimalWrite);
    }

    public void flushBuffer() throws IOException {
        if(count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    public void write(int b) throws IOException {
        if (count >= this.bufferLength) {
            flushBuffer();
        }
        buf[count++] = (byte)b;
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (len >= this.minimalWrite) {
            flushBuffer();
            out.write(b, off, len);
            return;
        }
        if(len > bufferLength - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }

	public int getMinimalWrite() {
		return minimalWrite;
	}

	public BufferedOutput setMinimalWrite(int minimalWrite) {
		if(minimalWrite < 0) {
        	this.minimalWrite = getMinWrite(minimalWrite);
        } else if(minimalWrite > this.bufferLength) {
        	this.minimalWrite = this.bufferLength;
        } else {
        	this.minimalWrite = minimalWrite;
        }
		return this;
	}
	
	public BufferedOutput setMinimalWriteAuto() {
		this.minimalWrite = getMinWrite(this.bufferLength);
		return this;
	}
	
	public byte[] getBuffer() {
		return this.buf;
	}
	
	public BufferedOutput setBuffer(byte[] buffer) {
		if (buffer == null || buffer.length <= 0) {
            throw Nice.illegal("Buffer is null or buffer size <= 0");
        }
		this.buf = buffer;
		this.bufferLength = this.buf.length;
		this.setMinimalWriteAuto();
		return this;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public BufferedOutput setCount(int count) {
		this.count = count;
		return this;
	}
	
	public OutputStream getRoot() {
		return this.out;
	}
	
	public BufferedOutput setRoot(OutputStream out) {
		this.out = out;
		return this;
	}
	
	public int getBufferLength() {
		return this.bufferLength;
	}
	
	public BufferedOutput setBufferLength(int len) {
		if(len > this.buf.length || len < 0) {
			len = this.buf.length;
		}
		this.bufferLength = len;
		return this;
	}
	
    
	/**
	 * Auto compute minimal write
	 */
	protected static int getMinWrite(int size) {
		return (size > 100) ? (int) (size*0.90) : size;
	}
}