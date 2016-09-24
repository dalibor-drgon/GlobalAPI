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

package wordnice.http.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLServerSocket;

import wordnice.http.server.HttpRequest.RequestHandler;
import wordnice.utils.Sockets;

public class HttpServer {
	
	public static interface SocketHandler {
		void handle(Socket client);
	}
	
	public static interface Handler extends RequestHandler {
		
		/**
		 * Prepare new client
		 * eg. set socket timeout, log IP etc...
		 * Called before parseRequest()
		 */
		public void prepareRequest(HttpRequest req) throws Exception;
		
		/**
		 * Handle new client
		 */
		public void handleRequest(HttpRequest req) throws Exception;
		
		/**
		 * Handle exception which occured while handling request by you
		 */
		public void handleException(HttpRequest req, Exception ex);
		
		/**
		 * Handle exception which occured while decoding request by HIORequest
		 */
		public void handleDecoderException(HttpRequest req, Exception ex);
		
	}

	
	protected ServerSocket _server;
	protected Handler _handler = null;
	
	public HttpServer(Sockets.Config config) throws IOException, GeneralSecurityException, NoSuchAlgorithmException {
		this(config.createServerSocket());
	}
	
	public HttpServer(String addr, int port) throws IOException {
		this.init(addr, port);
	}
	
	public HttpServer(SocketAddress addr) throws IOException {
		this.init(addr);
	}
	
	public HttpServer(String addr, int port, String keypass, String storepass, String keystorefile) throws IOException, GeneralSecurityException, NoSuchAlgorithmException {
		init(addr, port, keypass, storepass, keystorefile);
	}
	
	public HttpServer(SocketAddress addr, String keypass, String storepass, String keystorefile) throws IOException, GeneralSecurityException, NoSuchAlgorithmException {
		init(addr, keypass, storepass, keystorefile);
	}
	
	protected void init(String addr, int port) throws IOException {
		this.init(new InetSocketAddress(addr, port));
	}
	
	protected void init(SocketAddress addr) throws IOException {
		ServerSocket sock = new ServerSocket();
		sock.bind(addr);
		this._server = sock;
	}
	
	protected void init(String addr, int port, String keypass, String storepass, String keystorefile) 
			throws IOException, GeneralSecurityException, NoSuchAlgorithmException {
		this.init(new InetSocketAddress(addr, port), keypass, storepass, keystorefile);
	}
	
	protected void init(SocketAddress addr, String keypass, String storepass, String keystorefile) 
			throws IOException, GeneralSecurityException, NoSuchAlgorithmException {
		SSLServerSocket server = (SSLServerSocket) 
				Sockets.createSSL(keypass, storepass, keystorefile)
				.getServerSocketFactory().createServerSocket();
		server.bind(addr);
		this._server = server;
	}
	
	public HttpServer(ServerSocket sock) {
		this._server = sock;
	}
	
	public ServerSocket getServerSocket() {
		return this._server;
	}
	
	public Handler getHandler() {
		return this._handler;
	}
	
	public HttpServer setHandler(Handler handler) {
		this._handler = handler;
		return this;
	}
	
	protected void checkHandler() {
		if(this._handler == null) {
			throw new IllegalStateException("Please, call HIOServer.setHandler(...) before accepting!");
		}
	}
	
	public int getPort() {
		return this._server.getLocalPort();
	}
	
	public String getHostname() {
		return this._server.getInetAddress().getHostName();
	}
	
	public boolean isClosed() {
		try {
			return this._server.isClosed();
		} catch(Throwable t) { }
		return true;
	}
	
	public Socket accept() {
		try {
			return this._server.accept();
		} catch(IOException t) {}
		return null;
	}
	
	public void onAccept() {
		onAccept(true);
	}
	
	public void onAccept(boolean multiThread) {
		checkHandler();
		while(this.isClosed() == false) {
			final Socket sock = this.accept();
			if(sock != null) {
				if(multiThread) {
					Thread thr = new Thread() {
						@Override
						public void run() {
							HttpRequest hio = null;
							try {
								hio = new HttpRequest(sock);
								_handler.prepareRequest(hio);
								if(hio.parseRequest(_handler) != null) {
									try {
										_handler.handleRequest(hio);
									} catch(Exception ex) {
										_handler.handleException(hio, ex);
									}
								}
							} catch(Exception t) {
								_handler.handleDecoderException(hio, t);
							}
							try {
								hio.close();
							} catch(Exception tign) {}
						}
					};
					thr.setName("HIOServer:" + this.getPort());
					thr.start();
				} else {
					HttpRequest hio = null;
					try {
						hio = new HttpRequest(sock);
						_handler.prepareRequest(hio);
						if(hio.parseRequest(_handler) != null) {
							try {
								_handler.handleRequest(hio);
							} catch(Exception ex) {
								_handler.handleException(hio, ex);
							}
						}
					} catch(Exception t) {
						_handler.handleDecoderException(hio, t);
					}
					try {
						hio.close();
					} catch(Exception tign) {}
				}
			}
		}
	}
	
	public void onAcceptRaw(final SocketHandler handler) {
		while(!this.isClosed()) {
			Socket sock = this.accept();
			if(sock != null) {
				handler.handle(sock);
			}
		}
	}
	
	public boolean close() {
		if(this._server == null || this.isClosed()) {
			return false;
		}
		try {
			this._server.close();
			return true;
		} catch(Throwable t) {}
		return false;
	}
	
}
