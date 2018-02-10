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

public interface Limit
extends Cloneable {

	/** Return current position
	 * @return current position. Should be in range <0, maximum()>
	 */
	public int position();
	
	/** Sets current position
	 * @throws IllegalArgumentException when val < 0
	 * @throws RuntimeException when out of bounds: val < 0 || val > maximum()
	 * @return this
	 * @see {@link #throwLimitException()}
	 */
	//public Limit position(int pos) throws IllegalArgumentException, Masked;
	
	/** Return maximum position 
	 * @return maximum position
	 */
	public int maximum();
	
	/** Return available space
	 * @return available space
	 */
	public int available();
	
	/** Increment by one or fail
	 * @param val Value to increment with
	 * @throws RuntimeException when out of bounds
	 * @return New position
	 * @see {@link #throwLimitException()}
	 */
	public int increment();
	
	/** Increment by given value or fail
	 * @param val Value to increment with
	 * @throws RuntimeException when out of bounds
	 * @return New position
	 * @see {@link #throwLimitException()}
	 */
	public int increment(int val);
	
	/** Check if can increment by given value, but this method does not 
	 * change anything and does not fail
	 * @param val Value to increment with
	 * @return true if can increment without Masked exception being thrown
	 */
	public boolean canIncrement(int val);
	
	/**
	 * Resets position to zero and return "this"
	 * @return this
	 */
	public Limit reset();
	
	/**
	 * This method should be accesible and called when
	 * limit is reached
	 * @throws RuntimeException and Error when limit is reached (out of bounds)
	 */
	public void throwLimitException();
	
	/**
	 * Return new unlinked limit with exactly same position
	 * @return new unlinked limit with exactly same position
	 */
	public Limit clone();
	
}
