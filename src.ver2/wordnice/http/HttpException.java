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

package wordnice.http;

import java.io.IOException;

public class HttpException extends IOException {
	
	private static final long serialVersionUID = 20161005;
	
	protected int code = -1;
	protected String message = null;
	
	public HttpException() {
		super();
	}
	
	public HttpException(String string) {
		super(string);
	}
	
	public HttpException(Throwable t) {
		super(t);
	}
	
	public HttpException(String string,Throwable t) {
		super(string, t);
	}
	
	public HttpException setError(int code) {
		return this.setError(code, null);
	}
	
	
	public HttpException setError(int code, String message) {
		if(code < 0) throw new IllegalArgumentException("Negative code");
		this.code = code;
		this.message = message;
		return this;
	}
	
	public int getErrorCode() {
		return this.code;
	}
	
	public String getErrorMessage() {
		return this.message == null 
				? ((code == -1) ? null : (this.message = HttpStatus.getStatusMessage(code))) 
				: message;
	}

}
