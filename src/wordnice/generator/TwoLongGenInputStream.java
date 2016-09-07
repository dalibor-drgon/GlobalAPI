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

import wordnice.generator.Generator.TwoLongGenerator;

public class TwoLongGenInputStream
extends GenInputStream {
	
	long s0, s1;

	public TwoLongGenInputStream(TwoLongGenerator gen) {
		super(gen);
	}
	
	protected TwoLongGenerator get() {
		return (TwoLongGenerator) this.generator;
	}

	@Override
	public void mark() {
		TwoLongGenerator x = get();
		this.s0 = x.getState0();
		this.s1 = x.getState1();
	}

	@Override
	public Seed getMarkSeed() {
		return new AbstractSeed.TwoLongSeed(s0, s1);
	}

	@Override
	public void setMarkSeed(Seed markSeed) {
		this.setMarkedSeed(s0, s1);
	}
	
	public void setMarkedSeed(long s0, long s1) {
		if(s0 == 0 && s1 == 0) {
			s1 = 1L;
		}
		this.s0 = s0;
		this.s1 = s1;
	}
	
	public long getState0() {
		return this.s0;
	}
	
	public long getState1() {
		return this.s1;
	}
	
}
