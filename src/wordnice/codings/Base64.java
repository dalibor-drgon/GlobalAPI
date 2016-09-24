/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 * This material was derived from TLibs C library.
 * https://github.com/wordnice/TLibs
 */

package wordnice.codings;

import java.util.Arrays;

import wordnice.api.Nice;

public class Base64 {
	
	/*
	 * Tables
	 * * Characters (Bytes) returned on encode()
	 * * Bytes returned on decode()
	 */
	
	public static String EncodeString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	public static byte[] EncodeBytes = EncodeString.getBytes();
	public static char[] EncodeChars = EncodeString.toCharArray();
	public static byte[] DecodeBytes = null;
	
	public static String EncodeStringURL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
	public static byte[] EncodeBytesURL = EncodeStringURL.getBytes();
	public static char[] EncodeCharsURL = EncodeStringURL.toCharArray();
	public static byte[] DecodeBytesURL = null;
	
	static {
		DecodeBytes = buildDecode(EncodeBytes);
		DecodeBytesURL = buildDecode(EncodeBytesURL);
	}
	
	public static byte[] buildDecode(byte[] encode) {
		return buildDecode(encode, 0, encode.length);
	}
	
	public static byte[] buildDecode(byte[] encode, int off, int len) {
		Nice.checkBounds(encode, off, len);
		if(len < 64) throw new IllegalArgumentException("Length need to be at least 64 bytes!");
		len = off+64;
		byte[] ret = new byte[256];
		Arrays.fill(ret, (byte) 64); //64 = invalid
		int i = off;
		for(; i < len; i++) {
			ret[encode[i]] = (byte) (i-off);
		}
		return ret;
	}
	
	
	
	/*
	 * Decode
	 */
	
	public static byte[] decode(byte[] in) throws IllegalArgumentException {
		return decode(in, 0, in.length, DecodeBytes);
	}
	
	public static byte[] decode(byte[] in, int off, int len) throws IllegalArgumentException {
		return decode(in, off, len, DecodeBytes);
	}
	
	public static byte[] decode(byte[] in, int off, int len, byte[] filter) throws IllegalArgumentException {
		while(in[off + len - 1] == (byte) '=') len--;
		int outsz = (len / 4) * 3;
		int add = len & 0x03;
		outsz = (add <= 1) ? outsz : outsz+add-1;
		byte[] out = new byte[outsz];
		decode(out, 0, in, off, len, filter);
		return out;
	}
	
	public static int decode(byte[] out, int oi, byte[] in, int off, int len) throws IllegalArgumentException {
		return decode(out, oi, in, off, len, DecodeBytes);
	}
	
	public static int decode(byte[] out, int oi, byte[] in, int off, int len, byte[] filter) throws IllegalArgumentException {
		while(in[off + len - 1] == (byte) '=') len--;
		int i = off;
		int rl = off + (len & 0xFFFFFFFC);
		int curv = 0;
		while(i < rl) {
			int b1 = filter[(int) (in[i++] & 0xFF)];
			int b2 = filter[(int) (in[i++] & 0xFF)];
			int b3 = filter[(int) (in[i++] & 0xFF)];
			int b4 = filter[(int) (in[i++] & 0xFF)];
			if(b1 == 64 || b2 == 64 || b3 == 64 || b4 == 64) {
				throw new InvalidSyntaxException((i - 4), (i - 1));
			}
			
			curv  = (b1 << 18)
					| (b2 << 12)
					| (b3 << 6 )
					| (b4      );

			out[oi++] = (byte) ((curv >> 16) & 0xFF);
			out[oi++] = (byte) ((curv >> 8 ) & 0xFF);
			out[oi++] = (byte) ((curv      ) & 0xFF);
		}
		rl = (len & 0x03);
		if(rl > 0) {
			if(rl == 1) {
				throw new InvalidSyntaxException("Last unit does not have enough bytes!");
			}

			int b1 = filter[(int) (in[i++] & 0xFF)];
			int b2 = filter[(int) (in[i++] & 0xFF)];
			int b3;

			if(b1 == 64 || b2 == 64) {
				throw new InvalidSyntaxException((i - 2), (i - 1));
			}

			curv  = (b1 << 18);
			curv |= (b2 << 12);

			out[oi++] = (byte) ((curv >> 16) & 0xFF);


			if(rl == 3) {
				b3 = filter[in[i++]];
				if(b3 == 64) {
					throw new InvalidSyntaxException(i - 1);
				}

				curv |= (b3 << 6);
				out[oi++] = (byte) ((curv >> 8) & 0xFF);
			}
		}
		return oi;
	}
	
	
	
	
	/*
	 * Fast Decode (may return invalid data if invalid input)
	 */
	
