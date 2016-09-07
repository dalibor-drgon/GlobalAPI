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

import java.io.InputStream;

import wordnice.api.Nice;

public abstract class GenInputStream
extends InputStream {
	protected Generator generator;
	protected byte[] tmp = null;
	protected int off = 8;
	
	public GenInputStream(Generator gen) {
		if(gen == null) throw new IllegalArgumentException("Generator == null!");
		this.generator = gen;
		this.tmp = new byte[gen.blockSize()];
	}
	
	public int read() {
		if(off == tmp.length) {
			off = 0;
			generator.nextBlock(tmp);
		}
		return tmp[off++];
	}

	public int read(byte[] bytes) {
		readDirect(bytes, 0, bytes.length);
		return bytes.length;
	}
	
	public int read(byte[] bytes, int off, int len) {
        Nice.checkBounds(bytes, off, len);
        readDirect(bytes, off, len);
        return len;
	}
	
	public void readDirect(byte[] bytes, int off, int len) {
		int tmplen = this.tmp.length-this.off;
    	if(len <= tmplen) {
    		int endoff = this.off+len;
    		while(this.off != endoff) {
    			bytes[off++] = tmp[this.off++];
    		}
    	} else {
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

    public long skip(long n) {
    	if(n <= 0) return 0;
    	long orig = n;
    	int len = this.tmp.length-off;
    	if(n <= len) {
    		off += n;
    	} else {
    		n -= len;
    		generator.skipBlocks(n / this.tmp.length);
    		int rem = len % this.tmp.length;
    		generator.nextBlock(tmp);
    		off = rem;
    	}
    	return orig;
    }

    public int available() {
        return Integer.MAX_VALUE;
    }

    public boolean markSupported() {
        return true;
    }

    
    @Override
    public void mark(int ign) {
    	this.mark();
    }

    public void reset() {
    	off = this.tmp.length;
    }

    @Override
    public void close() {}

	public Generator getGenerator() {
		return generator;
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
	}

	public abstract void mark();
	
	public abstract Seed getMarkSeed();
	
	public abstract void setMarkSeed(Seed markSeed);
	
	public Generator cloneMark() {
		return this.generator.createForSeed(this.getMarkSeed());
	}
	
	@Override
	public GenInputStream clone() {
		return this.generator.createStream();
	}
}