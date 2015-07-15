package eu.wordnice.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HIOServer {
	
	public int port;
	public ServerSocket server;
	public long readtimeout;
	public boolean readpost;
	
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
	
	public void onAccept(HIOListener ac, boolean readpost, long timeout) {
		this.readpost = readpost;
		this.readtimeout = timeout;
		this.onAccept(ac);
	}
	
	public void onAccept(HIOListener ac) {
		Socket sock = null;
		HIO hio;
		while(this.isClosed() == false) {
			sock = this.accept();
			if(sock != null) {
				try {
					hio = new HIO(sock);
					Throwable t = hio.decode(this.readpost, this.readtimeout);
					if(t != null) {
						throw t;
					}
					ac.onAccept(hio);
				} catch(Throwable t) {
					System.out.println("HIO Socket server: " + t.getMessage());
				}
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
		} catch(Throwable t) { }
		return false;
	}
	
}
