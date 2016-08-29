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

import static wordnice.api.Api.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import gnu.trove.map.hash.THashMap;
import wordnice.api.Api;
import wordnice.api.Api.CannotDoIt;

public class AnythingSerializer {

	public static int serializeExceptionClauseDeepth = 1;
	public static int serializeExceptionStackDeepth = 5;

	public static Map<String, Object> putConfigToMap(ConfigurationSection in) {
		Map<String, Object> out = Api.createMap();
		putConfigToMap(in, out);
		return out;
	}
		
	public static void putConfigToMap(ConfigurationSection in, Map<String, Object> out) {
		Configuration cf = (Configuration) in;
		Iterator<String> keys = cf.getKeys(false).iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object obj = cf.get(key);
			out.put(key, (obj instanceof ConfigurationSection) 
						? putConfigToMap((ConfigurationSection) obj)
						: obj);
		}
	}
	
	public static void serializeException(Map<String,Object> out, Throwable t) {
		serializeException(out, t, null, serializeExceptionClauseDeepth, serializeExceptionStackDeepth);
	}
	
	public static void serializeException(Map<String,Object> out, Throwable t, String message) {
		serializeException(out, t, message, serializeExceptionClauseDeepth, serializeExceptionStackDeepth);
	}
	
	public static void serializeException(Map<String,Object> out, Throwable t, String message, int clause_deepth, int stack_trace_deepth) {
		out.put("Name", t.getClass().getName());
		if(message != null) {
			out.put("MessageCustom", message);
		}
		out.put("Message", t.getMessage());
		Throwable cause = t.getCause();
		if(cause == null) {
			out.put("Cause", null);
		} else if(clause_deepth != 0) {
			Map<String,Object> nevmap = new THashMap<String,Object>(5);
			serializeException(nevmap, cause, null, clause_deepth-1, stack_trace_deepth);
			out.put("Cause", nevmap);
		} else {
			out.put("Cause", cause.getClass().getName());
		}
		if(stack_trace_deepth != 0) {
			StackTraceElement[] st = t.getStackTrace();
			int i = 0;
			int end = (stack_trace_deepth < 0) 
					? st.length : Math.min(st.length, stack_trace_deepth);
			List<Object> nevlist = new ArrayList<Object>(end);
			while(i != end) {
				Map<String,Object> stmap = Api.createMap(4);
				serializeStackTrace(stmap, st[i++]);
				nevlist.add(stmap);
			}
			out.put("StackTrace", nevlist);
		}
	}
	
	
	/**
	 * Translate between String / Number / primitive
	 * 
	 * @return non-null value if can be translated (eg. "1234" -> Float -> 1234.0f).
	 * 		null if cannot be translated (eg. "hello" -> Int -> ???)
	 */
	@SuppressWarnings("unchecked")
	public static <X> X translatePrimitive(Object obj, Class<X> clz) {
		return (X) translatePrimitiveRaw(obj, clz);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X translatePrimitive(Object obj, Class<X> clz, X def) {
		return (X) translatePrimitiveRaw(obj, clz, def);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X translatePrimitiveOrDie(Object obj, Class<X> clz) throws CannotDoIt {
		return (X) translatePrimitiveOrDieRaw(obj, clz);
	}
	
	/**
	 * @see {@link translatePrimitive(Object, Class<? extends Object>)}
	 */
	public static <X> Object translatePrimitiveRaw(Object obj, Class<X> c) {
		if(obj == null) {
			return returnNull(c);
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
			return returnNull(c);
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
			return returnNull(c);
		}
		return null;
	}
	
	public static <X> Object translatePrimitiveRaw(Object obj, Class<X> c, X def) {
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
	
	public static <X> Object translatePrimitiveOrDieRaw(Object obj, Class<X> c) throws CannotDoIt {
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
	public static <X> X returnNull(Class<X> c) {
		if(Boolean.class.isAssignableFrom(c) || boolean.class.isAssignableFrom(c)) {
			return (X) Boolean.FALSE;
		}
		if(Byte.class.isAssignableFrom(c) || byte.class.isAssignableFrom(c)) {
			return (X) ((Byte) (byte)0);
		}
		if(Short.class.isAssignableFrom(c) || short.class.isAssignableFrom(c)) {
			return (X) ((Short) (short)0);
		}
		if(Integer.class.isAssignableFrom(c) || int.class.isAssignableFrom(c)) {
			return (X) ((Integer) (int)0);
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
		if(c.isPrimitive()) {
			return (X) ((Object) 0);
		}
		if(BigDecimal.class.isAssignableFrom(c)) {
			return (X) BigDecimal.ZERO;
		}
		if(BigInteger.class.isAssignableFrom(c)) {
			return (X) BigInteger.ZERO;
		}
		return null;
	}
	
	public static void serializeStackTrace(Map<String,Object> out, StackTraceElement st) {
		out.put("Class", st.getClassName());
		out.put("File", st.getFileName());
		out.put("FileLine", st.getLineNumber());
		out.put("Method", st.getMethodName());
	}
	
}
