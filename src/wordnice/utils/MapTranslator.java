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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import wordnice.utils.CollTranslator.CollEntryChecker;
import wordnice.utils.CollTranslator.CollEntryConverter;
import wordnice.utils.CollTranslator.CollProperties;

public class MapTranslator {

	public static interface MapEntryChecker<X,Y>
	extends CollEntryChecker<Y> {
		SkippableBoolean checkEntry(Object key, Object value);
	}
	
	public static interface MapEntryConverter<X,Y> 
	extends CollEntryConverter<Y>, MapEntryChecker<X,Y> {
		X convertKey(Object key) throws CannotDoIt, SkipIt;
	}
	
	public static MapProperties<Object, Object> createObjectMapProperties() {
		return new MapProperties<Object, Object>(Object.class,Object.class);
	}
	
	public static MapProperties<Object, Object> createObjectMapProperties(
			boolean allowNullKeys, boolean allowNullsValues) {
		return new MapProperties<Object, Object>(
				Object.class, allowNullKeys, Object.class, allowNullsValues);
	}
	
	public static MapProperties<Object, Object> createObjectMapProperties(
			boolean allowNullsValues) {
		return new MapProperties<Object, Object>(
				Object.class, Object.class, allowNullsValues);
	}
	
	public static MapProperties<String,Object> createStringObjectMapProperties() {
		return new MapProperties<String,Object>(String.class,Object.class);
	}
	
	public static MapProperties<String,Object> createStringObjectMapProperties(
			boolean allowNullsValues) {
		return new MapProperties<String,Object>(
				String.class,Object.class, allowNullsValues);
	}
	
	public static MapProperties<String,Object> createStringObjectMapProperties(
			boolean allowNullKeys, boolean allowNullsValues) {
		return new MapProperties<String,Object>(
				String.class, allowNullKeys, Object.class, allowNullsValues);
	}
	
	public static <X,Y> MapProperties<X,Y> createMapProperties(
			Class<X> keyClz, Class<Y> valClz) {
		return new MapProperties<X,Y>(keyClz, valClz);
	}
	
	public static <X,Y> MapProperties<X,Y> createMapProperties(
			Class<X> keyClz, Class<Y> valClz, boolean allowNullsValues) {
		return new MapProperties<X,Y>(keyClz, valClz, allowNullsValues);
	}
	
	public static <X,Y> MapProperties<X,Y> createMapProperties(
			Class<X> keyClz, boolean allowNullKeys,
			Class<Y> valClz, boolean allowNullsValues) {
		return new MapProperties<X,Y>(keyClz, allowNullKeys, valClz, allowNullsValues);
	}
	
