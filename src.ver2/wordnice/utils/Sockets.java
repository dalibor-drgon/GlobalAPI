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

package wordnice.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import wordnice.api.Nice;

public class Sockets {
	
	public static Config createConfig(int port) {
		return new Config(null, port);
	}
	
	public static Config createConfig(String host, int port) {
		return new Config(host, port);
	}
	
	public static Config createConfigSSL(int port,
			String keypass, String storepass, String keyfile) {
		return new Config(null, port, keypass, storepass, keyfile);
	}
	
	public static Config createConfigSSL(String host, int port,
			String keypass, String storepass, String keyfile) {
		return new Config(host, port, keypass, storepass, keyfile);
	}
	
	public static Config createConfigFromMap(Map<String,Object> props)
			throws IllegalArgumentException {
		return new Config(props);
	}
	
	public static class Config {

		protected boolean ssl = false;
		protected int port = 0;
		protected String host = "localhost";
		protected String keyPass = null;
		protected String storePass = null;
		protected String keyStoreFile = null;
		protected int pending = -1;
		
		public Config(String addr, int port) {
			if(port < 1 || port > Short.MAX_VALUE)
				throw Nice.illegal("Illegal port number " + port);
			
			this.ssl = false;
			this.host = addr;
			this.port = port;
		}
		
		public Config(String addr, int port, String keypass, String storepass, String keyfile) {
			if(port < 1 || port > Short.MAX_VALUE)
				throw Nice.illegal("Illegal port number " + port);
			if(keypass == null) throw Nice.illegal("Key password = null");
			if(storepass == null) throw Nice.illegal("Store password = null");
			if(keyfile == null) throw Nice.illegal("Key store file = null");
			this.ssl = true;
			this.host = addr;
			this.port = port;
			this.keyPass = keypass;
			this.storePass = storepass;
			this.keyStoreFile = keyfile;
		}
		
		public Config(Map<String,Object> props)
					throws IllegalArgumentException {
			String host = Nice.cast(props.get("Host"), String.class);
			if(host == null || host.isEmpty()) {
				host = null;
			}
			int port = Nice.cast(props.get("Port"), int.class);
			if(port < 1) {
				throw Nice.illegal("Unknown port (Port) " + port);
			}
			this.pending = Nice.cast(props.get("Pendings"), int.class, -1);
			boolean ssl = Nice.cast(props.get("SSL"), boolean.class);
			if(ssl) {
				String keyPass = Nice.cast(props.get("KeyPass"), String.class);
				String storePass = Nice.cast(props.get("StorePass"), String.class);
				String keyFile = Nice.cast(props.get("KeyStoreFile"), String.class);
				if(keyPass == null) {
					throw Nice.illegal("Unknown key password (KeyPass)");
				}
				if(storePass == null) {
					throw Nice.illegal("Unknown store password (StorePass)");
				}
				if(keyFile == null) {
					throw Nice.illegal("Unknown key store file (KeyStoreFile)");
				}
				this.ssl = true;
				this.host = host;
				this.port = port;
				this.keyPass = keyPass;
				this.keyStoreFile = keyFile;
				this.storePass = storePass;
			}
			
			if(!ssl) {
				this.ssl = false;
				this.host = host;
				this.port = port;
			}
		}

		public boolean isSsl() {
			return ssl;
		}

		public int getPort() {
			return port;
		}

		public String getHost() {
			return host;
		}

		public String getKeyPass() {
			return keyPass;
		}

		public String getStorePass() {
			return storePass;
		}

		public String getKeyStoreFile() {
			return keyStoreFile;
		}
		
		//pendings. 0 or smaller = default value
		public int getMaximumClients() {
			return this.pending;
		}
		
		public Socket createSocket() 
				throws NoSuchAlgorithmException, IOException, GeneralSecurityException, SecurityException {
			return Sockets.createSocket(this);
		}
		
		public ServerSocket createServerSocket() 
				throws NoSuchAlgorithmException, IOException, GeneralSecurityException, SecurityException {
			return Sockets.createServerSocket(this);
		}
		
	}

	/**
	 * Sockets
	 */
	
