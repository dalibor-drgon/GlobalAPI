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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eu.wordnice.cols.ImmArray;

public class Api {
	
	public static Random RANDOM = new Random();
	public static byte[] GENSTRING = "abcdefghijklmnopqrstuvwxyz1234567890QWERTZUIOPASDFGHJKLYXCVBNM".getBytes();
	
	protected static long RANDOM_SEED = System.nanoTime() + 0xCAFEBEEF;
	protected static InstanceMan unsafe;
	protected static int is64Bit = -1;
	
	
	public static Random getRandom() {
		return RANDOM;
	}
	
	
	public static final long randomLong() {
		long a = Api.RANDOM_SEED;
		Api.RANDOM_SEED = xorShift64(a);
		return a;
	}

	protected static final long xorShift64(long a) {
		a ^= (a << 21);
		a ^= (a >>> 35);
		a ^= (a << 4);
		return a;
	}
	
	public static final int randomInt(int n) {
		if(n < 0) {
			throw new IllegalArgumentException();
		}
		return (int) (((Api.randomLong() >>> 32) * n) >> 32);
	}
	
	/**
	 * Check if values in iterables are identical and in identical order
	 * If both are instanceof Collection, this method check sizes first
	 * Calls only once Itarable#iterator() for both iterables
	 */
	public static boolean equalsIterablesInOrder(Iterable<?> i1, Iterable<?> i2) {
		if(i1 == i2) {
			return true;
		}
		if(i1 == null) {
			if(i2 == null) {
				return true;
			}
			return false;
		} else if(i2 == null) {
			return false;
		}
		if(i1 instanceof Collection && i2 instanceof Collection) {
			if(((Collection<?>) i1).size() != ((Collection<?>) i2).size()) {
				return false;
			}
		}
		Iterator<?> it1 = i1.iterator();
		Iterator<?> it2 = i2.iterator();
		while(it1.hasNext()) {
			if(!it2.hasNext()) {
				return false;
			}
			Object c1 = it1.next();
			Object c2 = it2.next();
			if((c1 == null) ? c2 != null : !c1.equals(c2)) {
				return false;
			}
		}
		return !it2.hasNext();
	}
	
