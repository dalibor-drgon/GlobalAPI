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

import java.io.IOException;
import java.util.Arrays;

import wordnice.api.Api;
import wordnice.utils.CharArraySequence;
import wordnice.utils.NiceStringUtils;

public class EvictingStringBuilder
extends CharArraySequence
implements Appendable, CharSequence {
	
	int minimumRebuild = 0;
	
	public EvictingStringBuilder(int size) {
		this(size, rebuildSize(size));
	}
	
	public EvictingStringBuilder(int size, int minimumRebuild) {
		this.allocateBuffer(size);
		this.setMinimumRebuildSize(minimumRebuild);
	}
	
	public EvictingStringBuilder(char[] buff, int count) {
		this(buff, count, rebuildSize(buff.length));
	}
	
	public EvictingStringBuilder(char[] buff, int count, int minimumRebuild) {
		this.setBuffer(buff, count);
		this.setMinimumRebuildSize(minimumRebuild);
	}
	
	public boolean endsWith(char c) {
		if(this.count == 0) return true;
		return this.value[this.count-1] == c;
	}
	
	public boolean endsWithNewline() {
		if(this.count == 0) return true;
		return this.value[this.count-1] == '\n';
	}
	
	public int getMinimumRebuildSize() {
		return this.minimumRebuild;
	}
	
	public EvictingStringBuilder setMinimumRebuildSize(int size) {
		if(size < 1) {
			size = 1; //just append las
		} else if(size > this.value.length) {
			size = this.value.length; //remove whole buffer!
		}
		this.minimumRebuild = size;
		return this;
	}
	
	public void allocateBuffer(int size) {
		if(size <= 0) {
			throw new IllegalArgumentException("Given size ("+size+") is negative or zero!");
		}
		this.value = new char[size];
		this.value[0] = '\n';
		this.count = 1;
	}
	
	public void setBuffer(char[] buffer, int count) {
		if(buffer == null || buffer.length == 0) { //min 1
			throw new IllegalArgumentException("Buffer is null or has zero length!");
		}
		if(count > buffer.length) {
			throw new IllegalArgumentException("Given count of elemens ("+count+") > size or buffer ("+buffer.length+"!");
		}
		this.value = buffer;
		this.count = count;
	}
	
	//for overriding
	protected void loweredOffsetBy(int by) {}
	
	public int offset() {
		return 0;
	}
	
	public int offset(int r) {
		return r;
	}
	
	public char[] getBuffer() {
		return this.value;
	}
	
	public char[] getNewBuffer() {
		return Arrays.copyOf(this.value, this.count);
	}
	
	public String getConsole() {
		return new String(value, 0, this.count);
	}
	
	public String substringLast(int size) {
		if(size == 0) return "";
		int count = this.count - 1;
		while(this.value[count] == '\n') {
			count--;
		}
		count++;
		return substringLast(size, count);
	}
		
	public String substringLast(int size, int end) {
		if(size == 0) return "";
		int i = 0;
		if(size > 0 && size < end) {
			i = end-size;
		}
		return this.substring(i, end);
	}
	
	
	public String substring(int from, int to) {
		return new String(value, from, to-from);
	}
	
	public String substring(int from) {
		int count = this.count - 1;
		while(this.value[count] == '\n') {
			count--;
		}
		count++;
		return substring(from, count);
	}
	
	public String substringLastFromNewline() {
		return this.substringLastFrom(0, '\n');
	}
	
	public String substringLastFromNewline(int size) {
		return this.substringLastFrom(size, '\n');
	}
	
	public String substringLastFrom(char c) {
		return this.substringLastFrom(0, c);
	}
	
	public String substringLastFrom(int size, char c) {
		if(size == 0) return "";
		int i = 0;
		int count = this.count - 1;
		while(this.value[count] == c) {
			count--;
		}
		count++;
		if(size > 0 && size < count) {
			i = count-size;
		}
		for(; i < count; i++) {
			if(value[i] == c) {
				return substring(i+1, count);
			}
		}
		return substringLast(size, count);
	}
	
	
	/**
	 * Without allocating
	 */
	
	public CharArraySequence subSequenceLast(int size) {
		if(size == 0) return CharArraySequence.empty();
		int count = this.count - 1;
		while(this.value[count] == '\n') {
			count--;
		}
		count++;
		return subSequenceLast(size, count);
	}
		
	public CharArraySequence subSequenceLast(int size, int end) {
		if(size == 0) return CharArraySequence.empty();
		int i = 0;
		if(size > 0 && size < end) {
			i = end-size;
		}
		return this.subSequence(i, end);
	}
	
	
	public CharArraySequence subSequence(int from, int to) {
		return new CharArraySequence(value, from, to-from);
	}
	
	public CharArraySequence subSequence(int from) {
		int count = this.count - 1;
		while(this.value[count] == '\n') {
			count--;
		}
		count++;
		return subSequence(from, count);
	}
	
	public CharArraySequence subSequenceLastFromNewline() {
		return this.subSequenceLastFrom(0, '\n');
	}
	
	public CharArraySequence subSequenceLastFromNewline(int size) {
		return this.subSequenceLastFrom(size, '\n');
	}
	
	public CharArraySequence subSequenceLastFrom(char c) {
		return this.subSequenceLastFrom(0, c);
	}
	
	public CharArraySequence subSequenceLastFrom(int size, char c) {
		if(size == 0) return CharArraySequence.empty();
		int i = 0;
		int count = this.count - 1;
		while(this.value[count] == c) {
			count--;
		}
		count++;
		if(size > 0 && size < count) { //if size>=count, i = 0
			i = count-size;
		}
		for(; i < count; i++) {
			if(value[i] == c) {
				return subSequence(i+1, count);
			}
		}
		return subSequenceLast(size, count);
	}
	
	/**
	 * Utils
	 */
	
	protected int repairStart(int start, int len) {
		if(len > this.value.length) {
			return this.value.length-len+start;
		}
		return start;
	}
	
	protected int repairLength(int len) {
		if(len > this.value.length) {
			return this.value.length;
		}
		return len;
	}
	
	public void ensureCapacity(int len) {
		if(len >= (this.value.length-this.minimumRebuild)) {
			loweredOffsetBy(len);
			this.count = 0;
			return;
		}
		int free = this.value.length-this.count;
		/**
		 * Free 50 = 250-200
		 * 
		 * Min 100
		 * Len 20
		 */
		if(len > free) { //Rebuild!
			int stepDown = Math.max(this.minimumRebuild, len)-free;
			loweredOffsetBy(stepDown);
			this.count -= stepDown;
			System.arraycopy(this.value, stepDown, this.value, 0, this.count);
		}
	}
	
	protected void copyArray(CharSequence ch, int start, int len) {
		if(len == 0) return;
		int i = start;
		int end = start+len;
		for(; i < end; i++) {
			this.value[this.count++] = ch.charAt(i);
		}
	}
	
	protected void copyArray(char[] ch, int start, int len) {
		if(len == 0) return;
		System.arraycopy(ch, start, this.value, this.count, len);
		this.count += len;
	}

	//Print
	public EvictingStringBuilder print(byte[] csq) {
		return print(csq, 0, csq.length);
	}
	
	public EvictingStringBuilder print(byte[] csq, int start, int len) {
		Api.checkBounds(csq, start, len);
		return this.print(NiceStringUtils.toCharSequence(csq, start, len));
	}
	
	public EvictingStringBuilder print(ByteSequence bsq) {
		return print(bsq, 0, bsq.length());
	}
	
	public EvictingStringBuilder print(ByteSequence bsq, int start, int len) {
		Api.checkBounds(bsq, start, len);
		return this.print(NiceStringUtils.toCharSequence(bsq.array(), bsq.offset()+start, len));
	}
	
	public EvictingStringBuilder print(char[] csq) {
		return print(csq, 0, csq.length);
	}
	
	public EvictingStringBuilder print(char[] csq, int start, int len) {
		Api.checkBounds(csq, start, len);
		start = repairStart(start, len);
		len = repairLength(len);
		ensureCapacity(len);
		copyArray(csq, start, len);
		return this;
	}
	
	public EvictingStringBuilder print(CharSequence csq) {
		return print(csq, 0, csq.length());
	}
	
	public EvictingStringBuilder print(CharSequence csq, int start, int len) {
		Api.checkBounds(csq, start, len);
		if(csq instanceof CharArraySequence) {
			CharArraySequence cas = (CharArraySequence) csq;
			return print(cas.array(), cas.offset()+start, len);
		}
		start = repairStart(start, len);
		len = repairLength(len);
		ensureCapacity(len);
		copyArray(csq, start, len);
		return this;
	}
	
	public EvictingStringBuilder print(char c) {
		this.ensureCapacity(1);
		this.value[this.count++] = c;
		return this;
	}
	
	//println
	public EvictingStringBuilder println() {
		this.ensureCapacity(1);
		this.value[this.count++] = '\n';
		return this;
	}
	
	public EvictingStringBuilder println(char c) {
		this.ensureCapacity(2);
		if(this.value.length == 1) {
			this.value[this.count++] = '\n';
		} else {
			this.value[this.count++] = c;
			this.value[this.count++] = '\n';
		}
		return this;
	}
	
	public EvictingStringBuilder println(char[] csq) {
		return println(csq, 0, csq.length);
	}
	
	public EvictingStringBuilder println(char[] csq, int start, int len) {
		Api.checkBounds(csq, start, len);
		len++;
		start = repairStart(start, len);
		len = repairLength(len);
		ensureCapacity(len);
		len--;
		copyArray(csq, start, len);
		this.value[this.count++] = '\n';
		return this;
	}
	
	public EvictingStringBuilder println(CharSequence csq) {
		return println(csq, 0, csq.length());
	}
	
	public EvictingStringBuilder println(CharSequence csq, int start, int len) {
		Api.checkBounds(csq, start, len);
		if(csq instanceof CharArraySequence) {
			CharArraySequence cas = (CharArraySequence) csq;
			return println(cas.array(), cas.offset()+start, len);
		}
		len++;
		start = repairStart(start, len);
		len = repairLength(len);
		ensureCapacity(len);
		len--;
		copyArray(csq, start, len);
		this.value[this.count++] = '\n';
		return this;
	}
	
	public EvictingStringBuilder println(byte[] csq) {
		return println(csq, 0, csq.length);
	}
	
	public EvictingStringBuilder println(byte[] csq, int start, int len) {
		Api.checkBounds(csq, start, len);
		return this.println(NiceStringUtils.toCharSequence(csq, start, len));
	}
	
	public EvictingStringBuilder println(ByteSequence bsq) {
		return println(bsq, 0, bsq.length());
	}
	
	public EvictingStringBuilder println(ByteSequence bsq, int start, int len) {
		Api.checkBounds(bsq, start, len);
		return this.println(NiceStringUtils.toCharSequence(bsq.array(), bsq.offset()+start, len));
	}

	
	//test
	public static void main(String...strings) {
		EvictingStringBuilder ecb = new EvictingStringBuilder(350, 200);
		ecb.println("hellohellohellohellohellohellohellohellohello");
		ecb.println("hello one m8zhello one m8zhello one m8zhello one m8zhello one m8z");
		ecb.println("hello m8shello m8shello m8shello m8shello m8shello m8shello m8shello m8shello m8s");
		ecb.print("he23232323llohellohellohellohellohellohellohellohello");
		ecb.println("hellohellohellohellohellohellohellohellohello");
		ecb.println("hello one m8zhello one m8zhello one m8zhello one m8zhello one m8z");
		String rand = NiceStringUtils.genString(250);
		ecb.println(rand);
		System.out.println(ecb.substringLast(250).length() + " // " + ecb.substringLast(250).equals(rand));
		//ecb.println("hellohellohellohellohellohellohellohellohello");
		ecb.print("hello three ");
		ecb.print("m8ss");
		
		System.out.println("1. "+ecb.getConsole());
		System.out.println("2. "+ecb.substring('\n'));
		System.out.println("3. "+ecb.substringLastFromNewline(17));
		System.out.println("4. "+ecb.substringLastFromNewline(14));
		System.out.println("5. "+ecb.substringLast(15));
	}
	
	/**
	 * If size > 80, returns 20 or more
	 * otherwise 0 (not designed for small buffers)
	 */
	protected static int rebuildSize(int size) {
		return ((size > 100) ? (int)(size*0.20) :(
				(size > 80) ? 20 : 0));
	}

	@Override
	public char charAt(int i) {
		if(i < 0 || i >= this.count) {
			throw new ArrayIndexOutOfBoundsException(i);
		}
		return this.value[i];
	}

	@Override
	public int length() {
		return this.count;
	}

	@Override
	public EvictingStringBuilder append(CharSequence cs) throws IOException {
		return this.print(cs);
	}

	@Override
	public EvictingStringBuilder append(char chr) throws IOException {
		return this.print(chr);
	}

	@Override
	public EvictingStringBuilder append(CharSequence cs, int off, int end) throws IOException {
		return this.print(cs, off, end-off);
	}
	
}
