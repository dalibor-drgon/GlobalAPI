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

import java.nio.charset.Charset;
import java.util.Arrays;

import wordnice.api.Nice;
import wordnice.codings.ASCII;

public class ByteArraySequence implements ByteSequence {
	
	protected static final byte[] EMPTY_ARR = new byte[0];
	protected static final ByteArraySequence EMPTY_SEQ = new ByteArraySequence(EMPTY_ARR, 0, 0);
	
	public static ByteArraySequence empty() {
		return EMPTY_SEQ;
	}

	protected byte[] ch;
	protected int off = 0;
	protected int len;
	
	public ByteArraySequence(byte[] ch) {
		if(ch == null) ch = EMPTY_ARR;
		this.ch = ch;
		this.off = 0;
		this.len = ch.length;
	}
	
	public ByteArraySequence(byte[] ch, int off, int len) {
		if(ch == null) ch = EMPTY_ARR;
		Nice.checkBounds(ch, off, len);
		this.ch = ch;
		this.off = off;
		this.len = len;
	}
	
	@Override
	public int length() {
		return this.len;
	}
	
	@Override
	public int offset() {
		return this.off;
	}
	
	@Override
	public int offset(int r) {
		return this.off + r;
	}
	
	@Override
	public byte[] array() {
		return this.ch;
	}
	
	@Override
	public byte[] newArray() {
		return Arrays.copyOfRange(ch, off, len+off);
	}

	@Override
	public byte byteAt(int index) {
		if(index < 0 || index >= this.len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return this.ch[this.off + index];
	}

	@Override
	public ByteArraySequence subSequence(int start, int end) {
		int len = end-start;
		int suboff = off+start;
		if(start < 0 || start > this.len
				|| end < 0 || end > this.len 
				|| suboff+len > ch.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return new ByteArraySequence(ch, suboff, len);
	}
	
	@Override
	public String toString() {
		return new String(ch, off, len);
	}
	
	public String toString(Charset c) {
		return new String(ch, off, len, c);
	}

	@Override
	public int indexOf(byte c) {
		return indexOf(c, 0);
	}

	@Override
	public int indexOf(byte c, int off) {
		if(off > this.len) {
			throw Nice.bounds(off);
		}
		int i = this.off + off;
		int end = this.off + this.len;
		for(;i < end; i++) {
			if(this.ch[i] == c) {
				return i-this.off;
			}
		}
		return -1;
	}

	@Override
	public int indexOf(ByteSequence c) {
		return indexOf(c.array(), c.offset(), c.length(), 0);
	}

	@Override
	public int indexOf(ByteSequence c, int off) {
		return indexOf(c.array(), c.offset(), c.length(), off);
	}
	
	public int indexOf(byte[] arr, int aoff, int alen, int off) {
		Nice.checkBounds(arr, aoff, alen);
		if(alen == 1) {
			return indexOf(arr[aoff], off);
		}
		if(off > this.len) {
			throw Nice.bounds(off);
		}
		int from = this.off+off;
		int i = ASCII.indexOf(this.ch, from, this.len-off, arr, aoff, alen);
		return (i == -1) ? -1 : i-from;
	}

	public static void main(String...strings) {
		String str1 = "Hello m8sss ima rekt u all <3>>><3<<<333";
		ByteSequence bs = new ByteArraySequence(str1.getBytes());
		System.out.println("1: "+bs.indexOf(new ByteArraySequence("Hello".getBytes())));
		System.out.println("1: "+bs.indexOf(new ByteArraySequence("Helloh".getBytes())));
		System.out.println("1: "+bs.indexOf(new ByteArraySequence("Hello ".getBytes())));
		System.out.println("1: "+bs.indexOf(new ByteArraySequence(">><".getBytes())));
		System.out.println(bs.substring(2));
		System.out.println(bs.substringNew(2));
		System.out.println(bs.substring(2,6));
		System.out.println(bs.substringNew(2,6));
		System.out.println(bs.substring(bs.length()));
		System.out.println(bs.substringNew(bs.length()));
	}

	@Override
	public ByteSequence substring(int from) {
		return subSequence(from, this.len);
	}

	@Override
	public ByteSequence substring(int from, int to) {
		return subSequence(from, to);
	}

	@Override
	public ByteSequence substringNew(int from) {
		return substringNew(from, this.len);
	}

	@Override
	public ByteSequence substringNew(int start, int end) {
		if(start < 0 || start > this.len
				|| end < 0 || end > this.len 
				|| off+end > ch.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return new ByteArraySequence(Arrays.copyOfRange(ch, off+start, off+end));
	}
	
}
