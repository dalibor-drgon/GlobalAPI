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

import java.util.Map;

import javax.script.SimpleBindings;

import wordnice.api.Api;

public class NiceBindings extends SimpleBindings {

	protected Map<String,Object> __map;
	
	public NiceBindings() {
		this(Api.<String,Object>createMap());
	}
	
	public NiceBindings(int size) {
		this(Api.<String,Object>createMap(size));
	}
	
	public NiceBindings(Map<String,Object> map) {
		super(map);
		__map = map;
	}
	
	public void putAllForced(Map<String, ? extends Object> map) {
		__map.putAll(map);
	}
	
	public Map<String,Object> getMap() {
		return this.__map;
	}
	
	@Override
	public String toString() {
		return this.__map.toString();
	}
	
}
