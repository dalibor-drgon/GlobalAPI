/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.wordnice.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.jar.Manifest;

public class AnyClassLoader extends URLClassLoader {

	public AnyClassLoader() {
		super(new URL[0], ClassLoader.getSystemClassLoader());
	}
	
	public AnyClassLoader(ClassLoader cl) {
		super(new URL[0], cl);
	}
	
	public AnyClassLoader(URL[] urls, ClassLoader cl) {
		super(urls, cl);
	}
	
	
	public void addFile(File f) throws MalformedURLException {
		super.addURL(f.toURI().toURL());
	}
	
	public void addUrl(URL u) {
		super.addURL(u);
	}
	
	@SuppressWarnings("deprecation")
	public Class<?> addClass(byte[] b, int off, int l) {
		try {
			return super.defineClass(b, off, l);
		} catch(Throwable t) {
			return super.defineClass(null, b, off, l);
		}
	}
	
	public Class<?> addClass(String name, byte[] b, int off, int l) {
		return super.defineClass(name, b, off, l);
	}
	
	public Class<?> addClass(String name, byte[] b, int off, int l, ProtectionDomain pd) {
		return super.defineClass(name, b, off, l, pd);
	}
	
	public Package addPackage(String name, Manifest man, URL url) {
		return super.definePackage(name, man, url);
	}
	
	public Package addPackage(String name, String specTitle, String specVersion, 
			String specVendor, String implTitle, String implVersion, 
			String implVendor, URL sealBase) {
		return super.definePackage(name, specTitle, specVersion, specVendor, 
				implTitle, implVersion, implVendor, sealBase);
	}
	
	public Class<?> addClass(String name) throws IOException {
		return this.addClass(name, 
				this.getResourceAsStream(name.replace('.', '/') + ".class"));
	}
	
	public Class<?> addClass(InputStream in) throws IOException {
		return this.addClass(null, in);
	}
	
	public Class<?> addClass(String name, InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[8192];
		int cur;
		while((cur = in.read(buff)) > 0) {
			baos.write(buff, 0, cur);
		}
		byte[] bytes = baos.toByteArray();
		return this.defineClass(name, bytes, 0, bytes.length);
	}
	
	
	public Package getPackage(String name) {
		return super.getPackage(name);
	}
	
	public Package[] getPackages() {
		return super.getPackages();
	}
	
	public Class<?> getClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}
	
	public Class<?> getSystemClass(String name) throws ClassNotFoundException {
		return super.findSystemClass(name);
	}
	
	public Class<?> getLoadedClass(String name) {
		return super.findLoadedClass(name);
	}
	
	public URL getClassAsURL(String clz) {
		return super.getResource(clz.replace('.', '/') + ".class");
	}
	
	public URL getClassAsURL(String clz, String internal) {
		return super.getResource(clz.replace('.', '/') + '$' + internal.replace('.', '$') + ".class");
	}
	
	public InputStream getClassAsStream(String clz) {
		return super.getResourceAsStream(clz.replace('.', '/') + ".class");
	}
	
	public InputStream getClassAsStream(String clz, String internal) {
		return super.getResourceAsStream(clz.replace('.', '/') + '$' + internal.replace('.', '$') + ".class");
	}
	
	public byte[] getResourceAsBytes(String name) throws IOException {
		return Api.readInputBytes(super.getResourceAsStream(name));
	}
	
	public byte[] getClassAsBytes(String clz) throws IOException {
		return Api.readInputBytes(this.getClassAsStream(clz));
	}
	
	public byte[] getClassAsBytes(String clz, String internal) throws IOException {
		return Api.readInputBytes(this.getClassAsStream(clz, internal));
	}
	
}
