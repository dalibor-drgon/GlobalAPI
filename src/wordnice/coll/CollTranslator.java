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

package wordnice.coll;

import static wordnice.api.Nice.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import wordnice.utils.NiceConverter;

public class CollTranslator {
	
	public static interface CollEntryChecker<X> {
		/**
		 * Check value type
		 */
		SkippableBoolean checkValue(Object value);
	}
	
	public static interface CollEntryConverter<X> extends CollEntryChecker<X> {
		X convertValue(Object value) throws CannotDoIt, SkipIt;
	}
	
	public static CollProperties<Object> createObjectCollProperties() {
		return new CollProperties<Object>(Object.class);
	}
	
	public static CollProperties<Object> createObjectCollProperties(boolean allowNulls) {
		return new CollProperties<Object>(Object.class, allowNulls);
	}
	
	public static <X> CollProperties<X> createCollProperties(Class<X> clz) {
		return new CollProperties<X>(clz);
	}
	
	public static <X> CollProperties<X> createCollProperties(Class<X> clz, boolean allowNulls) {
		return new CollProperties<X>(clz, allowNulls);
	}
	
	
	/**
	 * Easy converting
	 * Supported classes are: Strings/primitives/BigDecimals/BigIntegers
	 */
	public static class CollProperties<X> implements CollEntryConverter<X>, CollEntryChecker<X> {
		/**
		 * Parent class of filtered classes
		 */
		protected Class<X> valueClass;
		
		/**
		 * Can be value null?
		 * NOTE: /skipNullValues/ variable is checked before this one
		 */
		protected boolean allowNullValues = false; 
		
		/**
		 * Should we skip null values?
		 * NOTE: This variable is checked before /valNull/ variable
		 */
		protected boolean skipNullValues = true; //skip null values (checked before valNull)
		
		/**
		 * Skip rather than fail
		 */
		protected boolean skipRatherThanFail = false;
		
		
		public CollProperties(Class<X> valueClass) {
			this.valueClass = valueClass;
		}
		
		public CollProperties(Class<X> valueClass, boolean allowNullValues) {
			this.valueClass = valueClass;
			this.setAllowNullValues(allowNullValues);
		}
		
		public CollProperties<X> setAllowNullValues(boolean allowNullValues) {
			this.allowNullValues = allowNullValues;
			if(this.allowNullValues) {
				this.skipNullValues = false;
			}
			return this;
		}
		
		public CollProperties<X> setSkipNullValues(boolean skipNullValues) {
			this.skipNullValues = skipNullValues;
			return this;
		}
		
		public CollProperties<X> setSkipRatherThanFail(boolean skipRatherThanFail) {
			this.skipRatherThanFail = skipRatherThanFail;
			return this;
		}
		
		public boolean getAllowNullValues() {
			return this.allowNullValues;
		}
		
		public Class<X> getValueClass() {
			return valueClass;
		}

		public boolean getSkipNullValues() {
			return skipNullValues;
		}

		public boolean getSkipRatherThanFail() {
			return skipRatherThanFail;
		}

		@Override
		public SkippableBoolean checkValue(Object value) {
			if(value == null) {
				if(skipNullValues) {
					return SkippableBoolean.SKIP;
				}
				if(!allowNullValues) {
					return (skipRatherThanFail) 
							? SkippableBoolean.SKIP : SkippableBoolean.FAIL;
				}
				return SkippableBoolean.ALLOW;
			}
			return valueClass.isInstance(value) 
					? SkippableBoolean.ALLOW : ((skipRatherThanFail)
							? SkippableBoolean.SKIP : SkippableBoolean.FAIL);
		}

		@Override
		public X convertValue(Object value) throws CannotDoIt, SkipIt {
			if(value == null) {
				if(skipNullValues) {
					throw skipIt();
				}
				if(!allowNullValues) {
					throw (skipRatherThanFail) 
							? skipIt() : cannotDoIt();
				}
				return null;
			}
			if(skipRatherThanFail) {
				try {
					return NiceConverter.translatePrimitiveOrDie(value, valueClass);
				} catch(CannotDoIt e) {
					throw skipIt();
				}
			}
			return NiceConverter.translatePrimitiveOrDie(value, valueClass);
		}

	}

	
	@SuppressWarnings("unchecked")
	public static <X> Collection<X> safeCastCollection(Object in, CollEntryChecker<X> ch) {
		if(!(in instanceof Collection)) {
			return null;
		}
		Collection<Object> check = (Collection<Object>) in;
		if(checkElements(check.iterator(), ch)) {
			return (Collection<X>) check;
		}
		return null;
	}
	
	public static boolean checkElements(
			Iterator<? extends Object> it, CollEntryChecker<?> conv) {
		if(it == null || conv == null) {
			return false;
		}
		while(it.hasNext()) {
			if(!conv.checkValue(it.next()).booleanValue()) {
				return false;
			}
		}
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <X> boolean translateCollectionTry(Object in, 
			Collection<X> out, CollEntryConverter<X> lec) {
		if(in == null) {
			return false;
		}
		Iterator<? extends Object> it = null;
		if(in instanceof Iterable) {
			it = ((Iterable<? extends Object>) in).iterator();
		} else if(in instanceof Iterator) {
			it = (Iterator<? extends Object>) in;
		} else {
			try {
				out.add(lec.convertValue(in));
				return true;
			} catch(CannotDoIt cdt){
				return false;
			} catch(SkipIt n) {
				return true;
			}
		}
		
		return translateCollection(it, out, lec);
	}
	
	/**
	 * Copy iterator values to given collection and translate them into given class
	 * 
	 * @param it InputStream collection iterator (call Collection/List/Set.iterator())
	 * @param OutputStream OutputStream collection where will be stored new translated values
	 * @param entry_clz Class into which values should be translated
	 * @param haveNull Can we process nulls?
	 * 
	 * @return true if we could translate all values, otherwise false
	 */
	public static <X> boolean translateCollection(Iterator<? extends Object> it, 
			Collection<X> out_, CollEntryConverter<X> lec) {
		if(it == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Collection<Object> out = (Collection<Object>) out_;
		while(it.hasNext()) {
			try {
				out.add(lec.convertValue(it.next()));
			} catch(CannotDoIt n) {
				return false;
			} catch(SkipIt n) {
				continue;
			}
		}
		return true;
	}
	
	public static <X> Collection<X> translateOrCreateCollection(
			Map<String,Object> map, String key, CollEntryConverter<X> conv,
			CollFactory factory, boolean put) {
		return translateOrCreateCollection(map, key, map.get(key), conv, factory, put);
	}
	
	@SuppressWarnings("unchecked")
	public static <X> Collection<X> translateOrCreateCollection(
			Map<String,Object> map, String key, Object mapValue, 
			CollEntryConverter<X> conv, CollFactory factory, boolean put) {
		Collection<X> col = null;
		if(mapValue != null) {
			if(mapValue instanceof Collection) {
				if(CollTranslator.checkElements(
						((Collection<X>) mapValue).iterator(), conv)) {
					return (Collection<X>) mapValue;
				}
			}
			col = factory.createColl();
			if(mapValue instanceof Map) {
				Collection<X> vals = ((Map<String,X>) mapValue).values();
				if(CollTranslator.checkElements(vals.iterator(), conv)) {
					return factory.createColl(vals);
				}
			}
			if(!CollTranslator.translateCollectionTry(
					mapValue, col, conv)) {
				col.clear();
			}
		}
		if(col == null) {
			col = factory.createColl();
		}
		if(put) map.put(key, col);
		return col;
	}
	
}
