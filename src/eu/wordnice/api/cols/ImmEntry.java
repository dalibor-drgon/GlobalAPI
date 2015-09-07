/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.wordnice.api.cols;

import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;

@Immutable
public class ImmEntry<X, Y> implements Entry<X, Y> {

	public X key;
	
	public Y val;
	
	public ImmEntry(X key, Y val) {
		this.key = key;
		this.val = val;
	}
	
	@Override
	public X getKey() {
		return this.key;
	}

	@Override
	public Y getValue() {
		return this.val;
	}

	@Override
	public Y setValue(Y value) {
		throw new UnsupportedOperationException("Immutable entry!");
	}
	
	
	@Override
	public String toString() {
		return this.key + "=" + this.val;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(!(obj instanceof Entry)) {
			return false;
		}
		Entry<?, ?> en = (Entry<?, ?>) obj;
		return ((this.key == null) 
					? (en.getKey() == null)
					: (this.key.equals(en.getKey()))
				&& (this.val == null) 
					? (en.getValue() == null)
					: (this.val.equals(en.getValue())));
	}

}
