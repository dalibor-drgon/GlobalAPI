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

package wordnice.optimizer;

import java.lang.instrument.ClassDefinition;
import java.util.Collection;

import javassist.ClassPool;

public interface Optimizable {
	
	public static interface OptimizedChecker {
		/**
		 * @return true when optimizable with given name was ran
		 * 		false when not found
		 */
		boolean has(String name);
	}
	
	/**
	 * Name if this optimizable
	 * Ideally in format: [library name or author].[optimized class name]
	 * eg. wordnice.Character
	 */
	String getName();

	/**
	 * Check depencies and return true if we can continue
	 */
	boolean canOptimize(OptimizedChecker optimized);
	
	/**
	 * Optimize what is needed.
	 */
	void optimize(ClassPool cp, Collection<ClassDefinition> output) throws Throwable;
	
	/**
	 * Called when all pending optimizations were done!
	 */
	void afterOptimize(ClassPool cp);
	
}
