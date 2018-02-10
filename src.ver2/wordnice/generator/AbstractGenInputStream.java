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

package wordnice.generator;

import wordnice.api.Nice;

public abstract class AbstractGenInputStream
extends GenInputStream {

	protected byte[] tmp = null;
	protected int off = 8;
	
	public AbstractGenInputStream(Generator gen) {
		if(gen == null) throw new IllegalArgumentException("Generator == null!");
		this.off = gen.blockSize();
		this.tmp = new byte[this.off];
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() {
		if(off == tmp.length) {
			off = 0;
			getGenerator().nextBlock(tmp);
		}
		return tmp[off++];
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(byte[] bytes) {
		read0(bytes, 0, bytes.length);
		return bytes.length;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] bytes, int off, int len) {
        Nice.checkBounds(bytes, off, len);
        read0(bytes, off, len);
        return len;
	}
	
	protected void read0(byte[] bytes, int off, int len) {
		int tmplen = this.tmp.length-this.off;
    	if(len <= tmplen) {
    		int endoff = this.off+len;
    		while(this.off != endoff) {
    			bytes[off++] = tmp[this.off++];
    		}
    	} else {
    		Generator generator = getGenerator();
    		while(this.off != this.tmp.length) {
    			bytes[off++] = tmp[this.off++];
    		}
    		len -= tmplen;
    		int rem = len % this.tmp.length;
    		len += off - this.tmp.length;
    		while(off <= len) {
    			generator.nextBlock(bytes, off);
    			off += this.tmp.length;
    		}
    		off = 0;
    		generator.nextBlock(tmp);
    		while(this.off != rem) {
    			bytes[off++] = tmp[this.off++];
    		}
    	}
    }

	/**
	 * @see java.io.InputStream#skip(long)
	 */
    public long skip(long n) {
    	if(n <= 0) return 0;
    	long orig = n;
    	int len = this.tmp.length-off;
    	if(n <= len) {
    		off += n;
    	} else {
    		Generator generator = getGenerator();
    		n -= len;
    		generator.skipBlocks(n / this.tmp.length);
    		int rem = len % this.tmp.length;
    		generator.nextBlock(tmp);
    		off = rem;
    	}
    	return orig;
    }

    /**
     * @see java.io.InputStream#available()
     * @return unlimited (Integer.MAX_VALUE)
     */
    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    /**
     * @see java.io.InputStream#markSupported()
     * @return true of false, depending of implementation
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public void mark(int ign) {
    	this.mark();
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#reset()
     */
    @Override
    public void reset() {
    	off = this.tmp.length;
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() {}
    
    @Override
	public Generator cloneMark() {
		return this.getGenerator().cloneFor(this.getMarkSeed());
	}
	
	@Override
	public GenInputStream clone() {
		return this.getGenerator().createStream();
	}
}