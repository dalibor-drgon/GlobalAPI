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

package wordnice.http.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;

import wordnice.codings.Base64;
import wordnice.coll.ConcurrentArray;
import wordnice.http.server.HttpRequest;
import wordnice.http.server.RequestData;
import wordnice.http.server.HttpRequestHandler;
import wordnice.http.server.ResponseData;

public class WebSocketServer
implements HttpRequestHandler {

	/**
	 * WebSocketHandler
	 * If any method throws IOException, connection is aborted and client is disconnected
	 * They are in order they are called
	 */
	public static interface WebSocketHandler {
		
		/**
		 * Called after handshake before listening for messages
		 * @return false if connection should be ignored and closed
		 */
		public boolean onHandshake(HttpRequest req) throws IOException;
		
		/**
		 * Called after handshake before listening for messages
		 */
		public void onConnect(WebSocket ws) throws IOException;
		
		/**
		 * Called on full message receive
		 * @return false if connection should be closed
		 */
		public boolean onMessage(WebSocket ws, WebSocketMessage msg) throws IOException;
		
		/**
		 * Called on pong receive
		 * @return false if connection should be closed
		 */
		public boolean onPong(WebSocket ws, WebSocketMessage msg) throws IOException;
		
		/**
		 * Handle exception which occured while decoding request by us
		 * @return false if connection should be closed
		 */
		public boolean onDecoderException(WebSocket ws, Exception ex);
		
		/**
		 * Cleanup request
		 * NOTE: This is called after onDecoderException!
		 */
		public void onClose(WebSocket ws, Throwable t);
		
	}

	protected ConcurrentArray<WebSocketHandler> handlers = 
			new ConcurrentArray<WebSocketHandler>();
	protected boolean acceptAnyWebsocket = false;
	protected boolean forceAnyWebsocket = false;
	protected int timeout = 30*1000;
	protected int readLimit = 1024*1024;
	
	/**
	 * Constuctor
	 */
	public WebSocketServer() {}
	
	public Collection<WebSocketHandler> getHandlers() {
		return this.handlers.snapshot();
	}
	
	public WebSocketServer addHandler(WebSocketHandler hand) {
		this.handlers.add(hand);
		return this;
	}
	
	public WebSocketServer addHandlers(WebSocketHandler... hand) {
		this.handlers.addAll(hand);
		return this;
	}
	
	public WebSocketServer addHandlers(Collection<WebSocketHandler> hand) {
		this.handlers.addAll(hand);
		return this;
	}
	
	public boolean isAcceptAnyWebsocket() {
		return acceptAnyWebsocket;
	}

	public WebSocketServer setAcceptAnyWebsocket(boolean acceptAnyWebsocket) {
		this.acceptAnyWebsocket = acceptAnyWebsocket;
		return this;
	}
	
	public boolean isForceAnyWebsocket() {
		return forceAnyWebsocket;
	}

	public WebSocketServer setForceAnyWebsocket(boolean forceAnyWebsocket) {
		this.forceAnyWebsocket = forceAnyWebsocket;
		return this;
	}
	
	public int getReadLimit() {
		return readLimit;
	}

	public WebSocketServer setReadLimit(int readLimit) {
		if(readLimit < 0) throw new IllegalArgumentException("Negative read limit");
		this.readLimit = readLimit;
		return this;
	}
	
	
	
	public int getTimeout() {
		return timeout;
	}

	public WebSocketServer setTimeout(int timeout) {
		if(timeout < 0) throw new IllegalArgumentException("Timeout is negative");
		this.timeout = timeout;
		return this;
	}
	
	@Override
	public boolean finishAfterHeads(HttpRequest hr) {
		WebSocket ws = null;
		try {
			int code = isWebSocket(hr);
			ResponseData res = hr.getResponse();
			if(code != 0) 
				res.setHead("Sec-WebSocket-Version", "13");
			if(code != -1) return code == 1;
			res.setHead("Upgrade", "websocket");
			res.setHead("Connection", "Upgrade");
			if(!this.onHandshake(hr)) return this.fail(hr) == 1;
			if(!sendAcceptResponse(hr)) return this.fail(hr)==1;
			
			hr.getOutputStream().flush();
			Socket sock = hr.getSocket();
			//OutputStream out = sock.getOutputStream();
			OutputStream out = hr.getOutputStream();
			InputStream in = hr.getInputStream();
			ws = new WebSocket(sock, out, in, hr.getRequest(), this);
			int timeout = this.getTimeout();
			if(timeout > 0) sock.setSoTimeout(timeout);
			if(this.onConnect(ws)) {
				ws.listen();
			}
		} catch(IOException t) {
			if(ws == null || !ws.isClosed())
				this.onDecoderException(ws, t);
		}
		if(ws != null && !ws.isClosed()) ws.closeSilent();
		return true;
	}
	
	/**
	 * @return -1 Continue, 0 error (not supported - continue), 1 die
	 */
	protected int isWebSocket(HttpRequest hr) {
		boolean hasForce = hr.has("WebSocketForce") || this.isForceAnyWebsocket();
		boolean hasWS = hr.has("WebSocket");
		if(!hasWS && !hasForce && !this.isAcceptAnyWebsocket())
			return 0; //Not websocket really
		RequestData rd = hr.getRequest();
		if(!"websocket".equalsIgnoreCase(rd.getHead("UPGRADE")))
			return (hasForce) ? this.fail(hr) : 0;
		String con = rd.getHead("CONNECTION");
		if(con == null || !con.toLowerCase().contains("upgrade")) return this.fail(hr);
		if(!"13".equals(rd.getHead("SEC-WEBSOCKET-VERSION"))) return this.fail(hr);
		return -1;
	}
	
	protected int fail(HttpRequest hr) {
		try {
			hr.getResponse().allowCORS().setConnectionClose();
			hr.writeResponse("400 Bad Request");
		} catch(Throwable ex) {}
		return 1;
	}
	
	protected boolean sendAcceptResponse(HttpRequest hr) throws IOException {
		RequestData rd = hr.getRequest();
		String key = rd.getHead("Sec-WebSocket-Key");
		if(key == null || key.length() < 2) return false;
		String toEncode = key+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		String token = new String(Base64.encodeToChars(DigestUtils.sha1(toEncode)));
		
		ResponseData res = hr.getResponse();
		res.setHead("Sec-WebSocket-Accept", token);
		res.allowCORS();
		hr.writeResponse("101 Switching Protocols");
		return true;
	}
	
	/*protected void listen(WebSocket ws) throws IOException {
		InputStream in = ws.getInputStream();
		int limit = 100*1024;
		while(!ws.isClosed()) {
			byte[] message = null;
			try {
				message = WebSockets.readMessage(in, limit, true);
			} catch(Exception ex) {
				this.close(ws, ex);
				break;
			}
			if(message != null) {
				if(!this.onMessage(ws, new ByteArraySequence(message), true)) {
					break;
				}
			}
		}
		ws.closeSilent();
	}*/
	
	public void onDecoderException(WebSocket ws, Exception exc) {
		Iterator<WebSocketHandler> handlers = this.handlers.iterator();
		while(handlers.hasNext())
			handlers.next().onDecoderException(ws, exc);
	}
	
	public boolean onMessage(WebSocket ws, WebSocketMessage msg) {
		Iterator<WebSocketHandler> handlers = this.handlers.iterator();
		while(handlers.hasNext()) {
			boolean status = false;
			Exception ex = null;
			try {
				status = handlers.next().onMessage(ws, msg);
			} catch(IOException io) {
				ex = io;
			}
			if(!status) {
				ws.closeSilent(ex);
				return false;
			}
		}
		return true;
	}
	
	public boolean onHandshake(HttpRequest req) {
		Iterator<WebSocketHandler> handlers = this.handlers.iterator();
		while(handlers.hasNext()) {
			boolean status = false;
			Exception ex = null;
			try {
				status = handlers.next().onHandshake(req);
			} catch(IOException io) {
				ex = io;
			}
			if(!status) {
				if(ex != null) this.onDecoderException(null, ex);
				return false;
			}
		}
		return true;
	}
	
	public boolean onConnect(WebSocket ws) {
		Iterator<WebSocketHandler> handlers = this.handlers.iterator();
		while(handlers.hasNext()) try {
			handlers.next().onConnect(ws);
		} catch(IOException ex) {
			ws.closeSilent(ex);
			return false;
		}
		return true;
	}
	
	public boolean onPong(WebSocket ws, WebSocketMessage wmsg) {
		Iterator<WebSocketHandler> handlers = this.handlers.iterator();
		while(handlers.hasNext()) {
			boolean status = false;
			Exception ex = null;
			try {
				status = handlers.next().onPong(ws, wmsg);
			} catch(IOException io) {
				ex = io;
			}
			if(!status) {
				ws.closeSilent(ex);
				return false;
			}
		}
		return true;
	}
	
	public void onCloseAndError(WebSocket ws, Exception t) {
		Iterator<WebSocketHandler> handlers = this.handlers.iterator();
		while(handlers.hasNext()) {
			WebSocketHandler handler = handlers.next();
			if(t != null) handler.onDecoderException(ws, t);
			handler.onClose(ws, t);
		}
	}
	

	/*** NOT NEEDED: ***/
	@Override
	public boolean acceptRequest(HttpRequest req) {
		return true;
	}

	@Override
	public boolean finishAfterFirstLine(HttpRequest req) {
		return false;
	}

	@Override
	public boolean handleRequest(HttpRequest req) {
		return false;
	}

	@Override
	public void handleDecoderException(HttpRequest req, Exception ex) {}

	@Override
	public void cleanup(HttpRequest req, boolean status) {}

}
