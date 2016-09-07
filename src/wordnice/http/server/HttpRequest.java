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
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wordnice.api.Nice;
import wordnice.api.Nice.CannotDoIt;
import wordnice.api.Nice.Masked;
import wordnice.api.Nice.Value;
import wordnice.codings.ASCII;
import wordnice.codings.URLCoder;
import wordnice.coll.MapWorker;
import wordnice.http.HttpFormatException;
import wordnice.http.client.HttpClient;
import wordnice.http.server.HttpServer.Handler;
import wordnice.streams.IUtils;
import wordnice.streams.OUtils;
import wordnice.utils.ArgsDecoder;
import wordnice.utils.ByteArraySequence;
import wordnice.utils.ByteSequence;
import wordnice.utils.Sockets;

public class HttpRequest implements Closeable {
	
	protected static String trim(String in) {
		if(in == null) return null;
		return in.trim();
	}
	
	protected static ByteArraySequence EQUALS = new ByteArraySequence("=".getBytes());
	protected static ByteArraySequence AND = new ByteArraySequence("&".getBytes());
	protected static Settings settingsDefault = new Settings() {
		public boolean getParsePost() {return true;}
		public boolean getParsePostMultipart() {return true;}
		public int getMultipartMemoryLimit() {return 16*1024;}
		public int getMultipartBufferSize() {return Nice.bufferSize;}
	};
	
	public static Settings getDefaultSettings() {
		return settingsDefault;
	}
	
	public static interface RequestHandler {

		/**
		 * @return when given request should be ignored returns null
		 * 		Otherwise instance of settings is returned
		 */
		public Settings getSettings(String httpType, String path, String method);
		
		/**
		 * @return true if request can be processed even without parsing heads
		 * 			false to continue parsing
		 */
		public boolean doAfterFirstLine(HttpRequest req) throws IOException;
		
		/**
		 * @return true if request can be processed even without post
		 * 			false to continue parsing
		 * 
		 * NOTE: This method is called even if parsing of post is disabled!
		 */
		public boolean doAfterHeads(HttpRequest req) throws IOException;
		
		public void cannotParseGet(RequestData in, String key, String value) throws CannotDoIt;
		
	}
	
	public static class Settings {

		public boolean parsePost = true;
		public boolean parsePostMultipart = true;
		public int multipartMemoryLimit = 16*1024;
		public int multipartBufferSize = Nice.bufferSize;
		
		public Settings() {}
		
		public boolean getParsePost() {
			return parsePost;
		}

		public Settings setParsePost(boolean parsePost) {
			this.parsePost = parsePost;
			return this;
		}

		public boolean getParsePostMultipart() {
			return parsePostMultipart;
		}

		public Settings setParsePostMultipart(boolean parsePostMultipart) {
			this.parsePostMultipart = parsePostMultipart;
			return this;
		}

		public int getMultipartMemoryLimit() {
			return multipartMemoryLimit;
		}

		public Settings setMultipartMemoryLimit(int multipartMemoryLimit) {
			this.multipartMemoryLimit = multipartMemoryLimit;
			return this;
		}
		
		public int getMultipartBufferSize() {
			return this.multipartBufferSize;
		}
		
		public Settings setetMultipartBufferSize(int multipartBufferSize) {
			this.multipartBufferSize = multipartBufferSize;
			return this;
		}
		
	}
	
	protected Socket socket;
	
	protected InputStream input; //Buffered 16K
	protected OutputStream output; //Buffered 16K
	
	protected RequestData inputData;
	protected ResponseData outputData;
	
	protected Settings settings = null;
	
	
	public HttpRequest(Socket sock) throws IOException {
		this.init(sock);
	}
	
	public void init(Socket sock) throws IOException {
		if(sock == null) throw Nice.illegal("Socket == null!");
		this.socket = sock;
		this.input = Nice.buffered(sock.getInputStream());
		this.output = Nice.buffered(sock.getOutputStream());
	}
	
