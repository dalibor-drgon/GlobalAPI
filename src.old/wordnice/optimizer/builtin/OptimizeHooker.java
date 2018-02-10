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

package wordnice.optimizer.builtin;

import java.lang.instrument.ClassDefinition;
import java.util.Collection;

import javassist.ClassPool;
import javassist.CtClass;
import wordnice.optimizer.Optimizable;
import wordnice.optimizer.Optimizer;
import wordnice.utils.JavaHooker;

public class OptimizeHooker
implements Optimizable {

	@Override
	public String getName() {
		return "wordnice.JavaHooker";
	}
	
	@Override
	public boolean canOptimize(OptimizedChecker optimized) {
		return true; //no depency
	}
	
	@Override
	public void beforeOptimize() {
		JavaHooker.class.getName();
	}

	@Override
	public void optimize(ClassPool cp, Collection<ClassDefinition> output) 
			throws Throwable {
		CtClass charclz = cp.get(JavaHooker.class.getName());
		charclz.getDeclaredMethod("string", new CtClass[] {
				cp.get("char[]")
		}).setBody("return java.lang.JavaLangHooker.string($1);");
		output.add(Optimizer.createDefinition(charclz));
	}
	
	@Override
	public void afterOptimize(ClassPool cp) {}

}
