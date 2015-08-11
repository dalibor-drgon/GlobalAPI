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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Api {
	
	public static Random RANDOM = new Random();
	public static byte[] GENSTRING = "abcdefghijklmnopqrstuvwxyz1234567890QWERTZUIOPASDFGHJKLYXCVBNM".getBytes();
	
	protected static InstanceMan unsafe;
	
	
	public static Thread[] getThreads() {
		Object gt = InstanceMan.getValue(null, Thread.class, "getThreads");
		if(gt instanceof Thread[]) {
			return (Thread[]) gt;
		}
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		return threadSet.toArray(new Thread[threadSet.size()]);
	}
	
	public static String join(String w, String... data) {
		StringBuilder ret = new StringBuilder();
		for(String d : data) {
			ret.append(d);
			ret.append(w);
		}
		return ret.substring(0, (ret.length() - w.length()));
	}
	
	public static int getParsedIPv4(String address) {
		int result = 0;
		for(String part : address.split("\\.")) {
			result = result << 8;
			result |= Integer.parseInt(part);
		}
		return result;
	}
	
	static public byte[] getIPv4Array(int addr){
		return new byte[]{ (byte) (addr >>> 24), 
				(byte)(addr >>> 16),
				(byte)(addr >>> 8),
				(byte)(addr) };
	}
	
	public static String getIPv4(int ip) {
		return String.format("%d.%d.%d.%d",
				 (ip >> 24 & 0xff),   
				 (ip >> 16 & 0xff),			 
				 (ip >> 8 & 0xff),	
				 (ip & 0xff));
	}
	
	public static <X, Y> Map<String, String> toStringMap(Map<X, Y> map) {
		Map<String, String> ret = new HashMap<String, String>();
		Api.toStringMap(ret, map);
		return ret;
	}
	
	public static <X, Y> void toStringMap(Map<String, String> out, Map<X, Y> map) {
		Iterator<Entry<X, Y>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<X, Y> ent = it.next();
			out.put(("" + ent.getKey()), ("" + ent.getValue()));
		}
	}
	
	public static <X, Y> void copyMap(Map<X, Y> out, Map<X, Y> map) {
		Iterator<Entry<X, Y>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<X, Y> ent = it.next();
			out.put(ent.getKey(), ent.getValue());
		}
	}
	
	public static <X> void toStringColl(Collection<String> out, Collection<X> set) {
		Iterator<X> it = set.iterator();
		while(it.hasNext()) {
			out.add(("" + it.next()));
		}
	}
	
	public static <X> void copyColl(Collection<X> out, Collection<X> set) {
		Iterator<X> it = set.iterator();
		while(it.hasNext()) {
			out.add(it.next());
		}
	}

	public static int indexOfObject(Object o, Object... vals) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		int i = 0;
		int size = vals.length;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int indexOfStringIgnoreCase(String o, String... vals) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		int i = 0;
		int size = vals.length;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equalsIgnoreCase(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@SafeVarargs
	public static <X> int indexOfSafe(X o, X... vals) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		int i = 0;
		int size = vals.length;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> int indexOfUnsafe(X o, X... vals) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		int i = 0;
		int size = vals.length;
		if(o == null) {
			for(; i < size; i++) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i < size; i++) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static boolean loadClass(String cls) {
		try {
			Class.forName(cls);
			return true;
		} catch (Throwable t) { }
		return false;
	}
	
	public static boolean loadClassAndDebug(String cls) {
		try {
			Class.forName(cls);
			System.out.println("Class loaded: '" + cls + "'");
			return true;
		} catch (Throwable t) {
			System.out.println("Cannot load class '" + cls + "', details:");
			t.printStackTrace();
		}
		return false;
	}
	
	
	public static String getCopiesOfString(String cp, int k) {
		StringBuilder sb = new StringBuilder(cp.length() * k);
		for(int i = 0; i < k; i++) {
			sb.append(cp);
		}
		return sb.toString();
	}
	
	public static String getEscapedString(String s) {
		return s.replace("\n", "\\n").replace("\t", "\\t")
				.replace("\b", "\\b").replace("\r", "\\r");
	}
	
	public static String getDeescapedString(String s) {
		return s.replace("\\n", "\n").replace("\\t", "\t")
				.replace("\\b", "\b").replace("\\r", "\r");
	}
	
	public static String getASCIString(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	public static Integer hash(String name) {
		if(name == null) {
			return 0;
		}
		return name.toLowerCase().hashCode();
	}
	
	public static Integer hashNormal(String name) {
		if(name == null) {
			return 0;
		}
		return name.hashCode();
	}
	
	
	/*** URL ***/
	
	public static String getURLDecoded(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch(Throwable t) {}
		return str;
	}
	
	public static String getURLEncoded(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch(Throwable t) {}
		return str;
	}
	
	
	/*** Files ***/
	
	public static String getRealPath(String file) {
		if(file == null) {
			return null;
		}
		return Api.getRealPath(new File(file));
	}
	
	public static String getRealPath(File file) {
		if(file == null) {
			return null;
		}
		try {
			return file.getCanonicalPath();
		} catch(Throwable t) {}
		return file.getAbsolutePath();
	}
	
	public static String getExtension(File file) {
		if(file == null) {
			return null;
		}
		return Api.getExtension(file.getName());
	}
	
	public static String getExtension(String file) {
		if(file == null) {
			return null;
		}
		int i = file.indexOf('.');
		if(i < 0 || (i + 2) > file.length()) {
			return null;
		}
		return file.substring((i + 1), file.length());
	}
	
	public static String getLastExtension(File file) {
		if(file == null) {
			return null;
		}
		return Api.getLastExtension(file.getName());
	}
	
	public static String getLastExtension(String file) {
		if(file == null) {
			return null;
		}
		int i = file.lastIndexOf('.');
		if(i < 0 || (i + 2) > file.length()) {
			return null;
		}
		return file.substring((i + 1), file.length());
	}
	
	public static File getFreeName(String old) {
		long i = 1;
		File nev = null;
		while(true) {
			nev = new File(old + i);
			if(!nev.exists()) {
				return nev;
			}
			i++;
		}
	}
	
	
	/*** CLASS LOADERS ***/
	public static ClassLoader getClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}
	
	public static Set<Class<?>> filterClasses(Collection<Class<?>> in, Class<?> ext) {
		Set<Class<?>> out = new HashSet<Class<?>>();
		Api.filterClasses(out, in, ext);
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
	
	public static Class<?> getClass(String name) throws Throwable {
		return (Class<?>) Class.forName(name);
	}
	
	public static Set<String> filterClassesString(Collection<String> in, String pref) {
		Set<String> out = new HashSet<String>();
		Api.filterClassesString(out, in, pref);
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
		Set<Class<?>> out = new HashSet<Class<?>>();
		Api.filterClasses(out, in, pref);
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
	
	public static Set<String> filterPackagesString(Collection<String> in, String pref) {
		Set<String> out = new HashSet<String>();
		Api.filterPackagesString(out, in, pref);
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
		Set<Package> out = new HashSet<Package>();
		Api.filterPackages(out, in, pref);
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
			out.add(Api.getClass(it.next()));
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
	
	private static void getClassesFolder(Collection<String> set, File fd, String pref) {
		String[] ls = fd.list();
		int i = 0;
		for(; i < ls.length; i++) {
			String curstr = ls[i];
			File cur = new File(fd, curstr);
			if(cur.isDirectory()) {
				Api.getClassesFolder(set, cur, (pref + curstr + "."));
			} else {
				String clsn = pref + curstr;
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					set.add(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
		}
	}
	
	public static Set<String> getClasses(File fd) throws Exception {
		Set<String> out = new HashSet<String>();
		Api.getClasses(out, fd);
		return out;
	}
	
	public static void getClasses(Collection<String> set, File fd) throws Exception {
		if(fd.isDirectory()) {
			Api.getClassesFolder(set, fd, "");
			return;
		}
		Api.getClasses(set, new ZipInputStream(new FileInputStream(fd)));
	}
	
	public static Set<String> getClasses(ZipInputStream zip) throws Exception {
		Set<String> set = new HashSet<String>();
		Api.getClasses(set, zip);
		return set;
	}
	
	public static void getClasses(Collection<String> set, ZipInputStream zip) throws Exception {
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
	
	
	public static String regexReplacer(String in, String p, Handler.OneHandler<String, Matcher> han) {
		return Api.regexReplacer(in, Pattern.compile(p), han);
	}
	
	public static String regexReplacer(String in, Pattern p, Handler.OneHandler<String, Matcher> han) {
		StringBuilder sb = new StringBuilder();
		Matcher m = p.matcher(in);
		String rep;
		int laste = 0;
		while(m.find()) {
			rep = han.handle(m);
			sb.append(in.substring(laste, m.start()));
			sb.append(rep);
			laste = m.end();
		}
		if(laste != 0) {
			sb.append(in.substring(laste, in.length()));
		}
		return sb.toString();
	}
	
	public static boolean equalsRegex(String in, String p) {
		return Pattern.matches(p, in);
	}
	
	public static boolean equalsRegexIgnoreCase(String in, String p) {
		return Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(in).matches();
	}
	
	public static boolean equalRegex(String in, Pattern p) {
		return p.matcher(in).matches();
	}
	
	public static String readFileChars(File file) throws IOException {
		Reader in = new InputStreamReader(new FileInputStream(file));
		String out = Api.readInputChars(in);
		in.close();
		return out;
	}
	
	public static String readInputChars(Reader read) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[8192];
		int cur;
		while((cur = read.read(buffer)) > 0) {
			sb.append(buffer, 0, cur);
		}
		return sb.toString();
	}
	
	public static byte[] readFileBytes(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		byte[] out = Api.readInputBytes(in);
		in.close();
		return out;
	}
	
	public static byte[] readInputBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int cur;
		while((cur = in.read(buffer)) > 0) {
			out.write(buffer, 0, cur);
		}
		return out.toByteArray();
	}
	
	
	/*** RANDOM ***/
	
	public static byte[] genBytes(int length) {
		return genBytes(length, Api.GENSTRING);
	}
	
	public static byte[] genBytes(int length, byte[] chars) {
		byte[] out = new byte[length];
		for (int i = 0; i < length; i++) {
			out[i] = chars[Api.RANDOM.nextInt(chars.length)];
		}
		return out;
	}
	
	public static String genString(int length) {
		return new String(Api.genBytes(length));
	}
	
	public static String genString(int length, byte[] chars) {
		return new String(Api.genBytes(length, chars));
	}
	
	public static int genInt(int length) {
		int nevi = 0;
		for (int i = 0; i < length; i++) {
			nevi *= 10;
			nevi += Api.RANDOM.nextInt(9);
		}
		return nevi;
	}
	
	public static long genLong(int length) {
		long nevi = 0;
		for (int i = 0; i < length; i++) {
			nevi *= 10;
			nevi += Api.RANDOM.nextInt(9);
		}
		return nevi;
	}
	
	
	/*** C & UNSAFE ***/
	
	public static InstanceMan getUnsafe() {
		if(Api.unsafe == null) {
			InstanceMan im = null;
			try {
				im = new InstanceMan(Api.getClass("sun.misc.Unsafe"), null);
				Map<String, ?> vals = im.getValues(im.c);
				Iterator<? extends Entry<String, ?>> it = vals.entrySet().iterator();
				while(it.hasNext()) {
					Entry<String, ?> ent = it.next();
					Object uns = ent.getValue();
					if(uns != null) {
						im.reinit(uns);
						Api.unsafe = im;
						return Api.unsafe;
					}
				}
			} catch(Throwable t) {
				Api.unsafe = new InstanceMan(new Object());
			}
		}
		return Api.unsafe;
	}
	
	public static boolean is64() {
		Object retv = Api.getUnsafe().getValue("ADDRESS_SIZE");
		if(retv == null) {
			return false;
		}
		return ((int) ((Integer) retv) == 8);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X createEmptyInstance(Class<X> cls) {
		try {
			return (X) getUnsafe().callMethod("allocateInstance", cls);
		} catch(Throwable t) {}
		return null;
	}
	
	public static void throv(Throwable t) {
		try {
			Api.getUnsafe().callMethod("throwException", new Object[] { t }, 
					new Class<?>[] { Throwable.class });
		} catch(Throwable ign) {}
		throw new RuntimeException(t);
	}
	
	
	/*** MEMORY ***/
	
	public static boolean memcpy(Object to, int posto, Object from, int posfrom, int size) {
		try {
			System.arraycopy(from, posfrom, to, posto, size);
			return true;
		} catch(Throwable t) {}
		return false;
	}
	
	public static boolean memcpy(long to, long from, long sz) {
		return Api.getUnsafe().callMethod("copyMemory", from, to, sz) != null;
	}
	
	public static boolean memset(long to, byte val, long sz) {
		return Api.getUnsafe().callMethod("setMemory", to, sz, val) != null;
	}
	
	
	public static long malloc(long sz) {
		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("allocateMemory", sz);
		if(valr == null) {
			return 0;
		}
		return (long) ((Long) valr.one);
	}
	
	public static long zalloc(long sz) {
		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("allocateMemory", sz);
		if(valr == null) {
			return 0;
		}
		long ptr = (long) ((Long) valr.one);
		Api.memset(ptr, Byte.MIN_VALUE, sz);
		return ptr;
	}
	
	public static long realloc(long ptr, long nevsz) {
		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("reallocateMemory", ptr, nevsz);
		if(valr == null) {
			return 0;
		}
		return (long) ((Long) valr.one);
	}
	
	public static void free(long ptr) {
		try {
			Api.getUnsafe().callMethod("freeMemory", ptr);
		} catch(Throwable t) {}
	}
	
	
	
	public static long getPointer(Object obj) {
		Object arr[] = new Object[] { obj };

		Val.OneVal<Object> valr = Api.getUnsafe().callMethod("arrayBaseOffset", Object[].class);
		if(valr == null) {
			return 0;
		}
		long base_offset = (long) ((Long) valr.one);
		
		if(Api.is64()) {
			valr = Api.getUnsafe().callMethod("getLong", arr, base_offset);
			if(valr != null) {
				return (long) ((Long) valr.one);
			}
		} else {
			valr = Api.getUnsafe().callMethod("getInt", arr, base_offset);
			if(valr != null) {
				return (int) ((Integer) valr.one);
			}
		}
		return 0;
	}
	
}