	public static byte[] decodeFast(byte[] in) {
		return decodeFast(in, 0, in.length, DecodeBytes);
	}
	
	public static byte[] decodeFast(byte[] in, int off, int len) {
		return decodeFast(in, off, len, DecodeBytes);
	}
	
	public static byte[] decodeFast(byte[] in, int off, int len, byte[] filter) {
		while(in[off + len - 1] == (byte) '=') len--;
		int outsz = (len / 4) * 3;
		int add = len & 0x03;
		outsz = (add <= 1) ? outsz : outsz+add-1;
		byte[] out = new byte[outsz];
		decodeFast(out, 0, in, off, len, filter);
		return out;
	}
	
	public static int decodeFast(byte[] out, byte[] in) {
		return decodeFast(out, 0, in, 0, in.length, DecodeBytes);
	}
	
	public static int decodeFast(byte[] out, int oi, byte[] in, int off, int len) {
		return decodeFast(out, oi, in, off, len, DecodeBytes);
	}
	
	public static int decodeFast(byte[] out, int oi, byte[] in, int off, int len, byte[] filter) {
		while(in[off + len - 1] == (byte) '=') len--;
		int i = off;
		int rl = off + (len & 0xFFFFFFFC);
		int curv = 0;
		while(i < rl) {
			curv  = (filter[in[i++] & 0xFF] << 18)
					| (filter[in[i++] & 0xFF] << 12)
					| (filter[in[i++] & 0xFF] << 6 )
					| (filter[in[i++] & 0xFF]      );

			out[oi++] = (byte) ((curv >> 16) & 0xFF);
			out[oi++] = (byte) ((curv >> 8 ) & 0xFF);
			out[oi++] = (byte) ((curv      ) & 0xFF);
		}
		rl = (len & 0x03);
		if(rl > 0) {
			curv  = (filter[in[i++]] << 18);
			curv |= (filter[in[i++]] << 12);

			out[oi++] = (byte) ((curv >> 16) & 0xFF);

			if(rl == 3) {
				curv |= (filter[in[i++]] << 6);
				out[oi++] = (byte) ((curv >> 8) & 0xFF);
			}
		}
		return oi;
	}
	
	
	
	/*
	 * Encode
	 */
	
	public static byte[] encode(byte[] in) {
		return encode(in, 0, in.length, EncodeBytes);
	}
	
	public static byte[] encode(byte[] in, int off, int len) {
		return encode(in, off, len, EncodeBytes);
	}
	
	public static byte[] encode(byte[] in, int off, int len, byte[] filter) {
		byte[] out = new byte[((len + 2) / 3) * 4];
		encode(out, 0, in, off, len, filter);
		return out;
	}
	
	public static int encode(byte[] out, int oi, byte[] in, int off, int len) {
		return encode(out, oi, in, off, len, EncodeBytes);
	}
	
