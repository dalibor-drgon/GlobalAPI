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
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

//import javaagent.JavaAgent;

public class Api {
	
	protected static Random rand = new Random();
	protected static Instrumentation instr;
	protected static InstanceMan unsafe;
	
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
		text = text.replaceAll("&0", (""+ChatColor.BLACK));
		text = text.replaceAll("&1", (""+ChatColor.DARK_BLUE));
		text = text.replaceAll("&2", (""+ChatColor.DARK_GREEN));
		text = text.replaceAll("&3", (""+ChatColor.DARK_AQUA));
		text = text.replaceAll("&4", (""+ChatColor.DARK_RED));
		text = text.replaceAll("&5", (""+ChatColor.DARK_PURPLE));
		text = text.replaceAll("&6", (""+ChatColor.GOLD));
		text = text.replaceAll("&7", (""+ChatColor.GRAY));
		text = text.replaceAll("&8", (""+ChatColor.DARK_GRAY));
		text = text.replaceAll("&9", (""+ChatColor.BLUE));
		text = text.replaceAll("&a", (""+ChatColor.GREEN));
		text = text.replaceAll("&b", (""+ChatColor.AQUA));
		text = text.replaceAll("&c", (""+ChatColor.RED));
		text = text.replaceAll("&d", (""+ChatColor.LIGHT_PURPLE));
		text = text.replaceAll("&e", (""+ChatColor.YELLOW));
		text = text.replaceAll("&f", (""+ChatColor.WHITE));
		text = text.replaceAll("&l", (""+ChatColor.BOLD));
		text = text.replaceAll("&o", (""+ChatColor.ITALIC));
		text = text.replaceAll("&r", (""+ChatColor.RESET));
		text = text.replaceAll("&m", (""+ChatColor.STRIKETHROUGH));
		text = text.replaceAll("&n", (""+ChatColor.UNDERLINE));
		return text;
	}
	
	public static String getDecoloredString(String text) {
		text = text.replaceAll((""+ChatColor.BLACK), "&0");
		text = text.replaceAll((""+ChatColor.DARK_BLUE), "&1");
		text = text.replaceAll((""+ChatColor.DARK_GREEN), "&2");
		text = text.replaceAll((""+ChatColor.DARK_AQUA), "&3");
		text = text.replaceAll((""+ChatColor.DARK_RED), "&4");
		text = text.replaceAll((""+ChatColor.DARK_PURPLE), "&5");
		text = text.replaceAll((""+ChatColor.GOLD), "&6");
		text = text.replaceAll((""+ChatColor.GRAY), "&7");
		text = text.replaceAll((""+ChatColor.DARK_GRAY), "&8");
		text = text.replaceAll((""+ChatColor.BLUE), "&9");
		text = text.replaceAll((""+ChatColor.GREEN), "&a");
		text = text.replaceAll((""+ChatColor.AQUA), "&b");
		text = text.replaceAll((""+ChatColor.RED), "&c");
		text = text.replaceAll((""+ChatColor.LIGHT_PURPLE), "&d");
		text = text.replaceAll((""+ChatColor.YELLOW), "&e");
		text = text.replaceAll((""+ChatColor.WHITE), "&f");
		text = text.replaceAll((""+ChatColor.BOLD), "&l");
		text = text.replaceAll((""+ChatColor.ITALIC), "&o");
		text = text.replaceAll((""+ChatColor.RESET), "&r");
		text = text.replaceAll((""+ChatColor.STRIKETHROUGH), "&m");
		text = text.replaceAll((""+ChatColor.UNDERLINE), "&n");
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
	
	public static Class<?> getClass(String name) {
		try {
			return (Class<?>) Class.forName(name);
		} catch(Throwable t) {}
		return null;
	}
	
	public static Set<Class<?>> getClasses(ClassLoader cl, String pref) {
		return Api.getClasses(new Set<Class<?>>(), cl, pref);
	}
	
	public static Set<Class<?>> getClasses(Set<Class<?>> set, String pref) {
		return Api.getClasses(set, Api.getClassLoader(), pref);
	}
	
	public static Set<Class<?>> getClasses(String pref) {
		return Api.getClasses(new Set<Class<?>>(), Api.getClassLoader(), pref);
	}
	
	public static Set<Class<?>> getClasses(Set<Class<?>> set, ClassLoader cl, String pref) {
		if(set == null) {
			return null;
		}

		Class<?>[] cls = null;
		if(cl != null) {
			cls = Api.getLoadedClasses(cl);
		} else {
			cls = Api.getAllLoadedClasses();
		}
		if(cls != null) {
			String name;
			for(Class<?> c : cls) {
				name = c.getCanonicalName();
				if(name != null && name.startsWith(pref)) {
					set.addWC(c);
				}
			}
		}
		return set;
	}
	
	public static Class<?>[] getLoadedClasses(ClassLoader loader) {
		return Api.getInstrumentation().getInitiatedClasses(loader);
	}
	
	public static Class<?>[] getAllLoadedClasses() {
		return Api.getInstrumentation().getAllLoadedClasses();
	}
	
	public static Instrumentation getInstrumentation() {
		//return JavaAgent.getInstrumentation();
		if(instr == null) {
			instr = Api.createEmptyInstance(Instrumentation.class);
		}
		return instr;
	}
	
	public static Set<Package> getPackages(String pref) {
		return Api.getPackages(new Set<Package>(), Api.getClassLoader(), pref);
	}
	
	public static Set<Package> getPackages(Set<Package> set, String pref) {
		return Api.getPackages(set, Api.getClassLoader(), pref);
	}
	
	public static Set<Package> getPackages(ClassLoader cl, String pref) {
		return Api.getPackages(new Set<Package>(), cl, pref);
	}
	
	public static Set<Package> getPackages(Set<Package> ret, ClassLoader cl, String pref) {
		if(cl == null || ret == null) {
			return null;
		}
		if(pref == null || pref.length() < 1) {
			Package[] arr = Package.getPackages();
			ret.addAllWC(arr, arr.length);
			return ret;
		}
		Package[] p = Package.getPackages();
		String name;
		for(Package pack : p) {
			name = pack.getName();
			if(name.startsWith(pref)) {
				ret.add(pack);
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static Vector<Class<?>> getClassesFromClassLoader(ClassLoader cl) {
		try {
			Field f = ClassLoader.class.getDeclaredField("classes");
			f.setAccessible(true);
			return (Vector<Class<?>>) f.get(cl);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
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
	
	public static String genString(int length) {
		return genString(length, ("abcdefghijklmnopqrstuvwxyz1234567890QWERTZUIOPASDFGHJKLYXCVBNM").toCharArray());
	}
	
	public static String genString(int length, char[] chars) {
		String sb = new String("");
		for (int i = 0; i < length; i++) {
			char c = chars[rand.nextInt(chars.length)];
			sb += c;
		}
		return sb;
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
		if(unsafe == null) {
			InstanceMan im = new InstanceMan(Api.getClass("sun.misc.Unsafe"), null);
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
			if(Api.getUnsafe().callMethod("throwException", new Object[] { t }, 
					new Class<?>[] { Throwable.class }) != null) {
				return; //unreach
			}
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
