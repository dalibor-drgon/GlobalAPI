/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Dalibor Drgoň <emptychannelmc@gmail.com>
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
import java.io.OutputStream;

import wordnice.api.Nice;
import wordnice.http.client.HttpClient;

public class ChunkedOutputStream extends OutputStream {
	
	static final byte[] END = new byte[] {0,0,0,0,'\r', '\n', '\r', '\n'};
	static final byte[] LETTERS = "0123456789abcdef".getBytes();

	protected OutputStream out;
	
    public ChunkedOutputStream(OutputStream out) {
		if(out == null) {
			throw Nice.illegal("OutputStream null!");
		}
		this.out = out;
	}

    @Override
    public void write(int i) throws IOException {
    	this.writeSize(1);
    	this.out.write(HttpClient.CRLF, 0, 2);
        this.out.write(i);
        this.out.write(HttpClient.CRLF, 0, 2);
    }
    
    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        this.writeSize(length);
        this.out.write(HttpClient.CRLF, 0, 2);
        this.out.write(b, offset, length);
        this.out.write(HttpClient.CRLF, 0, 2);
    }

    @Override
    public void flush() throws IOException {
    	this.out.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.write(END, 0, END.length);
        this.out.close();
    }
    
    /** Raw writing makes it even faster!
     * <pre>
     * {@code
     * byte[] part1 = ...
     * byte[] part2 = ...
     * int len = part1.length + part2.length;
     * startChunk(len);
     * writeChunk(part1);
     * writeChunk(part2);
     * finishChunk();
     * }
     * </pre>
     */
    
    public void startChunk(int length) throws IOException {
    	this.writeSize(length);
        this.out.write(HttpClient.CRLF, 0, 2);
    }
    
    public void writeChunk(byte[] bytes) throws IOException {
    	this.out.write(bytes);
    }
    
    public void writeChunk(byte[] bytes, int off, int len) throws IOException {
    	this.out.write(bytes, off, len);
    }
    
    public void finishChunk() throws IOException {
    	this.out.write(HttpClient.CRLF, 0, 2);
    }

    
    /** Utils
     */
    
    protected void writeSize(int val) throws IOException {
    	byte[] buf = new byte[8];
    	int charPos = 8;
        do {
            buf[--charPos] = LETTERS[val & 0x0F];
            val >>>= 4;
        } while (val != 0 && charPos > 0);
		this.out.write(buf, charPos, 8-charPos);
    }

}