	public Settings getSettings() {
		return (this.inputData != null) ? this.inputData.getSettings()
				: ((settings == null) ? settingsDefault : settings);
	}
	
	public HttpRequest setSettings(Settings set) {
		this.settings = set;
		if(this.inputData != null) {
			this.inputData.setSettings(set);
		}
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
			this.inputData = new RequestData().setSettings(settings);
		}
		return this.inputData;
	}
	
	public ResponseData getResponse() {
		if(this.outputData == null) {
			this.outputData = new ResponseData();
		}
		return this.outputData;
	}
	
	
	public HttpRequest writeResponseHeads() throws IOException {
		writeResponseHeads(this.getResponse(), this.getOutputStream());
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
	
	public HttpRequest parseRequestUntilPost() throws IOException {
		return this.parseRequestUntilPost(null);
	}
	
	/**
	 * @return null When handl
	 */
	public HttpRequest parseRequestUntilPost(final RequestHandler handler) throws IOException {
		if(parseRequestFirstLine(handler) == null) {
			return null;
		}
		if(handler.doAfterFirstLine(this)) {
			return null;
		}
		parseHeads();
		if(handler.doAfterHeads(this)) {
			return null;
		}
		return this;
	}
	
	public HttpRequest parseRequest() throws IOException {
		return this.parseRequest(null);
	}
	
	/**
	 * @return null When handl
	 */
	public HttpRequest parseRequest(final RequestHandler handler) throws IOException {
		if(parseRequestFirstLine(handler) == null) {
			return null;
		}
		if(handler.doAfterFirstLine(this)) {
			return null;
		}
		parseHeads();
		if(handler.doAfterHeads(this)) {
			return null;
		}
		parsePost();
		return this;
	}
	
	/**
	 * @return null when handler is not null and request was ignored. 
	 * 		otherwise returns this instance or throw exception
	 */
	public HttpRequest parseRequestFirstLine(final RequestHandler handler) throws IOException {
		return parseRequestFirstLine(handler, this.getRequest(), this.getInputStream())
				? this : null;
	}
	
	public HttpRequest parseHeads() throws IOException {
		parseHeads(this.getRequest().getOrCreateHeads(), this.getInputStream());
		return this;
	}
	
	public HttpRequest parsePost() throws IOException {
		parsePostIfNeeded(this.getRequest(), this.getInputStream());
		return this;
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
			this.inputData = null;
			this.outputData = null;
			this.settings = null;
			this.socket = null;
		}
	}
	
	/**
	 * Replace all \ to /
	 * Make sure there is no group of /
	 * Remove starting and ending /
	 * 
	 * eg. /pages\\\\index/logged-in//
	 * ->	pages/index/logged-in
	 */
	public static String securePath(String in) {
		int i = 0;
		int l = in.length();
		int start = -1;
		int first_start = 0;
		char bef_c = ' ';
		StringBuilder sb = null;
		while(i < l) {
			char c = in.charAt(i);
			if(c == '\\' || c == '/') {
				if(sb == null) {
					sb = new StringBuilder();
				}
				i++;
			} else {
				break;
			}
		}
		while(l != i) {
			char c = in.charAt(l-1);
			if(c == '\\' || c == '/') {
				if(sb == null) {
					sb = new StringBuilder();
				}
				l--;
			} else {
				break;
			}
		}
		if(i == l) {
			return "";
		}
		first_start = i;
		for(; i < l; i++) {
			char c = in.charAt(i);
			if(c == '\\' || c == '/') {
				if(start == -1) {
					start = i;
				}
			} else if(start != -1) {
				if(start < i-1 || bef_c == '\\') {
					if(sb == null) {
						sb = new StringBuilder();
						sb.append(in, first_start, start);
					} else {
						sb.append(in, first_start, start);
					}
					start = -1;
					first_start = i;
					if(start != 0) {
						sb.append('/');
					}
				} else {
					start = -1;
				}
			}
			bef_c = c;
		}
		if(sb == null) {
			return in;
		} else {
			sb.append(in, first_start, l);
			return sb.toString();
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
				heads.put(line.substring(0,splt).toLowerCase(), line.substring(splt_end));
			}
			if(line == null || !line.isEmpty()) {
				throw new HttpFormatException("Corrupted HTTP request: without proper ending");
			}
		} catch(EOFException ex) {
			throw new HttpFormatException("Corrupted HTTP request or socket closed", ex);
		}
		//ALL OK! We ended where POST begin!
	}
	
	public static boolean parseRequestFirstLine(final RequestHandler handler, final RequestData in, InputStream input) throws IOException {
		String path = IUtils.readLineStrict(input);
		if(path == null || path.length() < 5) {
			throw new HttpFormatException("Corrupted first line of HTTP request");
		}
		int zeroi = path.indexOf(' ');
		int lastzeroi = path.lastIndexOf(' ');
		if(zeroi == -1 || lastzeroi == zeroi) {
			throw new HttpFormatException("Corrupted first line of HTTP request");
		}
		in.setMethod(path.substring(0, zeroi));
		in.setHttpVersion(path.substring(lastzeroi + 1, path.length()));
		path = path.substring(zeroi + 1, lastzeroi);
		String originalPath = path;
		zeroi = originalPath.indexOf('?');
		if(zeroi > -1) {
			path = securePath(originalPath.substring(0, zeroi));
		} else {
			path = securePath(originalPath);
		}
		path = path.replace("../", "");
		
		Settings set = settingsDefault;
		if(handler != null) {
			set = handler.getSettings(in.getHttpVersion(), path, in.getMethod());
		}
		if(set == null) {
			return false;
		}
		in.setPath(path);
		in.setSettings(set);
		
		if(zeroi > -1) {
			final MapWorker inputGet = in.getOrCreateGet();
			ArgsDecoder.handleMap(
					new ArgsDecoder.MapHandler<String,String>() {
				
						@Override
						public void handle(String key, String val) throws Masked {
							key = URLCoder.decode(key);
							val = URLCoder.decode(val);
							try {
								inputGet.putArrayed(key, val);
							} catch(CannotDoIt c) {
								handler.cannotParseGet(in, key, val);
							}
						}
				
			}, originalPath, "=", "&", zeroi+1);
		}
		return true;
	}
	
	public static Settings getSettings(RequestData id) {
		return (id == null) ? settingsDefault : id.getSettings();
	}
	
	public static boolean hasPost(RequestData id, InputStream in) {
		return (getSettings(id).getParsePost() && (id.getMethod() == null 
				|| id.getMethod().equalsIgnoreCase("POST"))
				&& id.getHead("content-length") != null);
	}
	
	public static void parsePostIfNeeded(RequestData id, InputStream in) throws IOException {
		if(!hasPost(id, in)) {
			return;
		}
		parsePost(id, in);
	}
	
	public static void parsePost(final RequestData id, InputStream in) throws IOException {
		Settings set = getSettings(id);
		String ctype = id.getHead("content-type");
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
		final long clen = Nice.getAs(id.getHead("content-length"), long.class, -1L);
		if(clen < 0) {
			throw new HttpFormatException("Content length undefined or wrong ("+clen+")");
		}
		String type = ctype.substring(0, i);
		id.setContentType(type);
		try {
			ArgsDecoder.handleMap(new ArgsDecoder.MapHandler<String,String>() {
	
					@Override
					public void handle(String key, String val) throws Masked {
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
			
			}, ctype, "=",";", i+1);
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
		if(set != null && !set.getParsePostMultipart()) {
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
					ArgsDecoder.handleMap(new ArgsDecoder.MapHandler<String,String>() {
						
						@Override
						public void handle(String key, String val) throws Masked {
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
				
					}, originalCdis, "=",";", cdisSplit+1);
				} catch(Masked m) {
					throw (IOException) m.getCause();
				}
			}
			if(cdis.equals("form-data")) {
				AbstractPost.MultipartPost postdata = new AbstractPost.MultipartPost((filename.getValue() != null), set.getMultipartMemoryLimit());;
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
		final long clen = Nice.getAs(id.getHead("content-length"), long.class, -1L);
		if(clen < 0 || clen > Nice.maxArrayLength) {
			throw new HttpFormatException("Content length undefined, wrong or too big ("+clen+")");
		}
		final Map<String,Post> posts = id.getOrCreatePost();
		byte[] read = new byte[(int) clen];
		int readed = IUtils.tryToReadFully(in, read);
		ArgsDecoder.handleMap(new ArgsDecoder.MapHandler<ByteSequence,ByteSequence>() {

					@Override
					public void handle(ByteSequence key, ByteSequence val) {
						posts.put(key.toString(id.getCharset()),
								(val == null) ? null : new AbstractPost.PostBytes(val.newArray()));
					}
		}, new ByteArraySequence(read, 0, readed), EQUALS, AND);
	}
	
	
	public static void writeResponseHeads(ResponseData od, OutputStream out) throws IOException {
		Map<String,List<String>> heads = od.getHeads();
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
	
	public static void main(String...strings) throws Exception {
		HttpServer server = new HttpServer(new Sockets.Config("localhost", 25569));
		server.setHandler(new Handler() {

			@Override
			public Settings getSettings(String httpType, String path, String method) {
				System.out.println("GetSettings: " +method + " " + path + " " + httpType);
				return HttpRequest.getDefaultSettings();
			}

			@Override
			public void cannotParseGet(RequestData in, String key, String value) throws CannotDoIt {
				System.out.println("<--- Cannot parse get " + key + " = " + value);
			}

			@Override
			public void prepareRequest(HttpRequest req) throws Exception {
				req.getSocket().setSoTimeout(2000);
			}
			
			@Override
			public void handleRequest(HttpRequest req) throws Exception {
				System.out.println("----> Request: " + req.getRequest().getPath());
				
				RequestData id = req.getRequest();
				OutputStream out = req.getOutputStream();
				try {
					Post post = id.getPost().entrySet().iterator().next().getValue();
					
					req.writeResponseLineOK();
					req.getResponse().setContentType("text/plain")
						.setContentType(post.getContentType()).setContentLength(post.size());
					req.writeResponseHeads();
					
					post.writeTo(out);
				} catch(NullPointerException _ign) {
					_ign.printStackTrace();
					
					OUtils.write(out, "\r\nREQUEST: "+ id.getMethod() + " " + id.getPath() + " " + id.getHttpVersion());
					OUtils.write(out, "\r\nGET    : " + id.getGet());
					OUtils.write(out, "\r\nPOST   : " + id.getPost());
					OUtils.write(out, "\r\nHEAD   : " + id.getHeads());
					OUtils.write(out, "\r\n\r\n");
					Map<String,Post> post = id.getPost();
					if(post != null) {
						for(Entry<String,Post> entry : post.entrySet()) {
							OUtils.write(out, "\r\n\r\n"+entry.getKey()+"\r\n");
							entry.getValue().writeTo(out);
						}
					}
				}
				
			}

			@Override
			public void handleException(Exception ex, HttpRequest req) {
				System.out.println("Handle exception:");
				ex.printStackTrace();
			}

			@Override
			public void handleDecoderException(Exception ex, HttpRequest req) {
				System.out.println("Handle decoder exception:");
				ex.printStackTrace();
			}

			@Override
			public boolean doAfterFirstLine(HttpRequest req) {
				return false;
			}

			@Override
			public boolean doAfterHeads(HttpRequest req) {
				return false;
			}});
		System.out.println("Listening...");
		server.onAccept(false);
	}
	
}