	public static Socket createSocket(Config config) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		if(config.ssl) {
			return createSocket(config.host, config.port, config.keyPass, 
					config.storePass, config.keyStoreFile);
		}
		return createSocket(config.host, config.port);
	}
	
	public static Socket createSocket(int port) throws IOException {
		return createSocket(new InetSocketAddress("localhost", port));
	}
	
	public static Socket createSocket(String addr, int port) throws IOException {
		if(addr == null) addr = "localhost";
		return createSocket(new InetSocketAddress(addr, port));
	}
	
	public static Socket createSocket(SocketAddress addr) throws IOException {
		Socket sk = new Socket();
		sk.connect(addr);
		return sk;
	}
	
	public static Socket createSocket(String addr, int port, String keypass, String storepass, String keystorefile) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		if(addr == null) addr = "localhost";
		return createSocket(new InetSocketAddress(addr, port), keypass, storepass, keystorefile);
	}
	
	public static Socket createSocket(SocketAddress addr, String keypass, String storepass, String keystorefile) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		SSLSocket sock = (SSLSocket) createSSL(keypass, storepass, keystorefile)
					.getSocketFactory().createSocket();
		sock.connect(addr);
		return sock;
	}
	
	/**
	 * Server sockets
	 */
	
	public static ServerSocket createServerSocket(Config config) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		if(config.ssl) {
			return createServerSocket(config.host, config.port, config.keyPass, 
					config.storePass, config.keyStoreFile, config.pending);
		}
		return createServerSocket(config.host, config.port, config.pending);
	}
	
	public static ServerSocket createServerSocket(int port) throws IOException {
		return createServerSocket(new InetSocketAddress((String)null, port));
	}
	
	public static ServerSocket createServerSocket(int port, int pending) throws IOException {
		return createServerSocket(new InetSocketAddress((String)null, port), pending);
	}
	
	public static ServerSocket createServerSocket(String addr, int port) throws IOException {
		return createServerSocket(new InetSocketAddress(addr, port));
	}
	
	public static ServerSocket createServerSocket(String addr, int port, int pending) throws IOException {
		return createServerSocket(new InetSocketAddress(addr, port), pending);
	}
	
	public static ServerSocket createServerSocket(SocketAddress addr) throws IOException {
		ServerSocket sk = new ServerSocket();
		sk.bind(addr);
		return sk;
	}
	
	public static ServerSocket createServerSocket(SocketAddress addr, int pending) throws IOException {
		ServerSocket sk = new ServerSocket();
		sk.bind(addr, pending);
		return sk;
	}
	
	public static ServerSocket createServerSocket(
			String addr, int port, String keypass, String storepass, String keystorefile) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		return createServerSocket(new InetSocketAddress(addr, port), keypass, storepass, keystorefile);
	}
	
	public static ServerSocket createServerSocket(
			String addr, int port, String keypass, String storepass, String keystorefile, int pen) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		return createServerSocket(new InetSocketAddress(addr, port), keypass, storepass, keystorefile, pen);
	}
	
	public static ServerSocket createServerSocket(
			SocketAddress addr, String keypass, String storepass, String keystorefile) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		SSLServerSocket sock = (SSLServerSocket) createSSL(keypass, storepass, keystorefile)
					.getServerSocketFactory().createServerSocket();
		sock.bind(addr);
		return sock;
	}
	
	public static ServerSocket createServerSocket(
			SocketAddress addr, String keypass, String storepass, String keystorefile, int pen) 
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		SSLServerSocket sock = (SSLServerSocket) createSSL(keypass, storepass, keystorefile)
					.getServerSocketFactory().createServerSocket();
		sock.bind(addr, pen);
		return sock;
	}
	
	/**
	 * Create SSLContext for given certificate
	 */
	public static SSLContext createSSL(String keypass, String storepass, String keystorefile)
			throws IOException, GeneralSecurityException, SecurityException, NoSuchAlgorithmException {
		KeyStore kstore = KeyStore.getInstance("JKS");
		kstore.load(new FileInputStream(keystorefile), keypass.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(kstore, storepass.toCharArray());
		
		SSLContext sslcx = SSLContext.getInstance("TLS");
		sslcx.init(kmf.getKeyManagers(), null, null);
		return sslcx;
	}
	
}
