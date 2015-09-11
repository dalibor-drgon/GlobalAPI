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
 */

package eu.wordnice.api;

public class ByteString {
	
	public static boolean equals(byte[] str1, byte[] str2) {
		return ByteString.equals(str1, 0, str1.length, str2, 0, str2.length);
	}
	
	public static boolean equals(byte[] str1, byte[] str2, int len) {
		return ByteString.equals(str1, 0, len, str2, 0, len);
	}
	
	public static boolean equals(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len != str2_len) {
			return false;
		}
		int i1 = str1_off;
		int i2 = str2_off;
		for(; i1 < str1_len; i1++, i2++) {
			if(str1[i1] != str2[i2]) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean equalsIgnoreCase(byte[] str1, byte[] str2) {
		return ByteString.equalsIgnoreCase(str1, 0, str1.length, str2, 0, str2.length);
	}
	
	public static boolean equalsIgnoreCase(byte[] str1, byte[] str2, int len) {
		return ByteString.equalsIgnoreCase(str1, 0, len, str2, 0, len);
	}
	
	public static boolean equalsIgnoreCase(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len != str2_len) {
			return false;
		}
		str1_len += str1_off;
		str2_len += str2_off;
		int i1 = str1_off;
		int i2 = str2_off;
		for(; i1 < str1_len; i1++, i2++) {
			if(ByteChar.toLower(str1[i1]) != ByteChar.toLower(str2[i2])) {
				return false;
			}
		}
		return true;
	}
	
	
	
	public static int indexOf(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len < str2_len) {
			return -1;
		}
		if(str1_len == str2_len) {
			return ByteString.equals(str1, str1_off, str1_len, str2, str2_off, str2_len)
					? str1_off : -1;
		}
		str1_len += str1_off;
		str2_len += str2_off;
		int i2 = str2_off;
		int maxi = str1_len - str2_len + str1_off;
		int i = str1_off;
		for(; i <= maxi; i++, i2++) {
			if(ByteString.equals(str1, i, str2_len, str2, i2, str2_len)) {
				return i;
			}
		}
		return -1;
	}
	
	public static int indexOfIgnoreCase(byte[] str1, int str1_off, int str1_len,
			byte[] str2, int str2_off, int str2_len) {
		if(str1_len < str2_len) {
			return -1;
		}
		if(str1_len == str2_len) {
			return ByteString.equalsIgnoreCase(str1, str1_off, str1_len, str2, str2_off, str2_len)
					? str1_off : -1;
		}
		str1_len += str1_off;
		str2_len += str2_off;
		int i2 = str2_off;
		int maxi = str1_len - str2_len + str1_off;
		int i = str1_off;
		for(; i <= maxi; i++, i2++) {
			if(ByteString.equalsIgnoreCase(str1, i, str2_len, str2, i2, str2_len)) {
				return i;
			}
		}
		return -1;
	}
	
	public static Val.TwoVal<Integer, Integer> indexOf(byte[] str1, int str1_off, int str1_len,
			int minlen, Handler.FourVoidHandler<Val.TwoVal<Boolean, Integer>, byte[], Integer, Integer> handl) {
		if(str1_len < minlen) {
			return null;
		}
		Val.TwoVal<Boolean, Integer> check = new Val.TwoVal<Boolean, Integer>();
		int maxi = str1_len - minlen + str1_off;
		int i = str1_off;
		for(; i <= maxi; i++) {
			handl.handle(check, str1, i, (maxi + minlen - i));
			if(check.one) {
				return new Val.TwoVal<Integer, Integer>(i, check.two);
			}
		}
		return null;
	}
	
}
