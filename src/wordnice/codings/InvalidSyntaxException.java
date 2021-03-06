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

package wordnice.codings;

public class InvalidSyntaxException 
extends IllegalArgumentException {
	
	private static final long serialVersionUID = 1L;
	
	public int i1 = -1;
	public int i2 = -1;
	
	public InvalidSyntaxException(String msg) {
		super(msg);
	}
	
	public InvalidSyntaxException(int i) {
		super("Invalid syntax at index " + i);
		this.i1 = i;
	}
	
	public InvalidSyntaxException(int i, boolean at) {
		super("Invalid syntax " + (at ? "at" : "near") +" index " + i);
		this.i1 = i;
	}
	
	public InvalidSyntaxException(int i1, int i2) {
		super("Invalid syntax between indexes " + i1 + " - " + i2);
		this.i1 = i1;
		this.i2 = i2;
	}
	
	public int getStart() {
		return this.i1;
	}
	
	public int getEnd() {
		return this.i2;
	}
	
	public int getLength() {
		return this.i2 - this.i1;
	}
	
}
