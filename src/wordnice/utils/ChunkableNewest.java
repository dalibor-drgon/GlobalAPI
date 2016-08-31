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
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Instead of sending (eg) Newest 20kilobytes of console data every time,
 * send just Newest new lines.
 * This allows you to do more refreshes and save network bytes.
 * @param <K> Key type
 * @param <R> Return type
 */
public interface ChunkableNewest<K,R> {
	
	public static interface ChunkerString<R>
	extends Chunker<String, R> {}

	public static interface Chunker<K,R> {
		
		/**
		 * @return Key name, by which you can access chunker later.
		 * 		This key should not change with another calls of getNewest() & setNewest()
		 */
		K getKey();
		
		/**
		 * @return Last new content
		 */
		R getNewest();
		
		/**
		 * Sets index where getNewest() ended
		 * @throws IllegalStateException If getNewest() was not called
		 */
		void setNewest() throws IllegalStateException;
		
		/**
		 * Equivalent to calling getNewest(); setNewest()
		 * @return Last new content
		 */
		R getAndSetNewest();
		
	}
	
	/**
	 * This method is equivalent to:
	 * <pre>{@code
	 * return getChunker(key).getNewest();
	 * }</pre>
	 * 
	 * @return Last message under chunker with given key
	 * @throws NoSuchElementException When cannot find chunker for given key
	 */
	public R getNewest(K key) throws NoSuchElementException;
	
	/**
	 * This method is equivalent to:
	 * <pre>{@code
	 * Chunker ch = getChunker(key);
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
	 * @return Newly created Chunker starting 
	 * 		from the most possible outdated element
	 */
	public Chunker<K,R> createChunker();
	
	/**
	 * @param key Chunker key
	 * @return Chunker under given key
	 * @throws NoSuchElementException When cannot find chunker for given key
	 */
	public Chunker<K,R> getChunker(K key) throws NoSuchElementException;
	
	/**
	 * @return Unmodifiable map
	 */
	public Map<K, ? extends Chunker<K,R>> getChunkersReadOnly();
	
	/**
	 * @return Entry set
	 * 
	 * @note You should keep in mind that 
	 * 		some map implementations allow you to change entry value,
	 * 		but you should never do that, unless you know what you are doing
	 */
	public Set<? extends Entry<K, ? extends Chunker<K,R>>> getChunkersRemovable();
	public int getChunkersSize();
	public boolean isChunkersEmpty();
	public void clearChunkers();
	
}
