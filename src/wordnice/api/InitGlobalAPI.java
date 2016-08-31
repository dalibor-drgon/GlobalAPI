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

package wordnice.api;

import java.lang.instrument.Instrumentation;

import wordnice.javaagent.JavaAgent;

/**
 * On program start, make sure you call {@link OnlyOnce#initFirst(MiniLogger)}
 * (except bukkit / sponge plugins)
 * 
 * @author wordnice
 */
public class InitGlobalAPI {
	
	public static boolean all = false;
	public static boolean allMain = false;
	
	public interface MiniLogger {
		public void info(String str);
		public void severe(String str);
	}
	
	/**
	 * On main thread call this
	 */
	public static void initInMainThread(MiniLogger log) {
		if(allMain) {
			return;
		}
		allMain = true;
		log.info("Checking instrumentation...");
		Instrumentation ins = JavaAgent.get();
		log.info("Instrumentation: " + ins);
	}
	
	/**
	 * On program start, make sure you call this method
	 * (except bukkit / sponge plugins)
	 */
	public static void initFirst(MiniLogger log) {
		if(all) {
			return;
		}
		all = true;
		InitGlobalAPI.debugSQL(log);
	}
	
	
	
	protected static void debugDriver(MiniLogger log, String driver, String name) {
		try {
			if(Class.forName(driver) == null) {
				throw new NullPointerException("JVM just returned null!");
			}
			log.info(name + " (" + driver +") driver was FOUND AND LOADED!");
		} catch(Throwable t) {
			if(t instanceof LinkageError) {
				log.severe(name + " (" + driver +") driver was found, but NOT LOADED! " 
						+ t.getClass().getName() + ": " + t.getMessage() 
						+ " This can cause problems to addons using MainAPI!");
			} else {
				log.severe(name + " (" + driver +") driver was NOT FOUND! This can cause problems to addons using MainAPI!");
			}
		}
	}
	
	protected static void debugSQL(MiniLogger log) {
		InitGlobalAPI.debugDriver(log, "org.sqlite.JDBC","SQLite");
		InitGlobalAPI.debugDriver(log, "com.mysql.jdbc.Driver", "MySQL");
	}
	
}
