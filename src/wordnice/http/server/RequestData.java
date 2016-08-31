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

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import wordnice.api.Nice;
import wordnice.coll.MapWorker;
import wordnice.http.HttpFormatException;
import wordnice.http.server.HttpRequest.Settings;

public class RequestData {

	protected String method;
	protected String path;
	protected String httpVersion;
	
	protected MapWorker get;
	protected Map<String,String> heads;
	protected Map<String,Post> post;
	
	protected byte[] boundary;
	protected Charset charset;
	protected String contentType;
	protected Settings settings;
			
	
	public RequestData() {}
	
	public Settings getSettings() {
		if(settings == null) {
			return HttpRequest.settingsDefault;
		}
		return settings;
	}
	
	public boolean supportsGZIP() {
		String enc = this.getHead("accept-encoding");
		return enc != null && (enc.contains("gzip,") || enc.endsWith("gzip"));
	}
	
	public boolean supportsDeflate() {
		String enc = this.getHead("accept-encoding");
		return enc != null && (enc.contains("deflate,") || enc.endsWith("deflate"));
	}
	
	public Settings getSettingsIfHas() {
		return this.settings;
	}
	
	public RequestData setSettings(Settings set) {
		this.settings = set;
		return this;
	}

	public String getMethod() {
		return method;
	}

	public RequestData setMethod(String method) {
		this.method = method;
		return this;
	}

	public String getPath() {
		return path;
	}

	public RequestData setPath(String path) {
		this.path = path;
		return this;
	}

	public String getHttpVersion() {
		return httpVersion;
	}

	public RequestData setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
		return this;
	}

	public MapWorker getOrCreateGet() {
		if(get == null) {
			get = Nice.createMapWorker();
		}
		return get;
	}
	
	public MapWorker getGet() {
		return get;
	}
	
	public Object getGet(String key) {
		if(get == null) {
			return null;
		}
		return get.get(key);
	}

	public RequestData setGet(MapWorker get) {
		this.get = get;
		return this;
	}

	public Map<String,String> getOrCreateHeads() {
		if(heads == null) heads = Nice.createMap();
		return heads;
	}
	
	public Map<String,String> getHeads() {
		if(heads == null) return Collections.emptyMap();
		return heads;
	}
	
	public String getHead(String key) {
		if(heads == null) return null;
		return heads.get(key);
	}
	
	public RequestData setHeads(Map<String,String> heads) {
		this.heads = heads;
		return this;
	}
	
	public Map<String,Post> getOrCreatePost() {
		if(post == null) post = Nice.createMap();
		return this.post;
	}
	
	public Map<String,Post> getPost() {
		if(this.post == null) return Collections.emptyMap();
		return this.post;
	}
	
	public RequestData setPost(Map<String,Post> post) {
		this.post = post;
		return this;
	}
	
	public Post getPost(String key) {
		if(this.post == null) return null;
		return this.post.get(key);
	}
	
	public RequestData putPost(String key, Post post) {
		if(this.post == null) this.post = Nice.createMap();
		this.post.put(key, post);
		return this;
	}
	
	/**
	 * @return <pre> Boundary formated as \r\n--BOUNDARY
	 * 	CHAR:  \r\n - - BOUNDARY
	 * 	INDEX:  0 1 2 3 4..74max </pre>
	 */
	public byte[] getBoundary() {
		return boundary;
	}

	public RequestData setBoundary(CharSequence boundary) throws HttpFormatException {
		if(boundary.length() < 1) {
			throw new HttpFormatException(
					"Boundary zero length!");
		}
		int len = boundary.length();
		if(len > 70) {
			throw new HttpFormatException(
					"Boundary longer than maximum allowed "
					+ "length ("+boundary.length()+">70)!");
		}
		byte[] bd = new byte[len+4];
		bd[0] = '\r';
		bd[1] = '\n';
		bd[2] = '-';
		bd[3] = '-';
		int i = 0;
		for(; i < len; i++) {
			char c = boundary.charAt(i);
			if(c > 127) {
				throw new HttpFormatException(
						"Boundary is not ASCII only!");
			}
			bd[i+4] = (byte) c;
		}
		this.boundary = bd;
		return this;
	}

	public Charset getCharset() {
		if(charset == null) {
			charset = Charset.defaultCharset();
		}
		return charset;
	}

	public RequestData setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}
	
	public String getContentType() {
		return contentType;
	}

	public RequestData setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	
}