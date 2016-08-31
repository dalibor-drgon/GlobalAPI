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

package wordnice.utils;

import java.util.Collection;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.codings.Base64;

public class Gen {
	
	public static Gen get() {
		if(genStatic == null) genStatic = new Gen();
		return genStatic;
	}
	
	protected static Gen genStatic = null;
	protected String genString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	protected char[] genChars = genString.toCharArray();
	protected byte[] genBytes = genString.getBytes();
	protected char[] base64Chars = ("_."+genString).toCharArray();
	protected byte[] base64Bytes = ("_."+genString).getBytes();
	
	protected long seedLong = 12345678;
	
	public Gen() {
		setSeed(System.nanoTime());
	}
	
	public Gen(long seed) {
		setSeed(seed);
	}
	
	public long getSeed() {
		return seedLong;
	}
	
	public void setSeed(long nev) {
		if(nev == 0) nev = -1;
		seedLong = nev;
	}
	
	/**
	 * @return Newly generated long
	 * 
	 * @note This implementation will never return zero!
	 * 		(except genLong(long max))
	 */
	public long genLong() {
		seedLong ^= (seedLong << 21);
		seedLong ^= (seedLong >>> 35);
		seedLong ^= (seedLong << 4);
		return seedLong;
	}
	
	public long genLong(long max) {
	    seedLong ^= (seedLong << 21);
	    seedLong ^= (seedLong >>> 35);
	    seedLong ^= (seedLong << 4);
	    long out = (seedLong-1) % max;     
	    return (out < 0) ? -out : out;
	}
	
	public int genInt() {
	    seedLong ^= (seedLong << 21);
	    seedLong ^= (seedLong >>> 35);
	    seedLong ^= (seedLong << 4);
	    return (int) seedLong;
	}
	
	public int genInt(int max) {
	    seedLong ^= (seedLong << 21);
	    seedLong ^= (seedLong >>> 35);
	    seedLong ^= (seedLong << 4);
	    int out = (int) (seedLong-1) % max;     
	    return (out < 0) ? -out : out;
	}
	
	public int genBytesUnderEight(byte[] buff, int size) {
		return genBytesUnderEight(buff, 0, size);
	}
	
	public int genBytesUnderEight(byte[] buff, int off, int size) {
	    seedLong ^= (seedLong << 21);
	    seedLong ^= (seedLong >>> 35);
	    seedLong ^= (seedLong << 4);
	    size = size & 0x07;
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
	
	public void genBytesEight(byte[] buff) {
		genBytesEight(buff, 0);
	}
	
	public void genBytesEight(byte[] buff, int off) {
	    seedLong ^= (seedLong << 21);
	    seedLong ^= (seedLong >>> 35);
	    seedLong ^= (seedLong << 4);
	    buff[off++] = (byte)((seedLong >>  0) & 0xFF);
		buff[off++] = (byte)((seedLong >>  8) & 0xFF);
		buff[off++] = (byte)((seedLong >> 16) & 0xFF);
		buff[off++] = (byte)((seedLong >> 24) & 0xFF);
		buff[off++] = (byte)((seedLong >> 32) & 0xFF);
		buff[off++] = (byte)((seedLong >> 40) & 0xFF);
		buff[off++] = (byte)((seedLong >> 48) & 0xFF);
		buff[off++] = (byte)((seedLong >> 56) & 0xFF);
	}
	
	public void genBytes(byte[] buff) {
		genBytes(buff, 0, buff.length);
	}
	
	public void genBytes(byte[] buff, int off, int len) {
		if(len == 0) return;
		if(len <= 8) genBytesUnderEight(buff, off, len);
		int end = off + len - 8;
		while(off <= end) {
			genBytesEight(buff, off);
			off += 8;
		}
		end = end + 8 - off;
		if(end == 0) return;
		genBytesUnderEight(buff, off, end);
		return;
	}
	
	/**
	 * @return 32
	 */
	public int getTextIdLength() {
		return 32;
	}
	
	/**
	 * @return 24
	 */
	public int getRawIdLength() {
		return 24;
	}
	
	/**
	 * @return random 32-character ID (ASCII chars only)
	 * 		made from 24 real-bytes and encoded with Base64
	 */
	public String genStringID() {
		byte[] bt = new byte[24];
		char[] ch = new char[32];
		genBytes(bt);
		Base64.encodeToChars(ch, 0, bt, 0, 24, base64Chars);
		return Nice.string(ch);
	}
	
	public String genStringIDFor(Collection<? extends String> forCol) {
		String id = genStringID();
		while(forCol.contains(id)) id = genStringID();
		return id;
	}
	
	public String genStringIDFor(Map<? extends String, ? extends Object> forMap) {
		String id = genStringID();
		while(forMap.containsKey(id)) id = genStringID();
		return id;
	}
	
	public String genStringIDForValue(Map<? extends Object, ? extends String> forMap) {
		String id = genStringID();
		while(forMap.containsValue(id)) id = genStringID();
		return id;
	}
	
	/**
	 * @return 32-bytes long ASCII character only ID
	 */
	public byte[] genBytesID() {
		byte[] bt = new byte[32];
		genBytes(bt, 8, 24);
		Base64.encode(bt, 0, bt, 8, 24, base64Bytes);
		return bt;
	}
	
	
	
	/** CHARS */
	
	/**
	 * To array generating
	 * @param output Output array
	 * @param input Characters to generate from
	 * @param ooff Offset of output
	 * @param oolen Offset of 'input'
	 * @param ioff Length of output (result)
	 * @param ilen Length of 'input'
	 */
	public void genChars(char[] output, char[] input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input[ioff + genInt(ilen)];
		}
	}
	
