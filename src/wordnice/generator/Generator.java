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

import wordnice.utils.ByteSequence;

public interface Generator {
	
	public static interface TwoLongGenerator
	extends Generator {
		void setSeed(long s0, long s1);
		Generator createForSeed(long s0, long s1);
		long getState0();
		long getState1();
	}

	void nextBlock(byte[] bytes);
	void nextBlock(byte[] bytes, int off);
	int blockSize();
	
	long nextLong();
	long nextLong(long max);
	long nextLong(long from, long to);
	
	int nextInt();
	int nextInt(int max);
	int nextInt(int from, int to);
	
	int next(int bits);
	long next64(int bits);
	
	float nextFloat();
	double nextDouble();
	boolean nextBoolean();
	short nextShort();
	char nextChar();
	
	GenInputStream getStream();
	GenInputStream createStream();
	Generator clone();
	Generator createForSeed(Seed seed);
	Seed getSeed();

	void setSeed(Seed nev);
	void setRandomSeed();

	void skipBlocks(int loops);
	void skipBlocks(long loops);

	void genBytes(byte[] bytes);
	void genBytes(byte[] bytes, int off, int len);

	/**
	 * @return def 32 (24/3*4 Base64 encoded)
	 */
	int lengthTextID();

	/**
	 * @return def 24
	 */
	int lengthRawID();

	/**
	 * @return random 32-character ID (ASCII chars only)
	 * 		made from 24 real-bytes encoded with Base64
	 */
	String nextID();
	String generateIDFor(Collection<? extends String> forCol);
	String generateIDFor(Map<? extends String, ? extends Object> forMap);
	String generateIDForValue(Map<? extends Object, ? extends String> forMap);

	/**
	 * @return 32-bytes long ASCII character only ID
	 */
	byte[] nextTextID();

	/**
	 * Generate and base64 encode ID (32-bytes long) to buffer
	 * 
	 * @param bt Byte buffer with at least 32 length
	 */
	void nextTextID(byte[] bt);

	/**
	 * Generate and base64 encode ID (32-bytes long) to buffer
	 * 
	 * @param bt Byte buffer with at least 32 length
	 * @param off Offset
	 */
	void nextTextID(byte[] bt, int off);

	/**
	 * Returns 24-bytes long any-byte ID
	 * 
	 * @param 24-bytes long any-byte ID
	 */
	byte[] nextRawID();
	
	/**
	 * Generate ID (24-bytes long) to buffer
	 * 
	 * @param bt Byte buffer with at least 24 length
	 */
	void nextRawID(byte[] bt);

	/**
	 * Generate ID (24-bytes long) to buffer
	 * 
	 * @param bt Byte buffer with at least 24 length
	 * @param off Offset
	 */
	void nextRawID(byte[] bt, int off);

	/** CHARS */
	void nextChars(char[] output, char[] input, int ooff, int oolen, int ioff, int ilen);
	void nextChars(char[] output, char[] input);
	char[] nextChars(int len);
	char[] nextChars(char[] from, int len);
	char[] nextChars(char[] from, int foff, int flen, int len);
	void nextChars(Appendable out, char[] input, int off, int len, int outlen)
			throws IOException;
	void nextChars(Appendable out, char[] input, int outlen)
			throws IOException;
	void nextChars(Appendable out, int outlen) throws IOException;

	/** CHARS SEQ */
	void nextChars(char[] output, CharSequence input, int ooff, int oolen, int ioff, int ilen);
	void nextChars(char[] output, CharSequence input);
	char[] nextChars(CharSequence from, int len);
	char[] nextChars(CharSequence from, int foff, int flen, int len);
	void nextChars(Appendable out, CharSequence input, int off, int len, int outlen)
			throws IOException;
	void nextChars(Appendable out, CharSequence input, int outlen)
			throws IOException;

	/** STRING */
	String nextString(int len);
	String nextString(char[] from, int len);
	String nextString(char[] from, int foff, int flen, int len);

	/** STRING SEQ */
	String nextString(CharSequence from, int len);
	String nextString(CharSequence from, int foff, int flen, int len);

	/** BYTES */
	void nextBytes(byte[] output, byte[] input, int ooff, int oolen, int ioff, int ilen);
	void nextBytes(byte[] output, byte[] input);
	byte[] nextBytes(byte[] from, int len);
	byte[] nextBytes(byte[] from, int foff, int flen, int len);
	void nextBytes(OutputStream out, byte[] input, int off, int len, int outlen)
			throws IOException;
	void nextBytes(OutputStream out, byte[] input, int outlen)
			throws IOException;
	void nextBytes(OutputStream out, int outlen) throws IOException;

	/** BYTES SEQ */
	void nextBytes(byte[] output, ByteSequence input, int ooff, int oolen, int ioff, int ilen);
	void nextBytes(byte[] output, ByteSequence input);
	byte[] nextBytes(ByteSequence from, int len);
	byte[] nextBytes(ByteSequence from, int foff, int flen, int len);
	void nextBytes(OutputStream out, ByteSequence input, int off, int len, int outlen)
			throws IOException;
	void nextBytes(OutputStream out, ByteSequence input, int outlen)
			throws IOException;
	
	
}
