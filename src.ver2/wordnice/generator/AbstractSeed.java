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

import java.util.Arrays;

import wordnice.streams.OUtils;

public abstract class AbstractSeed
implements Seed {

	public int valueBytes(byte[] buff) {
		return valueBytes(buff, 0);
	}
	public byte[] valueBytes() {
		byte[] ret = new byte[this.length()];
		int len = valueBytes(ret);
		if(len != ret.length) {
			ret = Arrays.copyOf(ret, len);
		}
		return ret;
	}
	
	public static class LongSeed
	extends AbstractSeed {
		
		protected long seed0 = 1L;
		
		protected LongSeed() {}
		
		public LongSeed(long seed) {
			setSeed(seed);
		}
		
		public void setSeed(long seed) {
			this.seed0 = seed;
		}

		@Override
		public long valueLong() {
			return seed0;
		}

		@Override
		public long valueLong(int index) {
			return seed0;
		}

		@Override
		public int length() {
			return 8;
		}

		@Override
		public int valueBytes(byte[] buff, int off) {
			OUtils.writeLong(buff, off, seed0);
			return 16;
		}
		
	}
	
	public static class TwoLongSeed
	extends LongSeed {
		
		protected long seed1 = 1L;
		
		public TwoLongSeed(long seed0, long seed1) {
			setSeed(seed0, seed1);
		}
		
		public void setSeed(long seed0, long seed1) {
			this.seed0 = seed0;
			this.seed1 = seed1;
		}

		@Override
		public long valueLong() {
			return seed0;
		}
		
		public long valueLong2() {
			return seed1;
		}

		@Override
		public long valueLong(int index) {
			return (index == 0) ? seed0 : seed1;
		}

		@Override
		public int length() {
			return 16;
		}

		@Override
		public int valueBytes(byte[] buff, int off) {
			OUtils.writeLong(buff, off, seed0);
			OUtils.writeLong(buff, off+8, seed0);
			return 16;
		}
		
	}
	
}
