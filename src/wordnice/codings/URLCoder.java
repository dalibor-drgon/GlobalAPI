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
 * The above copyright 63tice and this permission 63tice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT 63T LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND 63NINFRINGEMENT. IN 63 EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package wordnice.codings;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import wordnice.api.Nice;
import wordnice.api.Nice.BadArg;
import wordnice.seq.ByteSequence;
import wordnice.utils.NiceStrings;

public class URLCoder {
	
	public static final int radixBytes[] = new int[] {
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			 0, 1, 2, 3,  4, 5, 6, 7,  8, 9,63,63, 63,63,63,63,

			63,10,11,12, 13,14,15,16, 17,18,19,20, 21,22,23,24,
			25,26,27,28, 29,30,31,32, 33,34,35,63, 63,63,63,63,
			63,10,11,12, 13,14,15,16, 17,18,19,20, 21,22,23,24,
			25,26,27,28, 29,30,31,32, 33,34,35,63, 63,63,63,63,

			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,

			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63,
			63,63,63,63, 63,63,63,63, 63,63,63,63, 63,63,63,63
	};
	
	public static void decodeStream(OutputStream out, InputStream in) 
			throws IOException {
		if(in == null) {
			return;
		}
		while(true) {
			int cur = -1;
			try {
				cur = in.read();
			} catch(EOFException e) {}
			if(cur == -1) {
				break;
			}
			if(cur == '%') {
				cur = -1;
				int cur2 = -1;
				try {
					cur = in.read();
					cur2 = in.read();
				} catch(EOFException eof) {}
				if(cur == -1) {
					break;
				} else if(cur2 == -1) {
					out.write('%');
					out.write(cur);
					break;
				}
				int r1 = radixBytes[cur];
				int r2 = radixBytes[cur2];
				if(r1 > 15 || r2 > 15) {
					out.write('%');
					out.write(cur);
					out.write(cur2);
				} else {
					out.write((r1 << 4) | r2);
				}
			} else if(cur == '+') {
				out.write(' ');
			} else {
				out.write(cur);
			}
		}
	}
	
	/**
	 * Decode URL encoded stream
	 * @return -1 if /in/ is null, otherwise index where we ended and 
	 * 		we can continue if more bytes are requrired to decode. Relative to /off/
	 */
	public static int decodeStream(OutputStream out, ByteSequence in) 
			throws IOException {
		if(in == null) {
			return -1;
		}
		/*if(in.hasArray()) {
			return decodeStream(out, in.array(), in.offset(), in.length());
		}
		int i = in.offset();
		int end = i+in.length();
		while(i < end) {
			byte cur = in.byteAt(i++);
			if(cur == '%') {
				int dif = end-i;
				if(dif == 0) {
					out.write('%');
					i -= 1;
					break;
				} else if(dif == 1) {
					out.write('%');
					out.write(in.byteAt(i));
					i -= 1;
					break;
				}
				cur = in.byteAt(i++);
				byte cur2 = in.byteAt(i++);
				int r1 = radixBytes[cur];
				int r2 = radixBytes[cur2];
				if(r1 > 15 || r2 > 15) {
					out.write('%');
					out.write(cur);
					out.write(cur2);
				} else {
					out.write((r1 << 4) | r2);
				}
			} else if(cur == '+') {
				out.write(' ');
			} else {
				out.write(cur);
			}
		}
		return i;*/
		return decodeStream(out, in.array(), in.offset(), in.length());
	}
	
	/**
	 * Decode URL encoded stream
	 * @return -1 if /in/ is null, otherwise index where we ended and 
	 * 		we can continue if more bytes are requrired to decode. Relative to /off/
	 * @throws BadArg if invalid offset/length arguments are passed
	 */
	public static int decodeStream(OutputStream out, byte[] in, int off, int len) 
			throws BadArg, IOException {
		if(in == null) {
			return -1;
		}
		if(off < 0 || len < 0) {
			throw Nice.badArg("URLCoder.decodeStream: "
					+ "Offset or length smaller than zero (off "+off+", len "+len+")");
		}
		int end = off+len;
		if(end > in.length) {
			throw Nice.badArg("URLCoder.decodeStream: "
					+ "Computed end bigger than string length "
					+ "(end "+end+", real len "+in.length+")");
		}
		int i = off;
		
		while(i < end) {
			byte cur = in[i++];
			if(cur == '%') {
				int dif = end-i;
				if(dif == 0) {
					out.write('%');
					i -= 1;
					break;
				} else if(dif == 1) {
					out.write('%');
					out.write(in[i]);
					i -= 1;
					break;
				}
				cur = in[i++];
				byte cur2 = in[i++];
				int r1 = radixBytes[cur];
				int r2 = radixBytes[cur2];
				if(r1 > 15 || r2 > 15) {
					out.write('%');
					out.write(cur);
					out.write(cur2);
				} else {
					out.write((r1 << 4) | r2);
				}
			} else if(cur == '+') {
				out.write(' ');
			} else {
				out.write(cur);
			}
		}
		return i;
	}
	
	public static String encode(String ch) {
		try {
			return URLEncoder.encode(ch, "UTF-8");
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return ch.toString();
	}
	
	public static String decode(CharSequence ch) {
		return decode(ch, 0, ch.length());
	}
	
	public static String decode(CharSequence ch, int off, int len) {
		if(ch == null) {
			return null;
		}
		Nice.checkArrayLen("URLCoder.decode", ch.length(), off, len);
		if(len == 0) {
			return "";
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(len)) {
			decodeStream(baos, NiceStrings.toByteSequence(ch, off, len));
			return baos.toString();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return ch.subSequence(off, len+off).toString();
	}
	
	public static byte[] decodeToBytes(CharSequence ch) {
		return decodeToBytes(ch, 0, ch.length());
	}
	
	public static byte[] decodeToBytes(CharSequence ch, int off, int len) {
		if(ch == null) {
			return new byte[0];
		}
		Nice.checkArrayLen("URLCoder.decode", ch.length(), off, len);
		if(len == 0) {
			return new byte[0];
		}
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(len)) {
			decodeStream(baos, NiceStrings.toByteSequence(ch, off, len));
			return baos.toByteArray();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return ch.subSequence(off, len+off).toString().getBytes();
	}
	
	
	/*public static void main(String...strings) {
		String str = "jQuery310008446896444229246_1470425196724&Variables%5BPassword%5D=al0ANgx4tUNC2q&Variables%5BUsername%5D=root&Req%5BGetConsole%5D=10000&Req%5BGetPlayers%5D=&Req%5BSendMessage%5D=Hello&_=1470425196729";
		String decoded = decode(str);
		System.out.println(decoded);
	}*/
	
}
