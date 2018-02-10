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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import wordnice.api.Nice;
import wordnice.http.server.RequestData;
import wordnice.seq.ByteArraySequence;
import wordnice.seq.CharArraySequence;
import wordnice.streams.ArrayOutputStream;
import wordnice.utils.NiceStrings;

public class WebSocket
implements Closeable, AutoCloseable {

	public WebSocket createWebSocket(Socket socket, OutputStream out, 
			InputStream in, RequestData req, WebSocketServer webSocketServer) {
		return new WebSocket(socket, out, in, req, webSocketServer);
	}
	
	protected WebSocket(Socket socket, OutputStream out, InputStream in, RequestData req, WebSocketServer webSocketServer) {
		this.socket = socket;
		this.outputStream = out;
		this.inputStream = in;
		this.initRequest = req;
		this.webSocketServer = webSocketServer;
	}

	@Override
	public String toString() {
		return "WebSocket [socket=" + socket + "]";
	}

	protected Socket socket;
	protected OutputStream outputStream;
	protected InputStream inputStream;
	protected RequestData initRequest;
	protected WebSocketServer webSocketServer;
	
	protected boolean answerClose = true;
	
	public Socket getSocket() {
		return socket;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public RequestData getInitRequest() {
		return initRequest;
	}
	
	public boolean isAnswerClose() {
		return answerClose;
	}

	public WebSocketServer getWebSocketServer() {
		return webSocketServer;
	}
	
	public WebSocket setAnswerClose(boolean answerClose) {
		this.answerClose = answerClose;
		return this;
	}
	
	
	/************ STRINGS *****************/
	
	public void sendMessage(char[] seq) throws IOException {
		if(seq == null) throw new IllegalArgumentException("Sequence == null");
		this.sendMessage(seq, 0, seq.length);
	}
	
	public void sendMessage(char[] seq, int off, int len) throws IOException {
		sendMessage(NiceStrings.toBytes(seq, off, len), false);
	}
	
	public void sendMessage(CharSequence seq) throws IOException {
		if(seq == null) throw new IllegalArgumentException("Sequence == null");
		this.sendMessage(seq, 0, seq.length());
	}
	
	public void sendMessage(CharSequence seq, int off, int len) throws IOException {
		if(seq instanceof CharArraySequence) {
			CharArraySequence chseq = (CharArraySequence) seq;
			Nice.checkBounds(chseq, off, len);
			sendMessage(NiceStrings.toBytes(chseq.array(), chseq.offset()+off, len), false);
		} else {
			sendMessage(NiceStrings.toBytes(seq, off, len), false);
		}
	}
	
	
	/****************** BYTES **************/
	
	public void sendMessage(ByteArraySequence seq) throws IOException {
		if(seq == null) throw new IllegalArgumentException("Sequence == null");
		this.sendMessage(seq.array(), seq.offset(), seq.length(), true);
	}
	
	public void sendMessage(ByteArraySequence seq, boolean isBinary) throws IOException {
		if(seq == null) throw new IllegalArgumentException("Sequence == null");
		this.sendMessage(seq.array(), seq.offset(), seq.length(), isBinary);
	}
	
	public void sendMessage(byte[] bytes) throws IOException {
		if(bytes == null) throw new IllegalArgumentException("Bytes == null");
		this.sendMessage(bytes, 0, bytes.length, true);
	}
	
	public void sendMessage(byte[] bytes, boolean isBinary) throws IOException {
		if(bytes == null) throw new IllegalArgumentException("Bytes == null");
		this.sendMessage(bytes, 0, bytes.length, isBinary);
	}
	
	public void sendMessage(byte[] bytes, int off, int len) throws IOException {
		this.sendMessage(bytes, off, len, true);
	}
	
	public void sendMessage(byte[] bytes, int off, int len, boolean isBinary) throws IOException {
		this.sendMessage(bytes, off, len, (isBinary) ? 130 : 129);
	}
	
	public void sendMessage(byte[] bytes, int off, int len, int flags) throws IOException {
		byte[] out = WebSockets.createUnmaskedFrame(bytes, off, len, flags);
		this.outputStream.write(out);
		this.outputStream.flush();
	}
	
	public void ping() throws IOException {
		this.ping(null, 0, 0);
	}
	
	public void ping(ByteArraySequence seq) throws IOException {
		if(seq == null) this.ping(null, 0, 0);
		else this.ping(seq.array(), seq.offset(), seq.length());
	}
	
	public void ping(byte[] bytes) throws IOException {
		if(bytes == null) this.ping(null, 0, 0);
		this.ping(bytes, 0, bytes.length);
	}
	
	public void ping(byte[] bytes, int off, int len) throws IOException {
		//TODO
		WebSockets.sendControlFrame(outputStream, WebSockets.FFin | WebSockets.OPing, null, bytes, off, len);
	}

	@Override
	public void close() throws IOException {
		this.close(null);
	}
	
	public void close(Exception ex) throws IOException {
		try {
			if(this.webSocketServer != null)
				this.webSocketServer.onCloseAndError(this, ex);
		} finally {
			if(this.inputStream != null) {
				try {
					this.inputStream.close();
				} catch(IOException t) {}
			}
			if(this.outputStream != null) {
				if(this.isAnswerClose()) {
					try {
						WebSockets.sendControlFrame(this.outputStream, WebSockets.FFin | WebSockets.OClose);
					} catch(IOException ign) {}
				}
				try {
					this.outputStream.flush();
				} catch(IOException t) {}
				try {
					this.outputStream.close();
				} catch(IOException t) {}
			}
			try {
				if(this.socket != null) this.socket.close();
			} finally {
				this.webSocketServer = null;
				this.initRequest = null;
				this.inputStream = null;
				this.outputStream = null;
				this.socket = null;
			}
		}
	}
	
	public void closeSilent() {
		try {
			this.close();
		} catch(IOException io) {}
	}
	
	public void closeSilent(Exception t) {
		try {
			this.close(t);
		} catch(IOException io) {}
	}

	public boolean isClosed() {
		if(this.socket == null) return true;
		return !this.socket.isConnected() || this.socket.isClosed();
	}
	
	protected ArrayOutputStream messagePart = null;
	protected boolean isBinary = true;
	
	public void listen() throws IOException {
		WebSocketServer server = this.webSocketServer;
		InputStream in = this.getInputStream();
		OutputStream out = this.getOutputStream();
		int limit = server.getReadLimit();
		byte[] mask = new byte[4];
		Nice.secureGenerator().nextBytes(mask);
		while(!this.isClosed()) {
			int flags = in.read();
			if(flags == -1) throw new EOFException();
			int opcode = flags & 0xF;
			flags >>>= 4;
			boolean rsv3 = (flags & 1) == 1;
			boolean rsv2 = (flags & 2) == 2;
			boolean rsv1 = (flags & 4) == 4;
			if(rsv1 || rsv2 || rsv3) //TODO
				throw new WebSocketException("Extensions not supported");
			boolean fin = (flags & 8) == 8;
			switch(opcode) {
				case 0:
					if(messagePart == null)
						throw new WebSocketException("Continous message without start");
					this.messagePart = WebSockets.readMessage(in, this.messagePart, limit, true, mask);
					if(fin) {
						ArrayOutputStream localPart = this.messagePart;
						this.messagePart = null;
						if(!server.onMessage(this, new WebSocketMessage(localPart.toByteArray(), isBinary))) return;
					}
					break;
				case 1:
				case 2:
					if(messagePart != null)
						throw new WebSocketException("New message without finishing before");
					if(fin) {
						if(!server.onMessage(this, new WebSocketMessage(WebSockets.readMessage(in, limit, true, mask), opcode == 2))) return;
					} else {
						this.messagePart = WebSockets.readMessage(in, this.messagePart, limit, true, mask);
						this.isBinary = (opcode == 2);
					}
					break;
				case 8:
					//Closed after listen
					return;
				case 9:
					WebSockets.sendControlFrame(out, WebSockets.FFin | WebSockets.OPong, null, WebSockets.readControlMessage(in, true, mask));
					break;
				case 0xA:
					if(!server.onPong(this, new WebSocketMessage(WebSockets.readControlMessage(in, true, mask), true))) return;
					break;
				default:
					throw new WebSocketException("Unknown opcode " + opcode);
			}
		}
	}
	
}
