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

import java.security.SecureRandom;

import wordnice.generator.AbstractGenerator;
import wordnice.generator.AbstractSeed;
import wordnice.generator.GenInputStream;
import wordnice.generator.Generator;
import wordnice.generator.NotMarkableGenInputStream;
import wordnice.streams.IUtils;
import wordnice.generator.Seed;

public class SecureGenerator 
extends AbstractGenerator
implements Generator {
	
	protected static class SecureGenInputStream
	extends NotMarkableGenInputStream {

		protected SecureGenerator generator;
		
		public SecureGenInputStream(SecureGenerator gen) {
			super(gen);
			this.generator = gen;
		}

		@Override
		public SecureGenerator getGenerator() {
			return this.generator;
		}

		@Override
		public int read() {
			return (byte) getGenerator().getRandom().nextInt();
		}
		
	}
	
	protected SecureRandom secureRandom;
	
	public SecureGenerator() {
		this.secureRandom = new SecureRandom();
	}
	
	public SecureGenerator(SecureRandom secureRandom) {
		this.secureRandom = secureRandom;
	}
	
	protected SecureRandom getRandom() {
		return this.secureRandom;
	}
	
	@Override
	public GenInputStream createStream() {
		return (GenInputStream) new SecureGenInputStream(this);
	}
	
	@Override
	public GenInputStream getStream() {
		if(stream == null) stream = new SecureGenInputStream(this);
		return (GenInputStream) stream;
	}

	@Override
	public long nextLong() {
		return this.secureRandom.nextLong();
	}
	
	@Override
	public int nextInt() {
		return this.secureRandom.nextInt();
	}
	
	@Override
	public int nextInt(int n) {
		if(n < 0) return -nextInt(-n);
		if(n == 0) return 0;
		return this.secureRandom.nextInt(n);
	}
	
	@Override
	public double nextDouble() {
		return this.secureRandom.nextDouble();
	}
	
	@Override
	public float nextFloat() {
		return this.secureRandom.nextFloat();
	}
	
	@Override
	public boolean nextBoolean() {
		return this.secureRandom.nextBoolean();
	}

	@Override
	public short nextShort() {
		return (short) this.secureRandom.nextInt();
	}

	@Override
	public char nextChar() {
		return (char) this.secureRandom.nextInt();
	}
	
	@Override
	public byte nextByte() {
		return (byte) this.secureRandom.nextInt();
	}
	
	@Override
	public void skipBlocks(int loops) {
		//nothing to do, fully random data
	}

	@Override
	public void skipBlocks(long loops) {
		//nothing to do, fully random data
	}

	@Override
	public SecureGenerator clone() {
		return new SecureGenerator();
	}

	@Override
	public Generator cloneFor(Seed seed) {
		return clone();
	}

	@Override
	public Seed getSeed() {
		//Just for compatibility, Generator is meant to be predictable
		return new AbstractSeed.LongSeed(IUtils.readLong(this.secureRandom.generateSeed(8), 0));
	}

	@Override
	public void setSeed(Seed nev) { }

	@Override
	public void setRandomSeed() { }
	
	@Override
	public void nextBytes(byte[] bytes) {
		this.secureRandom.nextBytes(bytes);
	}
	
	@Override
	public void nextBytes(byte[] bytes, int off, int len) {
		if(off == 0) {
			if(bytes == null) throw new IllegalArgumentException("Bytes == null");
			if(len == bytes.length) {
				this.secureRandom.nextBytes(bytes);
				return;
			}
		}
		super.nextBytes(bytes, off, len);
	}

}
