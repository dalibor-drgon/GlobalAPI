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

package test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import wordnice.http.server.HttpRequest;
import wordnice.http.server.HttpServer;
import wordnice.http.server.Post;
import wordnice.http.server.RequestData;
import wordnice.http.server.HttpRequestHandler;
import wordnice.http.websocket.WebSocket;
import wordnice.http.websocket.WebSocketMessage;
import wordnice.http.websocket.WebSocketServer;
import wordnice.http.websocket.WebSocketServer.WebSocketHandler;
import wordnice.streams.IUtils;
import wordnice.streams.OUtils;
import wordnice.utils.Sockets;

public class HttpServerTest
implements HttpRequestHandler {
	
	@Override
	public boolean acceptRequest(HttpRequest req) {
		try {
			req.getSocket().setSoTimeout(5000);
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		req.setProperty("WebSocket", true);
		return true;
	}
	
	@Override
	public boolean handleRequest(HttpRequest req) {
		try {
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
			} catch(RuntimeException _ign) {
				//_ign.printStackTrace();
				
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
		} catch(Exception t) {
			t.printStackTrace();
		}
		return true;
	}

	@Override
	public void handleDecoderException(HttpRequest req, Exception ex) {
		System.out.println("Handle decoder exception:");
		ex.printStackTrace();
	}

	@Override
	public boolean finishAfterFirstLine(HttpRequest req) {
		return false;
	}

	@Override
	public boolean finishAfterHeads(HttpRequest req) {
		return false;
	}
	
	@Override
	public void cleanup(HttpRequest req, boolean status) {
		System.out.println("Cleanup: " + status+", " + req.getRequest() );
	}

	
	//TEST
	public static void main(String...args) throws Throwable {
		Test.main(args);

		System.out.println("Creating server socket...");
		HttpServer server = HttpServer.create(Sockets.createConfig("localhost", 8080));
		HttpRequestHandler handler = new HttpServerTest();
		System.out.println("Listening...");
		WebSocketServer ws = new WebSocketServer();
		ws.addHandler(new WebSocketHandler() {

			@Override
			public void onConnect(WebSocket ws) throws IOException {
				System.out.println("Connect " + ws);
				ws.getSocket().setSoTimeout(30*1000);
				ws.sendMessage("Hey!");
			}

			@Override
			public boolean onMessage(WebSocket ws, WebSocketMessage message) throws IOException {
				System.out.println("Message " + ws);
				System.out.println("\t"+message.isBinary() + " " + message.length() + "\t" + message);
				byte[] ping = new byte[8];
				OUtils.writeLong(ping, 0, System.nanoTime());
				ws.ping(ping);
				return true;
			}

			@Override
			public void onClose(WebSocket ws, Throwable t) {
				System.out.println("Close " + ws);
				if(t != null) t.printStackTrace();
			}

			@Override
			public boolean onDecoderException(WebSocket ws, Exception ex) {
				System.out.println("DecoderException " + ws);
				ex.printStackTrace();
				return false;
			}

			@Override
			public boolean onHandshake(HttpRequest req) throws IOException {
				System.out.println("OnHandshake " + req);
				return true;
			}

			@Override
			public boolean onPong(WebSocket ws, WebSocketMessage msg) throws IOException {
				System.out.println("OnPong " + ws + " -> " + msg);
				if(msg.length() == 8) {
					System.out.println((System.nanoTime()-IUtils.readLong(msg.array(), msg.offset()))/1000000.0 + "ms");
				}
				return true;
			}
		});
		server.addHandler(ws);
		server.addHandler(handler);
		server.setClientExecutorService(Executors.newFixedThreadPool(5));
		server.run();
		//server.listen(handler);
		
		//Never continue:
		server.close();
	}
	
}
