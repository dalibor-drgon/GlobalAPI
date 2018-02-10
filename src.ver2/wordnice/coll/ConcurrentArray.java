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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ConcurrentArray<X>
implements Iterable<X> {

	public boolean acceptNull = false;
	public String nullString = "Value == null";
	
	//todo no synchronized
	protected Object lock = new Object();
	protected volatile Object[] array;
	protected volatile int size = 0;
	
	public ConcurrentArray() {}
	
	public ConcurrentArray(Object[] data) {
		if(data == null || data.length == 0) data = null;
		this.array = data;
	}
	
	protected int resize(int size) {
		if(this.array == null) {
			this.array = new Object[size];
			return 0;
		}
		int off = this.array.length;
		this.array = Arrays.copyOf(this.array, off+size);
		return off;
	}
	
	public void add(X val) {
		if(val == null && !acceptNull)
			throw new IllegalArgumentException(this.nullString);
		synchronized(lock) {
			int off = this.resize(1);
			this.array[off] = val;
			this.size = this.array.length;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addAll(X... hand) {
		if(hand == null) throw new IllegalArgumentException("Array " + this.nullString);
		if(hand.length == 0) return;
		synchronized(lock) {
			this.addAll0(Arrays.asList(hand), hand.length);
		}
	}
	
	public void addAll(Collection<X> hand) {
		if(hand == null) throw new IllegalArgumentException("Collection " + this.nullString);
		if(hand.isEmpty()) return;
		synchronized(lock) {
			this.addAll0(hand, hand.size());
		}
	}
	
	protected void addAll0(Iterable<X> hand, int len) {
		int i;
		Iterator<X> it = hand.iterator();
		if(!this.acceptNull) {
			for(i = 0; i < len && it.hasNext(); i++) {
				if(it.next() == null)
					throw new IllegalArgumentException("Index "+i+": " + this.nullString);
			}
			len = i;
		}
		int off = resize(len);
		it = hand.iterator();
		for(i = 0; i < len && it.hasNext(); i++) {
			this.array[off+i] = it.next();
		}
		if(i != len) this.array = Arrays.copyOf(this.array, i);
		this.size = this.array.length;
	}
	
	@Override
	public ListIterator<X> iterator() {
		return new ImmArrayIterator<X>(this.array, this.size());
	}
	
	public ListIterator<X> iterator(int index) {
		return new ImmArrayIterator<X>(this.array, this.size(), index);
	}
	
	public int size() {
		return this.size;
	}
	
	public boolean isEmpty() {
		return this.array == null || this.array.length == 0;
	}

	public List<X> snapshot() {
		if(this.array == null) return Collections.emptyList();
		Object[] arr = this.array;
		int i = 0, len = this.size();
		if(!this.acceptNull) {
			for(; i < len; i++) {
				if(arr[i] == null) break;
			}
			len = i;
		}
		return new ImmArray<X>(arr, len);
	}
	
	@Override
	public String toString() {
		if(this.isEmpty()) return "[]";
		return Arrays.toString(this.array);
	}
	
	public static void main(String...strings) {
		ConcurrentArray<String> ca = new ConcurrentArray<String>();
		ca.addAll("Hey","Whats up","bro!","What are you looking for?");
		List<String> snap = ca.snapshot();
		ca.add("Another reallocation");
		ca.add("And another one - Hope you enjoy");
		System.out.println("Current: " + ca.size() + " " + ca);
		System.out.println("Snapshot: " + snap.size() + " " + snap);
	}
	
}
