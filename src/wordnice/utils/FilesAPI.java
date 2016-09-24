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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilesAPI {

	/*** Files ***/
	
	
	public static String getExtension(String file) {
		if(file == null) {
			return null;
		}
		int l = file.lastIndexOf('/');
		if(l == -1) {
			l = 0;
		}
		int i = file.indexOf('.', l);
		if(i < 0 || (i + 1) == file.length()) {
			return null;
		}
		return file.substring((i + 1), file.length());
	}
	
	public static String getLastExtension(String file) {
		if(file == null) {
			return null;
		}
		int l = file.lastIndexOf('/');
		int i = file.lastIndexOf('.');
		if(i < 0 || (i + 1) == file.length() || l > i) {
			return null;
		}
		return file.substring((i + 1), file.length());
	}
	
	public static File getFreeName(File f) {
		if(f == null) {
			return null;
		}
		return getFreeName(f.getAbsoluteFile());
	}
	
	public static File getFreeName(String old) {
		if(old == null) {
			return null;
		}
		long i = 2;
		File nev = null;
		while(true) {
			nev = new File(old + i);
			if(!nev.exists()) {
				return nev;
			}
			i++;
		}
	}
	
	public static void createFile(File file) throws IOException {
		if(!file.exists()) {
			createDirForFile(file);
			file.createNewFile();
		}
	}
	
	public static File createDirForFile(File file) {
		if(file == null) {
			return null;
		}
		try {
			file = file.getCanonicalFile();
		} catch(Throwable t) {
			file = file.getAbsoluteFile();
		}
		File par = file.getParentFile();
		if(par != null && !par.exists()) {
			par.mkdirs();
		}
		return par;
	}
	
	public static void moveFile(File from, File to) throws IOException {
		if(!from.renameTo(to)) {
			byte[] buff = new byte[(int) Math.min(from.length(), 16384)];
			InputStream in = new FileInputStream(from);
			OutputStream out = new FileOutputStream(to);
			int cur = 0;
			while((cur = in.read(buff)) > 0) {
				out.write(buff, 0, cur);
			}
			in.close();
			out.close();
		}
		if(from.exists()) {
			from.delete();
		}
	}
	
	public static void read(InputStream in, OutputStream out) throws IOException {
		byte[] buff = new byte[16384];
		int cur = 0;
		while((cur = in.read(buff)) > 0) {
			out.write(buff, 0, cur);
		}
	}
	
	public static void read(File file, OutputStream out) throws IOException {
		FileInputStream in = new FileInputStream(file);
		try {
			byte[] buff = new byte[16384];
			int cur = 0;
			while((cur = in.read(buff)) > 0) {
				out.write(buff, 0, cur);
			}
		} catch(IOException ioe) {
			try {
				in.close();
			} catch(Exception ign) {}
			throw ioe;
		}
		in.close();
	}
	
	
	public static String readFileChars(File file) throws IOException {
		Reader in = new InputStreamReader(new FileInputStream(file));
		String out = null;
		try {
			out = readInputChars(in);
		} catch(Exception io) {
			try {
				in.close();
			} catch(Exception ex) {}
			throw io;
		}
		in.close();
		return out;
	}
	
	public static String readInputChars(Reader read) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[16384];
		int cur;
		while((cur = read.read(buffer)) > 0) {
			sb.append(buffer, 0, cur);
		}
		return sb.toString();
	}
	
	public static byte[] readFileBytes(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		byte[] out = null;
		try {
			out = readInputBytes(in);
		} catch(Exception io) {
			try {
				in.close();
			} catch(Exception ex) {}
			throw io;
		}
		in.close();
		return out;
	}
	
	public static byte[] readInputBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[16384];
		int cur;
		while((cur = in.read(buffer)) > 0) {
			out.write(buffer, 0, cur);
		}
		return out.toByteArray();
	}
	
	
	public static void deleteFolder(File fold) throws IOException {
		if(!fold.exists()) {
			return;
		}
		File[] files = fold.listFiles();
		int i = 0;
		for(; i < files.length; i++) {
			File cur = files[i];
			if(cur.isDirectory()) {
				deleteFolder(cur);
			} else {
				cur.delete();
			}
		}
		fold.delete();
	}
	
	public static void copyFolder(File to, File from) throws IOException {
		if(!from.exists()) {
			throw new FileNotFoundException(from.getAbsolutePath());
		}
		if(!to.exists()) {
			createDirForFile(to);
			Files.copy(Paths.get(from.getAbsolutePath()), 
					Paths.get(to.getAbsolutePath()));
		}
		File[] files = from.listFiles();
		int i = 0;
		for(; i < files.length; i++) {
			File cur = files[i];
			File target = new File(to, cur.getName());
			if(cur.isDirectory()) {
				copyFolder(target, cur);
			} else {
				Files.copy(Paths.get(cur.getAbsolutePath()), 
						Paths.get(target.getAbsolutePath()));
			}
		}
	}
	
	public static void copyFile(File to, File from) throws IOException {
		if(!from.exists()) {
			throw new FileNotFoundException(from.getAbsolutePath());
		}
		if(!to.exists()) {
			createDirForFile(to);
		} else {
			to.delete();
		}
		Files.copy(Paths.get(from.getAbsolutePath()), 
				Paths.get(to.getAbsolutePath()));
	}
	
	public static File readLink(File file) {
		if(file == null) {
			return null;
		}
		try {
			return Files.readSymbolicLink(Paths.get(file.getAbsolutePath())).toFile();
		} catch(Exception exc) {
			return file;
		}
	}
	
	public static File readFinalLink(File file) {
		if(file == null) {
			return null;
		}
		Path p = Paths.get(file.getAbsolutePath());
		try {
			while(true) {
				p = Files.readSymbolicLink(p);
			}
		} catch(Exception exc) {}
		return p.toFile();
	}
	
}