	public static int encode(byte[] out, int oi, byte[] in, int off, int len, byte[] filter) {
		int i = off;
		int rl = off + ((len / 3) * 3);
		int cur = 0;
		while(i < rl) {
			cur =	  (in[i++] & 0xFF) << 16
					| (in[i++] & 0xFF) << 8 
					| (in[i++] & 0xFF);
	
			out[oi++] = (byte) filter[((cur >> 18) & 0x3F)];
			out[oi++] = (byte) filter[((cur >> 12) & 0x3F)];
			out[oi++] = (byte) filter[((cur >> 6 ) & 0x3F)];
			out[oi++] = (byte) filter[((cur      ) & 0x3F)];
		}
		
		rl = len % 3;
		if(rl != 0) {
			cur = ((in[i++]) << 16);
	
			if(rl == 2) {
				cur |= ((in[i++]) << 8);
			}
			out[oi++] = (byte) filter[((cur >> 18) & 0x3F)];
			out[oi++] = (byte) filter[((cur >> 12) & 0x3F)];
	
			if(rl == 2) {
				out[oi++] = (byte) filter[((cur >> 6) & 0x3F)];
			} else {
				out[oi++] = '=';
			}
	
			out[oi++] = '=';
		}
		return oi;
	}
	
	public static char[] encodeToChars(byte[] in) {
		return encodeToChars(in, 0, in.length, EncodeChars);
	}
	
	public static char[] encodeToChars(byte[] in, int off, int len) {
		return encodeToChars(in, off, len, EncodeChars);
	}
	
	public static char[] encodeToChars(byte[] in, int off, int len, char[] filter) {
		char[] out = new char[((len + 2) / 3) * 4];
		encodeToChars(out, 0, in, off, len, filter);
		return out;
	}
	
	public static int encodeToChars(char[] out, int oi, byte[] in, int off, int len) {
		return encodeToChars(out, oi, in, off, len, EncodeChars);
	}
	
	public static int encodeToChars(char[] out, int oi, byte[] in, int off, int len, char[] filter) {
		int i = off;
		int rl = off + ((len / 3) * 3);
		int cur = 0;
		while(i < rl) {
			cur =	  (in[i++] & 0xFF) << 16
					| (in[i++] & 0xFF) << 8 
					| (in[i++] & 0xFF);
	
			out[oi++] = filter[((cur >> 18) & 0x3F)];
			out[oi++] = filter[((cur >> 12) & 0x3F)];
			out[oi++] = filter[((cur >> 6 ) & 0x3F)];
			out[oi++] = filter[((cur      ) & 0x3F)];
		}
		
		rl = len % 3;
		if(rl != 0) {
			cur = ((in[i++]) << 16);
	
			if(rl == 2) {
				cur |= ((in[i++]) << 8);
			}
			out[oi++] = filter[((cur >> 18) & 0x3F)];
			out[oi++] = filter[((cur >> 12) & 0x3F)];
	
			if(rl == 2) {
				out[oi++] = filter[((cur >> 6) & 0x3F)];
			} else {
				out[oi++] = '=';
			}
	
			out[oi++] = '=';
		}
		return oi;
	}
	
	
	/*
	 * Length
	 */
	
	public static int decodeLength(int len) {
		int outsz = (len / 4) * 3;
		int add = len & 0x03;
		return (add <= 1) ? outsz : outsz+add-1;
	}
	
	public static int encodeLength(int len) {
		return ((len + 2) / 3) * 4;
	}
	
	public static int decodeLength(byte[] in) {
		return decodeLength(in, 0, in.length);
	}
	
	public static int decodeLength(byte[] in, int off, int len) {
		while(in[off + len - 1] == (byte) '=') len--;
		int outsz = (len / 4) * 3;
		int add = len & 0x03;
		return (add <= 1) ? outsz : outsz+add-1;
	}
	
	public static int decodeLength(char[] in) {
		return decodeLength(in, 0, in.length);
	}
	
	public static int decodeLength(char[] in, int off, int len) {
		while(in[off + len - 1] == (byte) '=') len--;
		int outsz = (len / 4) * 3;
		int add = len & 0x03;
		return (add <= 1) ? outsz : outsz+add-1;
	}
	
	
}
