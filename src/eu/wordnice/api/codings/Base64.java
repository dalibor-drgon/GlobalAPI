/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 
 
 
 This material was derived from TLibs C library.
 https://github.com/wordnice/TLibs
 */

package eu.wordnice.api.codings;

public class Base64 {
	
	byte[] TRN_BASE64_CHARS = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();

	byte[] TRN_BASE64_BYTES = new byte[] {
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
	
	public byte[] decode(byte[] in, int off, int len) throws Exception {
		while(in[off + len - 1] == (byte) '=') {
			len--;
		}
		byte[] out = new byte[((len + 3) / 4) * 3];
		int i = off;
		int rl = (off + len) & 0xFFFFFFFC;
		int oi = 0;
		int curv = 0;
		for(; i < rl;) {
			byte b1 = TRN_BASE64_BYTES[in[i++]];
			byte b2 = TRN_BASE64_BYTES[in[i++]];
			byte b3 = TRN_BASE64_BYTES[in[i++]];
			byte b4 = TRN_BASE64_BYTES[in[i++]];
			if(b1 == 64 || b2 == 64 || b3 == 64 || b4 == 64) {
				throw new Exception("Unknow character near indexes " + (i - 4) + " - " + (i - 1));
			}
			
			curv  = (b1 << 18);
			curv |= (b2 << 12);
			curv |= (b3 << 6 );
			curv |= (b4      );

			out[oi++] = (byte) ((curv >> 16) & 0xFF);
			out[oi++] = (byte) ((curv >> 8 ) & 0xFF);
			out[oi++] = (byte) ((curv      ) & 0xFF);
		}
		rl = ((off + len) & 0x03);
		if(rl > 0) {
			if(rl == 1) {
				throw new Exception("Unknown base64 strings length");
			}

			byte b1 = TRN_BASE64_BYTES[in[i++]];
			byte b2 = TRN_BASE64_BYTES[in[i++]];
			byte b3;

			if(b1 == 64 || b2 == 64) {
				throw new Exception("Unknow character at the end after " + (i - 2));
			}

			curv  = (b1 << 18);
			curv |= (b2 << 12);

			out[oi++] = (byte) ((curv >> 16) & 0xFF);


			if(rl == 3) {
				b3 = TRN_BASE64_BYTES[in[i++]];
				if(b3 == 64) {
					throw new Exception("Unknow character at the end at index " + (i - 1));
				}

				curv |= (b3 << 6);
				out[oi++] = (byte) ((curv >> 8) & 0xFF);
			}
		}
		return out;
	}
	
	
}
