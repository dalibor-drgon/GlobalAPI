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

package eu.wordnice.api;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class CLoader extends URLClassLoader {

	public CLoader() {
		this(Api.getClassLoader());
	}
	
	public CLoader(ClassLoader cl) {
		this(new URL[0], cl);
	}
	
	public CLoader(URL[] urls, ClassLoader cl) {
		super(urls, cl);
	}
	
	
	public boolean addJar(File f) {
		try {
			return this.addJar(f.toURI().toURL());
		} catch(Throwable t) {}
		return false;
	}
	
	public boolean addJar(URL u) {
		try {
			super.addURL(u);
			return true;
		} catch(Throwable t) {}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public Class<?> defClass(byte[] b, int off, int l) {
		try {
			return super.defineClass(b, off, l);
		} catch(Throwable t) {}
		return null;
	}
	
	public Class<?> defClass(String name, byte[] b, int off, int l) {
		try {
			return super.defineClass(name, b, off, l);
		} catch(Throwable t) {}
		return null;
	}
	
	public Package getPackage(String name) {
		try {
			return super.getPackage(name);
		} catch(Throwable t) {}
		return null;
	}
	
	public Package[] getPackages(String name) {
		try {
			return super.getPackages();
		} catch(Throwable t) {}
		return null;
	}
	
	public Class<?> getLoadedClass(String name) {
		try {
			return super.findLoadedClass(name);
		} catch(Throwable t) {}
		return null;
	}
	
	public Class<?> getClass(String name) {
		try {
			return super.findClass(name);
		} catch(Throwable t) {}
		return null;
	}
	
}
