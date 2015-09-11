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

package eu.wordnice.api.codings;

public class Base64 {
	
	/*
	 * Tables
	 * * Characters (Bytes) returned on encode()
	 * * Bytes returned on decode()
	 */
	
	public static byte[] TRN_BASE64_CHARS = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();

	public static byte[] TRN_BASE64_BYTES = new byte[] {
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,62, 64,64,64,63,
		52,53,54,55, 56,57,58,59, 60,61,64,64, 64,64,64,64,

		64, 0, 1, 2,  3, 4, 5, 6,  7, 8, 9,10, 11,12,13,14,
		15,16,17,18, 19,20,21,22, 23,24,25,64, 64,64,64,64,
		64,26,27,28, 29,30,31,32, 33,34,35,36, 37,38,39,40,
		41,42,43,44, 45,46,47,48, 49,50,51,64, 64,64,64,64,

		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,

		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64,
		64,64,64,64, 64,64,64,64, 64,64,64,64, 64,64,64,64
	};
	
	
	
	
	/*
	 * Decode
	 */
	
	public static byte[] decode(byte[] in) throws InvalidSyntaxException {
		return Base64.decode(in, 0, in.length, Base64.TRN_BASE64_BYTES);
	}
	
	public static byte[] decode(byte[] in, int off, int len, byte[] filter) throws InvalidSyntaxException {
		while(in[off + len - 1] == (byte) '=') {
			len--;
		}
		int outsz = (len / 4) * 3;
		switch(len & 0x03) {
			case 3:
				outsz++;
			case 2:
				outsz++;
		}
		byte[] out = new byte[outsz];
		int i = off;
		int rl = off + (len & 0xFFFFFFFC);
		int oi = 0;
		int curv = 0;
		while(i < rl) {
			byte b1 = filter[(int) (in[i++] & 0xFF)];
			byte b2 = filter[(int) (in[i++] & 0xFF)];
			byte b3 = filter[(int) (in[i++] & 0xFF)];
			byte b4 = filter[(int) (in[i++] & 0xFF)];
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
				throw new InvalidSyntaxException("Unknown base64 strings length");
			}

			byte b1 = filter[(int) (in[i++] & 0xFF)];
			byte b2 = filter[(int) (in[i++] & 0xFF)];
			byte b3;

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
		return out;
	}
	
	public static int decode(byte[] out, byte[] in, int off, int len, byte[] filter) throws InvalidSyntaxException {
		while(in[off + len - 1] == (byte) '=') {
			len--;
		}
		int i = off;
		int rl = off + (len & 0xFFFFFFFC);
		int oi = 0;
		int curv = 0;
		while(i < rl) {
			byte b1 = filter[(int) (in[i++] & 0xFF)];
			byte b2 = filter[(int) (in[i++] & 0xFF)];
			byte b3 = filter[(int) (in[i++] & 0xFF)];
			byte b4 = filter[(int) (in[i++] & 0xFF)];
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
				throw new InvalidSyntaxException("Unknown base64 strings length");
			}

			byte b1 = filter[(int) (in[i++] & 0xFF)];
			byte b2 = filter[(int) (in[i++] & 0xFF)];
			byte b3;

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
	 * Fast Decode (could return invalid data)
	 */
	
	public static byte[] decodeFast(byte[] in) {
		return Base64.decodeFast(in, 0, in.length, Base64.TRN_BASE64_BYTES);
	}
	
	public static byte[] decodeFast(byte[] in, int off, int len, byte[] filter) {
		while(in[off + len - 1] == (byte) '=') {
			len--;
		}
		int outsz = (len / 4) * 3;
		switch(len & 0x03) {
			case 3:
				outsz++;
			case 2:
				outsz++;
		}
		byte[] out = new byte[outsz];
		int i = off;
		int rl = off + (len & 0xFFFFFFFC);
		int oi = 0;
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
		return out;
	}
	
	public static int decodeFast(byte[]out, byte[] in, int off, int len, byte[] filter) {
		while(in[off + len - 1] == (byte) '=') {
			len--;
		}
		int i = off;
		int rl = off + (len & 0xFFFFFFFC);
		int oi = 0;
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
		return Base64.encode(in, 0, in.length, Base64.TRN_BASE64_CHARS);
	}
	
	public static byte[] encode(byte[] in, int off, int len, byte[] filter) {
		byte[] out = new byte[((len + 2) / 3) * 4];
		int i = off;
		int rl = off + ((len / 3) * 3);
		int cur = 0;
		int oi = 0;
		while(i < rl) {
			cur =	  ((in[i++]) << 16)
					| ((in[i++]) << 8 )
					| ((in[i++])      );
	
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
		return out;
	}
	
	public static int encode(byte[] out, byte[] in, int off, int len, byte[] filter) {
		int i = off;
		int rl = off + ((len / 3) * 3);
		int cur = 0;
		int oi = 0;
		while(i < rl) {
			cur =	  ((in[i++]) << 16)
					| ((in[i++]) << 8 )
					| ((in[i++])      );
	
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
	
	
	/*
	 * Length
	 */
	
	public static int decodeLength(int len) {
		int outsz = (len / 4) * 3;
		switch(len & 0x03) {
			case 3:
				outsz++;
			case 2:
				outsz++;
		}
		return outsz;
	}
	
	public static int encodeLength(int len) {
		return ((len + 2) / 3) * 4;
	}
	
	public static int decodeLength(byte[] in, int off, int len) {
		while(in[off + len - 1] == (byte) '=') {
			len--;
		}
		int outsz = (len / 4) * 3;
		switch(len & 0x03) {
			case 3:
				outsz++;
			case 2:
				outsz++;
		}
		return outsz;
	}
	
	
}
