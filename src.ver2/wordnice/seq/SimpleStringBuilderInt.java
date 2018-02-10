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

package wordnice.seq;

import wordnice.api.Nice;

public class SimpleStringBuilderInt
extends AbstractSimpleStringBuilder {
	
	public SimpleStringBuilderInt() {
		this(Nice.builderSize);
	}
	
	public SimpleStringBuilderInt(int size) {
		if(size < 0) throw new IllegalArgumentException("Size ("+size+") < 0");
		if(size < Nice.MinBuilderSize) size = Nice.MinBuilderSize;
		this.currentBuffer = new char[size];
		this.addBufferToList(currentBuffer);
	}
	
	protected static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /** The list of buffers, which grows and never reduces. */
    protected char[][] buffers = new char[32][];
    protected int buffersSize = 0;
    /** The index of the current buffer. */
    protected int currentBufferIndex;
    /** The total count of bytes in all the filled buffers. */
    protected int filledBufferSum;
    /** The current buffer. */
    protected char[] currentBuffer;
    /** current size **/
    protected int count = 0;
	
    
    protected void addBufferToList(char[] buf) {
    	currentBufferIndex++;
        if(buffers.length == buffersSize) {
        	char[][] nevbuffers = new char[buffers.length << 1][];
        	System.arraycopy(buffers, 0, nevbuffers, 0, buffers.length);
        	buffers = nevbuffers;
        }
        buffers[buffersSize++] = buf;
    }

	@Override
	public SimpleStringBuilder append(char ch) {
		ensureSpace(1);
		currentBuffer[(count - filledBufferSum)] = ch;
		count++;
		return this;
	}

	@Override
	public SimpleStringBuilder append(char[] arr, int off, int len) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringBuilder append(String str, int off, int end) {
		int len = end-off;
		Nice.checkBounds(str, off, len);
		
		return this;
	}

	@Override
	public SimpleStringBuilder append(CharSequence s, int off, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleStringBuilder ensureSpace(int space) {
		return this;
	}

	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int capacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char[] getBuffer() {
		return null;
	}

	@Override
	public char[] getOrCreateBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getBufferOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

}
