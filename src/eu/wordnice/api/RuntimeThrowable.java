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
 */

package eu.wordnice.api;

import java.io.PrintStream;
import java.io.PrintWriter;

public class RuntimeThrowable extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public Throwable exc = null;
	
	public RuntimeThrowable(Throwable t) {
		this.exc = t;
	}
	
	@Override
	public void setStackTrace(StackTraceElement[] st) {
		this.exc.setStackTrace(st);
	}
	
	@Override
	public StackTraceElement[] getStackTrace() {
		return this.exc.getStackTrace();
	}
	
	@Override
	public Throwable fillInStackTrace() {
		return this.exc.fillInStackTrace();
	}
	
	@Override
	public void printStackTrace() {
		this.exc.printStackTrace();
	}
	
	@Override
	public void printStackTrace(PrintStream ps) {
		this.exc.printStackTrace(ps);
	}
	
	@Override
	public void printStackTrace(PrintWriter pw) {
		this.exc.printStackTrace(pw);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return this.exc.equals(obj);
	}
	
	@Override
	public String toString() {
		return this.exc.toString();
	}
	
}
