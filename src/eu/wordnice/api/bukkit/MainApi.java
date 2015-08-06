/*
 The MIT License (MIT)

 Copyright (c) 2015, Dalibor Drgoň <emptychannelmc@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package eu.wordnice.api.bukkit;

import eu.wordnice.api.Api;

public class MainApi extends org.bukkit.plugin.java.JavaPlugin {
	
	@Override
	public void onEnable() {
		try {
			this.getLogger().info("Instrumentation: " + Api.getInstrumentation());
			this.getLogger().info("System class loaded has " + Api.getLoadedClasses(Api.getClassLoader()).length + " classes!");
			this.getLogger().info("Totaly loaded " + Api.getAllLoadedClasses().length + " classes!");
		} catch(Throwable t) {}
		this.getLogger().info("MainAPI by wordnice was enabled! Hello!");
	}
	
	@Override
	public void onDisable() {
		this.getLogger().info("MainAPI by wordnice was disabled! Bye!");
	}
	
}
