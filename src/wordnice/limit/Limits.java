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

package wordnice.limit;

import wordnice.api.Nice;

public class Limits {

	public static Limit createLimit(int max) {
		return new SimpleLimit(max, 0);
	}
	
	public static Limit createLimit(int max, int pos) {
		return new SimpleLimit(max, pos);
	}
	
	public static EntryLimit createEntryLimit(int max) {
		return new SimpleEntryLimit(max, 0, max, 0, max, 0);
	}
	
	public static EntryLimit createEntryLimit(int max, int keyAndValMax) {
		return new SimpleEntryLimit(max, 0, keyAndValMax, 0, keyAndValMax, 0);
	}
	
	public static EntryLimit createEntryLimit(int max, int keyMax, int valMax) {
		return new SimpleEntryLimit(max, 0, keyMax, 0, valMax, 0);
	}
	
	protected static class SimpleLimit
	implements Limit {
		
		@Override
		public String toString() {
			return "Limit[max=" + maximum + ", pos=" + position + ", avail=" + available() + "]";
		}

		protected int maximum = 0;
		protected int position = 0;
		
		public SimpleLimit(int max, int pos) {
			if(max < 0) throw new IllegalArgumentException("Maximum is negative");
			if(pos < 0 || pos > max) throw new IllegalArgumentException("Position out of bounds");
			this.maximum = max;
			this.position = pos;
		}

		@Override
		public int position() {
			return this.position;
		}

		@Override
		public int maximum() {
			return this.maximum;
		}
		
		@Override
		public int available() {
			return this.maximum - this.position;
		}
		
		@Override
		public int increment() {
			return this.increment(1);
		}

		@Override
		public int increment(int val) {
			int nevpos = this.position + val;
			if(nevpos < 0 || nevpos > this.maximum) this.throwLimitException();
			return this.position = nevpos;
		}
		
		@Override
		public boolean canIncrement(int val) {
			int nevpos = this.position + val;
			return nevpos >= 0 && nevpos <= this.maximum;
		}

		@Override
		public Limit reset() {
			this.increment(-this.position);
			return this;
		}

		@Override
		public void throwLimitException() {
			throw new LimitException("Increment: Out of bounds");
		}

		@Override
		public Limit clone() {
			return new SimpleLimit(this.maximum(), this.position());
		}
		
	}
	
	protected static class SimpleLimitWithParent
	extends SimpleLimit {

		protected Limit parent;
		
		public SimpleLimitWithParent(int max, int pos, Limit parent) {
			super(max, pos);
			if(parent == null) throw new IllegalArgumentException("Limit parent == null");
			this.parent = parent;
		}
		
		@Override
		public int available() {
			return Math.min(parent.available(), super.available());
		}
		
		@Override
		public int increment(int val) {
			this.parent.increment(val);
			return super.increment(val);
		}
		
		@Override
		public boolean canIncrement(int val) {
			if(!this.parent.canIncrement(val)) return false;
			return super.canIncrement(val);
		}
		
		@Override
		public void throwLimitException() {
			this.parent.throwLimitException();
		}
	}
	
	protected static class SimpleEntryLimit
	extends SimpleLimit
	implements EntryLimit {
		
		@Override
		public String toString() {
			return "EntryLimit[key=" + keyLimit + ", value=" + valueLimit + ", this=" + super.toString() + "]";
		}

		protected Limit keyLimit;
		protected Limit valueLimit;
		
		public SimpleEntryLimit(int max, int pos, int keyMax, int keyPos, int valMax, int valPos) {
			super(max, pos);
			this.keyLimit = new SimpleLimitWithParent(keyMax, keyPos, this);
			this.valueLimit = new SimpleLimitWithParent(valMax, valPos, this);
		}
		
		public SimpleEntryLimit(EntryLimit lim) {
			super(Nice.notNull(lim, "EntryLimit == null").maximum(), lim.position());
			Limit keyLim = lim.keyLimit(), valLim = lim.valueLimit();
			this.keyLimit = new SimpleLimitWithParent(keyLim.maximum(), keyLim.position(), this);
			this.valueLimit = new SimpleLimitWithParent(valLim.maximum(), valLim.position(), this);
		}

		@Override
		public Limit keyLimit() {
			return this.keyLimit;
		}

		@Override
		public Limit valueLimit() {
			return this.valueLimit;
		}

		@Override
		public EntryLimit reset() {
			super.reset();
			this.keyLimit.reset();
			this.valueLimit.reset();
			return this;
		}

		@Override
		public EntryLimit clone() {
			return new SimpleEntryLimit(this);
		}
		
	}
	
	
	public static void main(String...strings) {
		EntryLimit lm = Limits.createEntryLimit(10, 8, 6).clone().clone();
		System.out.println(lm);
		System.out.println(lm.keyLimit().increment(5));
		System.out.println(lm.keyLimit().increment(3));
		System.out.println(lm.valueLimit().increment(1));
		System.out.println(lm);
		System.out.println(lm.valueLimit().increment(2));
		System.out.println(lm);
	}
	
}
