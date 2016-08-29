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

public interface ByteSequence {

	public int length();
	public int offset();
	public int offset(int r);
	public byte[] array();
	public byte[] newArray();

	public byte byteAt(int index);
	public ByteSequence subSequence(int start, int end);
	@Override
	public String toString();
	public String toString(Charset c);
	
	public int indexOf(byte c);
	public int indexOf(byte c, int off);
	public int indexOf(ByteSequence c);
	public int indexOf(ByteSequence c, int off);
	
	public ByteSequence substring(int from);
	public ByteSequence substring(int from, int to);
	public ByteSequence substringNew(int from);
	public ByteSequence substringNew(int from, int to);
	
}
