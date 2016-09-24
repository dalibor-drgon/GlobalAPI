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

package wordnice.api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import wordnice.coll.MapWorker;
import wordnice.coll.MapWorkerImpl;
import wordnice.generator.GenInputStream;
import wordnice.generator.Generator;
import wordnice.generator.builtin.SecureGenerator;
import wordnice.generator.builtin.XoRo;
import wordnice.seq.ByteSequence;
import wordnice.streams.ArrayOutputStream;
import wordnice.streams.ArrayOutputStreamInt;
import wordnice.streams.ArrayOutputStreamSimple;
import wordnice.streams.BufferedInput;
import wordnice.streams.BufferedOutput;

public class Nice {
	
	/**
	 * Create logger with given prefix
	 * @param name Name and prefix of logger
	 * @return Logger, parent of global logger with prefixed messaging
	 * @throws IllegalArgumentException Name == null
	 */
	public static Logger createLogger(String name)
			throws IllegalArgumentException {
		if(name == null) throw new IllegalArgumentException("Name == null");
		return new PrefixedLogger(name, name);
	}
	
	/**
	 * Create logger with given prefix
	 * @param className Class name used for debugging purpose
	 * @param name Prefix of logger
	 * @return Logger, parent of global logger with prefixed messaging
	 * @throws IllegalArgumentException Name == null or className == null
	 */
	public static Logger createLogger(String className, String name) {
		if(name == null) throw new IllegalArgumentException("Name == null");
		if(className == null) throw new IllegalArgumentException("ClassName == null");
		return new PrefixedLogger(className, name);
	}
	
	/**
	 * Create logger with given prefix
	 * @param clz Class where the logger is used
	 * @param name Prefix of logger
	 * @return Logger, parent of global logger with prefixed messaging
	 * @throws IllegalArgumentException Name == null or class == null
	 */
	public static Logger createLogger(Class<?> clz, String name) {
		if(clz == null) throw new IllegalArgumentException("Class == null");
		if(name == null) throw new IllegalArgumentException("Name == null");
		return new PrefixedLogger(clz.getName(), name);
	}
	
	/**
	 * Create logger for given class with prefix of class name
	 * @param clz Class where the logger is used
	 * @return Logger, parent of global logger with prefixed messaging
	 * @throws IllegalArgumentException class == null
	 */
	public static Logger createLogger(Class<?> clz) {
		if(clz == null) throw new IllegalArgumentException("Class == null");
		return new PrefixedLogger(clz.getName(), clz.getSimpleName());
	}
	
	protected static class PrefixedLogger extends Logger {
	  protected String prefix;
	  
	  protected PrefixedLogger(String className, String prefix) {
	    super(className, null);
	    this.prefix = ("[" + prefix + "] ");
	    setParent(Logger.getGlobal());
	    setLevel(Level.ALL);
	  }
	  
	  @Override
	  public void log(LogRecord logRecord) {
	    logRecord.setMessage(this.prefix.concat(logRecord.getMessage()));
	    super.log(logRecord);
	  }
	}
	
	/**
	 * Check given throwable if is java machine error 
	 * and rethrow when needed
	 */
	public static void checkError(Throwable t) {
		if(t instanceof Error) throw (Error)t;
	}
	
	/**
	 * Check given throwable if is java machine error
	 * Use this call when working with eg. Unsafe, reflections, 
	 * Instrumentation etc
	 * and rethrow when needed
	 */
	public static void checkUnsafeError(Throwable t) {
		if(t instanceof VirtualMachineError 
				|| t instanceof ThreadDeath) throw (Error)t;
	}
	
	public static ScriptEngine createJavascript() {
		try {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
			if(engine != null) return engine;
		} catch(RuntimeException re) {}
		throw new RuntimeException("No Javascript engine found!");
	}
	
	protected static Generator generator;
	protected static Generator secureGenerator;
	
	public static Generator generator() {
		if(generator == null) generator = new XoRo();
		return generator;
	}
	
	public static Generator secureGenerator() {
		if(secureGenerator == null) secureGenerator = new SecureGenerator();
		return secureGenerator;
	}
	
	public static GenInputStream generatorStream() {
		return generator().getStream();
	}
	
	public static GenInputStream secureGeneratorStream() {
		return secureGenerator().getStream();
	}
	
	public static final String LineSeparator = System.getProperty("line.separator");
	public static final byte[] LineSeparatorBytes = LineSeparator.getBytes();
	
	public static final Charset UTF8 = Charset.forName("UTF-8");
	