	/**
	 * Check if values in iterables are identical (in any order)
	 * This method check sizes first
	 * For first collection calls Iterable#iterator() only once, and
	 * several times for second collection (max = size of collection)
	 */
	public static boolean equalsCollections(Collection<?> i1, Collection<?> i2) {
		if(i1 == i2) {
			return true;
		}
		if(i1 == null) {
			if(i2 == null) {
				return true;
			}
			return false;
		} else if(i2 == null) {
			return false;
		}
		int size = i1.size();
		if(size != i2.size()) {
			return false;
		}
		boolean[] was = new boolean[size];
		Iterator<?> it1 = i1.iterator();
		for(int i = 0; i < size; i++) {
			if(!it1.hasNext()) {
				return false;
			}
			Object cur = it1.next();
			Iterator<?> it2 = i2.iterator();
			boolean wasCur = false;
			if(cur == null) {
				for(int seci = 0; seci < size; seci++) {
					if(!it2.hasNext()) {
						return false;
					}
					Object cur2 = it2.next();
					if(!was[seci]) {
						if(cur2 == null) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			} else {
				for(int seci = 0; seci < size; seci++) {
					if(!it2.hasNext()) {
						return false;
					}
					Object cur2 = it2.next();
					if(!was[seci]) {
						if(cur.equals(cur2)) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			}
			if(!wasCur || it2.hasNext()) {
				return false;
			}
		}
		return !it1.hasNext();
	}
	
	/**
	 * Check if values are identical (in any order)
	 */
	public static boolean equalsAnyOrder(Object[] arr1, int off1, Object[] arr2, int off2, int size) {
		if(arr1 == arr2 && off1 == off2) {
			return true;
		}
		if(arr1 == null) {
			if(arr2 == null) {
				return true;
			}
			return false;
		} else if(arr2 == null) {
			return false;
		}
		boolean[] was = new boolean[size];
		int size1 = size + off1;
		int size2 = size + off2;
		for(int i = off1; i < size1; i++) {
			Object cur = arr1[i];
			boolean wasCur = false;
			if(cur == null) {
				for(int seci = off2; seci < size2; seci++) {
					Object cur2 = arr2[seci];
					if(!was[seci]) {
						if(cur2 == null) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			} else {
				for(int seci = 0; seci < size; seci++) {
					Object cur2 = arr2[seci];
					if(!was[seci]) {
						if(cur.equals(cur2)) {
							was[seci] = true;
							wasCur = true;
							break;
						}
					}
				}
			}
			if(!wasCur) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] toArray(Collection<?> col) {
		if(col instanceof ImmArray) {
			return (X[]) ((ImmArray<X>) col).arr;
		}
		return ((Collection<X>) col).toArray((X[]) new Object[0]);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] toArray(Collection<?> col, Class<?> c) {
		if(col instanceof ImmArray) {
			return (X[]) ((ImmArray<X>) col).arr;
		}
		return ((Collection<X>) col).toArray((X[]) Array.newInstance(c, 0));
	}
	
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
	
	public static Map<String, String> toStringMap(Map<?, ?> map) {
		Map<String, String> ret = new HashMap<String, String>();
		Api.toStringMap(ret, map);
		return ret;
	}
	
	public static void toStringMap(Map<String, String> out, Map<?, ?> map) {
		Iterator<? extends Entry<?, ?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?, ?> ent = it.next();
			Object key = ent.getKey();
			Object val = ent.getValue();
			String str_key = (key == null) ? null : key.toString();
			String str_val = (val == null) ? null : val.toString();
			out.put(str_key, str_val);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void copyMap(Map<?, ?> out, Map<?, ?> map) {
		Iterator<? extends Entry<?, ?>> it = map.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?, ?> ent = it.next();
			((Map<Object, Object>)out).put(ent.getKey(), ent.getValue());
		}
	}
	
	public static List<String> toStringColl(Collection<?> set) {
		List<String> list = new ArrayList<String>();
		Api.toStringColl(list, set);
		return list;
	}
	
	public static void toStringColl(Collection<String> out, Collection<?> set) {
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			Object next = it.next();
			if(next == null) {
				out.add(null);
			} else {
				out.add(next.toString());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void copyColl(Collection<?> out, Collection<?> set) {
		Iterator<?> it = set.iterator();
		while(it.hasNext()) {
			((Collection<Object>) out).add(it.next());
		}
	}
	
	public static <X, Y> boolean equals(Object[] one, Object[] two, int size) {
		if(one == two) {
			return true;
		}
		while(size-- != 0) {
			Object on = one[size];
			Object tw = two[size];
			if((on == null) ? tw != null : !on.equals(tw)) {
				return false;
			}
		}
		return true;
	}
	
	public static <X, Y> boolean equals(Object[] one, int off1, Object[] two, int off2, int size) {
		if(one == two && off1 == off2) {
			return true;
		}
		size += off1;
		for(; off1 < size; off1++, off2++) {
			Object on = one[off1];
			Object tw = two[off2];
			if((on == null) ? tw != null : !on.equals(tw)) {
				return false;
			}
		}
		return true;
	}

	/***********
	 * INDEX OF
	 */
	
	public static int indexOf(Object o, Object[] vals) {
		return Api.indexOf(o, vals, 0);
	}
	
	public static int indexOf(Object o, Object[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
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
	
	public static int indexOfInsensitive(String o, String[] vals) {
		return Api.indexOfInsensitive(o, vals, 0);
	}
	
	public static int indexOfInsensitive(String o, String[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
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
	
	
	/****************
	 * LAST INDEX OF
	 */
	
	public static int lastIndexOf(Object o, Object[] vals) {
		return Api.lastIndexOf(o, vals, 0);
	}
	
	public static int lastIndexOf(Object o, Object[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		if(i >= vals.length) {
			i = vals.length - 1;
		}
		if(o == null) {
			for(; i >= 0; i--) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i >= 0; i--) {
				if(o.equals(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int lastIndexOfInsensitive(String o, String[] vals) {
		return Api.lastIndexOfInsensitive(o, vals, 0);
	}
	
	public static int lastIndexOfInsensitive(String o, String[] vals, int i) {
		if(vals == null || vals.length == 0) {
			return -1;
		}
		if(i >= vals.length) {
			i = vals.length - 1;
		}
		if(o == null) {
			for(; i >= 0; i--) {
				if(vals[i] == null) {
					return i;
				}
			}
		} else {
			for(; i >= 0; i--) {
				if(o.equalsIgnoreCase(vals[i])) {
					return i;
				}
			}
		}
		return -1;
	}
	
	
	
	
	/**********
	 * CLASSES
	 * PACKAGES
	 */
	
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
	
	public static String getANSI(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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
	
	public static String getRealPath(File file) {
		if(file == null) {
			return null;
		}
		try {
			return file.getCanonicalPath();
		} catch(Throwable t) {}
		return file.getAbsolutePath();
	}
	
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
		return Api.getFreeName(Api.getRealPath(f));
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
	
	public static void createFileIfNot(File file) throws IOException {
		if(!file.exists()) {
			Api.createDirForFile(file);
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
	
	
	/*** CLASS LOADERS ***/
	public static ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
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
	
	
	/**
	 * @see {@link Api#replaceRegex(String, Pattern, eu.wordnice.api.Handler.TwoHandler)}
	 */
	public static String replaceRegex(String in, String p, Handler.TwoHandler<CharSequence, String, Matcher> han) {
		return Api.replaceRegex(in, Pattern.compile(p), han);
	}
	
	/**
	 * @param in Input string
	 * @param p Pattern to find
	 * @param han Handler. Input: original string and matcher, 
	 *            output string which will be placed into found location.
	 *            If output string is null, is ignored.
	 *            If handler is null, all founds are removed.
	 * 
	 * @return String with replaced all founds
	 */
	public static String replaceRegex(String in, Pattern p, Handler.TwoHandler<CharSequence, String, Matcher> han) {
		Matcher m = p.matcher(in);
		if(!m.find()) {
			return in;
		}
		StringBuilder sb = new StringBuilder(in.length());
		int laste = 0;
		if(han != null) {
			while(true) {
				if(laste != m.start()) {
					sb.append(in, laste, m.start());
				}
				CharSequence rep = han.handle(in, m);
				if(rep != null) {
					sb.append(rep);
				}
				laste = m.end();
				if(!m.find()) {
					break;
				}
			}
		} else {
			while(true) {
				if(laste != m.start()) {
					sb.append(in, laste, m.start());
				}
				laste = m.end();
				if(!m.find()) {
					break;
				}
			}
		}
		if(laste != in.length()) {
			sb.append(in, laste, in.length());
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
	
	
	public static void deleteFolder(File fold) throws IOException {
		File[] files = fold.listFiles();
		int i = 0;
		for(; i < files.length; i++) {
			File cur = files[i];
			if(cur.isDirectory()) {
				Api.deleteFolder(cur);
			} else {
				cur.delete();
			}
		}
		fold.delete();
	}
	
	public static void copyFolder(File to, File from) throws IOException {
		boolean hasNio = false;
		try {
			java.nio.file.Files.class.getCanonicalName();
			hasNio = true;
		} catch(Throwable t) {}
		if(hasNio) {
			Api.copyFolderNio(to, from);
		} else {
			Api.copyFolderRaw(to, from);
		}
	}
	
	private static void copyFolderRaw(File to, File from) throws IOException {
		if(!to.exists()) {
			to.mkdirs();
		}
		File[] files = from.listFiles();
		int i = 0;
		for(; i < files.length; i++) {
			File cur = files[i];
			File target = new File(to, cur.getName());
			if(cur.isDirectory()) {
				Api.copyFolderRaw(target, cur);
			} else {
				target.createNewFile();
				FileInputStream in = new FileInputStream(cur);
				FileOutputStream out = new FileOutputStream(target);
				byte[] buff = new byte[8192];
				int red = 0;
				while((red = in.read(buff)) > 0) {
					out.write(buff, 0, red);
				}
				out.close();
				in.close();
			}
		}
	}
	
	private static void copyFolderNio(File to, File from) throws IOException {
		if(!to.exists()) {
			java.nio.file.Files.copy(java.nio.file.Paths.get(from.getAbsolutePath()), 
					java.nio.file.Paths.get(to.getAbsolutePath()));
		}
		File[] files = from.listFiles();
		int i = 0;
		for(; i < files.length; i++) {
			File cur = files[i];
			File target = new File(to, cur.getName());
			if(cur.isDirectory()) {
				Api.copyFolderNio(target, cur);
			} else {
				java.nio.file.Files.copy(java.nio.file.Paths.get(cur.getAbsolutePath()), 
						java.nio.file.Paths.get(target.getAbsolutePath()));
			}
		}
	}
	
	
	/*** RANDOM ***/
	
	public static byte[] genBytes(int length) {
		return Api.genBytes(length, Api.GENSTRING);
	}
	
	public static byte[] genBytes(int length, byte[] chars) {
		byte[] out = new byte[length];
		for (int i = 0; i < length; i++) {
			out[i] = chars[Api.randomInt(chars.length)];
		}
		return out;
	}
	
	public static int genIntLength(int length) {
		int nevi = 0;
		for (int i = 0; i < length; i++) {
			nevi *= 10;
			nevi += Api.randomInt(9);
		}
		return nevi;
	}
	
	public static long genLongLength(int length) {
		long nevi = 0;
		for (int i = 0; i < length; i++) {
			nevi *= 10;
			nevi += Api.RANDOM.nextInt(9);
		}
		return nevi;
	}
	
	/**
	 * Fast equals without substringing
	 * 
	 * @param str1 String one
	 * @param off1 Offset of string one
	 * @param str2 String two
	 * @param off2 Offset of string two
	 * @param len Length
	 * 
	 * @return `true` If strings are same, otherwise `false`
	 */
	public static boolean equals(CharSequence str1, int off1, CharSequence str2, int off2, int len) {
		len += off1;
		for(; off1 < len; off1++, off2++) {
			if(str1.charAt(off1) != str2.charAt(off2)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Fast case insensitive equals without substringing
	 * 
	 * @param str1 String one
	 * @param off1 Offset of string one
	 * @param str2 String two
	 * @param off2 Offset of string two
	 * @param len Length
	 * 
	 * @return `true` If strings are same, otherwise `false`
	 */
	public static boolean equalsIgnoreCase(CharSequence str1, int off1, CharSequence str2, int off2, int len) {
		len += off1;
		for(; off1 < len; off1++, off2++) {
			if(Character.toUpperCase(str1.charAt(off1)) != Character.toUpperCase(str2.charAt(off2))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Fast {@link String#replace(CharSequence, CharSequence)}
	 * 
	 * @param str String to process
	 * @param find String to find
	 * @param repl String to replace
	 * @param sensitive If `true`, match case sensitive
	 * 
	 * @return Final processed string
	 */
	public static String replace(String str, String find, String repl, boolean sensitive) {
		if(str == null || str.length() == 0
				|| find == null || find.length() == 0 || find.length() > str.length()
				|| repl == null) {
			return str;
		}
		if(!sensitive) {
			return Pattern.compile(find, Pattern.LITERAL | Pattern.CASE_INSENSITIVE)
					.matcher(str).replaceAll(Matcher.quoteReplacement(repl));
		}
		int start = 0;
		int end = str.indexOf(find, start);
		if(end == -1) {
			return str;
		}
		/*if(find.length() == 1 && repl.length() == 1) {
			char fc = find.charAt(0);
			char rc = repl.charAt(0);
			StringBuilder sb = new StringBuilder(str);
			while(end != -1) {
				start = end + 1;
				sb.setCharAt(end, rc);
				end = str.indexOf(fc, start);
			}
			return sb.toString();
		} else */{
			int inc = repl.length() - find.length();
			int inc2 = str.length() / find.length() / 512;
			inc2 = ((inc2 < 16) ? 16 : inc);
			StringBuilder sb = new StringBuilder(str.length() + ((inc < 0) ? 0 : (inc * inc2)));
			while(end != -1) {
				sb.append(str, start, end);
				sb.append(repl);
				start = end + find.length();
				end = str.indexOf(find, start);
			}
			if(start != str.length()) {
				sb.append(str, start, str.length());
			}
			return sb.toString();
		}
	}
	
	public static String replace(String str, String findtxt, String replacetxt) {
		return Api.replace(str, findtxt, replacetxt, true);
	}
	
	/**
	 * Replace multiple 
	 * 
	 * @param str Original string to process
	 * @param args Array of strings to find & replace 
	 *             {"find", "replace", "find2", "replace2", ...}
	 *             (will be call {@link Object#toString()})
	 * @param sensitive Match case sensitive
	 * 
	 * @return Final processed string
	 */
	public static String replace(String str, Object[] args, boolean sensitive) {
		int len = args.length;
		if((len & 0x01) == 0x01) {
			len--;
		}
		for(int i = 0; i < len;) {
			str = Api.replace(str, args[i++].toString(), args[i++].toString(), sensitive);
		}
		return str;
	}
	
	/**
	 * @see {@link Api#replace(String, Object[], boolean)}
	 */
	public static String replace(String str, Object[] args) {
		return Api.replace(str, args, true);
	}
	
	/**
	 * Replace multiple 
	 * 
	 * @param str Original string to process
	 * @param args Replaces (will be call {@link Object#toString()})
	 * @param sensitive Match case sensitive
	 * 
	 * @return Final processed string
	 */
	public static String replace(String str, Map<?, ?> args, boolean sensitive) {
		Iterator<? extends Entry<?, ?>> it = args.entrySet().iterator();
		while(it.hasNext()) {
			Entry<?, ?> ent = it.next();
			str = Api.replace(str, ent.getKey().toString(), ent.getValue().toString(), sensitive);
		}
		return str;
	}
	
	/**
	 * @see {@link Api#replace(String, Map, boolean)}
	 */
	public static String replace(String str, Map<?, ?> args) {
		return Api.replace(str, args, true);
	}
	
	
	public static String join(Object[] vals, String jch) {
		return Api.join(vals,  jch);
	}
	
	public static String join(Object[] vals, String jch, String start, String end) {
		StringBuilder sb = new StringBuilder();
		if(start != null) {
			sb.append(start);
		}
		if(jch == null || jch.length() == 0) {
			jch = null;
		}
		for(int i = 0, n = vals.length; i < n; i++) {
			if(i != 0 && jch != null) {
				sb.append(jch);
			}
			sb.append("" + vals[i]);
		}
		if(end != null) {
			sb.append(end);
		}
		return sb.toString();
	}
	
	public static String join(Iterator<?> it, String jch) {
		return Api.join(it,  jch);
	}
	
	public static String join(Iterator<?> it, String jch, String start, String end) {
		StringBuilder sb = new StringBuilder();
		if(start != null) {
			sb.append(start);
		}
		if(jch == null || jch.length() == 0) {
			jch = null;
		}
		if(it.hasNext()) {
			while(it.hasNext()) {
				sb.append("" + it.next());
				if(it.hasNext()) {
					if(jch != null) {
						sb.append(jch);
					}
				} else {
					break;
				}
			}
		}
		if(end != null) {
			sb.append(end);
		}
		return sb.toString();
	}
	
	public static String getMultiple(int times, char c) {
		char[] chars = new char[times];
		int i = 0;
		while(i < times) {
			chars[i++] = c;
		}
		return new String(chars);
	}
	
	public static String getMultiple(int times, String c, int off, int len) {
		char[] chars = new char[len];
		c.getChars(off, off + len, chars, 0);
		return Api.getMultiple(times, chars);
	}
	
	public static String getMultiple(int times, String c) {
		return Api.getMultiple(times, c.toCharArray());
	}
	
	public static String getMultiple(int times, char[] c) {
		if(c.length == 0) {
			return "";
		} else if(c.length == 1) {
			return Api.getMultiple(times, c[0]);
		}
		char[] chars = new char[times * c.length];
		int i = 0;
		while(i < times) {
			Api.memcpy(chars, (i * c.length), c, 0, c.length);
			i++;
		}
		return new String(chars);
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
					if(uns != null && im.c.isAssignableFrom(uns.getClass())) {
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
		if(Api.is64Bit == -1) {
			Object retv = Api.getUnsafe().getValue("ADDRESS_SIZE");
			if(retv == null) {
				Api.is64Bit = 1;
				return true;
			}
			Api.is64Bit = ((int) ((Integer) retv) == 8) ? 1 : 0;
		}
		return Api.is64Bit != 0;
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
	
	public static void memcpy(Object to, Object from, int size) {
		System.arraycopy(from, 0, to, 0, size);
	}
	
	public static void memcpy(Object to, int posto, Object from, int posfrom, int size) {
		System.arraycopy(from, posfrom, to, posto, size);
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
	
	
	
	/*** ROUND & FLOOR & CEIL ***/
	
	public static int fastround(double num) {
		return (int) ((num > 0.0) ? (num + 0.5) : (num - 0.5));
	}

	public static int fastroundf(float num) {
		return (int) ((num > 0.0F) ? (num + 0.5F) : (num - 0.5F));
	}

	public static int fastfloor(double num) {
		int lnum = (int) num;
		return ((lnum > 0) ? (lnum) : (lnum - 1));
	}

	public static int fastfloorf(float num) {
		int lnum = (int) num;
		return ((lnum > 0) ? (lnum) : (lnum - 1));
	}

	public static int fastceil(double num) {
		int lnum = (int) num;
		return ((lnum > 0) ? (lnum + 1) : (lnum));
	}

	public static int fastceilf(float num) {
		int lnum = (int) num;
		return ((lnum > 0) ? (lnum + 1) : (lnum));
	}
	
	
	
	public static long fastroundl(double num) {
		return (long) ((num > 0.0) ? (num + 0.5) : (num - 0.5));
	}

	public static long fastroundfl(float num) {
		return (long) ((num > 0.0F) ? (num + 0.5F) : (num - 0.5F));
	}

	public static long fastfloorl(double num) {
		long lnum = (long) num;
		return ((lnum > 0) ? (lnum) : (lnum - 1));
	}

	public static long fastfloorfl(float num) {
		long lnum = (long) num;
		return ((lnum > 0) ? (lnum) : (lnum - 1));
	}

	public static long fastceill(double num) {
		long lnum = (long) num;
		return ((lnum > 0) ? (lnum + 1) : (lnum));
	}

	public static long fastceilfl(float num) {
		long lnum = (long) num;
		return ((lnum > 0) ? (lnum + 1) : (lnum));
	}
	
	
	
}
