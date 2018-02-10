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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import wordnice.api.Nice;
import wordnice.api.Nice.MapFactory;

public class SerializeUtils {

	public static int SerializeExceptionClauseDepth = 2;
	public static int SerializeExceptionStackDepth = 10;

	/**
	 * @see #putConfigToMap(ConfigurationSection, Map, MapFactory)
	 * @return Map created with Nice.getMapFactory() with copied data 
	 * 		from given configuration section
	 */
	public static Map<String, Object> putConfigToMap(ConfigurationSection in) {
		Map<String, Object> out = Nice.createMap();
		putConfigToMap(out, Nice.getMapFactory(), in);
		return out;
	}

	/**
	 * @see #putConfigToMap(ConfigurationSection, Map, MapFactory)
	 * @return Map created with given factory with copied data 
	 * 		from given configuration section
	 */
	public static Map<String, Object> putConfigToMap(ConfigurationSection in, MapFactory fact) {
		Map<String, Object> out = fact.createMap();
		putConfigToMap(out, fact, in);
		return out;
	}

	/**
	 * @see #putConfigToMap(ConfigurationSection, Map, MapFactory)
	 */
	public static void putConfigToMap(Map<String, Object> out, ConfigurationSection in) {
		putConfigToMap(out, Nice.getMapFactory(), in);
	}

	/**
	 * Translate configuration section to map using given output and map factory
	 * If MapFactory is null, default one from Nice.getMapFactory() will be used
	 * @param out Output map. Cannot be null
	 * @param fact Factory for new maps. If null, then = Nice.getMapFactory()
	 * @param in Configuration section. Cannot be null
	 * @throws IllegalArgumentException If configurationSection or output map is null
	 */
	public static void putConfigToMap(Map<String, Object> out, MapFactory fact, ConfigurationSection in) {
		if(in == null) throw new IllegalArgumentException("ConfigurationSection == null!");
		if(out == null) throw new IllegalArgumentException("Output map == null!");
		if(fact == null) fact = Nice.getMapFactory();
		Configuration cf = (Configuration) in;
		Iterator<String> keys = cf.getKeys(false).iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object obj = cf.get(key);
			out.put(key, (obj instanceof ConfigurationSection) 
						? putConfigToMap((ConfigurationSection) obj, fact)
						: obj);
		}
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static Map<String,Object> serializeException(Throwable t) {
		return serializeException(null, null, t, null, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static Map<String,Object> serializeException(Throwable t, String message) {
		return serializeException(null, null, t, message, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static Map<String,Object> serializeException(MapFactory fact, Throwable t) {
		return serializeException(null, fact, t, null, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static Map<String,Object> serializeException(MapFactory fact,Throwable t, String message) {
		return serializeException(null, fact, t, message, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static void serializeException(Map<String,Object> out, Throwable t) {
		serializeException(out, null, t, null, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static void serializeException(Map<String,Object> out, Throwable t, String message) {
		serializeException(out, null, t, message, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static void serializeException(Map<String,Object> out, MapFactory fact, Throwable t) {
		serializeException(out, fact, t, null, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * @see #serializeException(Map, MapFactory, Throwable, String, int, int)
	 */
	public static void serializeException(Map<String,Object> out, MapFactory fact, Throwable t, String message) {
		serializeException(out, fact, t, message, SerializeExceptionClauseDepth, SerializeExceptionStackDepth);
	}

	/**
	 * Serialize given Throwable into map
	 * @param out Output map where throwable will be serialized. 
	 * 		If null, fact.createMap() is called
	 * @param fact Factory for new maps. If null, then = Nice.getMapFactory()
	 * @param t Throwable to serialize. Cannot be null
	 * @param message Info message. If null, it is ignored
	 * @param clause_depth How many clauses to serialize? 
	 * 		(-1 or any big number for practically unlimited)
	 * @param stack_trace_depth Maximum stack traces to serialize 
	 * 		(for this throwable and for clauses)
	 * @return Map where throwable was serialized 
	 * 		(first argument / newly allocated map if argument was null)
	 * @throws IllegalArgumentException if throwable == null
	 */
	public static Map<String,Object> serializeException(Map<String,Object> out, MapFactory fact, 
			Throwable t, String message, int clause_depth, int stack_trace_depth) {
		if(fact == null) fact = Nice.getMapFactory();
		if(t == null) throw new IllegalArgumentException ("Throwable == null!");
		if(message != null) {
			if(out == null) out = fact.createMap(5);
			out.put("CustomMessage", message);
		} else if(out == null) out = fact.createMap(4);
		out.put("Message", t.getMessage());
		out.put("Name", t.getClass().getName());
		Throwable cause = t.getCause();
		if(cause == null) {
			//out.put("Cause", null);
		} else if(clause_depth != 0) {
			out.put("Cause", serializeException(null, fact, cause, null, clause_depth-1, stack_trace_depth));
		} else {
			out.put("Cause", cause.getClass().getName());
		}
		if(stack_trace_depth != 0) {
			StackTraceElement[] st = t.getStackTrace();
			int i = 0;
			int end = (stack_trace_depth < 0) 
					? st.length : Math.min(st.length, stack_trace_depth);
			List<Object> nevlist = Nice.createList(end);
			while(i != end) {
				nevlist.add(serializeStackTrace(fact.<String,Object>createMap(4), null, st[i++]));
			}
			out.put("StackTrace", nevlist);
		}
		return out;
	}

	/**
	 * @see #serializeStackTrace(Map, MapFactory, StackTraceElement)
	 * @return "out" argument if not null or newly created map
	 * @return Newly created map with serialized StackTraceElement
	 */
	public static Map<String,Object> serializeStackTrace(StackTraceElement st) {
		return serializeStackTrace(Nice.<String,Object>createMap(), null, st);
	}

	/**
	 * @see #serializeStackTrace(Map, MapFactory, StackTraceElement)
	 * @return Newly created map with serialized StackTraceElement
	 */
	public static Map<String,Object> serializeStackTrace(MapFactory fact, StackTraceElement st) {
		return serializeStackTrace(null, fact, st);
	}

	/**
	 * @see #serializeStackTrace(Map, MapFactory, StackTraceElement)
	 * @return "out" argument if not null or newly created map with serialized StackTraceElement
	 */
	public static Map<String,Object> serializeStackTrace(Map<String,Object> out, StackTraceElement st) {
		return serializeStackTrace(out, null, st);
	}

	/**
	 * Serialize StackTraceElement into map
	 * @param out Output map. If null, it is created using given map factory
	 * @param mf Map factory. If null, default Nice.getMapFactory() is used
	 * @param st StackTraceElement to serialize. Cannot be null
	 * @return Map where stack trace was serialized ("out" argument if not null)
	 * @throws IllegalArgumentException When StackTraceElement == null
	 */
	public static Map<String,Object> serializeStackTrace(Map<String,Object> out, MapFactory mf, StackTraceElement st)
			throws IllegalArgumentException{
		if(st == null) throw new IllegalArgumentException("StackTraceElement == null!");
		if(out == null) {
			if(mf == null) mf = Nice.getMapFactory();
			out = mf.createMap(4);
		}
		out.put("Class", st.getClassName());
		out.put("File", st.getFileName());
		out.put("Line", st.getLineNumber());
		out.put("Method", st.getMethodName());
		return out;
	}
	
}
