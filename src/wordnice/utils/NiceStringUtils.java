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

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import wordnice.api.Api;

public class NiceStringUtils {
	
	public static String multireplace(String input, 
			Object[] find, Object[] repl) {
		String replaced = multireplace(input, 0, input.length(), find, repl, 
				0, Math.min(find.length, repl.length));
		return (replaced == null) ? input : replaced;
	}

	/**
	 * Fast multireplace
	 * @param input String to search
	 * @param off Offset at string
	 * @param slen Length of string after offset
	 * @param find Values to find
	 * @param repl Values to replace
	 * @param roff Offset of values to find & replace
	 * @param rlen Length of values to find & replace
	 * @return null when nothing replaced, replaced string otherwise
	 */
	public static String multireplace(String input, int off, int slen, 
			Object[] find, Object[] repl, int roff, int rlen) {
		StringBuilder sb = Api.sb();
		int i = off;
		int end = off+slen;
		rlen += roff; //rlen became rend
		int start = off;
		while(i < end) {
			for(int ir = roff; ir < rlen; ir++) {
				String f = String.valueOf(find[ir]);
				String r = String.valueOf(repl[ir]);
				if(i+f.length()> end) continue;
				if(input.regionMatches(i, f, 0, f.length())) {
					sb.append(input, start, i);
					sb.append(r);
					i += f.length();
					start = i;
					break;
				}
			}
			i++;
		}
		if(start == off) return null; //nothing found, return null
		return sb.append(input, start, end).toString();
	}
	
	public static String multireplace(String input, Object[] pair) {
		String replaced = multireplace(input, 0, input.length(), pair, 0, pair.length);
		return (replaced == null) ? input : replaced;
	}

	/**
	 * Fast multireplace
	 * @param input String to search
	 * @param off Offset at string
	 * @param slen Length of string after offset
	 * @param pair Values to find:replace
	 * @param roff Offset of values to find & replace
	 * @param rlen Length of values to find & replace
	 * @return null when nothing replaced, replaced string otherwise
	 */
	public static String multireplace(String input, int off, int slen, 
			Object[] pair, int roff, int rlen) {
		StringBuilder sb = Api.sb();
		int i = off;
		int end = off+slen;
		rlen += roff-1; //rlen became rend
		int start = off;
		while(i < end) {
			for(int ir = roff; ir < rlen; ) {
				String f = String.valueOf(pair[ir++]);
				String r = String.valueOf(pair[ir++]);
				if(i+f.length()> end) continue;
				if(input.regionMatches(i, f, 0, f.length())) {
					sb.append(input, start, i);
					sb.append(r);
					i += f.length();
					start = i;
					break;
				}
			}
			i++;
		}
		if(start == off) return null; //nothing found, return null
		return sb.append(input, start, end).toString();
	}
	
