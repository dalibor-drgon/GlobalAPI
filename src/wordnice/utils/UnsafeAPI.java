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

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import gnu.trove.set.hash.THashSet;
import sun.misc.Unsafe;
import wordnice.api.Nice;
import wordnice.api.Nice.VHandler;
import wordnice.javaagent.JavaAgent;

public class UnsafeAPI {

	public static class RuntimeThrowable extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		
		public RuntimeThrowable(Throwable t) {
			initCauseOfThis(t);
		}
		
		@Override
		public void setStackTrace(StackTraceElement[] st) {
			this.getCause().setStackTrace(st);
		}
		
		@Override
		public StackTraceElement[] getStackTrace() {
			return this.getCause().getStackTrace();
		}
		
		protected Throwable initCauseOfThis(Throwable cuz) {
			return super.initCause(cuz);
		}
		
		@Override
		public Throwable initCause(Throwable cuz) {
			return this.getCause().initCause(cuz);
		}
		
		@Override
		public Throwable getCause() {
			return this.getCause();
		}
		
		@Override
		public Throwable fillInStackTrace() {
			return this.getCause();
		}
		
		public void throwIt() throws Throwable {
			throw this.getCause();
		}
		
		@Override
		public boolean equals(Object obj) {
			return this.getCause().equals(obj);
		}
		
	}
	
	protected static Unsafe unsafe;
	protected static Method getThreads;
	
	/*** C & UNSAFE ***/
	
	public static Instrumentation getInstrumentation() {
		return JavaAgent.get();
	}
	
	public static Unsafe getUnsafe() {
		if(unsafe == null) {
			Class<?> clz = Unsafe.class;
			Field[] fields = clz.getDeclaredFields();
			for(int i = 0, n = fields.length; i < n; i++) {
				Field f = fields[i];
				if(!f.getType().equals(Unsafe.class)) {
					continue;
				}
				try {
					f.setAccessible(true);
					Unsafe unf = (Unsafe) f.get(null);
					if(unf != null) {
						return (unsafe = unf);
					}
				} catch(Throwable t) {}
			}
		}
		return unsafe;
	}
	
	public static boolean is64() {
		return (Unsafe.ADDRESS_SIZE == 8);
	}
	
	public static void throv(Throwable t) {
		Unsafe unf = getUnsafe();
		if(unf != null) {
			unf.throwException(t);
		}
		throw new RuntimeException(t);
	}
	
	
	/*** MEMORY ***/
	
	public static void memcpy(Object to, Object from, int size) {
		System.arraycopy(from, 0, to, 0, size);
	}
	
	public static void memcpy(Object to, int posto, Object from, int posfrom, int size) {
		System.arraycopy(from, posfrom, to, posto, size);
	}
	
	public static void memcpy(long to, long from, long sz) {
		getUnsafe().copyMemory(from, to, sz);
	}
	
	public static void memset(long to, byte val, long sz) {
		getUnsafe().setMemory(to, sz, val);
	}
	
	
	public static long malloc(long sz) {
		return getUnsafe().allocateMemory(sz);
	}
	
	public static long zalloc(long sz) {
		return getUnsafe().allocateMemory(sz);
	}
	
	public static long realloc(long ptr, long nevsz) {
		return getUnsafe().reallocateMemory(ptr, nevsz);
	}
	
	public static void free(long ptr) {
		getUnsafe().freeMemory(ptr);
	}
	
	public static long getPointer(Object obj) {
		Object arr[] = new Object[] { obj };
		long base_offset = getUnsafe().arrayBaseOffset(Object[].class);
		if(is64()) {
			return getUnsafe().getLong(arr, base_offset);
		} else {
			return getUnsafe().getInt(arr, base_offset);
		}
	}
	
	/*** CLASS LOADERS ***/
	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
	public static Set<Class<?>> filterClasses(Collection<Class<?>> in, Class<?> ext) {
		Set<Class<?>> out = new THashSet<Class<?>>();
		filterClasses(out, in, ext);
		return out;
	}
	
	public static void filterClasses(Collection<Class<?>> ret, Collection<Class<?>> in, Class<?> ext) {
		Iterator<Class<?>> it = in.iterator();
		while(it.hasNext()) {
			Class<?> clz = it.next();
			if(ext.isAssignableFrom(clz)) {
				ret.add(clz);
			}
		}
	}
	
	public static boolean instanceOf(Object o, Class<?> c) {
		if(o == null || c == null) {
			return false;
		}
		return c.isAssignableFrom(o.getClass());
	}
	
	public static boolean instanceOf(Class<?> o, Class<?> c) {
		if(o == null || c == null) {
			return false;
		}
		return c.isAssignableFrom(o);
	}
	
	public static Class<?> getClass(String name) throws Throwable {
		return (Class<?>) Class.forName(name);
	}
	
	public static Set<String> filterClassesString(Collection<String> in, String pref) {
		Set<String> out = Nice.createSet();
		filterClassesString(out, in, pref);
		return out;
	}
	
	public static void filterClassesString(Collection<String> out, Collection<String> in, String pref) {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			String cur = it.next();
			if(cur.startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<Class<?>> filterClasses(Collection<Class<?>> in, String pref) {
		Set<Class<?>> out = new THashSet<Class<?>>();
		filterClasses(out, in, pref);
		return out;
	}
	
	public static void filterClasses(Collection<Class<?>> out, Collection<Class<?>> in, String pref) {
		Iterator<Class<?>> it = in.iterator();
		while(it.hasNext()) {
			Class<?> cur = it.next();
			if(cur.getName().startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<String> filterClassesStringEquals(Collection<String> in, String pref) {
		Set<String> out = Nice.createSet();
		filterClassesStringEquals(out, in, pref);
		return out;
	}
	
	public static void filterClassesStringEquals(Collection<String> out, Collection<String> in, String pref) {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			String cur = it.next();
			if(cur.equals(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<Class<?>> filterClassesEquals(Collection<Class<?>> in, String pref) {
		Set<Class<?>> out = new THashSet<Class<?>>();
		filterClassesEquals(out, in, pref);
		return out;
	}
	
	public static void filterClassesEquals(Collection<Class<?>> out, Collection<Class<?>> in, String pref) {
		Iterator<Class<?>> it = in.iterator();
		while(it.hasNext()) {
			Class<?> cur = it.next();
			if(cur.getName().equals(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<String> filterPackagesString(Collection<String> in, String pref) {
		Set<String> out = Nice.createSet();
		filterPackagesString(out, in, pref);
		return out;
	}
	
	public static void filterPackagesString(Collection<String> out, Collection<String> in, String pref) {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			String cur = it.next();
			if(cur.startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static Set<Package> filterPackages(Collection<Package> in, String pref) {
		Set<Package> out = new THashSet<Package>();
		filterPackages(out, in, pref);
		return out;
	}
	
	public static void filterPackages(Collection<Package> out, Collection<Package> in, String pref) {
		Iterator<Package> it = in.iterator();
		while(it.hasNext()) {
			Package cur = it.next();
			if(cur.getName().startsWith(pref)) {
				out.add(cur);
			}
		}
	}
	
	public static void loadClasses(Collection<Class<?>> out, Collection<String> in) throws Throwable {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			out.add(getClass(it.next()));
		}
	}
	
	public static void loadClasses(Collection<Class<?>> out, Collection<String> in, ClassLoader cl) throws Throwable {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			out.add(cl.loadClass(it.next()));
		}
	}
	
	public static void loadPackages(Collection<Package> out, Collection<String> in) throws Throwable {
		Iterator<String> it = in.iterator();
		while(it.hasNext()) {
			out.add(Package.getPackage(it.next()));
		}
	}
	
	public static File getClassesLocation(Class<?> cls) throws URISyntaxException {
		return new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
	}
	
	public static File getClassesLocation(ClassLoader cl) throws URISyntaxException {
		return new File(cl.getResource("").toURI());
	}
	
	protected static void getClassesFolder(Collection<String> set, File fd, String pref) {
		String[] ls = fd.list();
		int i = 0;
		for(; i < ls.length; i++) {
			String curstr = ls[i];
			File cur = new File(fd, curstr);
			if(cur.isDirectory()) {
				getClassesFolder(set, cur, (pref + curstr + "."));
			} else {
				String clsn = pref + curstr;
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					set.add(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
		}
	}
	
	protected static void getClassesFolder(VHandler<String> handler, File fd, String pref) {
		String[] ls = fd.list();
		int i = 0;
		for(; i < ls.length; i++) {
			String curstr = ls[i];
			File cur = new File(fd, curstr);
			if(cur.isDirectory()) {
				getClassesFolder(handler, cur, (pref + curstr + "."));
			} else {
				String clsn = pref + curstr;
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					handler.handle(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
		}
	}
	
	public static Set<String> getClassesNear(Class<?> cls) throws Exception {
		Set<String> out = Nice.createSet();
		getClasses(out, getClassesLocation(cls));
		return out;
	}
	
	public static Set<String> getClasses(ClassLoader cl) throws Exception {
		Set<String> out = Nice.createSet();
		getClasses(out, getClassesLocation(cl));
		return out;
	}
	
	public static Set<String> getClasses(File fd) throws Exception {
		Set<String> out = Nice.createSet();
		getClasses(out, fd);
		return out;
	}
	
	public static void getClasses(Collection<String> set, File fd) throws Exception {
		if(fd.isDirectory()) {
			getClassesFolder(set, fd, "");
			return;
		}
		getClassesZip(set, new ZipInputStream(Nice.input(fd)));
	}
	
	public static void getClasses(VHandler<String> handler, File fd) throws Exception {
		if(fd.isDirectory()) {
			getClassesFolder(handler, fd, "");
			return;
		}
		getClassesZip(handler, new ZipInputStream(Nice.input(fd)));
	}
	
	public static Set<String> getClassesZip(ZipInputStream zip) throws Exception {
		Set<String> set = Nice.createSet();
		getClassesZip(set, zip);
		return set;
	}
	
	public static void getClassesZip(Collection<String> set, ZipInputStream zip) throws Exception {
		ZipEntry ent = null;
		while((ent = zip.getNextEntry()) != null) {
			if(!ent.isDirectory()) {
				String clsn = ent.getName();
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					set.add(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
			zip.closeEntry();
		}
		zip.close();
	}
	
	public static void getClassesZip(VHandler<String> handler, ZipInputStream zip) throws Exception {
		ZipEntry ent = null;
		while((ent = zip.getNextEntry()) != null) {
			if(!ent.isDirectory()) {
				String clsn = ent.getName();
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					handler.handle(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
			zip.closeEntry();
		}
		zip.close();
	}
	
	public static Thread[] getThreads() {
		try {
			getThreads = Thread.class.getDeclaredMethod("getThreads");
			getThreads.setAccessible(true);
			return (Thread[]) getThreads.invoke(null);
		} catch(Throwable t) {}
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		return threadSet.toArray(new Thread[threadSet.size()]);
	}
	
	public static Class<?> getClassSafe(String cls) {
		try {
			return Class.forName(cls);
		} catch (Throwable t) { }
		return null;
	}
	
	public static Class<?> getClassSafe(String cls, ClassLoader cl) {
		try {
			return Class.forName(cls, true, cl);
		} catch (Throwable t) {}
		return null;
	}
	
}
