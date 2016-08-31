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

package wordnice.http.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import wordnice.api.Nice;

public abstract class AbstractPost implements Post {
	
	protected static final byte[] EMPTY = new byte[0];
	
	protected String contentType = null;
	protected String fileName = null;
	protected Map<String,String> heads = null;
	
	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#getContentType()
	 */
	@Override
	public String getContentType() {
		return this.contentType;
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#getFileName()
	 */
	@Override
	public String getFileName() {
		return this.fileName;
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#setContentType(java.lang.String)
	 */
	@Override
	public Post setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#setFileName(java.lang.String)
	 */
	@Override
	public Post setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#getOrCreateHeads()
	 */
	@Override
	public Map<String,String> getOrCreateHeads() {
		if(heads == null) {
			heads = Nice.createMap();
		}
		return heads;
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#getHeads()
	 */
	@Override
	public Map<String,String> getHeads() {
		return heads;
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#getHead(java.lang.String)
	 */
	@Override
	public String getHead(String key) {
		if(heads == null) {
			return null;
		}
		return heads.get(key);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.sockets.Post#setHeads(java.util.Map)
	 */
	@Override
	public Post setHeads(Map<String,String> heads) {
		this.heads = heads;
		return this;
	}
	
	
	public static class MultipartPost 
	extends OutputStream implements Post {

		protected String contentType = null;
		protected String fileName = null;
		protected Map<String,String> heads = null;
		protected boolean isFile = false;
		protected ByteArrayOutputStream baos;
		protected byte[] content;
		protected boolean isEditing = true;
		
		protected File file = null;
		protected FileOutputStream stream = null;
		protected int maxsizeForMemory = 0;
		
		public MultipartPost(boolean isFile, int maxsize) {
			this.isFile = isFile;
			this.maxsizeForMemory = maxsize;
			if(!isFile) {
				baos = Nice.baos();
			}
		}
		
		@Override
		public void write(int b) throws IOException {
			write(new byte[]{(byte) b}, 0, 1);
		}
		
		public void write(byte[] bytes, int off, int len) throws FileNotFoundException, IOException {
			Nice.checkBounds(bytes, off, len);
			if(!isEditing || len == 0) {
				return;
			}
			if(isFile) {
				openStream().write(bytes, off, len);
			} else {
				if((len + baos.size()) > maxsizeForMemory) {
					isFile = true;
					write(bytes, off, len);
					return;
				}
				baos.write(bytes, off, len);
			}
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#writeTo(java.io.OutputStream)
		 */
		@Override
		public void writeTo(OutputStream out) throws IOException {
			if(isFile) {
				try(InputStream in = new FileInputStream(getOrCreateFile())) {
					long size = this.size();
					byte[] buffer = new byte[(size > 40000) ? 32*1024 : (int)Math.min(size, 4*1024)];
					IOUtils.copyLarge(in, out, buffer);
				}
			} else {
				out.write(content);
			}
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#size()
		 */
		@Override
		public long size() {
			try {
				return (this.isFile) ? getFile().length() : content.length;
			} catch (IOException e) {
				System.err.println("Error while getting length for temporary post file: ");
				e.printStackTrace();
				return 0;
			}
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getContent()
		 */
		@Override
		public byte[] getContent() {
			return content;
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getFile()
		 */
		@Override
		public File getFile() throws IOException {
			return (this.isFile) ? getOrCreateFile() : this.file;
		}
		
		public File getOrCreateFile() throws IOException {
			if(!this.isFile) {
				return null;
			}
			if(!this.isEditing) {
				return this.file;
			}
			if(this.file == null) {
				this.file = File.createTempFile("webserver_file", ".bin.tmp");
				if(baos != null) {
					openStream().write(baos.toByteArray());
					try {
						baos.close();
					} catch(IOException e) {}
					baos = null;
				}
				this.isFile = true;
			}
			return this.file;
		}
		
		public FileOutputStream openStream() throws FileNotFoundException, IOException {
			if(!this.isFile) {
				return null;
			}
			if(!this.isEditing) {
				return null;
			}
			if(this.stream == null) {
				this.stream = new FileOutputStream(getOrCreateFile());
			}
			return this.stream;
		}
		
		public void finish() {
			if(!this.isEditing) return;
			this.isEditing = false;
			
			if(isFile) {
				if(this.file == null || this.stream == null) {
					if(this.file != null) try {
						this.file.delete();
					} catch(Exception ign) {}
					this.isFile = false;
					this.file = null;
					this.stream = null;
					this.content = EMPTY;
				}
				if(this.stream != null) {
					try {
						this.stream.close();
					} catch(Exception e) {
						System.err.println("Error while closing stream for temporary file: ");
						e.printStackTrace();
					}
				}
			} else {
				content = baos.toByteArray();
				try {
					baos.close();
				} catch(IOException e) {}
			}
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#isFile()
		 */
		@Override
		public boolean isFile() {
			return this.isFile;
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getContentType()
		 */
		@Override
		public String getContentType() {
			return this.contentType;
		}

		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getFileName()
		 */
		@Override
		public String getFileName() {
			return this.fileName;
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#setContentType(java.lang.String)
		 */
		@Override
		public Post setContentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#setFileName(java.lang.String)
		 */
		@Override
		public Post setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getOrCreateHeads()
		 */
		@Override
		public Map<String,String> getOrCreateHeads() {
			if(heads == null) {
				heads = Nice.createMap();
			}
			return heads;
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getHeads()
		 */
		@Override
		public Map<String,String> getHeads() {
			return heads;
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#getHead(java.lang.String)
		 */
		@Override
		public String getHead(String key) {
			if(heads == null) {
				return null;
			}
			return heads.get(key);
		}
		
		/* (non-Javadoc)
		 * @see eu.wordnice.sockets.Post#setHeads(java.util.Map)
		 */
		@Override
		public Post setHeads(Map<String,String> heads) {
			this.heads = heads;
			return this;
		}
		
	};

	public static class PostBytes extends AbstractPost {

		protected byte[] bytes;
		
		public PostBytes(byte[] bytes) {
			this.bytes = bytes;
		}
		
		@Override
		public boolean isFile() {
			return false;
		}

		@Override
		public long size() {
			return bytes.length;
		}

		@Override
		public byte[] getContent() {
			return bytes;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			out.write(bytes);
		}

		@Override
		public File getFile() throws IOException {
			return null;
		}
		
	};
	
}