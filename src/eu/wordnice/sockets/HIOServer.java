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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import eu.wordnice.api.Api;
import eu.wordnice.api.Handler;

public class HIOServer {
	
	public int port;
	public ServerSocket server;
	public long readtimeout;
	public boolean readpost;
	public Handler.ThreeVoidHandler<Thread, Throwable, HIO> exc_handler;
	public Thread thr;
	
	public HIOServer(String addr, int port) throws IOException {
		this.port = port;
		ServerSocket sock = new ServerSocket();
		sock.bind(new InetSocketAddress(addr, port));
		this.server = sock;
		this.readtimeout = 30000;
		this.readpost = true;
	}
	
	public HIOServer(ServerSocket sock, int port) {
		this.port = port;
		this.server = sock;
		this.readtimeout = 30000;
		this.readpost = true;
	}
	
	public boolean isClosed() {
		try {
			return this.server.isClosed();
		} catch(Throwable t) { }
		return true;
	}
	
	public Socket accept() {
		try {
			return this.server.accept();
		} catch(Throwable t) { }
		return null;
	}
	
	public void onAccept(Handler.TwoVoidHandler<Thread, HIO> ac, boolean nevThread, boolean readpost, long timeout, Handler.ThreeVoidHandler<Thread, Throwable, HIO> thr_handler) {
		this.readpost = readpost;
		this.readtimeout = timeout;
		this.exc_handler = thr_handler;
		this.onAccept(ac, nevThread);
	}
	
	public void onAccept(final Handler.TwoVoidHandler<Thread, HIO> ac, boolean nevThread) {
		while(this.isClosed() == false) {
			final Socket sock = this.accept();
			if(sock != null) {
				if(nevThread) {
					new Thread() {
						@Override
						public void run() {
							HIO hio = null;
							try {
								hio = new HIO(sock);
								Throwable t = hio.decode(HIOServer.this.readpost, HIOServer.this.readtimeout);
								if(t != null) {
									Api.throv(t);
								}
								ac.handle(this, hio);
							} catch(Throwable t) {
								if(HIOServer.this.exc_handler != null) {
									HIOServer.this.exc_handler.handle(this, t, hio);
								}
							}
						}
					}.start();
				} else {
					HIO hio = null;
					try {
						hio = new HIO(sock);
						Throwable t = hio.decode(this.readpost, this.readtimeout);
						if(t != null) {
							Api.throv(t);
						}
						ac.handle(null, hio);
					} catch(Throwable t) {
						if(this.exc_handler != null) {
							this.exc_handler.handle(null, t, hio);
						}
					}
				}
			}
		}
	}
	
	public void onAcceptRaw(final Handler.OneVoidHandler<Socket> ac) {
		while(this.isClosed() == false) {
			final Socket sock = this.accept();
			if(sock != null) {
				ac.handle(sock);
			}
		}
	}
	
	public boolean close() {
		if(this.server == null || this.isClosed()) {
			return false;
		}
		try {
			this.server.close();
			return true;
		} catch(Throwable t) {}
		return false;
	}
	
}
