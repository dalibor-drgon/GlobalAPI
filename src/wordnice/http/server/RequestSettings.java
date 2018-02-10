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

package wordnice.http.server;

import wordnice.api.Nice;

public class RequestSettings {

	public static int DefaultMultipartMemoryLimit = 16*1024;
	public static int DefaultMultipartBufferSize = Nice.BufferSize;
	public static int DefaultMaxSimplePostSize = 128*1024;
	
	protected int multipartMemoryLimit = DefaultMultipartMemoryLimit;
	protected int multipartBufferSize = DefaultMultipartBufferSize;
	protected int maxSimplePostSize = DefaultMaxSimplePostSize;
	protected boolean parsePost = true;
	protected boolean parseMultipart = true;
	
	public RequestSettings() {}
	
	public boolean parsePost() {
		return this.parsePost;
	}
	
	public RequestSettings setParsePost(boolean parsePost) {
		this.parsePost = parsePost;
		return this;
	}
	
	public boolean parseMultipart() {
		return this.parseMultipart;
	}
	
	public RequestSettings setParseMultipart(boolean parseMultipart) {
		this.parseMultipart = parseMultipart;
		return this;
	}

	public int getMultipartMemoryLimit() {
		return multipartMemoryLimit;
	}

	public RequestSettings setMultipartMemoryLimit(int multipartMemoryLimit) {
		if(multipartMemoryLimit < 1)
			multipartMemoryLimit = DefaultMultipartMemoryLimit;
		this.multipartMemoryLimit = multipartMemoryLimit;
		return this;
	}
	
	public int getMultipartBufferSize() {
		return this.multipartBufferSize;
	}
	
	public RequestSettings setMultipartBufferSize(int multipartBufferSize) {
		if(multipartBufferSize < 1)
			multipartBufferSize = DefaultMultipartBufferSize;
		this.multipartBufferSize = multipartBufferSize;
		return this;
	}

	public int getMaxSimplePostSize() {
		return maxSimplePostSize;
	}

	public RequestSettings setMaxSimplePostSize(int maxSimplePostSize) {
		if(maxSimplePostSize < 1)
			maxSimplePostSize = DefaultMaxSimplePostSize;
		this.maxSimplePostSize = maxSimplePostSize;
		return this;
	}
	
}