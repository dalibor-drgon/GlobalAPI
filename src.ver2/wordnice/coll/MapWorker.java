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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import wordnice.api.Nice.CollFactory;
import wordnice.api.Nice.ListFactory;
import wordnice.api.Nice.MapFactory;
import wordnice.coll.CollTranslator.CollEntryChecker;
import wordnice.coll.CollTranslator.CollEntryConverter;
import wordnice.coll.MapTranslator.MapEntryChecker;
import wordnice.coll.MapTranslator.MapEntryConverter;

public interface MapWorker extends Iterable<Entry<String,Object>> {

	/**
	 * Current sufix = Does NOT support multidimensional keys
	 * 
	 * @param key
	 * @return
	 */
	boolean containsKeyCur(String key);

	/**
	 * Current sufix = Does NOT support multidimensional keys
	 * 
	 * @param name Key
	 * @return Object under given key
	 */
	Object getCur(String key);
	
	/**
	 * Current sufix = Does NOT support multidimensional keys
	 * 
	 * @param name Key
	 * @return Object under given key casted to given primitive
	 */
	<X> X getCur(String key, Class<X> clz);
	
	ListFactory getListFactory();
	MapFactory getMapFactory();
	MapWorker setListFactory(ListFactory nev);
	MapWorker setMapFactory(MapFactory nev);
	
	/*************************************************************************
	 * All methods under this comment support multi-dimensional keys with DOTS
	 */
	
	/**
	 * @return true if there is any value under given key
	 */
	boolean containsKey(String key);
	
	/**
	 * @return object value under given key
	 */
	Object get(String key);
	
	/**
	 * @return String/Number/byte[]/char[] casted to String
	 */
	String getString(String key);

	/**
	 * @return String/byte[] casted to byte[]
	 */
	byte[] getBytes(String key);

	/**
	 * @return casted Boolean
	 */
	boolean getBoolean(String key);

	/**
	 * @return casted Byte
	 */
	byte getByte(String key);

	/**
	 * @return casted Short
	 */
	short getShort(String key);

	/**
	 * @return casted Int
	 */
	int getInt(String key);

	/**
	 * @return casted Long
	 */
	long getLong(String key);

	/**
	 * @return casted Float
	 */
	float getFloat(String key);

	/**
	 * @return casted Double
	 */
	double getDouble(String key);

	/**
	 * Get as primitive number/String/BigDecimal/BigInteger/Map<String,Object>/Collection<Object,>
	 * @return Get and translate if possible variable under given key
	 */
	<X> X get(String key, Class<X> clz);

	
	/**
	 * @return If already collection, return.
	 * 		Otherwise try to convert value into List and return
	 */
	Collection<Object> getColl(String path);
	
	/**
	 * @return Get and check if variable under given 
	 * 	name is collection with instances of given class
	 */
	Collection<Object> castColl(String key);
	
	/**
	 * @return Get and check if variable under given 
	 * 	name is collection with instances of given class
	 */
	<X> Collection<X> castColl(String key, Class<X> entryClz, boolean canHaveNull);
	
	/**
	 * @return Get and check if variable under given 
	 * 	name is collection with instances of given class
	 */
	<X> Collection<X> castColl(String key, CollEntryChecker<X> checker);
	
	
	/**
	 * @return Map under given key masked as MapWorker or null
	 */
	MapWorker castMapWorker(String key);
	
	/**
	 * @return Current map
	 */
	Map<String,Object> getMap();
	
	/**
	 * @return Map
	 */
	Map<String, Object> castMap(String path);
	
	/**
	 * @return Map
	 */
	<V> Map<String, V> castMap(String path, Class<V> valClz, boolean valNull);
	
	/**
	 * @return Map
	 */
	<V> Map<String, V> castMap(String path, MapEntryChecker<String, V> checker);
	
	
	
	/**
	 * @return instance if translation was succesful, null otherwise
	 */
	<X> Collection<X> translateList(String path, Class<X> entryClz, boolean canHaveNull);
	
	/**
	 * @return instance if translation was succesful, null otherwise
	 */
	<X> Collection<X> translateList(String path, CollEntryConverter<X> conv);
	
	/**
	 * @return instance if translation was succesful, null otherwise
	 */
	<X> Collection<X> translateSet(String path, Class<X> entryClz, boolean canHaveNull);
	
	/**
	 * @return instance if translation was succesful, null otherwise
	 */
	<X> Collection<X> translateSet(String path, CollEntryConverter<X> conv);
	
	/**
	 * @return instance if translation was succesful, null otherwise
	 */
	<X> Collection<X> translateCollection(String path, CollEntryConverter<X> conv, CollFactory factory);
	
	/**
	 * @return Map
	 */
	<V> Map<String, V> translateMap(String path, Class<V> valClz, boolean valNull);
	
	/**
	 * @return Map
	 */
	<V> Map<String, V> translateMap(String path, MapEntryConverter<String, V> converter);
	
	/**
	 * @return Size of internal map
	 */
	int size();
	
	/**
	 * @param path Path. Has logic to parse multi-dimensional keys with DOTS
	 * 		(eg. "Users.Root.Password"
	 * 			-> getMap("Users").getMap("Root").put("Password", val)
	 * @param val Value to put
	 * @return Old object under this key
	 */
	Object put(String path, Object val);
	
	/**
	 * @param path Key. Cannot be multidimensional-key
	 * @param val Value to put
	 * @return Old object under this key
	 */
	Object putCur(String key, Object val);
	
	/**
	 * For compatibility with URL queries
	 * 
	 * @param path URL (jsonp) path. Has logic to parse multi-dimensional keys with []
	 * 		(eg. "Users[Root][Password]" or eg "[Users][Root][Password]"
	 * 			-> getMap("Users").getMap("Root").put("Password", val);
	 * 		eg. "Users[Flags][]" or "[Users][Flags][]"
	 * 			-> getMap("Users").getColl("Flags").add(val);
	 * @param val Value to put
	 * @return Old object under this key
	 */
	Object putArrayed(String path, Object val);
	
	
	/**
	 * @return Map under given key masked as MapWorker or null
	 */
	MapWorker getMapWorker(String key);
	
	/**
	 * @return Map in any case
	 */
	Map<String, Object> getMap(String path);
	
	
	/**
	 * Support multi-dimensional keys with DOTS
	 */
	Object remove(String path);
	
	void clear();
	void putAll(Map<String, ? extends Object> m);
	void putAll(String path, Map<String, ? extends Object> m);
	
}
