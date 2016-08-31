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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.codings.URLCoder;

public class ResponseData {
	
	public ResponseData() {}
	
	protected Map<String,List<String>> heads;

	public ResponseData setHeads(Map<String, List<String>> heads) {
		this.heads = heads;
		return this;
	}
	
	
	public Map<String,List<String>> getOrCreateHeads() {
		if(this.heads == null) {
			this.heads = Nice.createMap();
		}
		return this.heads;
	}
	
	public Map<String,List<String>> getHeads() {
		return (this.heads == null) 
				? Collections.<String,List<String>>emptyMap()
				: this.heads;
	}
	
	public List<String> getOrCreateHead(String key) {
		List<String> list = this.getOrCreateHeads().get(key);
		if(list == null) {
			list = Nice.createList();
			this.heads.put(key, list);
		}
		return list;
	}
	
	public List<String> getHead(String key) {
		if(this.heads == null) {
			return Collections.emptyList();
		}
		List<String> list = this.heads.get(key);
		return (list == null) 
				? Collections.<String>emptyList() : 
					list;
	}
	
	public ResponseData setHeadEncoded(String key, Object _value) {
		return this.setHead(key, (_value == null) 
				? "null" : URLCoder.encode(_value.toString()));
	}
	
	public ResponseData setHead(String key, Object _value) {
		String value = (_value == null) ? "null" : _value.toString();
		List<String> head = this.getOrCreateHead(key);
		if(head.isEmpty()) {
			head.add(value);
		} else if(head.size() == 1) {
			head.set(0, value);
		} else {
			head.clear();
			head.add(value);
		}
		return this;
	}
	
	public ResponseData addHead(String key, Object value) {
		this.getOrCreateHead(key).add(value == null ? "null" : value.toString());
		return this;
	}
	
	public ResponseData addHeadEncoded(String key, Object value) {
		this.getOrCreateHead(key).add(value == null 
				? "null" : URLCoder.encode(value.toString()));
		return this;
	}
	
	public ResponseData setContentLength(long len) {
		return this.setHead("Content-Length", len);
	}
	
	public ResponseData setContentType(CharSequence type) {
		return this.setHead("Content-Type", type);
	}
	
	public ResponseData setContentType(CharSequence type, CharSequence charset) {
		return this.setHead("Content-Type", type+"; charset="+charset);
	}
	
	public ResponseData setConnectionClose() {
		return this.setHead("Connection", "close");
	}
	
	public ResponseData setACAO() {
		return this.setACAO(null, "*", 1000);
	}
	
	public ResponseData setACAO(CharSequence method, CharSequence origins, int maxage) {
		if(method == null) method = "POST, GET, OPTIONS";
		if(origins == null) origins = "*";
		this.setHead("Access-Control-Allow-Origin", origins);
		this.setHead("Access-Control-Allow-Methods", method);
		this.setHead("Access-Control-Max-Age", maxage);
		this.setHead("Access-Control-Allow-Headers", "origin, x-csrf-token, content-type, accept");
		return this;
	}
	
}