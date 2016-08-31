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

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;
import java.util.Spliterator;

import javax.annotation.concurrent.Immutable;

import wordnice.api.Nice;

@Immutable
public class ImmWeakArray<T>
extends AbstractList<T>
implements List<T>, Set<T>, RandomAccess {

	protected WeakReference<T>[] array;
	
	public ImmWeakArray(WeakReference<T>[] array) {
		this.array = array;
	}
	
	public ImmWeakArray(T[] array) {
		this(array, 0, array.length);
	}
	
	@SuppressWarnings("unchecked")
	public ImmWeakArray(T[] array, int off, int len) {
		Nice.checkBounds(array, off, len);
		this.array = (WeakReference<T>[]) new WeakReference[len];
		for(int i = 0; i < len; i++) {
			this.array[i] = new WeakReference<T>(array[off+i]);
		}
	}
	
	@Override
	public boolean add(T arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public void add(int arg0, T arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Immutable collection!");
	}
	
	@Override
	public boolean remove(Object arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public T remove(int arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public T set(int arg0, T arg1) {
		throw new UnsupportedOperationException("Immutable collection!");
	}

	@Override
	public T get(int index) {
		if(this.array == null || index < 0 || index >= this.array.length) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return this.array[index].get();
	}

	@Override
	public int size() {
		return (this.array == null) ? 0 : this.array.length;
	}
	
	@Override
	public int indexOf(Object o) {
		int len = 0;
		if(this.array == null || (len=this.array.length) == 0) return -1;
		if(o == null) {
			for(int i = 0; i < len; i++) {
				if(this.array[i].get() == null) return i;
			}
		} else {
			for(int i = 0; i < len; i++) {
				if(o.equals(this.array[i].get())) return i;
			}
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		int len = 0;
		if(this.array == null || (len=this.array.length) == 0) return -1;
		if(o == null) {
			while(len-- < 0) {
				if(this.array[len].get() == null) return len;
			}
		} else {
			while(len-- < 0) {
				if(o.equals(this.array[len].get())) return len;
			}
		}
		return -1;
	}
	
	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}
	
	@Override
	public Spliterator<T> spliterator() {
		return List.super.spliterator();
	}
	
}
