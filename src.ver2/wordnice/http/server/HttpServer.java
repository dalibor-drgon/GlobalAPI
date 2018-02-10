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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import wordnice.api.Nice;
import wordnice.coll.ConcurrentArray;
import wordnice.utils.Sockets;

public class HttpServer
implements Closeable, AutoCloseable, Runnable {

	public static HttpServer create(ServerSocket socket) {
		if(socket == null) throw new IllegalArgumentException("Server socket == null");
		return new HttpServer(socket);
	}
	
	public static HttpServer create(Sockets.Config cfg) 
			throws NoSuchAlgorithmException, SecurityException, IOException, GeneralSecurityException {
		if(cfg == null) throw new IllegalArgumentException("Server config == null");
		return new HttpServer(cfg.createServerSocket());
	}
	
	protected HttpServer(ServerSocket sock) {
		this.server = sock;
	}
	
	/** Server socket used for accepting **/
	protected ServerSocket server;
	
	/** Handlers for each new client **/
	protected ConcurrentArray<HttpRequestHandler> handlers = new ConcurrentArray<HttpRequestHandler>();
	
	/** ExecutorService used for accepting clients, parsing requests and handling them.
	 * If null, accepting, parsing and handling will be done in current thread **/
	protected ExecutorService clientExecutorService;
	
	/** ScheduledExecutorService useful for checks and protections addonds **/
	protected ScheduledExecutorService scheduledExecutorService;
	
	/** Collection with current requests **/
	protected Collection<HttpRequest> currentRequests;
	
	public ExecutorService getClientExecutorService() {
		return clientExecutorService;
	}

	public HttpServer setClientExecutorService(ExecutorService clientExecutorService) {
		this.clientExecutorService = clientExecutorService;
		return this;
	}
	
	protected ScheduledExecutorService getScheduledExecutorService() {
		if(this.scheduledExecutorService == null)
			this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
		return scheduledExecutorService;
	}
	
	public HttpServer initScheduledExecutorService(ScheduledExecutorService service) {
		if(this.scheduledExecutorService != null)
			throw new IllegalStateException("Cannot change scheduled executor service once loaded");
		if(service == null)
			throw new IllegalArgumentException("Given service is null!");
		this.scheduledExecutorService = service;
		return this;
	}
	
	public HttpServer schedule(Runnable r) {
		this.getScheduledExecutorService().submit(r);
		return this;
	}
	
	public HttpServer scheduleDelayed(Runnable r, long delay) {
		this.getScheduledExecutorService().schedule(r, delay, TimeUnit.MILLISECONDS);
		return this;
	}
	
	/*public HttpServer scheduleDelayed(Runnable r, long delay, TimeUnit tm) {
		this.getScheduledExecutorService().schedule(r, delay, tm);
		return this;
	}*/
	
	public HttpServer scheduleRepeating(Runnable rn, long period) {
		this.getScheduledExecutorService().scheduleWithFixedDelay(rn, period, period, TimeUnit.MILLISECONDS);
		return this;
	}
	
	/*public HttpServer scheduleRepeating(Runnable rn, long period, TimeUnit tm) {
		this.getScheduledExecutorService().scheduleWithFixedDelay(rn, period, period, tm);
		return this;
	}*/

	public Collection<HttpRequest> getCurrentRequests() {
		return currentRequests;
	}

	public HttpServer storeCurrentRequests(boolean storeThem) {
		if(storeThem) {
			if(this.currentRequests == null)
				this.currentRequests = Nice.createWeakSet();
		} else {
			this.currentRequests = null;
		}
		return this;
	}
	
	public ServerSocket getServerSocket() {
		return this.server;
	}
	
	public int getPort() {
		if(this.server == null) return -1;
		return this.server.getLocalPort();
	}
	
	public String getHostname() {
		InetAddress addr = getAddress();
		return (addr == null) ? null : addr.getHostName();
	}
	
	public InetAddress getAddress() {
		if(this.server == null) return null;
		return this.server.getInetAddress();
	}
	
	public Collection<HttpRequestHandler> getHandlers() {
		return this.handlers.snapshot();
	}
	
	public HttpServer addHandler(HttpRequestHandler hand) {
		this.handlers.add(hand);
		return this;
	}
	
	public HttpServer addHandlers(HttpRequestHandler... hand) {
		this.handlers.addAll(hand);
		return this;
	}
	
	public HttpServer addHandlers(Collection<HttpRequestHandler> hand) {
		this.handlers.addAll(hand);
		return this;
	}
	
	public boolean isClosed() {
		return this.server == null || !this.server.isBound() || this.server.isClosed();
	}
	
	public Socket accept() {
		if(this.server != null) try {
			return this.server.accept();
		} catch(IOException t) {}
		return null;
	}
	
	protected static boolean process(Iterable<HttpRequestHandler> handlers, Socket socketClient) {
		HttpRequest req = null;
		Exception decoderException = null;
		do {
			//Init
			try {	req = new HttpRequest(socketClient);
			} catch(Exception ex) {
				decoderException = ex; break;
			}
			for(HttpRequestHandler handler : handlers)
				if(!handler.acceptRequest(req)) break;
			
			//First line
			try {	req.parseFirstLine();
			} catch(Exception ex) {
				decoderException = ex; break;
			}
			for(HttpRequestHandler handler : handlers)
				if(handler.finishAfterFirstLine(req)) break;
			
			//Heads
			try {	req.parseHeads();
			} catch(Exception ex) {
				decoderException = ex; break;
			}
			for(HttpRequestHandler handler : handlers)
				if(handler.finishAfterHeads(req)) break;
			
			//Post if needed
			try {	req.parsePost();
			} catch(Exception ex) {
				decoderException = ex; break;
			}
			//.. and finish
			for(HttpRequestHandler handler : handlers)
				if(handler.handleRequest(req)) break;
		} while(false);
		
		boolean status = decoderException == null;
		for(HttpRequestHandler handler : handlers) {
			if(decoderException != null)
				handler.handleDecoderException(req, decoderException);
			handler.cleanup(req, status);
		}
		
		if(req != null && (req.closeOnFinish() || !status)) {
			try {
				req.close();
			} catch(IOException tign) {}
		}
		return status;
	}
	
	@Override
	public void run() {
		if(this.isClosed()) throw new IllegalStateException("Server closed");
		this.listen(this.getClientExecutorService());
	}
	
	protected void listenSingleThreaded() {
		while(!this.isClosed()) {
			final Socket socketClient = this.accept();
			if(socketClient == null) continue;
			process(this.getHandlers(), socketClient);
		}
	}
	
	protected void listen(ExecutorService exec) {
		if(exec == null) {
			this.listenSingleThreaded();
			return;
		}
		while(!this.isClosed()) {
			final Socket socketClient = this.accept();
			if(socketClient == null) continue;
			exec.submit(new RequestCallable(socketClient, this.getHandlers()));
		}
	}
	
	public void acceptTo(Consumer<Socket> cons) {
		if(cons == null) throw new IllegalArgumentException("Consumer == null");
		while(!this.isClosed()) {
			Socket sock = this.accept();
			if(sock != null) cons.accept(sock);
		}
	}
	
	@Override
	public void close() throws IOException {
		if(this.server == null) return;
		try {
			this.server.close();
		} finally {
			this.server = null;
		}
	}

	@Override
	public String toString() {
		return "HttpServer [getPort()=" + getPort() + ", getHostname()=" + getHostname() + ", getHandlers()="
				+ getHandlers() + ", isClosed()=" + isClosed() + "]";
	}
	
	protected static class RequestCallable
	implements Callable<Boolean> {

		protected Socket client = null;
		protected Collection<HttpRequestHandler> handlers = null;
		
		protected RequestCallable(Socket socket, Collection<HttpRequestHandler> handlers) {
			this.client = socket;
			this.handlers = handlers;
		}
		
		@Override
		public Boolean call() throws Exception {
			if(!HttpServer.process(handlers, client))
				throw new RuntimeException("Failed");
			return true;
		}
		
	}
	
}