	/**
	 * Fast equals without substringing
	 * 
	 * @param str1 String one
	 * @param off1 Offset of string one
	 * @param str2 String two
	 * @param off2 Offset of string two
	 * @param len Length
	 * 
	 * @return `true` If strings are same, otherwise `false`
	 */
	public static boolean equals(CharSequence str1, int off1, CharSequence str2, int off2, int len) {
		if(len == 0) return true;
		if(str1 instanceof String && str2 instanceof String) {
			return ((String) str1).regionMatches(off1, (String)str2, off2, len);
		}
		len += off1;
		for(; off1 < len; off1++, off2++) {
			if(str1.charAt(off1) != str2.charAt(off2)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Fast case insensitive equals without substringing
	 * 
	 * @param str1 String one
	 * @param off1 Offset of string one
	 * @param str2 String two
	 * @param off2 Offset of string two
	 * @param len Length
	 * 
	 * @return `true` If strings are same, otherwise `false`
	 */
	public static boolean equalsIgnoreCase(CharSequence str1, int off1, CharSequence str2, int off2, int len) {
		if(str1 instanceof String && str2 instanceof String) {
			return ((String) str1).regionMatches(true,off1, (String)str2, off2, len);
		}
		len += off1;
		for(; off1 < len; off1++, off2++) {
			int c1 = str1.charAt(off1);
			int c2 = str2.charAt(off2);
			if(c1 == c2) continue;
			int u1 = Character.toUpperCase(c1);
			int u2 = Character.toUpperCase(c2);
			if(u1 == u2) continue;
			if(Character.toLowerCase(u1) != Character.toLowerCase(u2)) return false;
		}
		return true;
	}
	
	
	public static int indexOf(CharSequence cs, char find, int from, int to) {
		while(from < to) {
			if(cs.charAt(from) == find) {
				return from;
			}
			from++;
		}
		return -1;
	}
	
	
	//toByteBuffer
	public static ByteBuffer toByteBuffer(char[] chars, int off, int len, Charset charset) {
		CharBuffer charBuffer = CharBuffer.wrap(chars, off, len);
	    return charset.encode(charBuffer);
	}
	
	public static ByteBuffer toByteBuffer(CharSequence cs, int off, int len, Charset charset) {
		if(cs instanceof CharArraySequence) {
			CharArraySequence ca = (CharArraySequence) cs;
			return toByteBuffer(ca.array(), ca.offset(off), len, charset);
		}
		CharBuffer charBuffer = CharBuffer.wrap(cs, off, len);
	    return charset.encode(charBuffer);
	}
	
	
	//toBytes char[]
	public static byte[] toBytes(char[] chars, int off, int len) {
		return toBytes(chars, off, len, Api.UTF8);
	}
	
	public static byte[] toBytes(char[] chars, int off, int len, Charset charset) {
	    ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
	    byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit());
	    //Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
	    //Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
	    return bytes;
	}
	
	public static ByteArraySequence toByteSequence(char[] chars, int off, int len) {
		return toByteSequence(chars, off, len, Api.UTF8);
	}
	
	public static ByteArraySequence toByteSequence(char[] chars, int off, int len, Charset charset) {
		ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
		return new ByteArraySequence(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit()-byteBuffer.position());
	}
	
	public static void toBytes(DataOutput out, char[] chars, int off, int len) throws IOException {
		toBytes(out, chars, off, len, Api.UTF8);
	}
	
	public static void toBytes(DataOutput out, char[] chars, int off, int len, Charset charset) throws IOException {
		ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
		out.write(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit()-byteBuffer.position());
	}
	
	public static void toBytes(OutputStream out, char[] chars, int off, int len) throws IOException {
		toBytes(out, chars, off, len, Api.UTF8);
	}
	
	public static void toBytes(OutputStream out, char[] chars, int off, int len, Charset charset) throws IOException {
		ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
	    out.write(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit()-byteBuffer.position());
	}

	
	
	//toBytes CharSequence
	public static byte[] toBytes(CharSequence chars, int off, int len) {
		return toBytes(chars, off, len, Api.UTF8);
	}
	
	public static byte[] toBytes(CharSequence chars, int off, int len, Charset charset) {
	    ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
	    byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit());
	    //Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
	    //Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
	    return bytes;
	}
	
	public static ByteArraySequence toByteSequence(CharSequence chars, int off, int len) {
		return toByteSequence(chars, off, len, Api.UTF8);
	}
	
	public static ByteArraySequence toByteSequence(CharSequence chars, int off, int len, Charset charset) {
		ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
		return new ByteArraySequence(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit()-byteBuffer.position());
	}
	
	public static void toBytes(DataOutput out, CharSequence chars, int off, int len) throws IOException {
		toBytes(out, chars, off, len, Api.UTF8);
	}
	
	public static void toBytes(DataOutput out, CharSequence chars, int off, int len, Charset charset) throws IOException {
		ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
		out.write(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit()-byteBuffer.position());
	}
	
	public static void toBytes(OutputStream out, CharSequence chars, int off, int len) throws IOException {
		toBytes(out, chars, off, len, Api.UTF8);
	}
	
	public static void toBytes(OutputStream out, CharSequence chars, int off, int len, Charset charset) throws IOException {
		ByteBuffer byteBuffer = toByteBuffer(chars, off, len, charset);
	    out.write(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit()-byteBuffer.position());
	}
	
	
	//toCharsBuffer
	public static CharBuffer toCharBuffer(byte[] bytes, int off, int len, Charset charset) {
		ByteBuffer bf = ByteBuffer.wrap(bytes, off, len);
		return charset.decode(bf);
	}
	
	public static CharBuffer toCharBuffer(ByteSequence bs, int off, int len, Charset charset) {
		ByteBuffer bf = ByteBuffer.wrap(bs.array(), bs.offset(off), len);
		return charset.decode(bf);
	}
	
	//toChars byte[]
	public static char[] toChars(byte[] bytes, int off, int len) {
		return toChars(bytes, off, len, Api.UTF8);
	}
	
	public static char[] toChars(byte[] bytes, int off, int len, Charset charset) {
		CharBuffer cb = toCharBuffer(bytes, off, len, charset);
	    return Arrays.copyOfRange(cb.array(),
	            cb.position(), cb.limit());
	}
	
	public static CharArraySequence toCharSequence(byte[] bytes, int off, int len) {
		return toCharSequence(bytes, off, len, Api.UTF8);
	}
	
	public static CharArraySequence toCharSequence(byte[] bytes, int off, int len, Charset charset) {
		CharBuffer cb = toCharBuffer(bytes, off, len, charset);
	    return new CharArraySequence(cb.array(),
	            cb.position(), cb.limit()-cb.position());
	}
	
	
	//toChars ByteSequence
	public static char[] toChars(ByteSequence bytes, int off, int len) {
		return toChars(bytes, off, len, Api.UTF8);
	}
	
	public static char[] toChars(ByteSequence bytes, int off, int len, Charset charset) {
		CharBuffer cb = toCharBuffer(bytes, off, len, charset);
	    return Arrays.copyOfRange(cb.array(),
	            cb.position(), cb.limit());
	}
	
	public static CharArraySequence toCharSequence(ByteSequence bytes, int off, int len) {
		return toCharSequence(bytes, off, len, Api.UTF8);
	}
	
	public static CharArraySequence toCharSequence(ByteSequence bytes, int off, int len, Charset charset) {
		CharBuffer cb = toCharBuffer(bytes, off, len, charset);
	    return new CharArraySequence(cb.array(),
	            cb.position(), cb.limit()-cb.position());
	}
	
	
	/**
	 * Randoms
	 */
	protected static String generatorDefaultString = "0123456789abcdefghijklmnopqrstuvwxyABCDEFGHIJKLMNOPQRSTUVWXY";
	protected static char[] generatorDefaultChars = generatorDefaultString.toCharArray();
	protected static byte[] generatorDefaultBytes = generatorDefaultString.getBytes();
	
	public static String genString(int len) {
		return genString(len, generatorDefaultChars);
	}
	
	public static String genString(int len, char[] chars) {
		return String.copyValueOf(genChars(len, chars));
	}
	
	public static String genString(int len, CharSequence chars) {
		return String.copyValueOf(genChars(len, chars));
	}
	
	public static char[] genChars(int len) {
		return genChars(len, generatorDefaultChars);
	}
	
	public static char[] genChars(int len, char[] chars) {
		Random rd = Api.getRandom();
		char[] out = new char[len];
		int i = 0;
		for(; i < len; i++) {
			out[i] = chars[rd.nextInt(chars.length)];
		}
		return out;
	}
	
	public static char[] genChars(int len, CharSequence chars) {
		Random rd = Api.getRandom();
		char[] out = new char[len];
		int i = 0;
		for(; i < len; i++) {
			out[i] = chars.charAt(rd.nextInt(chars.length()));
		}
		return out;
	}
	
	public static void genBytesSecure(byte[] bytes) {
		Api.getRandom().nextBytes(bytes);
	}
	
	public static byte[] genBytes(int len) {
		return genBytes(len, generatorDefaultBytes);
	}
	
	public static byte[] genBytes(int len, byte[] bytes) {
		Random rd = Api.getRandom();
		byte[] out = new byte[len];
		int i = 0;
		for(; i < len; i++) {
			out[i] = bytes[rd.nextInt(bytes.length)];
		}
		return out;
	}
	
	/**
	 * @note No argument checking!
	 * 
	 * @param bytearr Byte array. Recommended size 4 bytes
	 * @param c Character to convert
	 * @return Number of bytes
	 */
	public static int toBytes(byte[] bytearr, char c) {
		if ((c >= 0x0001) && (c <= 0x007F)) {
            bytearr[0] = (byte) c;
            return 1;
        } else if (c > 0x07FF) {
            bytearr[0] = (byte) (0xE0 | ((c >> 12) & 0x0F));
            bytearr[1] = (byte) (0x80 | ((c >>  6) & 0x3F));
            bytearr[2] = (byte) (0x80 | ((c >>  0) & 0x3F));
            return 3;
        } else {
            bytearr[0] = (byte) (0xC0 | ((c >>  6) & 0x1F));
            bytearr[1] = (byte) (0x80 | ((c >>  0) & 0x3F));
            return 2;
        }
	}
	
	/**
	 * @note No argument checking!
	 * 
	 * @param bytearr Byte array. Recommended size 4 bytes
	 * @param off Offset of array
	 * @param c Character to convert
	 * @return Number of bytes
	 */
	public static int toBytes(byte[] bytearr, int off, char c) {
		if ((c >= 0x0001) && (c <= 0x007F)) {
            bytearr[off] = (byte) c;
            return 1;
        } else if (c > 0x07FF) {
            bytearr[off++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
            bytearr[off++] = (byte) (0x80 | ((c >>  6) & 0x3F));
            bytearr[off] = (byte) (0x80 | ((c >>  0) & 0x3F));
            return 3;
        } else {
            bytearr[off++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
            bytearr[off] = (byte) (0x80 | ((c >>  0) & 0x3F));
            return 2;
        }
	}
	
	/**
	 * @note No argument checking!
	 * 
	 * @param out OutputStream stream to write. Can write 1-3 bytes
	 * @param c Character to convert
	 * @return Number of bytes
	 * @throws IOException Writing error
	 */
	public static int toBytes(OutputStream out, char c) throws IOException {
		if ((c >= 0x0001) && (c <= 0x007F)) {
            out.write((byte) c);
            return 1;
        } else if (c > 0x07FF) {
        	out.write((byte) (0xE0 | ((c >> 12) & 0x0F)));
        	out.write((byte) (0x80 | ((c >>  6) & 0x3F)));
        	out.write((byte) (0x80 | ((c >>  0) & 0x3F)));
            return 3;
        } else {
        	out.write((byte) (0xC0 | ((c >>  6) & 0x1F)));
        	out.write((byte) (0x80 | ((c >>  0) & 0x3F)));
            return 2;
        }
	}
	
}