	/**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
	public static final int MaxArrayLength = Integer.MAX_VALUE - 8;
	
	public static int BufferSize = 8*1024;
	public static int builderSize = 512;
	public static int MinBuilderSize = 128;
	public static byte[] LoopBytesBuffer = new byte[BufferSize];
	public static char[] LoopCharsBuffer = new char[BufferSize];
	
	public static BufferedInputStream input(File f) throws FileNotFoundException {
		return new BufferedInput(new FileInputStream(f), Nice.BufferSize);
	}
	
	public static BufferedOutputStream output(File f) throws FileNotFoundException {
		return new BufferedOutput(new FileOutputStream(f), Nice.BufferSize);
	}
	
	public static BufferedInputStream input(File f, int buffer) 
			throws FileNotFoundException {
		return new BufferedInput(new FileInputStream(f), buffer);
	}
	
	public static BufferedOutputStream output(File f, int buffer) 
			throws FileNotFoundException {
		return new BufferedOutput(new FileOutputStream(f), buffer);
	}
	
	public static StringBuilder createStringBuilder() {
		return new StringBuilder(Nice.builderSize);
	}
	
	public static StringBuilder createStringBuilder(int cap) {
		return new StringBuilder(cap);
	}
	
	public static ArrayOutputStream createArrayOutput() {
		return new ArrayOutputStreamInt(Nice.BufferSize);
	}
	
	public static ArrayOutputStream createArrayOutput(int cap) {
		return new ArrayOutputStreamInt(cap);
	}

	public static ArrayOutputStream createArrayOutputSmall() {
		return new ArrayOutputStreamSimple(Nice.BufferSize);
	}
	
	public static ArrayOutputStream createArrayOutputSmall(int cap) {
		return new ArrayOutputStreamSimple(cap);
	}
	
	public static ByteArrayInputStream createArrayInput(byte[] buff, int off, int len) {
		return new ByteArrayInputStream(buff, off, len);
	}
	
	public static ByteArrayInputStream createArrayInput(byte[] buff) {
		return new ByteArrayInputStream(buff);
	}
	
	public static BufferedInputStream buffered(InputStream in) {
		if(in instanceof BufferedInputStream) {
			return ((BufferedInputStream) in);
		}
		return new BufferedInput(in, Nice.BufferSize);
	}
	
	public static BufferedOutputStream buffered(OutputStream in) {
		if(in instanceof BufferedOutputStream) {
			return ((BufferedOutputStream) in);
		}
		return new BufferedOutput(in, Nice.BufferSize);
	}
	
	public static BufferedInputStream buffered(InputStream in, int cap) {
		if(in instanceof BufferedInputStream) {
			return ((BufferedInputStream) in);
		}
		return new BufferedInput(in, cap);
	}
	
	public static BufferedOutputStream buffered(OutputStream in, int cap) {
		if(in instanceof BufferedOutputStream) {
			return ((BufferedOutputStream) in);
		}
		return new BufferedOutput(in, cap);
	}
	
	public static EOFException eof() {
		return new EOFException();
	}
	
	public static EOFException eof(int readed, int wanted) {
		return new EOFException("Readed " + readed + " bytes from " + wanted + " total!");
	}
	
	public static void checkLength(long len) {
		if(len > MaxArrayLength || len < 0) {
			throw new UnsupportedOperationException(
					"Tried to allocate array with " + len + " objects, aborting!");//mask
		}
	}
	
	/**
	 * @return Recomended size for buffer
	 * 		If size >= 400kb, returns 32kib
	 * 		If size >= 200kb, returns 16kib
	 * 		If size >= 8kib, returns 8kib
	 * 		Else returns given size
	 */
	public static int bufferSize(long sizeToMove) {
		return (sizeToMove >= 400000) ? 32*1024 : (
				(sizeToMove >= 200000) ? 16*1024 : (
						(sizeToMove >= 8*1024) ? 8*1024 : (int)sizeToMove));
	}
	
	public static void notNull(Object obj, String msg)
			throws IllegalArgumentException {
		if(obj == null) throw new IllegalArgumentException(msg);
	}
	
	public static IllegalArgumentException illegal(String msg)
			throws IllegalArgumentException {
		throw new IllegalArgumentException(msg);
	}
	
	public static ArrayIndexOutOfBoundsException bounds(int x)
			throws ArrayIndexOutOfBoundsException {
		throw new ArrayIndexOutOfBoundsException(x);
	}
	
