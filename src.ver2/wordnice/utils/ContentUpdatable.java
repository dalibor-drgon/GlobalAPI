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
import java.util.NoSuchElementException;

/**
 * Instead of sending (eg) Newest 20kilobytes of console data every time,
 * send just Newest new lines.
 * This allows you to do more refreshes and save network bytes.
 * @param <K> Key type
 * @param <R> Return type
 */
public interface ContentUpdatable<K,R> {
	
	/**
	 * This method is equivalent to:
	 * <pre>{@code
	 * return getUpdate(key).getNewest();
	 * }</pre>
	 * 
	 * @return Last message under chunker with given key
	 * @throws NoSuchElementException When cannot find chunker for given key
	 */
	public R getNewest(K key) throws NoSuchElementException;
	
	/**
	 * This method is equivalent to:
	 * <pre>{@code
	 * getUpdate(key).setNewest();
	 * }</pre>
	 * 
	 * @return Last message under chunker with given key
	 * @throws NoSuchElementException When cannot find chunker for given key
	 */
	public void setNewest(K key) throws NoSuchElementException;
	
	/**
	 * This method is equivalent to:
	 * <pre>{@code
	 * Update ch = getUpdate(key);
	 * R ret = ch.getNewest();
	 * ch.setNewest();
	 * return ret;
	 * }</pre>
	 * 
	 * @return Last message under chunker with given key
	 * @throws NoSuchElementException When cannot find chunker for given key
	 */
	public R getAndSetNewest(K key) throws NoSuchElementException;
	
	/**
	 * @return Newly created Update starting 
	 * 		from the most possible outdated element
	 */
	public ContentUpdate<K,R> createUpdate();
	
	/**
	 * @param key Update key
	 * @return Update under given key, never return null
	 * @throws NoSuchElementException When cannot find chunker for given key
	 */
	public ContentUpdate<K,R> getUpdateOrThrow(K key) throws NoSuchElementException;
	
	/**
	 * @param key Update key
	 * @return Update under given key or null if not found
	 */
	public ContentUpdate<K,R> getUpdate(K key);
	
	/**
	 * @return Unmodifiable map
	 */
	public Map<K, ? extends ContentUpdate<K,R>> getUpdates();
	
	/**
	 * Remove update and return it if found, otherwise return null
	 * @param key Update key
	 * @return removed update or null if not found
	 */
	public ContentUpdate<K,R> removeUpdate(K key);
	
	/**
	 * Return true if there are any updates registered, false otherwise
	 * @return true if there are any updates registered, false otherwise
	 */
	public boolean hasUpdates();
	
	/**
	 * Clear all updates
	 */
	public void clearUpdates();
	
}
