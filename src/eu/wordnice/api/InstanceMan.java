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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
	
	
	public void reinit(Object o) {
		this.c = o.getClass();
		this.o = o;
	}
	
	public void reinit(Class<?> c, Object o) {
		this.c = c;
		this.o = o;
	}
	
	
	public Object getValue(String name) {
		return InstanceMan.getValue(this.o, this.c, name);
	}
	
	public Object getValue(Class<?> c, String name) {
		return InstanceMan.getValue(this.o, c, name);
	}
	
	
	public boolean setValue(String name, Object newval) {
		return InstanceMan.setValue(this.o, this.c, name, newval);
	}
	
	public boolean setValue(Class<?> c, String name, Object newval) {
		return InstanceMan.setValue(this.o, c, name, newval);
	}
	
	

	public Val.OneVal<Object> callMethod(String name, Object... args) {
		return InstanceMan.callMethod(this.o, this.c, name, args);
	}
	
	public Val.OneVal<Object> callMethod(Class<?> c, String name, Object... args) {
		return InstanceMan.callMethod(this.o, c, name, args);
	}
	
	public Val.OneVal<Object> callMethod(String name, Object[] args, Class<?>[] clzs) {
		return InstanceMan.callMethod(this.o, this.c, name, args, clzs);
	}
	
	public Val.OneVal<Object> callMethod(Class<?> c, String name, Object[] args, Class<?>[] clzs) {
		return InstanceMan.callMethod(this.o, c, name, args, clzs);
	}
	
	
	public <X> Map<String, X> getValues(Class<X> ext) {
		return InstanceMan.getValues(this.o, this.c, ext);
	}
	
	public Set<Field> getFields() {
		return InstanceMan.getFields(this.c);
	}
	
	public Set<Method> getMethods() {
		return InstanceMan.getMethods(this.c);
	}
	
	
	
	/*** Static methods ***/
	
	public static Val.OneVal<Object> callMethod(Object o, String name, Object... args) {
		return InstanceMan.callMethod(o, o.getClass(), name, args);
	}
	
	public static Val.OneVal<Object> callMethod(Object o, Class<?> c, String name, Object... args) {
		Set<Class<?>> clzs_list = InstanceMan.getArgClasses(args);
		return InstanceMan.callMethod(o, c, name, args, clzs_list.toArray(new Class<?>[clzs_list.size()]));
	}
	
	public static Val.OneVal<Object> callMethod(Object o, String name, Object[] args, Class<?>[] clzs) {
		return InstanceMan.callMethod(o, o.getClass(), name, args, clzs);
	}
	
	public static Val.OneVal<Object> callMethod(Object o, Class<?> c, String name, Object[] args, Class<?>[] clzs) {
		Method m = null;
		while(c != null) {
			try {
				m = c.getDeclaredMethod(name, clzs);
				try {
					m.setAccessible(true);
				} catch(Throwable t) {}
				return new Val.OneVal<Object>(m.invoke(o, args));
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return null;
	}
	
	
	public static Object getValue(Object o, String name) {
		return InstanceMan.getValue(o, o.getClass(), name);
	}
	
	public static Object getValue(Object o, Class<?> c, String name) {
		Field f = null;
		while(c != null) {
			try {
				f = c.getDeclaredField(name);
				try {
					f.setAccessible(true);
				} catch(Throwable t) {}
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
				try {
					f.setAccessible(true);
				} catch(Throwable t) {}
				try {
					f.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				} catch(Throwable t) {}
				f.set(o, newval);
				return true;
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return false;
	}
	
	
	public static <X> Map<String, X> getValues(Object o, Class<X> ext) {
		return InstanceMan.getValues(o, o.getClass(), ext);
	}

	@SuppressWarnings("unchecked")
	public static <X> Map<String, X> getValues(Object o, Class<?> c, Class<X> ext) {
		Map<String, X> ret = new Map<String, X>();
		
		Field[] f = null;
		Object val = null;
		if(ext == null) {
			Object[] vals = new Object[1];
			Object[] nams = new Object[1];
			while(c != null) {
				try {
					f = c.getDeclaredFields();
					for(Field fi : f) {
						try {
							fi.setAccessible(true);
						} catch(Throwable t) {}
						try {
							vals[0] = fi.get(o);
							nams[0] = fi.getName();
							ret.addAllWC(nams, vals, 1);
						} catch(Throwable t) {}
					}
				} catch(Throwable t) {}
				c = c.getSuperclass();
			}
		} else {
			while(c != null) {
				try {
					f = c.getDeclaredFields();
					for(Field fi : f) {
						try {
							fi.setAccessible(true);
						} catch(Throwable t) {}
						try {
							fi.setInt(fi, fi.getModifiers() & ~Modifier.FINAL);
						} catch(Throwable t) {}
						try {
							val = fi.get(o);
							if(val != null) {
								if(ext.isAssignableFrom(fi.getType())) {
									ret.addWC(fi.getName(), (X) val);
								}
							}
						} catch(Throwable t) {}
					}
				} catch(Throwable t) {}
				c = c.getSuperclass();
			}
		}
		return ret;
	}
	
	public static Set<Field> getFields(Class<?> c) {
		Set<Field> ret = new Set<Field>();
		
		Field[] f = null;
		while(c != null) {
			try {
				f = c.getDeclaredFields();
				for(Field fi : f) {
					try {
						fi.setAccessible(true);
					} catch(Throwable t) {}
					try {
						fi.setInt(fi, fi.getModifiers() & ~Modifier.FINAL);
					} catch(Throwable t) {}
					ret.addWC(fi);
				}
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return ret;
	}
	
	public static Set<Method> getMethods(Class<?> c) {
		Set<Method> ret = new Set<Method>();
		
		Method[] m = null;
		while(c != null) {
			try {
				m = c.getDeclaredMethods();
				for(Method mc : m) {
					try {
						mc.setAccessible(true);
					} catch(Throwable t) {}
					ret.addWC(mc);
				}
			} catch(Throwable t) {}
			c = c.getSuperclass();
		}
		return ret;
	}
	
	
	
	/*** API ***/
	
	public static Set<Class<?>> getArgClasses(Object... args) {
		Set<Class<?>> set = new Set<Class<?>>();
		int i = 0;
		Object cur;
		for(; i < args.length; i++) {
			cur = args[i];
			if(cur != null) {
				set.addWC(InstanceMan.getOriginalClass(cur.getClass()));
			} else {
				set.addWC(null);
			}
		}
		return set;
	}
	
	public static Class<?> getOriginalClass(Class<?> clz) {
		if(clz == null) {
			return null;
		}
		if(!clz.isArray() && !clz.isPrimitive()) {
			if(clz.equals(Boolean.class)) {
				return boolean.class;
			}
			if(clz.equals(Byte.class)) {
				return byte.class;
			}
			if(clz.equals(Short.class)) {
				return short.class;
			}
			if(clz.equals(Character.class)) {
				return char.class;
			}
			if(clz.equals(Integer.class)) {
				return int.class;
			}
			if(clz.equals(Long.class)) {
				return long.class;
			}
			if(clz.equals(Float.class)) {
				return float.class;
			}
			if(clz.equals(Double.class)) {
				return double.class;
			}
			if(clz.equals(Void.class)) {
				return void.class;
			}
		}
		return clz;
	}
	
}