	public static void checkBounds(byte[] arr, int offset, int length) 
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if(arr == null) throw Nice.illegal("Null array!");
        if(length < 0) throw new ArrayIndexOutOfBoundsException("Length ("+length+") < 0");
        if(offset < 0) throw new ArrayIndexOutOfBoundsException("Offset ("+offset+") < 0");
        if(offset+length > arr.length) 
        	throw new ArrayIndexOutOfBoundsException("Offset+Length ("
        			+(offset+length)+") > Array length ("+arr.length+")");
	}
	
	public static void checkBounds(char[] arr, int offset, int length) 
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if(arr == null) throw Nice.illegal("Null array!");
        if(length < 0) throw new ArrayIndexOutOfBoundsException("Length ("+length+") < 0");
        if(offset < 0) throw new ArrayIndexOutOfBoundsException("Offset ("+offset+") < 0");
        if(offset+length > arr.length) 
        	throw new ArrayIndexOutOfBoundsException("Offset+Length ("
        			+(offset+length)+") > Array length ("+arr.length+")");
	}
	
	public static void checkBounds(Object[] arr, int offset, int length) 
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if(arr == null) throw Nice.illegal("Null array!");
        if(length < 0) throw new ArrayIndexOutOfBoundsException("Length ("+length+") < 0");
        if(offset < 0) throw new ArrayIndexOutOfBoundsException("Offset ("+offset+") < 0");
        if(offset+length > arr.length) 
        	throw new ArrayIndexOutOfBoundsException("Offset+Length ("
        			+(offset+length)+") > Array length ("+arr.length+")");
	}
	
	public static void checkBounds(CharSequence obj, int offset, int length) 
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if(obj == null) throw Nice.illegal("Null sequence!");
        if(length < 0) throw new ArrayIndexOutOfBoundsException("Length ("+length+") < 0");
        if(offset < 0) throw new ArrayIndexOutOfBoundsException("Offset ("+offset+") < 0");
        if(offset+length > obj.length()) 
        	throw new ArrayIndexOutOfBoundsException("Offset+Length ("
        			+(offset+length)+") > Array length ("+obj.length()+")");
	}
	
	public static void checkBounds(ByteSequence obj, int offset, int length) 
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		if(obj == null) throw Nice.illegal("Null sequence!");
        if(length < 0) throw new ArrayIndexOutOfBoundsException("Length ("+length+") < 0");
        if(offset < 0) throw new ArrayIndexOutOfBoundsException("Offset ("+offset+") < 0");
        if(offset+length > obj.length()) 
        	throw new ArrayIndexOutOfBoundsException("Offset+Length ("
        			+(offset+length)+") > Array length ("+obj.length()+")");
	}
	
	public static class Debug {
	    public static StackTraceElement getCaller() { 
	        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
	        for (int i=1; i<stElements.length; i++) {
	            StackTraceElement ste = stElements[i];
	            if (!ste.getClassName().equals(Debug.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0) {
	                return ste;
	            }
	        }
	        return null;
	     }
	    public static StackTraceElement getCallerCaller() { 
	        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
	        String callerClassName = null;
	        for (int i=1; i<stElements.length; i++) {
	            StackTraceElement ste = stElements[i];
	            if (!ste.getClassName().equals(Debug.class.getName())&& ste.getClassName().indexOf("java.lang.Thread")!=0) {
	                if (callerClassName==null) {
	                    callerClassName = ste.getClassName();
	                } else if (!callerClassName.equals(ste.getClassName())) {
	                    return ste;
	                }
	            }
	        }
	        return null;
	     }
	}
	
	
	protected static boolean debug = false;
	
	public static interface MapFactory {
		public <K, V> Map<K, V> createMap();
		public <K, V> Map<K, V> createMap(int cap);
		public <K, V> Map<K, V> createMap(Map<K, V> in);
	}
	
	public static interface ConcurrentMapFactory {
		public <K, V> ConcurrentMap<K, V> createMap();
		public <K, V> ConcurrentMap<K, V> createMap(int cap);
		public <K, V> ConcurrentMap<K, V> createMap(Map<K, V> in);
	}
	
	public static interface CollFactory {
		public <V> Collection<V> createColl();
		public <V> Collection<V> createColl(int cap);
		public <V> Collection<V> createColl(Collection<V> in);
	}
	
	public static interface SetFactory extends CollFactory {
		public <V> Set<V> createColl();
		public <V> Set<V> createColl(int cap);
		public <V> Set<V> createColl(Collection<V> in);
	};
	
	public static interface ListFactory extends CollFactory {
		public <V> List<V> createColl();
		public <V> List<V> createColl(int cap);
		public <V> List<V> createColl(Collection<V> in);
	};
	
	
	protected static ListFactory listFactory = new ListFactory() {

		@Override
		public <V> List<V> createColl() {
			return new ArrayList<V>();
		}

		@Override
		public <V> List<V> createColl(int cap) {
			return new ArrayList<V>(cap);
		}

		@Override
		public <V> List<V> createColl(Collection<V> in) {
			return new ArrayList<V>(in);
		}
		
	};
	
	protected static SetFactory setFactory = new SetFactory() {

		@Override
		public <V> Set<V> createColl() {
			return new THashSet<V>();
		}

		@Override
		public <V> Set<V> createColl(int cap) {
			return new THashSet<V>(cap);
		}

		@Override
		public <V> Set<V> createColl(Collection<V> in) {
			return new THashSet<V>(in);
		}
		
	};
	
	protected static SetFactory weakSetFactory = new SetFactory() {

		@Override
		public <V> Set<V> createColl() {
			return Collections.newSetFromMap(new WeakHashMap<V,Boolean>());
		}

		@Override
		public <V> Set<V> createColl(int cap) {
			return Collections.newSetFromMap(new WeakHashMap<V,Boolean>(cap));
		}

		@Override
		public <V> Set<V> createColl(Collection<V> in) {
			Set<V> set = ((in instanceof Set) 
					? this.<V>createColl(in.size()) 
					: this.<V>createColl());
			set.addAll(in);
			return set;
		}
		
	};
	
	protected static MapFactory mapFactory = new MapFactory() {

		@Override
		public <K, V> Map<K, V> createMap() {
			return new THashMap<K, V>();
		}

		@Override
		public <K, V> Map<K, V> createMap(int cap) {
			return new THashMap<K, V>(cap);
		}

		@Override
		public <K, V> Map<K, V> createMap(Map<K, V> in) {
			return new THashMap<K, V>(in);
		}
		
	};
	
	protected static MapFactory weakMapFactory = new MapFactory() {

		@Override
		public <K, V> Map<K, V> createMap() {
			return new WeakHashMap<K, V>();
		}

		@Override
		public <K, V> Map<K, V> createMap(int cap) {
			return new WeakHashMap<K, V>(cap);
		}

		@Override
		public <K, V> Map<K, V> createMap(Map<K, V> in) {
			return new WeakHashMap<K, V>(in);
		}
		
	};
	
	protected static MapFactory linkedMapFactory = new MapFactory() {

		@Override
		public <K, V> Map<K, V> createMap() {
			return new LinkedHashMap<K, V>();
		}

		@Override
		public <K, V> Map<K, V> createMap(int cap) {
			return new LinkedHashMap<K, V>(cap);
		}

		@Override
		public <K, V> Map<K, V> createMap(Map<K, V> in) {
			return new LinkedHashMap<K, V>(in);
		}
		
	};
	
	protected static ConcurrentMapFactory concurrentMapFactory = new ConcurrentMapFactory() {

		@Override
		public <K, V> ConcurrentMap<K, V> createMap() {
			//return new ConcurrentHashMapV8<K, V>();
			return new ConcurrentHashMap<K,V>();
		}

		@Override
		public <K, V> ConcurrentMap<K, V> createMap(int cap) {
			//return new ConcurrentHashMapV8<K, V>(cap);
			return new ConcurrentHashMap<K,V>(cap);
		}

		@Override
		public <K, V> ConcurrentMap<K, V> createMap(Map<K, V> in) {
			//return new ConcurrentHashMapV8<K, V>(in);
			return new ConcurrentHashMap<K,V>(in);
		}
		
	};
	
	/*protected static ConcurrentMapFactory concurrentLinkedMapFactory = new ConcurrentMapFactory() {

		@Override
		public <K, V> ConcurrentMap<K, V> createMap() {
			//return new ConcurrentLinkedHashMap.Builder<K, V>().build();
			return Collections.
		}

		@Override
		public <K, V> ConcurrentMap<K, V> createMap(int cap) {
			//return new ConcurrentLinkedHashMap.Builder<K, V>().initialCapacity(cap).build();
		}

		@Override
		public <K, V> ConcurrentMap<K, V> createMap(Map<K, V> in) {
			/*ConcurrentMap<K, V> ret = new ConcurrentLinkedHashMap.Builder<K, V>()
					.initialCapacity(in.size()).build();
			ret.putAll(in);
			return ret;* /
		}
		
	};*/
	
	/**
	 * @return Map factory
	 */
	public static MapFactory getMapFactory() {
		return mapFactory;
	}
	
	/**
	 * @return Old Map factory
	 */
	public static MapFactory setMapFactory(MapFactory nev) {
		MapFactory old = mapFactory;
		mapFactory = nev;
		return old;
	}
	
	/**
	 * @return Weak Map factory
	 */
	public static MapFactory getWeakMapFactory() {
		return weakMapFactory;
	}
	
	/**
	 * @return Old Weak Map factory
	 */
	public static MapFactory setWeakMapFactory(MapFactory nev) {
		MapFactory old = weakMapFactory;
		weakMapFactory = nev;
		return old;
	}
	
	/**
	 * @return LinkedMap factory
	 */
	public static MapFactory getLinkedMapFactory() {
		return linkedMapFactory;
	}
	
	/**
	 * @return Old LinkedMap factory
	 */
	public static MapFactory setLinkedMapFactory(MapFactory nev) {
		MapFactory old = linkedMapFactory;
		linkedMapFactory = nev;
		return old;
	}
	
	/**
	 * @return Concurrent Map factory
	 */
	public static ConcurrentMapFactory getConcurrentMapFactory() {
		return concurrentMapFactory;
	}
	
	/**
	 * @return Old concurrentMap factory
	 */
	public static ConcurrentMapFactory setConcurrentMapFactory(ConcurrentMapFactory nev) {
		ConcurrentMapFactory old = concurrentMapFactory;
		concurrentMapFactory = nev;
		return old;
	}
	
	/**
	 * @return Concurrent Linked Map factory
	 */
	/*public static ConcurrentMapFactory getConcurrentLinkedMapFactory() {
		return concurrentLinkedMapFactory;
	}*/
	
	/**
	 * @return Old concurrentMap factory
	 */
	/*public static ConcurrentMapFactory setConcurrentLinkedMapFactory(ConcurrentMapFactory nev) {
		ConcurrentMapFactory old = concurrentLinkedMapFactory;
		concurrentLinkedMapFactory = nev;
		return old;
	}*/
	
	/**
	 * @return Set factory
	 */
	public static SetFactory getSetFactory() {
		return setFactory;
	}
	
	/**
	 * @return Old Set factory
	 */
	public static SetFactory setSetFactory(SetFactory nev) {
		SetFactory old = setFactory;
		setFactory = nev;
		return old;
	}
	
	/**
	 * @return Weak Set factory
	 */
	public static SetFactory getWeakSetFactory() {
		return weakSetFactory;
	}
	
	/**
	 * @return Old Weak Set factory
	 */
	public static SetFactory setWeakSetFactory(SetFactory nev) {
		SetFactory old = weakSetFactory;
		weakSetFactory = nev;
		return old;
	}
	
	/**
	 * @return List factory
	 */
	public static ListFactory getListFactory() {
		return listFactory;
	}
	
	/**
	 * @return Old List factory
	 */
	public static ListFactory setListFactory(ListFactory nev) {
		ListFactory old = listFactory;
		listFactory = nev;
		return old;
	}
	
	/**
	 * @return New Map
	 */
	public static <K, V> Map<K, V> createMap() {
		/*
		 * Use those functions in your code!
		 * Do it like pro and use replacer in your editor:
		 * DIRTY REGEX:  new (T)?(Hash)?(Map)\<[a-zA-Z]*\,( )*?[a-zA-Z]*\>
		 * REPLACE    :  Api.createMap
		 */
		return getMapFactory().createMap();
	}
	
	/**
	 * @return New Map ideally allocated with given size
	 */
	public static <K, V> Map<K, V> createMap(int cap) {
		return getMapFactory().createMap(cap);
	}
	
	/**
	 * @return New Map with elements from given map
	 */
	public static <K, V> Map<K, V> createMap(Map<K, V> map) {
		return getMapFactory().createMap(map);
	}
	
	/**
	 * @return New Weak Map
	 */
	public static <K, V> Map<K, V> createWeakMap() {
		return getWeakMapFactory().createMap();
	}
	
	/**
	 * @return New Weak Map ideally allocated with given size
	 */
	public static <K, V> Map<K, V> createWeakMap(int cap) {
		return getWeakMapFactory().createMap(cap);
	}
	
	/**
	 * @return New Weak Map with elements from given map
	 */
	public static <K, V> Map<K, V> createWeakMap(Map<K, V> map) {
		return getWeakMapFactory().createMap(map);
	}
	
	/**
	 * @return New Linked Map
	 */
	public static <K, V> Map<K, V> createLinkedMap() {
		return getLinkedMapFactory().createMap();
	}
	
	/**
	 * @return New Linked Map ideally allocated with given size
	 */
	public static <K, V> Map<K, V> createLinkedMap(int cap) {
		return getLinkedMapFactory().createMap(cap);
	}
	
	/**
	 * @return New Linked Map with elements from given map
	 */
	public static <K, V> Map<K, V> createLinkedMap(Map<K, V> map) {
		return getLinkedMapFactory().createMap(map);
	}
	
	/**
	 * @return New concurrent map
	 */
	public static <K, V> Map<K, V> createConcurrentMap() {
		return getConcurrentMapFactory().createMap();
	}
	
	/**
	 * @return New concurrent Map ideally allocated with given size
	 */
	public static <K, V> Map<K, V> createConcurrentMap(int cap) {
		return getConcurrentMapFactory().createMap(cap);
	}
	
	/**
	 * @return New concurrent Map with elements from given map
	 */
	public static <K, V> Map<K, V> createConcurrentMap(Map<K, V> map) {
		return getConcurrentMapFactory().createMap(map);
	}
	
	/**
	 * @return New ConcurrentLinked map
	 */
	/*public static <K, V> Map<K, V> createConcurrentLinkedMap() {
		return getConcurrentLinkedMapFactory().createMap();
	}*/
	
	/**
	 * @return New ConcurrentLinked Map ideally allocated with given size
	 */
	/*public static <K, V> Map<K, V> createConcurrentLinkedMap(int cap) {
		return getConcurrentLinkedMapFactory().createMap(cap);
	}*/
	
	/**
	 * @return New ConcurrentLinked Map with elements from given map
	 */
	/*public static <K, V> Map<K, V> createConcurrentLinkedMap(Map<K, V> map) {
		return getConcurrentLinkedMapFactory().createMap(map);
	}*/
	
	
	/**
	 * @return New Set
	 */
	public static <V> Set<V> createSet() {
		return getSetFactory().createColl();
	}
	
	/**
	 * @return New Set ideally allocated with given size
	 */
	public static <V> Set<V> createSet(int cap) {
		return getSetFactory().createColl(cap);
	}
	
	/**
	 * @return New Set with elements from given collection
	 */
	public static <V> Set<V> createSet(Collection<V> set) {
		return getSetFactory().createColl(set);
	}
	
	/**
	 * @return New Set with elements from given array
	 */
	public static <V,W extends V> Set<V> createSet(W[] arr) {
		return createSet(Arrays.<V>asList(arr));
	}
	
	/**
	 * @return New Weak Set
	 */
	public static <V> Set<V> createWeakSet() {
		return getWeakSetFactory().createColl();
	}
	
	/**
	 * @return New Weak Set ideally allocated with given size
	 */
	public static <V> Set<V> createWeakSet(int cap) {
		return getWeakSetFactory().createColl(cap);
	}
	
	/**
	 * @return New Weak Set with elements from given collection
	 */
	public static <V> Set<V> createWeakSet(Collection<V> set) {
		return getWeakSetFactory().createColl(set);
	}
	
	/**
	 * @return New Weak Set with elements from given array
	 */
	public static <V,W extends V> Set<V> createWeakSet(W[] arr) {
		return createWeakSet(Arrays.<V>asList(arr));
	}
	
	/**
	 * @return New List
	 */
	public static <V> List<V> createList() {
		return getListFactory().createColl();
	}
	
	/**
	 * @return New List ideally allocated with given size
	 */
	public static <V> List<V> createList(int cap) {
		return getListFactory().createColl(cap);
	}
	
	/**
	 * @return New List with elements from given collection
	 */
	public static <V> List<V> createList(Collection<V> set) {
		return getListFactory().createColl(set);
	}
	
	/**
	 * @return New List with elements from given array
	 */
	public static <V,W extends V> List<V> createList(W[] arr) {
		return createList(Arrays.<V>asList(arr));
	}
	
	
	/**
	 * Errors
	 */
	
	static CannotDoIt cannotDoItException = new CannotDoIt();
	static SkipIt skipItException = new SkipIt();
	static BadFormat badFormatException = new BadFormat();
	static BadArg badArgException = new BadArg();
	
	public static boolean debug() {
		return debug;
	}
	
	public static void debug(boolean nev) {
		debug = nev;
	}
	
	public static CannotDoIt cannotDoIt() {
		if(debug) return new CannotDoIt();
		return cannotDoItException;
	}
	
	public static SkipIt skipIt() {
		if(debug) return new SkipIt();
		return skipItException;
	}
	
	public static BadFormat badFormat() {
		if(debug) return new BadFormat();
		return badFormatException;
	}
	
	public static BadFormat badFormat(String message) {
		return new BadFormat(message);
	}
	
	public static BadFormat badFormat(Throwable cause) {
		return new BadFormat(cause);
	}
	
	public static BadFormat badFormat(String message, Throwable cause) {
		return new BadFormat(message, cause);
	}
	
	
	public static BadArg badArg() {
		if(debug) return new BadArg();
		return badArgException;
	}
	
	public static BadArg badArg(String message) {
		return new BadArg(message);
	}
	
	public static BadArg badArg(Throwable cause) {
		return new BadArg(cause);
	}
	
	public static BadArg badArg(String message, Throwable cause) {
		return new BadArg(message, cause);
	}
	
	public static Masked mask(Throwable t) {
		return new Masked(t);
	}
	
	public static class BadArg extends IllegalArgumentException {
		
		protected static final StackTraceElement[] STACK_NULL = new StackTraceElement[0];
		private static final long serialVersionUID = 1L;

		public BadArg() {}
		
		public BadArg(Throwable cause) {
			super(cause);
		}
		
		public BadArg(String msg) {
			super(msg);
		}
		
		public BadArg(String msg, Throwable cause) {
			super(msg, cause);
		}
		
		@Override
		public Throwable fillInStackTrace() {
			if(debug) {
				super.fillInStackTrace();
			}
			return this;
		}
		
	}
	
	public static class FastRuntimeException extends RuntimeException {
		
		protected static final StackTraceElement[] STACK_NULL = new StackTraceElement[0];
		private static final long serialVersionUID = 1L;

		public FastRuntimeException() {}
		
		public FastRuntimeException(Throwable cause) {
			super(cause);
		}
		
		public FastRuntimeException(String msg) {
			super(msg);
		}
		
		public FastRuntimeException(String msg, Throwable cause) {
			super(msg, cause);
		}
		
		@Override
		public Throwable fillInStackTrace() {
			if(debug) {
				super.fillInStackTrace();
			}
			return this;
		}
		
	}
	
	public static class BadFormat extends FastRuntimeException {

		private static final long serialVersionUID = 1L;
		
		int off = -1;
		int end = -1;
		
		private BadFormat() {}
		
		private BadFormat(String message) {
			super(message);
		}
		
		private BadFormat(Throwable cause) {
			super(cause);
		}
		
		private BadFormat(String message, Throwable cause) {
			super(message, cause);
		}
		
		public BadFormat setOffset(int off) {
			this.off = off;
			return this;
		}
		
		public BadFormat setEnd(int end) {
			this.end = end;
			return this;
		}
		
		public BadFormat setOffsetAndEnd(int off, int end) {
			this.off = off;
			this.end = end;
			return this;
		}
		
		public BadFormat setOffsetAndLength(int off, int len) {
			this.off = off;
			this.end = off + len;
			return this;
		}
		
		@Override
		public String getLocalizedMessage() {
			return this.getMessage();
		}
		
		@Override
		public String getMessage() {
			if(this.getOffset() == -1 && this.getEnd() == -1) {
				return super.getMessage();
			} else if(this.getOffset() == -1) {
				return super.getMessage() 
						+ " (ending offset " + this.getEnd() + ")";
			} else if(this.getEnd() == -1) {
				return super.getMessage() 
					+ " (starting offset " + this.getOffset()+ ")";
			}
			return super.getMessage() 
					+ " (starting offset " + this.getOffset() 
					+ ", ending offset " + this.getEnd() 
					+ ", total length " + this.getLength() + ")";
		}
		
		public int getOffset() {
			return this.off;
		}
		
		public int getEnd() {
			return this.end;
		}
		
		public int getLength() {
			return this.end - this.off;
		}
		
	}
	
	public static class CannotDoIt extends FastRuntimeException {

		private static final long serialVersionUID = 1L;

		private CannotDoIt() {}
		
	}
	
	public static class SkipIt extends FastRuntimeException {

		private static final long serialVersionUID = 1L;

		private SkipIt() {}
		
	}
	
	public static class Masked extends FastRuntimeException {

		private static final long serialVersionUID = 1L;

		private Masked(Throwable t) {
			super(t);
		}
		
	}
	
	public static enum SkippableBoolean {
		ALLOW,
		SKIP,
		FAIL;
		
		public boolean booleanValue() {
			return this == ALLOW;
		}
		
		/**
		 * Throw exception on SKIP or FAIL, returns true on ALLOW
		 */
		public boolean throwException() throws SkipIt, CannotDoIt {
			if(this == ALLOW) {
				return true;
			}
			if(this == SKIP) {
				throw skipIt();
			}
			throw cannotDoIt();
		}
	}
	
	public static class Value<V> {
		
		protected V value;
		
		public Value() {}
		
		public Value(V val) {
			this.value = val;
		}
		
		public V setValue(V nev) {
			V old = value;
			value = nev;
			return old;
		}
		
		public V getValue() {
			return value;
		}
		
	}
	
	public static class MutableEntry<K,V> 
	implements Map.Entry<K,V>, Cloneable {
		
		protected K key;
		protected V value;
		
		public MutableEntry() {}
		
		public MutableEntry(K key, V val) {
			this.key = key;
			this.value = val;
		}
		
		public K setKey(K nev) {
			K old = key;
			key = nev;
			return old;
		}
	
		@Override
		public K getKey() {
			return key;
		}
	
		@Override
		public V getValue() {
			return value;
		}
	
		@Override
		public V setValue(V nev) {
			V old = value;
			value = nev;
			return old;
		}
		
		@Override
		public MutableEntry<K,V> clone() {
			return new MutableEntry<K,V>(key, value);
		}
		
	}

	public static interface DataEntryHandler<K, V> {
		public boolean handle(MutableEntry<K,V> entry);
	}
	
	/**
	 * @return New map worker
	 */
	public static MapWorker createMapWorkerForMap(Map<String, ? extends Object> map) {
		return new MapWorkerImpl(map);
	}
	
	/**
	 * @return New empty map worker
	 */
	public static MapWorker createMapWorker() {
		return new MapWorkerImpl();
	}
	
	/**
	 * @return New empty map worker
	 */
	public static MapWorker createMapWorker(int cap) {
		return new MapWorkerImpl(Nice.<String,Object>createMap(cap));
	}
	
	public static void checkArrayLen(String prefix, int real, int off, int len) {
		if(off < 0) {
			throw badArg(prefix + ": Offset under zero ("+off+")");
		}
		if(len < 0) {
			throw badArg(prefix + ": Length under zero ("+len+")");
		}
		int end = off+len;
		if(end > real) {
			throw badArg(prefix + ": Offset+Length > Array length ("+end+" > "+real+")");
		}
	}
	
	public static void checkArrayEnd(String prefix, int real, int off, int end) {
		if(off < 0) {
			throw badArg(prefix + ": Offset under zero ("+off+")");
		}
		if(off > end) {
			throw badArg(prefix + ": Start > End ("+off+" > "+end+")");
		}
		if(end > real) {
			throw badArg(prefix + ": End > Array length ("+end+" > "+real+")");
		}
	}
	
	public static boolean checkArrayLen(int real, int off, int len) {
		if(off < 0 || len < 0) {
			return false;
		}
		int end = off+len;
		if(end > real) {
			return false;
		}
		return true;
	}
	
	public static boolean checkArrayEnd(int real, int off, int end) {
		return !(off < 0 || off > end || end > real);
	}
	
	
	
	public static String replace(String in, Object[] arr) {
		int i = 0;
		int l = arr.length & 0xFFFFFFFE;
		while(i < l) {
			in = StringUtils.replace(in, ""+arr[i++], ""+arr[i++]);
		}
		return in;
	}

	/**
	 * Translate between String / Number / primitive
	 * 
	 * @return non-null value if can be translated (eg. "1234" -> Float -> 1234.0f).
	 * 		null or zero if cannot be translated (eg. "hello" -> Int -> ???)
	 */
	@SuppressWarnings("unchecked")
	public static <X> X cast(Object obj, Class<X> clz) {
		return (X) castRaw(obj, clz);
	}

	@SuppressWarnings("unchecked")
	public static <X> X cast(Object obj, Class<X> clz, X def) {
		return (X) castOrDefaultObject(obj, clz, def);
	}

	@SuppressWarnings("unchecked")
	public static <X> X castOrThrow(Object obj, Class<X> clz) throws CannotDoIt {
		return (X) translatePrimitiveOrDieRaw(obj, clz);
	}

	public static Object castOrDefaultObject(Object obj, Class<?> c, Object def) {
		if(obj == null) {
			return def;
		}
		if(c.isInstance(obj)) { //Compatible for other classes than primitives
			return obj;
		}
		if(obj instanceof CharSequence) {
			obj = obj.toString();
		} else if(obj instanceof byte[]) {
			obj = new String((byte[]) obj);
		} else if(obj instanceof char[]) {
			obj = new String((char[]) obj);
		}
		if(!(obj instanceof String) && !(obj instanceof Number) && !(obj.getClass().isPrimitive())) {
			return def;
		}
		if(String.class.isAssignableFrom(c)) {
			return obj;
		} else if(!Number.class.isAssignableFrom(c) && !(c.isPrimitive())) {
			return def;
		}
		if(obj instanceof Number) {
			Number num = (Number) obj;
			if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
				return num.intValue() != 0;
			}
			if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
				return num.byteValue();
			}
			if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
				return num.shortValue();
			}
			if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
				return num.intValue();
			}
			if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
				return num.longValue();
			}
			if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
				return num.floatValue();
			}
			if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
				return num.doubleValue();
			}
			if(BigDecimal.class.isAssignableFrom(c)) {
				return BigDecimal.valueOf(num.doubleValue());
			}
			if(BigInteger.class.isAssignableFrom(c)) {
				return BigInteger.valueOf(num.longValue());
			}
			return def;
		}
		String str = obj.toString();
		
		try {
			if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
				return Boolean.parseBoolean(str);
			}
			if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
				return Byte.parseByte(str);
			}
			if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
				return Short.parseShort(str);
			}
			if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
				return Integer.parseInt(str);
			}
			if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
				return Long.parseLong(str);
			}
			if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
				return Float.parseFloat(str);
			}
			if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
				return Double.parseDouble(str);
			}
			if(BigDecimal.class.isAssignableFrom(c)) {
				return new BigDecimal(str);
			}
			if(BigInteger.class.isAssignableFrom(c)) {
				return new BigInteger(str);
			}
		} catch(NumberFormatException e) {}
		return def;
	}

	public static <X> Object castRaw(Object obj, Class<X> c) {
		if(obj == null) {
			return castNull(c);
		}
		if(c.isInstance(obj)) { //Compatible for other classes than primitives
			return obj;
		}
		if(obj instanceof CharSequence) {
			obj = obj.toString();
		} else if(obj instanceof byte[]) {
			obj = new String((byte[]) obj);
		} else if(obj instanceof char[]) {
			obj = new String((char[]) obj);
		}
		if(!(obj instanceof String) && !(obj instanceof Number) && !(obj.getClass().isPrimitive())) {
			return castNull(c);
		}
		if(String.class.isAssignableFrom(c)) {
			return obj;
		} else if(!Number.class.isAssignableFrom(c) && !(c.isPrimitive())) {
			return null;
		}
		if(obj instanceof Number) {
			Number num = (Number) obj;
			if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
				return num.intValue() != 0;
			}
			if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
				return num.byteValue();
			}
			if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
				return num.shortValue();
			}
			if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
				return num.intValue();
			}
			if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
				return num.longValue();
			}
			if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
				return num.floatValue();
			}
			if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
				return num.doubleValue();
			}
			if(BigDecimal.class.isAssignableFrom(c)) {
				return BigDecimal.valueOf(num.doubleValue());
			}
			if(BigInteger.class.isAssignableFrom(c)) {
				return BigInteger.valueOf(num.longValue());
			}
			return null;
		}
		String str = obj.toString();
		
		try {
			if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
				return Boolean.parseBoolean(str);
			}
			if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
				return Byte.parseByte(str);
			}
			if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
				return Short.parseShort(str);
			}
			if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
				return Integer.parseInt(str);
			}
			if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
				return Long.parseLong(str);
			}
			if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
				return Float.parseFloat(str);
			}
			if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
				return Double.parseDouble(str);
			}
			if(BigDecimal.class.isAssignableFrom(c)) {
				return new BigDecimal(str);
			}
			if(BigInteger.class.isAssignableFrom(c)) {
				return new BigInteger(str);
			}
		} catch(NumberFormatException e) {
			return castNull(c);
		}
		return null;
	}

	static <X> Object translatePrimitiveOrDieRaw(Object obj, Class<X> c) throws CannotDoIt {
		if(obj == null) {
			throw cannotDoIt();
		}
		if(c.isInstance(obj)) { //Compatible for other classes than primitives
			return obj;
		}
		if(obj instanceof CharSequence) {
			obj = obj.toString();
		} else if(obj instanceof byte[]) {
			obj = new String((byte[]) obj);
		} else if(obj instanceof char[]) {
			obj = new String((char[]) obj);
		}
		if(!(obj instanceof String) && !(obj instanceof Number) && !(obj.getClass().isPrimitive())) {
			throw cannotDoIt();
		}
		if(String.class.isAssignableFrom(c)) {
			return obj;
		} else if(!Number.class.isAssignableFrom(c) && !(c.isPrimitive())) {
			throw cannotDoIt();
		}
		if(obj instanceof Number) {
			Number num = (Number) obj;
			if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
				return num.intValue() != 0;
			}
			if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
				return num.byteValue();
			}
			if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
				return num.shortValue();
			}
			if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
				return num.intValue();
			}
			if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
				return num.longValue();
			}
			if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
				return num.floatValue();
			}
			if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
				return num.doubleValue();
			}
			if(BigDecimal.class.isAssignableFrom(c)) {
				return BigDecimal.valueOf(num.doubleValue());
			}
			if(BigInteger.class.isAssignableFrom(c)) {
				return BigInteger.valueOf(num.longValue());
			}
			throw cannotDoIt();
		}
		String str = obj.toString();
		
		try {
			if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
				return Boolean.parseBoolean(str);
			}
			if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
				return Byte.parseByte(str);
			}
			if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
				return Short.parseShort(str);
			}
			if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
				return Integer.parseInt(str);
			}
			if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
				return Long.parseLong(str);
			}
			if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
				return Float.parseFloat(str);
			}
			if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
				return Double.parseDouble(str);
			}
			if(BigDecimal.class.isAssignableFrom(c)) {
				return new BigDecimal(str);
			}
			if(BigInteger.class.isAssignableFrom(c)) {
				return new BigInteger(str);
			}
		} catch(NumberFormatException e) {}
		throw cannotDoIt();
	}

	@SuppressWarnings("unchecked")
	public static <X> X castNull(Class<X> c) {
		if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
			return (X) Boolean.FALSE;
		}
		if(c.isPrimitive()) {
			return (X) ((Object) 0);
		}
		if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
			return (X) ((Integer) (int)0);
		}
		if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
			return (X) ((Byte) (byte)0);
		}
		if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
			return (X) ((Short) (short)0);
		}
		if(Character.class.isAssignableFrom(c) || char.class.isAssignableFrom(c)) {
			return (X) ((Character) (char)0);
		}
		if(Long.class.isAssignableFrom(c) || long.class.isAssignableFrom(c)) {
			return (X) ((Long) (long)0);
		}
		if(Float.class.isAssignableFrom(c) || float.class.isAssignableFrom(c)) {
			return (X) ((Float) (float)0);
		}
		if(Double.class.isAssignableFrom(c) || double.class.isAssignableFrom(c)) {
			return (X) ((Double) (double)0);
		}
		if(BigDecimal.class.isAssignableFrom(c)) {
			return (X) BigDecimal.ZERO;
		}
		if(BigInteger.class.isAssignableFrom(c)) {
			return (X) BigInteger.ZERO;
		}
		return null;
	}
	
}
