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

import static wordnice.api.Api.badFormat;
import static wordnice.api.Api.cannotDoIt;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import wordnice.api.Api;
import wordnice.api.Api.BadFormat;
import wordnice.api.Api.CannotDoIt;
import wordnice.api.Api.CollFactory;
import wordnice.api.Api.DataEntry;
import wordnice.api.Api.MapFactory;
import wordnice.api.Api.Value;
import wordnice.codings.ASCII;
import wordnice.utils.ArgsDecoder.CollHandler;
import wordnice.utils.CollTranslator.CollEntryChecker;
import wordnice.utils.CollTranslator.CollEntryConverter;
import wordnice.utils.MapTranslator.MapEntryChecker;
import wordnice.utils.MapTranslator.MapEntryConverter;

public abstract class AbstractMapWorker 
implements MapWorker {

	
	@Override
	public abstract Map<String, Object> getMap();
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return getMap().entrySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#size()
	 */
	@Override
	public int size() {
		return getMap().size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#containsKeyCur(java.lang.String)
	 */
	@Override
	public boolean containsKeyCur(String key) {
		if(key == null || key.indexOf('.') == -1) {
			return getMap().containsKey(key);
		}
		throw Api.illegal("Path '" + key + "' cannot be multidimensional!");
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getCur(java.lang.String)
	 */
	@Override
	public Object getCur(String key) {
		if(key == null || key.indexOf('.') == -1) {
			return getMap().get(key);
		}
		throw Api.illegal("Path '" + key + "' cannot be multidimensional!");
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getCur(java.lang.String, java.lang.Class)
	 */
	@Override
	public <X> X getCur(String key, Class<X> clz) {
		return AnythingSerializer.translatePrimitive(getCur(key), clz);
	}
	
	/**
	 * @return /true/ when found and fill /res/ parameter, /false/ when failed
	 */
	protected boolean findPath(String path, DataEntry<Map<String,Object>, String> res) {
		return findPath(this.getMap(), path, res, false, this.getMapFactory());
	}
	
	protected static boolean isNumber(CharSequence in) {
		if(in.length() > 10) {
			return false;
		}
		int i = 0;
		int l = in.length();
		for(;i < l; i++) {
			char c = in.charAt(i);
			if(c > 255 || !ASCII.isDigit(c)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(String path) {
		DataEntry<Map<String,Object>, String> data = new DataEntry<>();
		if(!findPath(path, data)) {
			return false;
		}
		return data.getKey().containsKey(data.getValue());
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getObject(java.lang.String)
	 */
	@Override
	public Object get(String path) {
		DataEntry<Map<String,Object>, String> data = new DataEntry<>();
		if(!findPath(path, data)) {
			return null;
		}
		return data.getKey().get(data.getValue());
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getString(java.lang.String)
	 */
	@Override
	public String getString(String name) {
		Object obj = this.get(name);
		if(obj instanceof CharSequence || obj instanceof Number) {
			return obj.toString();
		}
		if(obj instanceof byte[]) {
			return new String((byte[]) obj);
		}
		if(obj instanceof char[]) {
			return new String((char[]) obj);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes(String name) {
		Object obj = this.get(name);
		if(obj instanceof CharSequence || obj instanceof Number) {
			return obj.toString().getBytes();
		}
		if(obj instanceof byte[]) {
			return (byte[]) obj;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String name) {
		return get(name, boolean.class);
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(String name) {
		return get(name, byte.class);
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String name) {
		return get(name, short.class);
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String name) {
		return get(name, int.class);
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String name) {
		return get(name, long.class);
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String name) {
		return get(name, float.class);
	}

	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String name) {
		return get(name, double.class);
	}
	
	/* (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapGetter#getValueAsPrimitive(java.lang.String, java.lang.Class)
	 */
	@Override
	public <X> X get(String name, Class<X> clz) {
		return AnythingSerializer.translatePrimitive(this.get(name), clz);
	}


	@Override
	public Collection<Object> castColl(String key) {
		return this.castColl(key, CollTranslator.createObjectCollProperties());
	}


	@Override
	public <X> Collection<X> castColl(String key, Class<X> entryClz, boolean canHaveNull) {
		if(entryClz == null) {
			throw Api.illegal("Null Entry class!");
		}
		return this.castColl(key, 
				CollTranslator.createCollProperties(entryClz, canHaveNull));
	}


	@Override
	public <X> Collection<X> castColl(String key, CollEntryChecker<X> checker) {
		if(checker == null) {
			throw Api.illegal("Null checker!");
		}
		return CollTranslator.safeCastCollection(this.get(key), checker);
	}


	@Override
	public Map<String, Object> castMap(String key) {
		return this.castMap(key, MapTranslator.createStringObjectMapProperties());
	}


	@Override
	public <V> Map<String, V> castMap(String path, Class<V> valClz, boolean valNull) {
		if(valClz == null) {
			throw Api.illegal("Null value class!");
		}
		return this.castMap(path, 
				MapTranslator.createMapProperties(
						String.class, true, valClz, valNull));
	}


	@Override
	public <V> Map<String, V> castMap(String path, MapEntryChecker<String, V> checker) {
		if(checker == null) {
			throw Api.illegal("Null checker!");
		}
		return MapTranslator.safeCastMap(this.get(path), checker);
	}
	
	@Override
	public <V> Map<String, V> translateMap(String path, Class<V> valClz, boolean valNull) {
		if(valClz == null) {
			throw Api.illegal("Null value class!");
		}
		return translateMap(path, MapTranslator.createMapProperties(
				String.class, true, valClz, valNull));
	}

	@Override
	public <V> Map<String, V> translateMap(String path, MapEntryConverter<String, V> converter) {
		if(converter == null) {
			throw Api.illegal("Null converter!");
		}
		return null;
	}
	
	@Override
	public <X> Collection<X> translateList(String path, Class<X> entryClz, boolean canHaveNull) {
		if(entryClz == null) {
			throw Api.illegal("Null entry class!");
		}
		return translateList(path, CollTranslator.createCollProperties(entryClz, canHaveNull));
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getList(java.lang.String, eu.wordnice.api.utils.CollTranslator.CollEntryConverter)
	 */
	@Override
	public <X> Collection<X> translateList(String path, CollEntryConverter<X> conv) {
		return translateCollection(path, conv, Api.getListFactory());
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getSet(java.lang.String, java.lang.Class, boolean)
	 */
	@Override
	public <X> Collection<X> translateSet(String path, Class<X> entryClz, boolean canHaveNull) {
		if(entryClz == null) {
			throw Api.illegal("Null entry class!");
		}
		return translateSet(path, CollTranslator.createCollProperties(entryClz, canHaveNull));
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getSet(java.lang.String, eu.wordnice.api.utils.CollTranslator.CollEntryConverter)
	 */
	@Override
	public <X> Collection<X> translateSet(String path, CollEntryConverter<X> conv) {
		return translateCollection(path, conv, Api.getSetFactory());
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getCollection(java.lang.String, eu.wordnice.api.utils.CollTranslator.CollEntryConverter, eu.wordnice.api.Api.CollFactory)
	 */
	@Override
	public <X> Collection<X> translateCollection(String path, 
			CollEntryConverter<X> conv, CollFactory factory) {
		if(conv == null) {
			throw Api.illegal("Null converter!");
		}
		if(factory == null) {
			factory = Api.getListFactory();
		}
		DataEntry<Map<String,Object>, String> data = new DataEntry<>();
		Object mapValue = null;
		if(findPath(this.getMap(), path, data, true, this.getMapFactory())) {
			mapValue = data.getKey().get(data.getValue());
		}
		return CollTranslator.translateOrCreateCollection(
				data.getKey(), data.getValue(), mapValue, conv, factory, false);
	}
	
	@Override
	public MapWorker castMapWorker(String key) {
		Map<String,Object> map = castMap(key);
		return (map == null) ? null : Api.createMapWorkerForMap(map);
	}
	
	@Override
	public Object put(String path, Object val) {
		DataEntry<Map<String,Object>, String> data = new DataEntry<>();
		findPath(this.getMap(), path, data, true, this.getMapFactory());
		return data.getKey().put(data.getValue(), val);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#putCur(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object putCur(String key, Object val) {
		if(key == null || key.indexOf('.') == -1) {
			return getMap().put(key, val);
		}
		throw Api.illegal("Path '" + key + "' cannot be multidimensional!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#putArrayed(java.lang.String, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object putArrayed(String path, Object val) {
		DataEntry<Object, String> data = new DataEntry<>();
		findPathArray(this.getMap(), path, data, true, this.getMapFactory(), this.getListFactory());
		Object colmap = data.getKey();
		path = data.getValue();
		if(colmap instanceof List) {
			List<Object> list = (List<Object>) colmap;
			if(path.isEmpty()) {
				list.add(val);
			} else {
				int index = Integer.parseInt(path);
				if(index >= this.size()) {
					list.add(val); //failsafe
				} else {
					list.set(index, val);
				}
			}
		} else { //Map
			Map<String,Object> map = (Map<String,Object>) colmap;
			if(path.isEmpty()) {
				MapTranslator.add(map, val, 0);
				return null;
			} else {
				map.put(path, val);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getList(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Collection<Object> getColl(String path) {
		Object obj = this.get(path);
		if(obj instanceof Collection) {
			return (Collection<Object>) obj;
		}
		return null;
	}


	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getMapWorker(java.lang.String)
	 */
	@Override
	public MapWorker getMapWorker(String key) {
		return Api.createMapWorkerForMap(this.getMap(key));
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#getMap(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getMap(String path) {
		Object obj = this.get(path);
		if(obj instanceof Map) {
			return (Map<String, Object>) obj;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#remove(java.lang.String)
	 */
	@Override
	public Object remove(String path) {
		DataEntry<Map<String,Object>, String> data = new DataEntry<>();
		if(!findPath(path, data)) {
			return false;
		}
		return data.getKey().remove(data.getValue());
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#clear()
	 */
	@Override
	public void clear() {
		getMap().clear();
	}

	/*
	 * (non-Javadoc)
	 * @see eu.wordnice.api.utils.MapWorker#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<String, ? extends Object> m) {
		getMap().putAll(m);
	}

	@Override
	public void putAll(String path, Map<String, ? extends Object> m) {
		getMap(path).putAll(m);
	}
	

	@SuppressWarnings("unchecked")
	protected static Object getOrCreateCollOrMap(
			Map<String,? extends Object> map, String key, CollFactory fact, 
			Value<Boolean> retBool, boolean create)
					throws CannotDoIt {
		Object obj = map.get(key);
		if(obj instanceof Collection) {
			return (Collection<Object>) obj;
		}
		if(obj instanceof Map) {
			return (Map<String,Object>) obj;
		}
		retBool.setValue(false);
		if(!create) {
			throw cannotDoIt();
		}
		Collection<?> ret = fact.createColl();
		((Map<String, Object>) map).put(key, ret);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected static Map<String, Object> getOrCreateMap(
			Map<String,? extends Object> map, String key, MapFactory fact, 
			Value<Boolean> retBool, boolean create)
					throws CannotDoIt {
		Object obj = map.get(key);
		if(obj instanceof Map) {
			return (Map<String,Object>) obj;
		}
		retBool.setValue(false);
		if(!create) {
			throw cannotDoIt();
		}
		Map<String, Object> ret = fact.createMap();
		if(obj instanceof Iterable) {
			Iterator<Object> it = ((Iterable<Object>) obj).iterator();
			while(it.hasNext()) {
				ret.put(""+ret.size(), it.next());
			}
		}
		((Map<String, Object>) map).put(key, ret);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected static Object getOrCreateCollOrMap(
			List<? extends Object> list, int key, CollFactory fact,
			Value<Boolean> retBool, boolean create)
					throws CannotDoIt {
		if(list.size() > key) {
			Object obj = list.get(key);
			if(obj instanceof Collection) {
				return (Collection<Object>) obj;
			}
			if(obj instanceof Map) {
				return (Map<String,Object>) obj;
			}
		}
		retBool.setValue(false);
		if(!create) {
			throw cannotDoIt();
		}
		if(key >= list.size()) {
			key = list.size();
		}
		Collection<?> ret = fact.createColl();
		((List<Object>) list).add(key, ret);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected static Map<String, Object> getOrCreateMap(
			List<? extends Object> list, int key, MapFactory fact, 
			Value<Boolean> retBool, boolean create)
				throws CannotDoIt {
		Object obj = null;
		if(list.size() > key) {
			obj = list.get(key);
			if(obj instanceof Map) {
				return (Map<String,Object>) obj;
			}
		}
		retBool.setValue(false);
		if(!create) {
			throw cannotDoIt();
		}
		Map<String, Object> ret = fact.createMap();
		if(obj instanceof Iterable) {
			Iterator<Object> it = ((Iterable<Object>) obj).iterator();
			while(it.hasNext()) {
				ret.put(""+ret.size(), it.next());
			}
		}
		if(key >= list.size()) {
			key = list.size();
		}
		((List<Object>) list).add(key, ret);
		return ret;
	}
	
	/**
	 * @return	Returns true when entry was found without creating
	 * 			Returns false when entry was not found / or was created
	 */
	protected static boolean findPathArray(Map<String,Object> map, String path, 
			final DataEntry<Object, String> res, final boolean create,
			final MapFactory mapFactory, final CollFactory collFactory) throws BadFormat {
		res.setKey(map); //Key can be only Map or List!!!
		if(path == null) {
			res.setValue(null);
			return true;
		}
		int i = path.indexOf('[');
		if(i == -1) {
			res.setValue(path);
			return true;
		}
		int arrayIndex = path.indexOf("[]");
		if(arrayIndex != -1 && arrayIndex != path.length()-2) {
			throw badFormat("AbstractMapWorker.findPathArray: Array at disallowed place!").setOffsetAndLength(arrayIndex, 2);
		}
		if(arrayIndex == 0) {
			throw badFormat("AbstractMapWorker.findPathArray: Not compatible").setOffsetAndLength(0, 2);
		}
		final Value<Boolean> ret = new Value<>(true);
		try {
			ArgsDecoder.handleCollInside(new CollHandler<String>() {

				@SuppressWarnings("unchecked")
				@Override
				public void handle(String nev) {
					String old = res.getValue();
					//TL;DD
					
					if(old == null) {
						res.setValue(nev);
						return;
					}
					Object obj = res.getKey();
					if(obj instanceof Map) {
						Map<String,Object> map = (Map<String,Object>) obj;
						if(nev.isEmpty() || isNumber(nev)) {
							res.setKey(getOrCreateCollOrMap(map, old, collFactory, ret, create));
							//If empty value is passed, this is the last step!
						} else {
							res.setKey(getOrCreateMap(map, old, mapFactory, ret, create));
						}
					} else {
						List<Object> col = (List<Object>) obj;
						int oldI = Integer.parseInt(old);
						if(nev.isEmpty() || isNumber(nev)) {
							res.setKey(getOrCreateCollOrMap(col, oldI, collFactory, ret, create));
							//If empty value is passed, this is the last step!
						} else {
							res.setKey(getOrCreateMap(col, oldI, mapFactory, ret, create));
						}
					}
					res.setValue(nev);
				}
				
			}, path, '[', ']');
		} catch(CannotDoIt c) {
			return false; /* Can fail only when "create = false" */
		}
		//System.out.println(ret.getValue()); //DEBUG
		return ret.getValue();
	}
	
	/**
	 * @return	Returns true when entry was found without creating
	 * 			Returns false when entry was not found / or was created
	 */
	protected static boolean findPath(Map<String,Object> map, String path, 
			final DataEntry<Map<String,Object>, String> res, final boolean create,
			final MapFactory mapFactory) {
		res.setKey(map);
		if(path == null) {
			res.setValue(null);
			return true;
		}
		int i = path.indexOf('.');
		if(i == -1) {
			res.setValue(path);
			return true;
		}
		final Value<Boolean> ret = new Value<>(true);
		try {
			ArgsDecoder.handleColl(new CollHandler<String>() {
	
				@SuppressWarnings("unchecked")
				@Override
				public void handle(String nev) {
					String old = res.getValue();
					Map<String,Object> map = res.getKey();
					if(old != null) {
						Object obj = map.get(old);
						if(!(obj instanceof Map)) {
							if(create) {
								ret.setValue(false);
								Map<String,Object> nevmap = mapFactory.createMap();
								if(map.containsKey(old) && 
										!MapTranslator.translateMapTry(
										obj, map, MapTranslator
											.createStringObjectMapProperties())) {
									map.clear();
								}
								map.put(old, nevmap);
								map = nevmap;
							} else {
								throw cannotDoIt(); //
							}
						} else {
							map = (Map<String,Object>) obj;
						}
						res.setKey(map);
					}
					res.setValue(nev);
					
				}
				
			}, path, ".");
		} catch(CannotDoIt c) {
			return false; /* Can fail only when "create = false" */
		}
		return ret.getValue();
	}
	
	@Override
	public String toString() {
		return (this.getMap() == null) ? "{}" : this.getMap().toString();
	}
	
}
