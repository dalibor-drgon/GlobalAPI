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

import wordnice.api.Nice;

public class CharArraySequence implements CharSequence {
	
	protected static final char[] EMPTY_ARR = new char[0];
	protected static final CharArraySequence EMPTY_SEQ = new CharArraySequence(EMPTY_ARR, 0, 0);
	
	public static CharArraySequence empty() {
		return EMPTY_SEQ;
	}

	protected char[] value;
	protected int off = 0;
	protected int count;
	
	protected CharArraySequence() {}
	
	public CharArraySequence(char[] ch) {
		if(ch == null) ch = EMPTY_ARR;
		this.value = ch;
		this.count = ch.length;
	}
	
	public CharArraySequence(char[] ch, int off, int len) {
		if(ch == null) ch = EMPTY_ARR;
		Nice.checkBounds(ch, off, len);
		this.value = ch;
		this.off = off;
		this.count = len;
	}
	
	@Override
	public int length() {
		return this.count;
	}
	
	public int offset() {
		return this.off;
	}
	
	public int offset(int r) {
		return this.off + r;
	}
	
	public char[] array() {
		return this.value;
	}

	@Override
	public char charAt(int index) {
		if(index < 0 || index >= this.count) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return this.value[this.off + index];
	}

	@Override
	public CharArraySequence subSequence(int start, int end) {
		int len = end-start;
		if(len < 0 || start < 0 || start >= this.count || end < 0 || end >= this.count) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return new CharArraySequence(value, off+start, len);
	}
	
	@Override
	public String toString() {
		return new String(value, off, count);
	}

}
