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

package eu.wordnice.sockets;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import eu.wordnice.api.Api;
import eu.wordnice.api.ArgsDecoder;
import eu.wordnice.api.ByteString;
import eu.wordnice.api.Handler;
import eu.wordnice.api.Val;
import eu.wordnice.api.Val.TwoVal;
import eu.wordnice.streams.Input;
import eu.wordnice.streams.Output;
import eu.wordnice.streams.InputAdv;
import eu.wordnice.streams.OutputAdv;

public class HIO implements Closeable {
	
	public static boolean BLOCK_FAVICON = true;
	public static Handler.OneVoidHandler<Val.TwoVal<String, String>> URL_DECODER = 
			new Handler.OneVoidHandler<Val.TwoVal<String, String>>() {
				@Override
				public void handle(TwoVal<String, String> strings) {
					if(strings.one != null) {
						strings.one = Api.getURLDecoded(strings.one);
					}
					if(strings.two != null) {
						strings.two = Api.getURLDecoded(strings.two);
					}
				}
			};
	
	public Socket sock;
	public Input in;
	public Output out;
	
	public String METHOD;
	public String PATH;
	public String REQTYPE;
	public Map<String,String> GET;
	public Map<String,String> POST;
	public Map<String,String> HEAD;
	
	public HIO(Socket sock) throws IOException {
		this.sock = sock;
		/**
		 * Avoid Buffered streams, we are reading with 16kB buffer
		 */
		this.in = InputAdv.forStream(sock.getInputStream());
		this.out = OutputAdv.forStream(sock.getOutputStream());
	}
	
	public Throwable decode(boolean readPost, long to) {
		try {
			this.decodeException(readPost, to);
		} catch(Throwable t) {
			return t;
		}
		return null;
	}
	
	public void decodeException(boolean readPost, long to) throws Throwable {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		boolean read = false;
		try {
			byte[] buff = new byte[1024 * 16];
			int cur = this.in.read();
			if(cur == -1) {
				throw new IOException("Cannot start reading from socket!");
			}
			bout.write(cur);
			
			while((cur = this.in.read(buff, 0, buff.length)) > 0) {
				bout.write(buff, 0, cur);
				if(cur >= 2 && buff[cur - 2] == '\n' && buff[cur - 1] == '\n') {
					break;
				}
				if(cur >= 3 && buff[cur - 3] == '\n' && buff[cur - 2] == '\r' && buff[cur - 1] == '\n') {
					break;
				}
			}
			read = true;
		} catch(Throwable t) { }
		
		if(read) {
			int i = 0;
			int ei = 0;
			byte[] bytes = bout.toByteArray();
			while(true) {
				if(i >= bytes.length) {
					throw new Exception("Not HTTP format");
				}
				if(bytes[i] == '\n') {
					ei = i;
					break;
				}
				i++;
			}
			if(ei == 0) {
				throw new Exception("Not HTTP format");
			}
			String path = new String(bytes, 0, ei);
			int zeroi = path.indexOf(' ');
			int lastzeroi = path.lastIndexOf(' ');
			if(zeroi == -1 || lastzeroi == zeroi) {
				throw new Exception("Not HTTP format");
			}
			this.METHOD = path.substring(0, zeroi);
			this.REQTYPE = path.substring(lastzeroi + 1, path.length());
			this.PATH = path.substring(zeroi + 1, lastzeroi);
			this.PATH = this.PATH.replaceAll("//", "/");
			if(!this.PATH.startsWith("/")) {
				this.PATH = "/" + this.PATH;
			}
			if((!this.PATH.equals("/favicon.ico") || !HIO.BLOCK_FAVICON)) {
				zeroi = this.PATH.indexOf('?');
				if(zeroi > -1) {
					this.GET = new HashMap<String,String>();
					ArgsDecoder.decodeString(this.GET, this.PATH.substring(zeroi + 1, this.PATH.length()), 
							"=", "&", URL_DECODER);
					this.PATH = this.PATH.substring(0, zeroi);
				}
				
				ei += 1;
				
				if(bytes[bytes.length - 3] == '\n' && bytes[bytes.length - 2] == '\r' 
						&& bytes[bytes.length - 1] == '\n') {
					this.parseHead(new String(bytes, ei, bytes.length - 3 - ei));
				} else if(bytes[bytes.length - 2] == '\n' && bytes[bytes.length - 1] == '\n') {
					this.parseHead(new String(bytes, ei, bytes.length - 2 - ei));
				} else {
					Val.TwoVal<Integer, Integer> index = ByteString.indexOf(bytes, ei, bytes.length - ei, 2, 
							new Handler.FourVoidHandler<Val.TwoVal<Boolean, Integer>, byte[], Integer, Integer>() {
								@Override
								public void handle(Val.TwoVal<Boolean, Integer> ret, byte[] arr, Integer offset, Integer len) {
									if(len > 2 && arr[offset] == '\n' && arr[offset + 1] == '\r' 
											&& arr[offset + 2] == '\n') {
										ret.one = true;
										ret.two = 3;
										return;
									}
									if(arr[offset] == '\n' && arr[offset + 1] == '\n') {
										ret.one = true;
										ret.two = 2;
										return;
									}
									ret.one = false;
								}
							}
					);
					if(index == null) {
						throw new Exception("Corrupted data!");
					}
					this.parseHead(new String(bytes, ei, index.one - ei));
					if(readPost) {
						/*int from = index.one + index.two;
						System.out.println("Left for post... not supported yet: " 
								+ new String(bytes, from, bytes.length - from));*/
					}
				}
			}
			
		} else {
			throw new Exception("Not HTTP format");
		}
	}
	
	private void parseHead(String str) {
		this.HEAD = new HashMap<String,String>();
		ArgsDecoder.decodeString(this.HEAD, str, ":", "\n",
				new Handler.OneVoidHandler<Val.TwoVal<String, String>>() {
					@Override
					public void handle(TwoVal<String, String> strings) {
						if(strings.one != null) {
							strings.one = Api.getURLDecoded(strings.one).trim().toLowerCase();
						}
						if(strings.two != null) {
							strings.two = Api.getURLDecoded(strings.two).trim();
						}
					}
				}
		);
	}
	
	@Override
	public void close() throws IOException {
		try {
			this.in.close();
			this.out.close();
		} catch(Throwable t) {}
		this.sock.close();
	}
	
	
	
}
