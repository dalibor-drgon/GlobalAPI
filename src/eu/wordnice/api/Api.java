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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Api {
	
	public static Random rand = new Random();
	public static byte[] GENSTRING = "abcdefghijklmnopqrstuvwxyz1234567890QWERTZUIOPASDFGHJKLYXCVBNM".getBytes();
	
	protected static InstanceMan unsafe;
	
	
	public static Set<Thread> getThreads() {
		Object gt = InstanceMan.getValue(null, Thread.class, "getThreads");
		if(gt instanceof Thread[]) {
			return new Set<Thread>((Thread[]) gt);
		}
		java.util.Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		Set<Thread> retv = new Set<Thread>();
		retv.addAllWC(threadSet);
		return retv;
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
		/*return new byte[]{ (byte) addr, 
				(byte)(addr >>> 8),
				(byte)(addr >>> 16),
				(byte)(addr >>> 24) };*/
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
		Map<String, String> ret = new Map<String, String>();
		X x;
		Y y;
		for(int i = 0; i < map.size(); i++) {
			x = map.getNameI(i);
			y = map.getI(i);
			ret.addWC(x.toString(), y.toString());
		}
		return ret;
	}
	
	public static <X> Set<String> toStringSet(Set<X> set) {
		Set<String> ret = new Set<String>();
		X x;
		for(int i = 0; i < set.size(); i++) {
			x = set.get(i);
			ret.addWC(x.toString());
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X[] toArray(X... vals) {
		return vals;
	}
	
	public static Object[] toArrayO(Object... vals) {
		return vals;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> Set<X> toSet(X... vals) {
		if(vals == null) {
			return null;
		}
		return new Set<X>(vals);
	}
	
	public static <X> Set<X> toSetX(Object... vals) {
		if(vals == null) {
			return null;
		}
		Set<X> ret = new Set<X>();
		ret.addAllWC(vals, vals.length);
		return ret;
	}
	
	public static Set<Object> toSetO(Object... vals) {
		return new Set<Object>(vals);
	}
	
	public static <X, Y> Map<X, Y> toMap(X[] x, Y[] y) {
		if(x == null || y == null) {
			return null;
		}
		if(x.length != y.length) {
			return null;
		}
		return Api.toMap(x, y, x.length);
	}
	
	public static <X, Y> Map<X, Y> toMap(X[] x, Y[] y, int len) {
		if(x == null || y == null) {
			return null;
		}
		Map<X,Y> map = new Map<X,Y>();
		map.addAll(x, y, len);
		return map;
	}
	
	public static <X, Y> Map<X, Y> toMapX(Object[] x, Object[] y) {
		if(x == null || y == null) {
			return null;
		}
		if(x.length != y.length) {
			return null;
		}
		return Api.toMapX(x, y, x.length);
	}
	
	public static <X, Y> Map<X, Y> toMapX(Object[] x, Object[] y, int len) {
		if(x == null || y == null) {
			return null;
		}
		Map<X,Y> map = new Map<X,Y>();
		map.addAll(x, y, len);
		return map;
	}
	
	public static <X, Y> Map<X, Y> toMapO(Object[] x, Object[] y) {
		if(x == null || y == null) {
			return null;
		}
		if(x.length != y.length) {
			return null;
		}
		return Api.toMapO(x, y, x.length);
	}
	
	public static <X, Y> Map<X, Y> toMapO(Object[] x, Object[] y, int len) {
		Map<X, Y> map = new Map<X, Y>();
		map.addAll(x, y, len);
		return map;
	}

	public static boolean equalsO(Object o, Object... vals) {
		if(vals == null || vals.length < 1) {
			return false;
		}
		for(Object obj : vals) {
			if(obj == o || (obj != null && obj.equals(o))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean equalsSL(String o, String... vals) {
		if(vals == null || vals.length < 1) {
			return false;
		}
		for(String obj : vals) {
			if(obj == o || (obj != null && obj.equalsIgnoreCase(o))) {
				return true;
			}
		}
		return false;
	}
	
	@SafeVarargs
	public static <X> boolean equals(X o, X... vals) {
		if(vals == null || vals.length < 1) {
			return false;
		}
		for(Object obj : vals) {
			if(obj == o || (obj != null && obj.equals(o))) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> boolean equalsUnsafe(X o, X... vals) {
		if(vals == null || vals.length < 1) {
			return false;
		}
		for(Object obj : vals) {
			if(obj == o || (obj != null && obj.equals(o))) {
				return true;
			}
		}
		return false;
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
			System.out.println("Class loaded: \"" + cls + "\"");
			return true;
		} catch (Throwable t) {
			System.out.println("Cant load class \"" + cls + "\" INFO:");
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
	
	public static String getColoredString(String text) {
		text = text.replace("&0", "\u00A7\u0000");
		text = text.replace("&1", "\u00A7\u0001");
		text = text.replace("&2", "\u00A7\u0002");
		text = text.replace("&3", "\u00A7\u0003");
		text = text.replace("&4", "\u00A7\u0004");
		text = text.replace("&5", "\u00A7\u0005");
		text = text.replace("&6", "\u00A7\u0006");
		text = text.replace("&7", "\u00A7\u0007");
		text = text.replace("&8", "\u00A7\u0008");
		text = text.replace("&9", "\u00A7\u0009");
		text = text.replace("&a", new String(new char[] {0x00A7, 0x000A}));
		text = text.replace("&b", "\u00A7\u000B");
		text = text.replace("&c", "\u00A7\u000C");
		text = text.replace("&d", new String(new char[] {0x00A7, 0x000D}));
		text = text.replace("&e", "\u00A7\u000E");
		text = text.replace("&f", "\u00A7\u000F");
		text = text.replace("&k", "\u00A7\u0010");
		text = text.replace("&l", "\u00A7\u0011");
		text = text.replace("&m", "\u00A7\u0012");
		text = text.replace("&n", "\u00A7\u0013");
		text = text.replace("&o", "\u00A7\u0014");
		text = text.replace("&r", "\u00A7\u0015");
		return text;
	}
	
	public static String getDecoloredString(String text) {
		text = text.replace("\u00A7\u0000", "&0");
		text = text.replace("\u00A7\u0001", "&1");
		text = text.replace("\u00A7\u0002", "&2");
		text = text.replace("\u00A7\u0003", "&3");
		text = text.replace("\u00A7\u0004", "&4");
		text = text.replace("\u00A7\u0005", "&5");
		text = text.replace("\u00A7\u0006", "&6");
		text = text.replace("\u00A7\u0007", "&7");
		text = text.replace("\u00A7\u0008", "&8");
		text = text.replace("\u00A7\u0009", "&9");
		text = text.replace(new String(new char[] {0x00A7, 0x000A}), "&a");
		text = text.replace("\u00A7\u000B", "&b");
		text = text.replace("\u00A7\u000C", "&c");
		text = text.replace(new String(new char[] {0x00A7, 0x000D}), "&d");
		text = text.replace("\u00A7\u000E", "&e");
		text = text.replace("\u00A7\u000F", "&f");
		text = text.replace("\u00A7\u0010", "&k");
		text = text.replace("\u00A7\u0011", "&l");
		text = text.replace("\u00A7\u0012", "&m");
		text = text.replace("\u00A7\u0013", "&n");
		text = text.replace("\u00A7\u0014", "&o");
		text = text.replace("\u00A7\u0015", "&r");
		return text;
	}
	
	public static java.util.ArrayList<Object> toListO(Set<?> set) {
		if(set == null) {
			return null;
		}
		int sz = set.size();
		if(sz < 1) {
			return new java.util.ArrayList<Object>();
		}
		java.util.ArrayList<Object> list = new java.util.ArrayList<Object>();
		Object o;
		for(int i = 0; i < sz; i++) {
			o = set.get(i);
			list.add(o);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> java.util.ArrayList<X> toListX(Set<?> set) {
		if(set == null) {
			return null;
		}
		int sz = set.size();
		if(sz < 1) {
			return new java.util.ArrayList<X>();
		}
		java.util.ArrayList<X> list = new java.util.ArrayList<X>();
		X x;
		for(int i = 0; i < sz; i++) {
			x = (X) set.get(i);
			list.add(x);
		}
		return list;
	}
	
	public static <X> java.util.ArrayList<X> toList(Set<X> set) {
		if(set == null) {
			return null;
		}
		int sz = set.size();
		if(sz < 1) {
			return new java.util.ArrayList<X>();
		}
		java.util.ArrayList<X> list = new java.util.ArrayList<X>();
		X x;
		for(int i = 0; i < sz; i++) {
			x = set.get(i);
			list.add(x);
		}
		return list;
	}
	
	public static String getEscapedString(String s) {
		s = s.replaceAll("\n", "\\n");
		s = s.replaceAll("\t", "\\t");
		s = s.replaceAll("\b", "\\b");
		s = s.replaceAll("\r", "\\r");
		return s;
	}
	
	public static String getDeescapedString(String s) {
		s = s.replaceAll("\\n", "\n");
		s = s.replaceAll("\\t", "\t");
		s = s.replaceAll("\\b", "\b");
		s = s.replaceAll("\\r", "\r");
		return s;
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
		}
	}
	
	
	/*** CLASS LOADERS ***/
	public static ClassLoader getClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}
	
	public static Set<Class<?>> getFilteredClasses(Set<Class<?>> in, Class<?> ext) {
		return Api.getFilteredClasses(new Set<Class<?>>(), in, ext);
	}
	
	public static Set<Class<?>> getFilteredClasses(Set<Class<?>> ret, Set<Class<?>> in, Class<?> ext) {
		if(ret == null) {
			return null;
		}
		if(in == null || in.size() < 1) {
			return ret;
		}
		if(ext == null) {
			ext = Object.class;
		}
		Class<?> c;
		for(int i = 0; i < in.size(); i++) {
			c = in.get(i);
			if(ext.isAssignableFrom(c) == true) { //c.isAssignableFrom(ext) == true) {
				ret.addWC(c);
			}
		}
		return ret;
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
	
	public static Set<String> filterClasses(Set<String> in, String pref) {
		Set<String> out = new Set<String>();
		Api.filterClasses(out, in, pref);
		return out;
	}
	
	public static void filterClasses(Set<String> out, Set<String> in, String pref) {
		if(pref == null || pref.length() == 0) {
			int i = 0;
			int size = in.size();
			for(; i < size; i++) {
				String cur = in.get(i);
				if(cur.startsWith(pref)) {
					out.addWC(cur);
				}
			}
			return;
		}
		int i = 0;
		int size = in.size();
		for(; i < size; i++) {
			String cur = in.get(i);
			if(cur.startsWith(pref) && cur.startsWith(pref)) {
				out.addWC(cur);
			}
		}
	}
	
	public static Set<String> filterPackages(Set<String> in, String pref) {
		Set<String> out = new Set<String>();
		Api.filterPackages(out, in, pref);
		return out;
	}
	
	public static void filterPackages(Set<String> out, Set<String> in, String pref) {
		if(pref == null || pref.length() == 0) {
			int i = 0;
			int size = in.size();
			for(; i < size; i++) {
				String cur = in.get(i);
				int ind = cur.lastIndexOf('.');
				if(ind != -1) {
					out.add(cur.substring(0, ind));
				}
			}
			return;
		}
		int i = 0;
		int size = in.size();
		for(; i < size; i++) {
			String cur = in.get(i);
			int ind = cur.lastIndexOf('.');
			if(ind != -1 && cur.startsWith(pref)) {
				out.add(cur.substring(0, ind));
			}
		}
	}
	
	public static void loadClasses(Set<Class<?>> out, Set<String> in) throws Throwable {
		int i = 0;
		int size = in.size();
		while(i < size) {
			out.addWC(Api.getClass(in.get(i++)));
		}
	}
	
	public static void loadClasses(Set<Class<?>> out, Set<String> in, ClassLoader cl) throws Throwable {
		int i = 0;
		int size = in.size();
		while(i < size) {
			out.addWC(cl.loadClass(in.get(i++)));
		}
	}
	
	public static void loadPackages(Set<Package> out, Set<String> in) throws Throwable {
		int i = 0;
		int size = in.size();
		while(i < size) {
			out.addWC(Package.getPackage(in.get(i++)));
		}
	}
	
	public static File getClassesLocation(Class<?> cls) throws URISyntaxException {
		return new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
	}
	
	public static File getClassesLocation(ClassLoader cl) throws URISyntaxException {
		return new File(cl.getResource("").toURI());
	}
	
	private static void getClassesFolder(Set<String> set, File fd, String pref) {
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
					set.addWC(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
		}
	}
	
	public static Set<String> getClasses(File fd) throws Exception {
		if(fd.isDirectory()) {
			Set<String> set = new Set<String>();
			Api.getClassesFolder(set, fd, "");
			return set;
		}
		return Api.getClasses(new ZipInputStream(new FileInputStream(fd)));
	}
	
	public static void getClasses(Set<String> set, File fd) throws Exception {
		if(fd.isDirectory()) {
			Api.getClassesFolder(set, fd, "");
			return;
		}
		Api.getClasses(set, new ZipInputStream(new FileInputStream(fd)));
	}
	
	public static Set<String> getClasses(ZipInputStream zip) throws Exception {
		Set<String> set = new Set<String>();
		Api.getClasses(set, zip);
		return set;
	}
	
	public static void getClasses(Set<String> set, ZipInputStream zip) throws Exception {
		ZipEntry ent = null;
		while((ent = zip.getNextEntry()) != null) {
			if(!ent.isDirectory()) {
				String clsn = ent.getName();
				if(clsn.endsWith(".class") && clsn.indexOf('$') == -1) {
					set.addWC(clsn.substring(0, clsn.length() - 6).replace(File.separatorChar, '.'));
				}
			}
			zip.closeEntry();
		}
		zip.close();
	}
	
	
	public static String getMatchedString(String in, String p, Handler.OneHandler<String, Matcher> han) {
		return Api.getMatchedString(in, Pattern.compile(p), han);
	}
	
	public static String getMatchedString(String in, Pattern p, Handler.OneHandler<String, Matcher> han) {
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
	
	public static boolean equalStrings(String in, String p) {
		return Api.equalStrings(in, Pattern.compile(p));
	}
	
	public static boolean equalStrings(String in, Pattern p) {
		return p.matcher(in).find();
	}
	
	
	public static String readFileChars(File file) {
		return Api.readFileChars(Api.getRealPath(file));
	}
	
	public static String readFileChars(String file) {
		StringBuilder sb = new StringBuilder();
		try {
			InputStream in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			char[] buffer = new char[1024 * 4];
			int cur;
			while((cur = br.read(buffer)) > 0) {
				sb.append(buffer, 0, cur);
			}
			br.close();
			in.close();
		} catch(Throwable t) {
			t.printStackTrace();
			if(sb.length() < 1) {
				return null;
			}
		}
		return sb.toString();
	}
	
	public static byte[] readFileBytes(File file) {
		return Api.readFileBytes(Api.getRealPath(file));
	}
	
	public static byte[] readFileBytes(String file) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			InputStream in = new FileInputStream(file);
			byte[] buffer = new byte[1024 * 4];
			int cur;
			while((cur = in.read(buffer)) > 0) {
				out.write(buffer, 0, cur);
			}
			in.close();
		} catch(Throwable t) {
			t.printStackTrace();
			if(out.size() < 1) {
				return null;
			}
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
			out[i] = chars[rand.nextInt(chars.length)];
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
			nevi += rand.nextInt(9);
		}
		return nevi;
	}
	
	public static long genLong(int length) {
		long nevi = 0;
		for (int i = 0; i < length; i++) {
			nevi *= 10;
			nevi += rand.nextInt(9);
		}
		return nevi;
	}
	
	
	/*** C & UNSAFE ***/
	
	public static InstanceMan getUnsafe() {
		if(Api.unsafe == null) {
			InstanceMan im = null;
			try {
				im = new InstanceMan(Api.getClass("sun.misc.Unsafe"), null);
			} catch(Throwable t) {
				im = new InstanceMan(new Object());
			}
			Object val = im.getValue("theUnsafe");
			Api.unsafe = im;
			if(val != null) {
				im.reinit(val);
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
