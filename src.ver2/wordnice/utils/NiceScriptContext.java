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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import javax.script.Bindings;
import javax.script.SimpleScriptContext;

public class NiceScriptContext 
extends SimpleScriptContext {

	public NiceScriptContext() {
		this(new NiceBindings(), null);
	}
	
	public NiceScriptContext(boolean hasGlobal) {
		this(new NiceBindings(), (hasGlobal) ? new NiceBindings() : null);
	}
	
	public NiceScriptContext(Bindings scope) {
		this(scope, null);
	}
	
	public NiceScriptContext(Bindings scope, Bindings global) {
		this(scope, global, 
				new InputStreamReader(System.in),
				new PrintWriter(System.out, true),
				new PrintWriter(System.err, true));
	}
	
	public NiceScriptContext(Bindings scope, Bindings global,
			Reader in, Writer out, Writer err) {
		super.engineScope = scope;
		super.globalScope = global;
		super.reader = in;
		super.writer = out;
		super.errorWriter = err;
	}
	
	@Override
	public String toString() {
		return "ScriptContext(engine="
				+((this.engineScope == null) ? null : this.engineScope.values())
				+", global="+((this.globalScope == null) ? null : this.globalScope.values())+")";
	}
	
}