	public void genChars(char[] output, char[] input) {
		genChars(output, input, 0, output.length, 0, input.length);
	}
	
	public char[] genChars(int len) {
		return genChars(genChars, 0, genChars.length, len);
	}
	
	public char[] genChars(char[] from, int len) {
		return genChars(from, 0, from.length, len);
	}
	
	public char[] genChars(char[] from, int foff, int flen, int len) {
		char[] ch = new char[len];
		genChars(ch, from, 0, ch.length, foff, flen);
		return ch;
	}
	
	/** CHARS SEQ */
	
	public void genChars(char[] output, CharSequence input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input.charAt(ioff + genInt(ilen));
		}
	}
	
	public void genChars(char[] output, CharSequence input) {
		genChars(output, input, 0, output.length, 0, input.length());
	}
	
	public char[] genChars(CharSequence from, int len) {
		return genChars(from, 0, from.length(), len);
	}
	
	public char[] genChars(CharSequence from, int foff, int flen, int len) {
		char[] ch = new char[len];
		genChars(ch, from, 0, ch.length, foff, flen);
		return ch;
	}
	
	
	
	/** STRING */
	
	public void genString(char[] output, char[] input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input[ioff + genInt(ilen)];
		}
	}
	
	public void genString(char[] output, char[] input) {
		genString(output, input, 0, output.length, 0, input.length);
	}
	
	public String genString(int len) {
		return genString(genChars, 0, genChars.length, len);
	}
	
	public String genString(char[] from, int len) {
		return genString(from, 0, from.length, len);
	}
	
	public String genString(char[] from, int foff, int flen, int len) {
		return Nice.string(genChars(from, foff, flen, len));
	}
	
	/** CHARS SEQ */
	
	public void genString(char[] output, CharSequence input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input.charAt(ioff + genInt(ilen));
		}
	}
	
	public void genString(char[] output, CharSequence input) {
		genString(output, input, 0, output.length, 0, input.length());
	}
	
	public String genString(CharSequence from, int len) {
		return genString(from, 0, from.length(), len);
	}
	
	public String genString(CharSequence from, int foff, int flen, int len) {
		return Nice.string(genChars(from, foff, flen, len));
	}
	
	
	
	/** BYTES */
	
	public void genChars(byte[] output, byte[] input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input[ioff + genInt(ilen)];
		}
	}
	
	public void genChars(byte[] output, byte[] input) {
		genChars(output, input, 0, output.length, 0, input.length);
	}
	

	public byte[] genChars(byte[] from, int len) {
		return genChars(from, 0, from.length, len);
	}
	
	public byte[] genChars(byte[] from, int foff, int flen, int len) {
		byte[] ch = new byte[len];
		genChars(ch, from, 0, ch.length, foff, flen);
		return ch;
	}
	
	/** BYTES SEQ */
	
	public void genChars(byte[] output, ByteSequence input, 
			int ooff, int oolen, int ioff, int ilen) {
		oolen += ooff;
		while(ooff < oolen) {
			output[ooff++] = input.byteAt(ioff + genInt(ilen));
		}
	}
	
	public void genChars(byte[] output, ByteSequence input) {
		genChars(output, input, 0, output.length, 0, input.length());
	}
	
	public byte[] genChars(ByteSequence from, int len) {
		return genChars(from, 0, from.length(), len);
	}
	
	public byte[] genChars(ByteSequence from, int foff, int flen, int len) {
		byte[] ch = new byte[len];
		genChars(ch, from, 0, ch.length, foff, flen);
		return ch;
	}
}
