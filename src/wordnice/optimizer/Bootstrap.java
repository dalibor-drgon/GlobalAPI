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

package wordnice.optimizer;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import wordnice.api.Nice.BHandler;

public class Bootstrap {
	
	public static interface EntryFile {
		String getName();
		String getUniqueName();
		void writeTo(OutputStream out, byte[] buffer) throws IOException;
	}
	
	public static Handler createPrefixedHandler(String prefix, BHandler<String> handler) {
		return new PrefixedHandler(prefix, handler);
	}
	
	public static Handler createPrefixedHandler(String prefix) {
		return new PrefixedHandler(prefix, null);
	}
	
	public static Handler createBooleanHandler(BHandler<String> hand) {
		return new PrefixedHandler(null, hand);
	}
	
	public static Handler createAllHandler() {
		return new PrefixedHandler(null, null);
	}
	
	public static interface Handler {
		/**
		 * @param name Name of file to add
		 * @return Special name if given entry can be added, null otherwise
		 */
		String canBootstrap(String name);
	}
	
	static class PrefixedHandler
	implements Handler {

		protected String prefix;
		protected BHandler<String> handler;
		
		public PrefixedHandler(String prefix) {
			this(prefix, null);
		}
		
		public PrefixedHandler(BHandler<String> handler) {
			this(null, handler);
		}
		
		public PrefixedHandler(String prefix, BHandler<String> handler) {
			if(prefix == null || prefix.isEmpty()) prefix = null;
			this.prefix = prefix;
			this.handler = handler;
		}
		
		@Override
		public String canBootstrap(String name) {
			if(this.handler != null && !handler.handle(name)) return null;
			return (this.prefix == null) ? name : prefix+name;
		}
		
	}
	
	static abstract class Stream 
	implements Bootstrap.EntryFile, Closeable, AutoCloseable {

		protected String name;
		protected String unique;
		protected InputStream stream;
		
		protected Stream(String name, String unique) {
			this.name = name;
			this.unique = (unique == null) ? name : unique;
		}
		
		@Override
		public String getName() {
			return this.name;
		}
		
		@Override
		public String getUniqueName() {
			return this.unique;
		}

		@Override
		public void writeTo(OutputStream out, byte[] buffer) throws IOException {
			IOUtils.copyLarge(stream, out, buffer);
		}
		
		@Override
		public void close() throws IOException {
			if(stream != null) stream.close();
			stream = null;
		}
		
	}
	
	public static class FileStream extends Stream {

		protected File file;
		
		public FileStream(String name, String unique, File file) {
			super(name, unique);
			this.file = file;
		}
		
		@Override
		public void writeTo(OutputStream out, byte[] buffer) throws IOException {
			this.stream = new FileInputStream(this.file);
			this.file = null;
			try {
				super.writeTo(out, buffer);
			} finally {
				InputStream in = this.stream;
				this.stream = null;
				in.close();
			}
		}
		
	}
	
	public static class URLStream extends Stream {

		protected URL url;
		
		public URLStream(String name, String unique, URL url) {
			super(name, unique);
			this.url = url;
		}
		
		@Override
		public void writeTo(OutputStream out, byte[] buffer) throws IOException {
			this.stream = url.openStream();
			this.url = null;
			try {
				super.writeTo(out, buffer);
			} finally {
				InputStream in = this.stream;
				this.stream = null;
				in.close();
			}
		}
		
	}
	
	public static class ZIPStream extends Stream {

		protected File file;
		protected String entry;
		
		public ZIPStream(String name, String unique, File file, String entry) {
			super(name, unique);
			this.file = file;
			this.entry = entry;
		}
		
		@Override
		public void writeTo(OutputStream out, byte[] buffer) throws IOException {
			try(ZipFile zf = new ZipFile(this.file);
				InputStream in = zf.getInputStream(zf.getEntry(entry))) {
				IOUtils.copyLarge(in, out, buffer);
			} finally {
				this.file = null;
			}
		}
		
	}
	
}
