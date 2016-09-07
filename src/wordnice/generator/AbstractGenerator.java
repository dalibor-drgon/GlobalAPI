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
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import wordnice.codings.Base64;
import wordnice.streams.OUtils;
import wordnice.utils.ByteSequence;
import wordnice.utils.JavaHooker;

public abstract class AbstractGenerator 
implements Generator {
	
	protected static final long DOUBLE_MASK = (1L << 53) - 1;
	protected static final double NORM_53 = 1. / (1L << 53);
	protected static final long FLOAT_MASK = (1L << 24) - 1;
	protected static final double NORM_24 = 1. / (1L << 24);
	
	protected static String genString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	protected static char[] genChars = genString.toCharArray();
	protected static byte[] genBytes = genString.getBytes();
	protected static char[] base64Chars = ("_."+genString).toCharArray();
	protected static byte[] base64Bytes = ("_."+genString).getBytes();
	
	protected GenInputStream stream = null;

	/*
	 * (non-Javadoc)
	 * @see wordnice.generator.Generator#nextLong()
	 */
	@Override
	public abstract long nextLong();
	
	@Override
	public abstract Generator clone();

	@Override
	public abstract Generator createForSeed(Seed seed);

	@Override
	public abstract Seed getSeed();

	@Override
	public abstract void setSeed(Seed nev);

	@Override
	public abstract void setRandomSeed();
	
	@Override
	public void nextBlock(byte[] bytes) {
		OUtils.writeLong(bytes, 0, nextLong());
	}

	@Override
	public void nextBlock(byte[] bytes, int off) {
		OUtils.writeLong(bytes, off, nextLong());
	}

	@Override
	public int blockSize() {
		return 8; //sizeof(long)
	}


	@Override
	public long nextLong(long bound) {
		if (bound <= 0) return -nextLong(-bound);
        else if(bound == 0) return 0;
        long threshold = (0x7fffffffffffffffL - bound + 1) % bound;
        for (; ; ) {
            long bits = nextLong() & 0x7fffffffffffffffL;
            if (bits >= threshold)
                return bits % bound;
        }
	}
	
	@Override
	public int next(int bits) {
		return (int) (nextLong() & ((1 << bits) - 1));
	}
	
	@Override
	public long next64(int bits) {
		return (nextLong() & ((1L << bits) - 1L));
	}

	@Override
	public long nextLong(final long lower, final long upper) {
		if (upper < lower) return upper + nextLong(lower - upper);
        return lower + nextLong(upper - lower);
	}

	@Override
	public int nextInt() {
		return (int) nextLong();
	}

	@Override
	public int nextInt(int bound) {
		if (bound < 0) return -nextInt(-bound);
        else if(bound == 0) return 0;
        int threshold = (0x7fffffff - bound + 1) % bound;
        for (; ; ) {
            int bits = (int) (nextLong() & 0x7fffffff);
            if (bits >= threshold)
                return bits % bound;
        }
	}

	@Override
	public int nextInt(final int lower, final int upper) {
		if (upper < lower) return upper + nextInt(lower - upper);
        return lower + nextInt(upper - lower);
	}

	@Override
	public float nextFloat() {
		return (float) ((nextLong() & FLOAT_MASK) * NORM_24);
	}

	@Override
	public double nextDouble() {
		return (nextLong() & DOUBLE_MASK) * NORM_53;
	}

	@Override
	public boolean nextBoolean() {
		return (nextLong() & 1) != 0;
	}

	@Override
	public short nextShort() {
		return (short) nextLong();
	}

	@Override
	public char nextChar() {
		return (char) nextLong();
	}

	@Override
	public GenInputStream getStream() {
		if(stream == null) stream = new UniGenInputStream(this);
		return stream;
	}

	@Override
	public GenInputStream createStream() {
		return clone().getStream();
	}

	@Override
	public void skipBlocks(int loops) {
		while(loops-- != 0) nextLong();
	}

	@Override
	public void skipBlocks(long loops) {
		while(loops-- != 0) nextLong();
	}

	protected void nextBytesEight(byte[] buff, int off) {
		OUtils.writeLong(buff,  off, nextLong());
	}
	
	protected int nextBytesUnderEight(byte[] buff, int off, int size) {
		long seedLong = nextLong();
		size = size & 0x07; //zero = 8
	    switch(size) {
	    	case 7:
	    		buff[off++] = (byte)((seedLong >>  0) & 0xFF);
	    	case 6:
	    		buff[off++] = (byte)((seedLong >>  8) & 0xFF);
	    	case 5:
	    		buff[off++] = (byte)((seedLong >> 16) & 0xFF);
	    	case 4:
	    		buff[off++] = (byte)((seedLong >> 24) & 0xFF);
	    	case 3:
	    		buff[off++] = (byte)((seedLong >> 32) & 0xFF);
	    	case 2:
	    		buff[off++] = (byte)((seedLong >> 40) & 0xFF);
	    	case 1:
	    		buff[off++] = (byte)((seedLong >> 48) & 0xFF);
	    	case 0:
	    		buff[off++] = (byte)((seedLong >> 56) & 0xFF);
	    }
	    return size;
	}
	
	/* (non-Javadoc)
	 * @see wordnice.utils.Generator#genBytes(byte[])
	 */
	@Override
	public void genBytes(byte[] bytes) {
		genBytes(bytes, 0, bytes.length);
	}
	
	/* (non-Javadoc)
	 * @see wordnice.utils.Generator#genBytes(byte[], int, int)
	 */
	@Override
	public void genBytes(byte[] bytes, int off, int len) {
		if(len == 0) return;
		if(len <= 8) nextBytesUnderEight(bytes, off, len);
		int end = off + len - 8;
		while(off <= end) {
			OUtils.writeLong(bytes, off, nextLong());
			off += 8;
		}
		end = end + 8 - off;
		if(end == 0) return;
		nextBytesUnderEight(bytes, off, end);
		return;
	}

	@Override
	public int lengthTextID() {
		return 32;
	}

	@Override
	public int lengthRawID() {
		return 24;
	}

	@Override
	public String nextID() {
		byte[] bt = new byte[24];
		char[] ch = new char[32];
		nextBytesEight(bt, 0);
		nextBytesEight(bt, 8);
		nextBytesEight(bt, 16);
		Base64.encodeToChars(ch, 0, bt, 0, 24, base64Chars);
		return JavaHooker.string(ch);
	}

	@Override
	public String generateIDFor(Collection<? extends String> forCol) {
		String id = nextID();
		while(forCol.contains(id)) id = nextID();
		return id;
	}

	@Override
	public String generateIDFor(Map<? extends String, ? extends Object> forMap) {
		String id = nextID();
		while(forMap.containsKey(id)) id = nextID();
		return id;
	}

	@Override
	public String generateIDForValue(Map<? extends Object, ? extends String> forMap) {
		String id = nextID();
		while(forMap.containsValue(id)) id = nextID();
		return id;
	}

	@Override
	public byte[] nextTextID() {
		byte[] bt = new byte[this.lengthTextID()];
		nextTextID(bt);
		return bt;
	}

	@Override
	public void nextTextID(byte[] bt) {
		nextBytesEight(bt, 8);
		nextBytesEight(bt, 16);
		nextBytesEight(bt, 24);
		Base64.encode(bt, 0, bt, 8, 24, base64Bytes);
	}

	@Override
	public void nextTextID(byte[] bt, int off) {
		nextBytesEight(bt, off+8);
		nextBytesEight(bt, off+16);
		nextBytesEight(bt, off+24);
		Base64.encode(bt, off, bt, off+8, 24, base64Bytes);
	}

	@Override
	public byte[] nextRawID() {
		byte[] bt = new byte[this.lengthRawID()];
		nextRawID(bt);
		return bt;
	}

	@Override
	public void nextRawID(byte[] bt) {
		nextBytesEight(bt, 0);
		nextBytesEight(bt, 8);
		nextBytesEight(bt, 16);
	}

	@Override
	public void nextRawID(byte[] bt, int off) {
		nextBytesEight(bt, off);
		nextBytesEight(bt, off+8);
		nextBytesEight(bt, off+16);
	}

	@Override
	public void nextChars(char[] output, char[] input, int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input[ioff + nextInt(ilen)];
		}
	}

	@Override
	public void nextChars(char[] output, char[] input) {
		nextChars(output, input, 0, output.length, 0, input.length);
	}

	@Override
	public char[] nextChars(int len) {
		return nextChars(genChars, 0, genChars.length, len);
	}

	@Override
	public char[] nextChars(char[] from, int len) {
		return nextChars(from, 0, from.length, len);
	}

	@Override
	public char[] nextChars(char[] from, int foff, int flen, int len) {
		char[] ch = new char[len];
		nextChars(ch, from, 0, ch.length, foff, flen);
		return ch;
	}
	
	@Override
	public void nextChars(char[] output, CharSequence input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input.charAt(ioff + nextInt(ilen));
		}
	}

	@Override
	public void nextChars(char[] output, CharSequence input) {
		nextChars(output, input, 0, output.length, 0, input.length());
	}

	@Override
	public char[] nextChars(CharSequence from, int len) {
		return nextChars(from, 0, from.length(), len);
	}

	@Override
	public char[] nextChars(CharSequence from, int foff, int flen, int len) {
		char[] ch = new char[len];
		nextChars(ch, from, 0, ch.length, foff, flen);
		return ch;
	}

	@Override
	public String nextString(int len) {
		return nextString(genChars, 0, genChars.length, len);
	}

	@Override
	public String nextString(char[] from, int len) {
		return nextString(from, 0, from.length, len);
	}

	@Override
	public String nextString(char[] from, int foff, int flen, int len) {
		return JavaHooker.string(nextChars(from, foff, flen, len));
	}

	@Override
	public String nextString(CharSequence from, int len) {
		return nextString(from, 0, from.length(), len);
	}

	@Override
	public String nextString(CharSequence from, int foff, int flen, int len) {
		return JavaHooker.string(nextChars(from, foff, flen, len));
	}
	
	@Override
	public void nextBytes(byte[] output, byte[] input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input[ioff + nextInt(ilen)];
		}
	}

	@Override
	public void nextBytes(byte[] output, byte[] input) {
		nextBytes(output, input, 0, output.length, 0, input.length);
	}
	
	@Override
	public byte[] nextBytes(byte[] from, int len) {
		return nextBytes(from, 0, from.length, len);
	}

	@Override
	public byte[] nextBytes(byte[] from, int foff, int flen, int len) {
		byte[] ch = new byte[len];
		nextBytes(ch, from, 0, ch.length, foff, flen);
		return ch;
	}
	
	@Override
	public void nextBytes(byte[] output, ByteSequence input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input.byteAt(ioff + nextInt(ilen));
		}
	}
	
	@Override
	public void nextBytes(byte[] output, ByteSequence input) {
		nextBytes(output, input, 0, output.length, 0, input.length());
	}

	@Override
	public byte[] nextBytes(ByteSequence from, int len) {
		return nextBytes(from, 0, from.length(), len);
	}
	
	@Override
	public byte[] nextBytes(ByteSequence from, int foff, int flen, int len) {
		byte[] ch = new byte[len];
		nextBytes(ch, from, 0, ch.length, foff, flen);
		return ch;
	}

	@Override
	public void nextChars(Appendable out, char[] input, int ioff, int ilen, int outlen)
			throws IOException {
		while(outlen-- != 0) out.append(input[ioff + nextInt(ilen)]);
	}

	@Override
	public void nextChars(Appendable out, char[] input, int outlen)
			throws IOException {
		nextChars(out, input, 0, input.length, outlen);
	}
	
	@Override
	public void nextChars(Appendable out, int outlen) throws IOException {
		nextChars(out, genChars, 0, genChars.length, outlen);
	}

	@Override
	public void nextChars(Appendable out, CharSequence input, int ioff, int ilen, int outlen)
			throws IOException {
		while(outlen-- != 0) out.append(input.charAt(ioff + nextInt(ilen)));
	}

	@Override
	public void nextChars(Appendable out, CharSequence input, int outlen)
			throws IOException {
		nextChars(out, input, 0, input.length(), outlen);
	}

	@Override
	public void nextBytes(OutputStream out, byte[] input, int ioff, int ilen, int outlen)
			throws IOException {
		while(outlen-- != 0) out.write(input[ioff + nextInt(ilen)]);
	}

	@Override
	public void nextBytes(OutputStream out, byte[] input, int outlen)
			throws IOException {
		nextBytes(out, input, 0, input.length, outlen);
	}
	
	@Override
	public void nextBytes(OutputStream out, int outlen) throws IOException {
		nextBytes(out, genBytes, 0, genBytes.length, outlen);
	}

	@Override
	public void nextBytes(OutputStream out, ByteSequence input, int ioff, int ilen, int outlen)
			throws IOException {
		while(outlen-- != 0) out.write(input.byteAt(ioff + nextInt(ilen)));
	}

	@Override
	public void nextBytes(OutputStream out, ByteSequence input, int outlen)
			throws IOException {
		nextBytes(out, input, 0, input.length(), outlen);
	}

}
