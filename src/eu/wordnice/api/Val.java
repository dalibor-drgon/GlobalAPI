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

package eu.wordnice.api;

public class Val {
	
	public static class OneVal<X> {
		
		public X one;
		
		public OneVal() {}
		
		public OneVal(X x) {
			this.one = x;
		}
		
	}
	
	public static class TwoVal<X, Y> {
		
		public X one;
		public Y two;
		
		public TwoVal() {}
		
		public TwoVal(X x, Y y) {
			this.one = x;
			this.two = y;
		}
		
	}
	
	public static class ThreeVal<X, Y, Z> {
		
		public X one;
		public Y two;
		public Z three;
		
		public ThreeVal() {}
		
		public ThreeVal(X x, Y y, Z z) {
			this.one = x;
			this.two = y;
			this.three = z;
		}
		
	}
	
	public static class FourVal<X, Y, Z, A> {
		
		public X one;
		public Y two;
		public Z three;
		public A four;
		
		public FourVal() {}
		
		public FourVal(X x, Y y, Z z, A a) {
			this.one = x;
			this.two = y;
			this.three = z;
			this.four = a;
		}
		
	}
	
	public static class FiveVal<X, Y, Z, A, B> {
		
		public X one;
		public Y two;
		public Z three;
		public A four;
		public B five;
		
		public FiveVal() {}
		
		public FiveVal(X x, Y y, Z z, A a, B b) {
			this.one = x;
			this.two = y;
			this.three = z;
			this.four = a;
			this.five = b;
		}
		
	}
	
	public static class SixVal<X, Y, Z, A, B, C> {
		
		public X one;
		public Y two;
		public Z three;
		public A four;
		public B five;
		public C six;
		
		public SixVal() {}
		
		public SixVal(X x, Y y, Z z, A a, B b, C c) {
			this.one = x;
			this.two = y;
			this.three = z;
			this.four = a;
			this.five = b;
			this.six = c;
		}
		
	}


	
}
