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

package wordnice.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wordnice.api.Nice;
import wordnice.codings.URLCoder;
import wordnice.streams.OUtils;
import wordnice.utils.ByteSequence;

public class HttpClient {
	
	public static final byte[] CRLF = new byte[] {'\r','\n'};
	public static final byte[] DELIMITER = new byte[] {':', ' '};
	public static final String MIME = "application/octet-stream";
	public static final byte[] MIME_BYTES = MIME.getBytes();
	
	protected Socket sock;
	protected InputStream input;
	protected OutputStream output;
	protected Map<CharSequence,List<Object>> heads;
	
	public HttpClient(Socket sock) throws IOException {
		this(sock, Nice.buffered(sock.getInputStream()), 
				Nice.buffered(sock.getOutputStream()));
	}
	
	public HttpClient(Socket sock, InputStream in, OutputStream out) {
		this.sock = sock;
		this.input = in;
		this.output = out;
		this.heads = Nice.createMap();
	}
	
	public Socket getSocket() {
		return this.sock;
	}
	
	public InputStream getInput() {
		return this.input;
	}
	
	public OutputStream getOutput() {
		return this.output;
	}
	
	public Map<CharSequence,List<Object>> getHeads() {
		return Collections.unmodifiableMap(this.heads);
	}
	
	public List<Object> getOrCreateHead(CharSequence key) {
		List<Object> list = this.heads.get(key);
		if(list == null) {
			list = Nice.createList();
			this.heads.put(key, list);
		}
		return list;
	}
	
	public List<Object> getHead(CharSequence key) {
		List<Object> list = this.heads.get(key);
		return (list == null) 
				? Collections.<Object>emptyList() : 
					Collections.unmodifiableList(list);
	}
	
	public HttpClient setHead(CharSequence key, Object value) {
		List<Object> head = this.getOrCreateHead(key);
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
	
	public HttpClient addHead(CharSequence key, Object value) {
		this.getOrCreateHead(key).add(value);
		return this;
	}
	
	public HttpClient setHeadContentLength(int len) {
		return this.setHead("Content-Length", len);
	}
	
	public HttpClient setHeadContentType(CharSequence type) {
		return this.setHead("Content-Type", type);
	}
	
	public HttpClient setHeadContentTypeWithCharset(CharSequence type, CharSequence charset) {
		return this.setHead("Content-Type", type+"; charset="+charset);
	}
	
	public HttpClient setHeadConnectionClose() {
		return this.setHead("Connection", "close");
	}
	
	public HttpClient writeHeads() throws IOException {
		return writeHeads(true);
	}
	
	public HttpClient writeHeads(boolean encode) throws IOException {
		OutputStream out = this.getOutput();
		for(Entry<CharSequence,List<Object>> rootEntry : this.heads.entrySet()) {
			CharSequence _key = rootEntry.getKey();
			String key = _key+""; //TODO URL coder encode for CharSequence
			if(encode) key = URLCoder.encode(key);
			List<Object> values = rootEntry.getValue();
			for(Object _val : values) {
				String val = _val+"";
				if(encode) val = URLCoder.encode(val);
				OUtils.write(out,key);
				out.write(DELIMITER);
				OUtils.write(out,val);
				out.write(CRLF);
			}
		}
		out.write(CRLF);
		out.flush();
		return this;
	}
	
	/**
	 * Bellow for clients connecting to server
	 */
	
	
	public HttpClient writeRequest(CharSequence path) throws IOException {
		return this.writeRequest(path, null, null, null);
	}
	
	public HttpClient writeRequest(CharSequence path, CharSequence method) throws IOException {
		return this.writeRequest(path, method, null, null);
	}
	
	public HttpClient writeRequest(CharSequence path, CharSequence method, 
			CharSequence httpver)
			throws IOException {
		return writeRequest(path, method, httpver, null);
	}
	
	public HttpClient writeRequest(CharSequence path, CharSequence method, 
			CharSequence httpver, CharSequence afterPath)
			throws IOException {
		if(path == null) path = "/";
		if(method == null) method = "GET";
		if(httpver == null) httpver = "HTTP/1.1";
		OutputStream out = this.getOutput();
		OUtils.write(out,method);
		out.write(' ');
		OUtils.write(out,path);
		if(afterPath != null) {
			out.write('?');
			OUtils.write(out,afterPath);
		}
		out.write(' ');
		OUtils.write(out,httpver);
		out.write(CRLF);
		return this;
	}
	
	public HttpClient writeRequest(ByteSequence path) throws IOException {
		return this.writeRequest(path, null, null, null);
	}
	
	public HttpClient writeRequest(ByteSequence path, ByteSequence method) throws IOException {
		return this.writeRequest(path, method, null, null);
	}
	
	public HttpClient writeRequest(ByteSequence path, ByteSequence method, 
			ByteSequence httpver)
			throws IOException {
		return writeRequest(path, method, httpver, null);
	}
	
	public HttpClient writeRequest(ByteSequence path, ByteSequence method, ByteSequence httpver, ByteSequence afterPath)
			throws IOException {
		OutputStream out = this.getOutput();
		if(method == null) out.write("GET".getBytes()); else OUtils.write(out,method);
		out.write(' ');
		if(path == null) out.write('/'); else OUtils.write(out,path);
		if(afterPath != null) {
			out.write('?');
			OUtils.write(out,afterPath);
		}
		out.write(' ');
		if(httpver == null) out.write("HTTP/1.1".getBytes()); else OUtils.write(out,httpver);
		out.write(CRLF);
		return this;
	}

}
