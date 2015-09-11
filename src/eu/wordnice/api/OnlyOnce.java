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

public class OnlyOnce {
	
	public interface OnlyOnceLogger {
		public void info(String str);
		public void severe(String str);
	}
	
	/**
	 * On program start, make sure you call debugAll()
	 * (except bukkit / sponge plugins)
	 */
	public static void debugAll(OnlyOnceLogger log) {
		OnlyOnce.debugSQL(log);
	}
	
	public static void debugDriver(OnlyOnceLogger log, String driver, String name) {
		try {
			if(Class.forName(driver) == null) {
				throw new NullPointerException();
			}
			log.info(name + " driver was FOUND and loaded!");
		} catch(Throwable t) {
			if(t instanceof LinkageError) {
				log.severe(name + " driver was FOUND, but NOT LOADED! " 
						+ t.getClass().getName() + ": " + t.getMessage() 
						+ " This can cause problems to addons using MainAPI!");
			} else {
				log.severe(name + " driver was NOT FOUND! This can cause problems to addons using MainAPI!");
			}
		}
	}
	
	public static void debugSQL(OnlyOnceLogger log) {
		OnlyOnce.debugDriver(log, "org.sqlite.JDBC","SQLite");
		OnlyOnce.debugDriver(log, "com.mysql.jdbc.Driver", "MySQL");
	}
	
}
