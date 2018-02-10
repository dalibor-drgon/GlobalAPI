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

import java.util.Map;
import java.util.Map.Entry;

import wordnice.api.Nice;
import wordnice.api.Nice.ListFactory;
import wordnice.api.Nice.MapFactory;

public class MapWorkerImpl extends AbstractMapWorker {

	protected Map<String, Object> map;
	protected MapFactory mapFactory = null;
	protected ListFactory collFactory = null;
	
	public ListFactory getListFactory() {
		return this.collFactory;
	}
	
	public MapFactory getMapFactory() {
		return this.mapFactory;
	}
	
	public MapWorkerImpl setListFactory(ListFactory nev) {
		if(nev == null) {
			throw Nice.badArg("MapWorker.setCollFactory: Null ListFactory argument");
		}
		collFactory = nev;
		return this;
	}
	
	public MapWorkerImpl setMapFactory(MapFactory nev) {
		if(nev == null) {
			throw Nice.badArg("MapWorker.setMapFactory: Null CollFactory argument");
		}
		mapFactory = nev;
		return this;
	}
	
	public MapWorkerImpl() {
		this(Nice.getLinkedMapFactory(), Nice.getListFactory());
	}
	
	public MapWorkerImpl(MapFactory mp, ListFactory cf) {
		this.setMapFactory(mp).setListFactory(cf);
		this.map = this.getMapFactory().createMap();
	}
	
	public MapWorkerImpl(Map<String,? extends Object> map) {
		this(Nice.getLinkedMapFactory(), Nice.getListFactory(), map);
	}
	
	@SuppressWarnings("unchecked")
	public MapWorkerImpl(MapFactory mp, ListFactory cf, Map<String,? extends Object> map) {
		this.setMapFactory(mp).setListFactory(cf);
		this.map = (Map<String, Object>) map;
	}
	
	@Override
	public Map<String, Object> getMap() {
		return this.map;
	}
	
	
	public static void main(String...strings) {
		Nice.debug(true); //Fill stack traces etc
		MapWorker mp = new MapWorkerImpl();
		
		mp.putArrayed("Variables[Password]", "al0ANgx4tUNC2q");
		
		mp.put("one.two.three", new Object());
		mp.putCur("cur", "Hey");
		mp.put("Helloz", "Yea");
		mp.put("one.two.one.two.one.two.one.two", 1);
		mp.putArrayed("Servers", 123);
		System.out.println("---");
		mp.putArrayed("Servers[]", 123);
		mp.putArrayed("Servers[]", 123);
		mp.putArrayed("Servers[Users]", 123);
		mp.putArrayed("[Servers2]", 123);
		mp.putArrayed("[Servers2][]", 123);
		mp.putArrayed("Servers[Users2]", 123);
		mp.putArrayed("Servers[Users2][]", 123);
		mp.putArrayed("Servers[Users2][]", 123);
		mp.putArrayed("Servers[Users2][]", 234);
		//mp.putArrayed("Servers[Users2][][]", 123); //OK IF = Array at disallowed place!
		//mp.putArrayed("Servers[Users2][][Hello]", 123); //OK IF = Array at disallowed place!
		
		System.out.println();
		System.out.println();
		System.out.println("Whole map:");
		System.out.println(mp.getMap());
		
		System.out.println();
		System.out.println();
		System.out.println("Getting:");
		
		System.out.println(mp.getColl("Servers.Users2"));
		System.out.println(mp.getColl("Servers"));
		System.out.println(mp.castColl("Servers"));
		System.out.println(mp.translateCollection("Servers", CollTranslator.createObjectCollProperties(), null));
		System.out.println(mp.get("Servers.Users", int.class));
		System.out.println(mp.get("Servers.Users2", int.class));
		
		System.out.println();
		System.out.println();
		System.out.println("Iterating:");
		//Iterating
		for(Entry<String,Object> ent : mp) {
			System.out.println(ent.getKey() + ": " + ent.getValue());
		}
	}
	

}
