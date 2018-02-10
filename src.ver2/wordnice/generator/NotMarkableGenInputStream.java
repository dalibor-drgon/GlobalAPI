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

import java.io.IOException;

public abstract class NotMarkableGenInputStream
extends GenInputStream {
	
	public NotMarkableGenInputStream() {}
	public NotMarkableGenInputStream(Generator gen) {
		if(gen == null) throw new IllegalArgumentException("Generator == null");
	}
	
    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void reset() throws IOException {
    	throw new IOException("mark/reset not supported");
    }

	@Override
	public void mark() {}

	@Override
	public Seed getMarkSeed() {
		return this.getGenerator().getSeed();
	}

	@Override
	public void setMarkSeed(Seed markSeed) {}

	@Override
	public int read() {
		return (byte) getGenerator().nextByte();
	}
	
	@Override
	public int read(byte[] bytes) {
		getGenerator().nextBytes(bytes);
		return bytes.length;
	}
	
	@Override
	public int read(byte[] bytes, int off, int len) {
		this.getGenerator().nextBytes(bytes, off, len);
		return len;
	}
	
}
