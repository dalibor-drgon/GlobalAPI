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

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import wordnice.api.Nice;
import wordnice.api.Nice.Masked;
import wordnice.api.Nice.Value;
import wordnice.codings.ASCII;
import wordnice.codings.URLCoder;
import wordnice.http.HttpFormatException;
import wordnice.http.client.HttpClient;
import wordnice.seq.InternalSplitter;
import wordnice.seq.ByteArraySequence;
import wordnice.seq.ByteSequence;
import wordnice.streams.IUtils;
import wordnice.streams.OUtils;

public class HttpRequest
implements Closeable, AutoCloseable {
	
	protected static String trim(String in) {
		if(in == null) return null;
		return in.trim();
	}
	
	protected static ByteArraySequence EQUALS = new ByteArraySequence("=".getBytes());
	protected static ByteArraySequence AND = new ByteArraySequence("&".getBytes());
	
	protected Socket socket;
	
	protected InputStream input; //Buffered 16K
	protected OutputStream output; //Buffered 16K
	
	protected RequestData inputData;
	protected ResponseData outputData;	
	
	protected boolean closeOnFinish = true;
	protected Map<String,Object> props;
	
	public HttpRequest(Socket sock) throws IOException {
		this.init(sock);
	}
	
	protected void init(Socket sock) throws IOException {
		if(sock == null) throw new IllegalArgumentException("Socket == null!");
		this.socket = sock;
		this.input = Nice.buffered(sock.getInputStream());
		this.output = Nice.buffered(sock.getOutputStream());
	}
	
	public boolean closeOnFinish() {
		return closeOnFinish;
	}

	public HttpRequest setCloseOnFinish(boolean closeOnFinish) {
		this.closeOnFinish = closeOnFinish;
		return this;
	}

	public RequestSettings getRequestSettings() {
		return this.getRequest().getSettings();
	}
	
	public HttpRequest setRequestSettings(RequestSettings set) {
		this.getRequest().setSettings(set);
		return this;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
	
	public InputStream getInputStream() {
		return this.input;
	}
	
	public OutputStream getOutputStream() {
		return this.output;
	}
	
	public RequestData getRequest() {
		if(this.inputData == null) {
			this.inputData = new RequestData();
		}
		return this.inputData;
	}
	
	public ResponseData getResponse() {
		if(this.outputData == null) {
			this.outputData = new ResponseData();
		}
		return this.outputData;
	}
	
	@SuppressWarnings("unchecked")
	public Object getProperty(String name) {
		if(this.props == null) return null;
		Object obj = this.props.get(name);
		if(obj instanceof Reference)
			return ((Reference<Object>) obj).get();
		return obj;
	}
	
	public void setProperty(String name, Object val) {
		if(this.props == null) {
			if(val == null) return;
			this.props = Nice.createMap();
		}
		this.props.put(name, val);
	}
	
	public void setWeakProperty(String name, Object val) {
		if(this.props == null) {
			if(val == null) return;
			this.props = Nice.createMap();
		}
		this.props.put(name, new WeakReference<Object>(val));
	}
	
	public boolean has(String property) {
		return this.getProperty(property) != null;
	}
	
	
	public HttpRequest writeResponseHeads() throws IOException {
		writeResponseHeads(this.getResponse().getHeads(), this.getOutputStream());
		return this;
	}
	
	public HttpRequest writeResponseLineOK() throws IOException {
		return this.writeResponseLine(null, null);
	}
	
	public HttpRequest writeResponseLineOK(CharSequence httpversion) throws IOException {
		return this.writeResponseLine(null, httpversion);
	}
	
	public HttpRequest writeResponseLine(CharSequence status) throws IOException {
		return this.writeResponseLine(status, null);
	}
	
	public HttpRequest writeResponseLine(CharSequence status, CharSequence httpversion) throws IOException {
		writeResponseLine(status, httpversion, this.getOutputStream());
		return this;
	}
	
	public HttpRequest writeResponseOK() throws IOException {
		return this.writeResponse(null, null);
	}
	
	public HttpRequest writeResponseOK(CharSequence httpversion) throws IOException {
		return this.writeResponse(null, httpversion);
	}
	
	public HttpRequest writeResponse(CharSequence status) throws IOException {
		return this.writeResponse(status, null);
	}
	
	public HttpRequest writeResponse(CharSequence status, CharSequence httpversion) throws IOException {
		writeResponse(status, httpversion, this.getResponse().getHeads(), this.getOutputStream());
		return this;
	}
	
	public boolean isClosed() {
		if(this.socket == null) return true;
		return !this.socket.isConnected() || this.socket.isClosed();
	}
	
	public void closeSilent() {
		try {
			this.close();
		} catch(IOException io) {}
	}
	
	/**
	 * @return null when handler is not null and request was ignored. 
	 * 		otherwise returns this instance or throw exception
	 */
	public HttpRequest parseFirstLine() throws IOException {
		final RequestData in = this.getRequest();
		InputStream input = this.getInputStream();
		String path = IUtils.readLineStrict(input);
		if(path == null || path.length() < 5) {
			throw new HttpFormatException("Corrupted first line of HTTP request - too short, bad formatting");
		}
		int zeroi = path.indexOf(' ');
		int lastzeroi = path.lastIndexOf(' ');
		if(zeroi == -1 || lastzeroi == zeroi) {
			throw new HttpFormatException("Corrupted first line of HTTP request - bad formatting");
		}
		in.setMethod(path.substring(0, zeroi).toUpperCase());
		in.setHttpVersion(path.substring(lastzeroi + 1, path.length()).toUpperCase());
		path = path.substring(zeroi + 1, lastzeroi);
		String originalPath = path;
		zeroi = originalPath.indexOf('?');
		if(zeroi > -1) {
			path = originalPath.substring(0, zeroi);
		}
		if(path.length() == 0)
			throw new HttpFormatException("Path zero length");
		if(path.length() == 1 && path.charAt(0) == '*') {
			//Allow asterisk
		} else {
			path = URLCoder.decode(path);
			if(path.length() == 0 || path.charAt(0) != '/' || path.indexOf('\\') != -1 || path.indexOf("/../") != -1)
				throw new HttpFormatException("Path contains illegal characters");
			path = removeDoubleBackslashes(path);
		}
		
		in.setSettings(this.getRequestSettings());
		in.setPath(path);
		
		if(zeroi > -1) {
			final Map<String,Object> inputGet = in.getOrCreateGet();
			InternalSplitter.handleMap(
					new BiConsumer<String,String>() {
				
						@Override
						public void accept(String key, String val) throws Masked {
							key = URLCoder.decode(key);
							val = URLCoder.decode(val);
							inputGet.put(key, val);
						}
				
			}, originalPath, "=", "&", zeroi+1, true);
		}
		return this;
	}
	
	public HttpRequest parseHeads() throws IOException {
		parseHeads(this.getRequest().getOrCreateHeads(), this.getInputStream());
		return this;
	}
	
	public boolean hasPost() {
		return hasPost(this.getRequest());
	}
	
	public HttpRequest parsePost() throws IOException {
		if(this.hasPost()) parsePost(this.getRequest(), this.getInputStream());
		return this;
	}
	
	public static String removeDoubleBackslashes(String in) {
		int i = 0;
		int end = in.length();
		int start = -1;
		int first_start = 0;
		StringBuilder sb = null;
		int endOrig = end;
		while(end != i) {
			char c = in.charAt(end-1);
			if(c == '/') {
				end--;
			} else {
				break;
			}
		}
		if(i == end) {
			return "/";
		}
		for(; i < end; i++) {
			char c = in.charAt(i);
			if(c == '/') {
				if(start == -1) start = i;
			} else if(start != -1) {
				if(start < i-1) {
					if(sb == null) sb = Nice.createStringBuilder();
					if(first_start != start) sb.append(in, first_start, start);
					first_start = i;
					sb.append('/');
				}
				start = -1;
			}
		}
		if(sb == null) {
			if(end != endOrig) return in.substring(0, end);
			return in;
		} else {
			if(start == -1 && end != endOrig)
				start = end;
			if(start != -1) {
				if(first_start != start) sb.append(in, first_start, start);
			}
			return sb.toString();
		}
	}
	
	@Override
	public void close() throws IOException {
		if(this.output != null) {
			try {
				this.output.flush();
			} catch(IOException t) {}
			
			try {
				this.output.close();
			} catch(IOException t) {}
		}
		
		if(this.input != null) {
			try {
				this.input.close();
			} catch(IOException t) {}
		}
		RequestData rd = this.inputData;
		if(rd != null) {
			Map<String,Post> posts = rd.getPost();
			if(posts != null && !posts.isEmpty()) {
				for(Entry<String,Post> entry : posts.entrySet()) {
					Post post = entry.getValue();
					if(!post.isFile()) {
						continue;
					}
					File f = post.getFile();
					if(!f.exists()) {
						continue;
					}
					try {
						f.delete();
					} catch(Exception e) {}
				}
			}
		}
		try {
			if(this.socket != null) this.socket.close();
		} finally {
			//gc
			this.input = null;
			this.output = null;
			this.props = null;
			this.inputData = null;
			this.outputData = null;
			this.socket = null;
		}
	}
	
	public static void parseHeads(Map<String,String> heads, InputStream input) throws IOException {
		try {
			//Parse headers
			String line = null;
			while((line = trim(IUtils.readLineStrict(input))) != null 
					&& !line.isEmpty()) {
				int splt = line.indexOf(':');
				int splt_end = splt+1;
				if(splt_end < line.length()) {
					if(line.charAt(splt_end) == ' ') {
						splt_end++; //remove space too if found "name: value"
					}
				}
				heads.put(line.substring(0,splt).toUpperCase(), line.substring(splt_end));
			}
			if(line == null || !line.isEmpty()) {
				throw new HttpFormatException("Corrupted HTTP request: without proper ending");
			}
		} catch(EOFException ex) {
			throw new HttpFormatException("Corrupted HTTP request or socket closed", ex);
		}
		//ALL OK! We ended where POST begin!
	}
	
	public static boolean hasPost(RequestData id) {
		return (id.getSettings().parsePost() && (id.getMethod() == null 
				|| id.getMethod().equals("POST"))
				&& id.getHead("CONTENT-LENGTH") != null);
	}
	
	public static void parsePost(final RequestData id, InputStream in) throws IOException {
		RequestSettings set = id.getSettings();
		String ctype = id.getHead("CONTENT-TYPE");
		if(ctype == null) {
			id.setContentType(null);
			parsePostSimple(id, in);
			return;
		}
		int i = ctype.indexOf(';');
		if(i == -1) {
			id.setContentType(ctype);
			if(ctype.startsWith("multipart")) {
				throw new HttpFormatException("Got multipart post without bondary!");
			}
			parsePostSimple(id, in);
			return;
		}
		final long clen = Nice.cast(id.getHead("CONTENT-LENGTH"), long.class, -1L);
		if(clen < 0) {
			throw new HttpFormatException("Content length undefined or wrong ("+clen+")")
					.setError(411, "Length Required");
		}
		String type = ctype.substring(0, i);
		id.setContentType(type);
		try {
			InternalSplitter.handleMap(new BiConsumer<String,String>() {
	
					@Override
					public void accept(String key, String val) throws Masked {
						if(key != null && val != null) {
							key = key.trim().toLowerCase();
							val = val.trim();
							if(key.equals("boundary")) {
								try {
									id.setBoundary(val);
								} catch(IOException ioe) {
									throw Nice.mask(ioe);
								}
							} else if(key.equals("charset")) {
								if(Charset.isSupported(val)) {
									id.setCharset(Charset.forName(key));
								}
							}
						}
					}
			
			}, ctype, "=",";", i+1, true);
		} catch(Masked masked) {
			throw (IOException) masked.getCause();
		}
		byte[] boundary = id.getBoundary();
		if(!type.startsWith("multipart") && boundary == null) {
			parsePostSimple(id, in);
			return;
		}
		if(boundary == null) {
			throw new HttpFormatException("Multipart POST without boundary!");
		}
		if(set != null && !set.parseMultipart()) {
			return;
		}
		byte[] startbuff = new byte[Math.max(boundary.length*5, set.getMultipartBufferSize())];
		//byte[] startbuff2 = new byte[startbuff.length];
		IUtils.readFully(in, startbuff, 0, boundary.length);
		if(!ASCII.equals(startbuff, 0, boundary.length-2, boundary, 2, boundary.length-2)
				|| startbuff[boundary.length-2] != '\r'
				|| startbuff[boundary.length-1] != '\n') {
			throw new HttpFormatException("Multipart POST begining mismach!");
		}
		//PrefixedInputStream pis = new PrefixedInputStream(in); //resource
		Map<String,Post> post = id.getOrCreatePost();
		final Value<String> name = new Value<String>();
		final Value<String> filename = new Value<String>();
		while(true) {
			Map<String,String> head = Nice.createMap();
			//System.out.println("Parsing heads... " + pis.getPrefix() + " / " + startbuff + ","+ startbuff2);
			parseHeads(head, in);
			String cdis = head.get("content-disposition");
			if(cdis == null) {
				throw new HttpFormatException("Multipart block without content-disposition!");
			}
			name.setValue(null);
			filename.setValue(null);
			int cdisSplit = cdis.indexOf(';');
			if(cdisSplit != -1) {
				String originalCdis = cdis;
				cdis = cdis.substring(0, cdisSplit);
				try {
					InternalSplitter.handleMap(new BiConsumer<String,String>() {
						
						@Override
						public void accept(String key, String val) throws Masked {
							if(key != null && val != null) {
								key = key.trim().toLowerCase();
								val = val.trim();
								if(key.equals("name")) {
									name.setValue(val);
								} else if(key.equals("filename")) {
									filename.setValue(val);
								} else {
									/*throw Api.mask(new HTTPFormatException("Unknown value with key " 
											+ key + " in boundary content-disposition!"));*/
								}
							}
						}
				
					}, originalCdis, "=",";", cdisSplit+1, true);
				} catch(Masked m) {
					throw (IOException) m.getCause();
				}
			}
			if(cdis.equals("form-data")) {
				AbstractPost.MultipartPost postdata = new AbstractPost.MultipartPost((filename.getValue() != null), set.getMultipartMemoryLimit());
				postdata.setFileName(filename.getValue());
				postdata.setContentType(head.get("content-type"));
				postdata.setHeads(head);
				while(true) {
					IUtils.readUntil(in, postdata, boundary, 0, boundary.length, startbuff); //change to radUntilEOF
					IUtils.readFully(in, startbuff, 0, 2);
					if(startbuff[0] == '\r' && startbuff[1] == '\n') {
						//OK! Next entry incoming...
						postdata.finish();
						post.put(name.getValue(), postdata);
						break;
					} else if(startbuff[0] == '-' && startbuff[1] == '-') {
						//End of request!
						postdata.finish();
						post.put(name.getValue(), postdata);
						return;
					} else {
						//Lets say there is a file with same bytes as boundary has... continue reading
						postdata.write(boundary);
						postdata.write(startbuff, 0, 2);
					}
				}
			} else {
				throw new HttpFormatException("Unknown content-disposition: '" + cdis + "'");
			}
		}
	}
	
	public static void parsePostSimple(final RequestData id, InputStream in) throws IOException {
		final long clen = Nice.cast(id.getHead("CONTENT-LENGTH"), long.class, -1L);
		if(clen < 0) {
			throw new HttpFormatException("Content-length undefined or wrong ("+clen+")")
					.setError(411, "Length Required");
		}
		if(clen > Nice.MaxArrayLength) {
			throw new HttpFormatException("Content-length too big ("+clen+")")
					.setError(413, "Payload Too Large");
		}
		final Map<String,Post> posts = id.getOrCreatePost();
		byte[] read = new byte[(int) clen];
		int readed = IUtils.tryToReadFully(in, read);
		InternalSplitter.handleMap(new BiConsumer<ByteSequence,ByteSequence>() {
					@Override
					public void accept(ByteSequence key, ByteSequence val) {
						posts.put(key.toString(id.getCharset()),
								(val == null) ? null : new AbstractPost.PostBytes(val.newArray()));
					}
		}, new ByteArraySequence(read, 0, readed), EQUALS, AND);
	}
	
	public static void writeResponse(CharSequence status, 
			CharSequence httpversion, Map<String,List<String>> heads, OutputStream out) throws IOException {
		writeResponseLine(status, httpversion, out);
		writeResponseHeads(heads, out);
	}
	
	public static void writeResponseHeads(Map<String,List<String>> heads, OutputStream out) throws IOException {
		if(heads != null && !heads.isEmpty()) {
			for(Entry<String,List<String>> rootEntry : heads.entrySet()) {
				String key = rootEntry.getKey();
				List<String> values = rootEntry.getValue();
				for(String val : values) {
					OUtils.write(out, key);
					out.write(HttpClient.DELIMITER);
					OUtils.write(out, val);
					out.write(HttpClient.CRLF);
				}
			}
		}
		out.write(HttpClient.CRLF);
		out.flush();
	}
	
	public static void writeResponseLine(CharSequence status, 
			CharSequence httpversion, OutputStream out) throws IOException {
		if(status == null) status = "200 OK"; //ok!
		if(httpversion == null) httpversion = "HTTP/1.1";
		OUtils.write(out, httpversion);
		out.write((byte)' ');
		OUtils.write(out, status);
		out.write(HttpClient.CRLF);
	}

	@Override
	public String toString() {
		return "HttpRequest [socket=" + socket + ", props=" + props + "]";
	}
	
}