	/**
	 * Easy converting
	 * Supported classes are: Strings/primitives/BigDecimals/BigIntegers
	 */
	public static class MapProperties<X,Y> extends CollProperties<Y>
	implements MapEntryConverter<X,Y> {
		/**
		 * Parent class of filtered classes
		 */
		protected Class<X> keyClass;
		
		/**
		 * Can be key null?
		 * NOTE: /skipNullKeys/ variable is checked before this one
		 */
		protected boolean allowNullKeys = true;
		
		/**
		 * Should we skip null keys?
		 * NOTE: This value is checked before /allowNullKeys/ variable
		 */
		protected boolean skipNullKeys = false; //skip null values (checked before valNull)
		
		
		public MapProperties(Class<X> keyClass, Class<Y> valueClass) {
			super(valueClass);
			this.keyClass = keyClass;
		}
		
		public MapProperties(Class<X> keyClass, Class<Y> valueClass, 
				boolean allowNullValues) {
			super(valueClass, allowNullValues);
			this.keyClass = keyClass;
		}
		
		public MapProperties(Class<X> keyClass, boolean allowNullKeys ,
				Class<Y> valueClass, boolean allowNullValues) {
			super(valueClass, allowNullValues);
			this.keyClass = keyClass;
			this.setAllowNullKeys(allowNullKeys);
		}
		
		@Override
		public MapProperties<X,Y> setAllowNullValues(boolean allowNullValues) {
			super.setAllowNullValues(allowNullValues);
			return this;
		}
		
		@Override
		public MapProperties<X,Y> setSkipNullValues(boolean skipNullValues) {
			this.skipNullValues = skipNullValues;
			return this;
		}
		
		@Override
		public MapProperties<X,Y> setSkipRatherThanFail(boolean skipRatherThanFail) {
			this.skipRatherThanFail = skipRatherThanFail;
			return this;
		}
		
		public MapProperties<X,Y> setAllowNullKeys(boolean allowNullKeys) {
			this.allowNullKeys = allowNullKeys;
			if(allowNullKeys) {
				this.skipNullKeys = false;
			}
			return this;
		}
		
		public MapProperties<X,Y> setSkipNullKeys(boolean skipNullKeys) {
			this.skipNullKeys = skipNullKeys;
			return this;
		}
		
		public boolean getAllowNullKeys() {
			return this.allowNullKeys;
		}
		
		public Class<X> getKeyClass() {
			return keyClass;
		}

		public boolean getSkipNullKeys() {
			return skipNullKeys;
		}

		@Override
		public SkippableBoolean checkEntry(Object key, Object value) {
			if(key == null) {
				if(skipNullKeys) {
					return SkippableBoolean.SKIP;
				}
				if(!allowNullKeys) {
					return (skipRatherThanFail) 
							? SkippableBoolean.SKIP : SkippableBoolean.FAIL;
				}
				return checkValue(value);
			}
			return keyClass.isInstance(key) 
					? checkValue(value) : ((skipRatherThanFail)
							? SkippableBoolean.SKIP : SkippableBoolean.FAIL);
		}

		@Override
		public X convertKey(Object key) throws CannotDoIt, SkipIt {
			if(key == null) {
				if(skipNullKeys) {
					throw skipIt();
				}
				if(!allowNullKeys) {
					throw (skipRatherThanFail) 
							? skipIt() : cannotDoIt();
				}
				return null;
			}
			if(skipRatherThanFail) {
				try {
					return AnythingSerializer.translatePrimitiveOrDie(key, keyClass);
				} catch(CannotDoIt e) {
					throw skipIt();
				}
			}
			return AnythingSerializer.translatePrimitiveOrDie(key, keyClass);
		}

	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> Map<X,Y> safeCastMap(Object in,
			MapEntryChecker<X,Y> mech) {
		if(!(in instanceof Map)) {
			return null;
		}
		Map<Object,Object> check = (Map<Object,Object>) in;
		if(checkElements(check.entrySet().iterator(), mech)) {
			return (Map<X,Y>) check;
		}
		return null;
	}
	
	public static <X,Y> boolean checkElements(
			Iterator<?> entry_it, 
			MapEntryChecker<X,Y> mec) {
		if(entry_it == null || mec == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Iterator<Entry<Object,Object>> it = (Iterator<Entry<Object,Object>>) entry_it;
		while(it.hasNext()) {
			Entry<Object,Object> next = it.next();
			Object key = next.getKey();
			Object val = next.getValue();
			if(!mec.checkEntry(key, val).booleanValue()) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static <X,Y> boolean translateMapTry(Object in, 
			final Map<X,Y> out,
			final MapEntryConverter<X,Y> conc) {
		if(in instanceof Map) {
			return translateMap(((Map<Object,Object>) in).entrySet().iterator(), 
					out, conc);
		}
		
		Iterator<Object> it = null;
		if(in instanceof Iterable) {
			it = ((Iterable<Object>) in).iterator();
		} else if(in instanceof Iterator) {
			it = (Iterator<Object>) in;
		} else {
			try {
				out.put(conc.convertKey(0), conc.convertValue(in));
				return true;
			} catch(CannotDoIt cdt){
				return false;
			} catch(SkipIt n) {
				return true;
			}
		}
		
		if(it != null) {
			return CollTranslator.translateCollection(it, new AbstractCollection<Y>() {
				@Override
				public boolean add(Y obj) throws CannotDoIt {
					out.put(conc.convertKey(out.size()), obj);
					return true;
				}
				
				@Override
				public Iterator<Y> iterator() {
					return null;
				}

				@Override
				public int size() {
					return 0;
				}
			}, conc);
		}
		return false;
	}
	
	public static <X,Y> boolean translateMap(
			Iterator<?> entry_it,
			Map<X,Y> out,
			MapEntryConverter<X,Y> conc) {
		if(entry_it == null || conc == null) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Iterator<Entry<Object,Object>> it = (Iterator<Entry<Object,Object>>) entry_it;
		while(it.hasNext()) {
			Entry<? extends Object,? extends Object> next = it.next();
			Object key = next.getKey();
			Object val = next.getValue();
			try {
				out.put(conc.convertKey(key), conc.convertValue(val));
			} catch(CannotDoIt n) {
				return false;
			} catch(SkipIt n) {
				continue;
			}
		}
		return true;
	}
	
	/**
	 * Add continuesly from given starting index
	 * This method does not check for keys, so it can
	 * overwrite some values!
	 * 
	 * Returns last used index if we added something to map,
	 * otherwise returns /i/-1
	 */
	@SuppressWarnings("unchecked")
	public static int addContinuesly(Map<String,? extends Object> ret,
			Iterator<? extends Object> it, int i) {
		while(it.hasNext()) {
			((Map<String,Object>)ret).put(""+i++, it.next());
		}
		return i-1;
	}
	
	/**
	 * Do not overwrite existing values! Find empty keys
	 * 
	 * @return Last used index to put or /i/-1 if nothing was added to map
	 */
	public static int add(Map<String,? extends Object> ret,
			Iterator<? extends Object> it, int i) {
		i--;
		while(it.hasNext()) {
			i = add(ret, it.next(), i+1);
		}
		return i;
	}
	
	/**
	 * Find empty index, put to map with key of empty index and return index
	 */
	@SuppressWarnings("unchecked")
	public static int add(Map<String, ? extends Object> map, Object obj, int i) {
		while(map.containsKey(""+i)) {
			i++;
		}
		((Map<String,Object>)map).put(""+i, obj);
		return i;
	}
	
	/**
	 * Find empty index
	 */
	public static int findIndex(Map<String, ? extends Object> map) {
		int i = 0;
		while(map.containsKey(""+i)) {
			i++;
		}
		return i;
	}
	
	/**
	 * Find empty index
	 */
	public static int findIndex(Map<String, ? extends Object> map, int i) {
		while(map.containsKey(""+i)) {
			i++;
		}
		return i;
	}
	
}
