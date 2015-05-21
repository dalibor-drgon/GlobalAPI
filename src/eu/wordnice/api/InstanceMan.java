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

import java.lang.reflect.Field;
import java.util.ArrayList;

public class InstanceMan {
	
	public Object o;
	public Class<?> c;
	
	public InstanceMan(Object o) {
		this.c = o.getClass();
		this.o = o;
	}
	
	public InstanceMan(Class<?> c, Object o) {
		this.c = c;
		this.o = o;
	}
	
	
	public Object getValue(String name) {
		/*
		Class<?> c = this.c;
		while(c != null) {
			try {
				return c.getDeclaredField(name).get(this.o);
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return null;
		*/
		return InstanceMan.getValue(this.o, this.c, name);
	}
	
	public String[] getFields() {
		return InstanceMan.getFields(this.c);
	}
	
	
	public static Object getValue(Object o, String name) {
		return InstanceMan.getValue(o, o.getClass(), name);
	}
	
	public static Object getValue(Object o, Class<?> c, String name) {
		Field f = null;
		while(c != null) {
			try {
				f = c.getDeclaredField(name);
				f.setAccessible(true);
				return f.get(o);
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return null;
	}
	
	public static boolean setValue(Object o, String name, Object newval) {
		return InstanceMan.setValue(o, o.getClass(), name, newval);
	}
	
	public static boolean setValue(Object o, Class<?> c, String name, Object newval) {
		Field f = null;
		while(c != null) {
			try {
				f = c.getDeclaredField(name);
				f.setAccessible(true);
				f.set(o, newval);
				return true;
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return false;
	}
	
	public static String[] getFields(Class<?> c) {
		ArrayList<String> ret = new ArrayList<String>();
		
		Field[] f = null;
		while(c != null) {
			try {
				f = c.getDeclaredFields();
				for(Field fi : f) {
					try {
						fi.setAccessible(true);
					} catch(Throwable t) {}
					ret.add(fi.getName());
				}
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		
		return ret.toArray(new String[ret.size()]);
	}
	
}
