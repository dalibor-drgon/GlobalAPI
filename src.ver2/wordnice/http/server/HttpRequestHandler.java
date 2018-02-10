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

public interface HttpRequestHandler {
	
	/**
	 * Prepare new client
	 * eg. set socket timeout, log IP etc...
	 * Called before parsing
	 * 
	 * @return false if request should be closed
	 */
	public boolean acceptRequest(HttpRequest req);
	
	/**
	 * @return true if request can be processed even without parsing heads
	 * 			false to continue parsing
	 */
	public boolean finishAfterFirstLine(HttpRequest req);
	
	/**
	 * @return true if request can be processed even without post
	 * 			false to continue parsing
	 */
	public boolean finishAfterHeads(HttpRequest req);
	
	/**
	 * Handle fully parsed client request
	 * 
	 * @return true if request was processed and can be closed
	 */
	public boolean handleRequest(HttpRequest req);
	
	
	/**
	 * Handle exception which occured while decoding request by us
	 */
	public void handleDecoderException(HttpRequest req, Exception ex);
	
	
	/**
	 * Cleanup request
	 */
	public void cleanup(HttpRequest req, boolean status);
	
}