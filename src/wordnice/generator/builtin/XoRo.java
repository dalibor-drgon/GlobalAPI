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

package wordnice.generator.builtin;

import wordnice.generator.AbstractGenerator;
import wordnice.generator.AbstractSeed;
import wordnice.generator.Generator;
import wordnice.generator.Generator.TwoLongGenerator;
import wordnice.generator.Seed;
import wordnice.generator.TwoLongGenInputStream;

public class XoRo 
extends AbstractGenerator
implements TwoLongGenerator {
	
	protected long state0;
	protected long state1;
	
	public XoRo() {
		this.setRandomSeed();
	}
	
	public XoRo(Seed seed) {
		this.setSeed(seed);
	}
	
	public XoRo(XoRo xr) {
		this.state0 = xr.state0;
		this.state1 = xr.state1;
	}
	
	public XoRo(long state0, long state1) {
		this.setSeed(state0, state1);
	}
	
	@Override
	public TwoLongGenInputStream createStream() {
		return (TwoLongGenInputStream) super.createStream();
	}
	
	@Override
	public TwoLongGenInputStream getStream() {
		if(stream == null) stream = new TwoLongGenInputStream(this);
		return (TwoLongGenInputStream) stream;
	}

	@Override
	public long nextLong() {
		final long s0 = state0;
        long s1 = state1;
        final long result = s0 + s1;
        s1 ^= s0;
        state0 = Long.rotateLeft(s0, 55) ^ s1 ^ (s1 << 14); // a, b
        state1 = Long.rotateLeft(s1, 36); // c
        return result;
	}
	
	@Override
	public void skipBlocks(int loops) {
		while(loops-- != 0) {
			long s1 = state1 ^ state0;
			state0 = Long.rotateLeft(state0, 55) ^ s1 ^ (s1 << 14); // a, b
			state1 = Long.rotateLeft(s1, 36); // c
		}
	}

	@Override
	public void skipBlocks(long loops) {
		while(loops-- != 0) {
			long s1 = state1 ^ state0;
			state0 = Long.rotateLeft(state0, 55) ^ s1 ^ (s1 << 14); // a, b
			state1 = Long.rotateLeft(s1, 36); // c
		}
	}

	@Override
	public XoRo clone() {
		return new XoRo(this);
	}

	@Override
	public Generator createForSeed(Seed seed) {
		return new XoRo(seed);
	}
	
	public Generator createForSeed(long s0, long s1) {
		return new XoRo(s0, s1);
	}

	@Override
	public Seed getSeed() {
		return new AbstractSeed.TwoLongSeed(state0, state1);
	}

	@Override
	public void setSeed(Seed nev) {
		setSeed(nev.valueLong(), nev.valueLong(1));
	}
	
	public void setSeed(long s0, long s1) {
		if(s0 == 0 && s1 == 0) {
			s1 = 1L;
		}
		this.state0 = s0;
		this.state1 = s1;
	}

	@Override
	public void setRandomSeed() {
		this.state0 = System.nanoTime();
		this.state1 = 1L;
	}

	@Override
	public long getState0() {
		return this.state0;
	}

	@Override
	public long getState1() {
		return this.state1;
	}

}
