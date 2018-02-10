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
import java.util.logging.Level;
import java.util.logging.Logger;

import wordnice.javaagent.JavaAgent;

/**
 * Init needed for standalone applications
 * (not for plugins)
 * 
 * @author wordnice
 */
public class InitNiceAPI {
	
	public static boolean all = false;
	public static boolean allMain = false;
	public static boolean optimizations = false;
	
	/**
	 * Its best to call this when your application is starting
	 * Init everything needed
	 */
	public static void initAll(Logger log) {
		initFirst(log);
		initInMainThread(log);
		//initOptimizations(log);
	}
	
	/**
	 * !!! CALL THIS ASAP ON PROGRAM START IN MAIN THREAD !!!
	 * 
	 * On main thread call this after initFirst()
	 * (except bukkit / sponge plugins)
	 */
	public static void initInMainThread(Logger log) {
		if(allMain) {
			return;
		}
		allMain = true;
		log.info("Checking instrumentation...");
		Instrumentation ins = JavaAgent.get();
		log.finest("Instrumentation: " + ins);
	}
	
	/**
	 * !!! CALL THIS ASAP ON PROGRAM START !!!
	 * 
	 * On program start, make sure you call this method
	 * (except bukkit / sponge plugins)
	 * Call this before initInMainThread()
	 */
	public static void initFirst(Logger log) {
		if(all) {
			return;
		}
		all = true;
		InitNiceAPI.debugSQL(log);
	}
	
	/**
	 * !!! CALL THIS ASAP ON PROGRAM START IN MAIN THREAD !!!
	 * 
	 * Call this after initInMainThread() in main thread
	 * (except bukkit / sponge plugins)
	 */
	/*public static void initOptimizations(final Logger log) {
		if(optimizations) return;
		try {
			Collection<Bootstrap.EntryFile> bf = new ArrayList<Bootstrap.EntryFile>();
			Optimizer.addBootstrap(bf, JavaUtils.getClassesLocation(Optimizer.class), 
					new Bootstrap.Handler() {

				@Override
				public String canBootstrap(String name) {
					if(name.startsWith("java") || name.startsWith("optimized")) {
						log.finest("Adding " + name + " to boostrap!");
						return "(wordnice)"+name;
					}
					return null;
				}
			});
			Optimizer.forceBootstrap(bf);
			optimizations = true;
		} catch (Exception e) {
			log.log(Level.SEVERE, 
					"Error occured while adding needed files to bootstrap! "
					+ "Aborting builtin optimizations of NiceAPI...", e);
			return;
		}
		
		/*Collection<Optimizable> opts = new ArrayList<Optimizable>();
		opts.add(new OptimizeHooker());
		Optimizer.forceOptimizations(opts);* /
	}*/
	
	
	
	protected static void debugDriver(Logger log, String driver, String name) {
		try {
			if(Class.forName(driver) == null) {
				throw new NullPointerException("JVM just returned null!");
			}
			log.info(name + " (" + driver +") driver was FOUND AND LOADED!");
		} catch(Throwable t) {
			if(t instanceof LinkageError) {
				log.log(Level.SEVERE, name + " (" + driver +") driver was found, "
						+ "but NOT LOADED! " + t.getClass().getName() + ": " + t.getMessage() 
						+ " This can cause problems to addons using MainAPI!", t);
			} else {
				log.log(Level.SEVERE, name + " (" + driver +") driver was NOT FOUND! "
						+ "This can cause problems to addons using MainAPI!", t);
			}
		}
	}
	
	protected static void debugSQL(Logger log) {
		InitNiceAPI.debugDriver(log, "org.sqlite.JDBC", "SQLite");
		InitNiceAPI.debugDriver(log, "com.mysql.jdbc.Driver", "MySQL");
	}

	public static void main(String...strings) {
		Logger lg = Nice.createLogger(InitNiceAPI.class);
		lg.setLevel(Level.FINEST);
		lg.setFilter(null);
		Logger.getGlobal().setLevel(Level.FINEST);
		Logger.getGlobal().setFilter(null);
		InitNiceAPI.initAll(lg);
		System.out.println("java.util.function.Consumer");
		System.out.println(System.getProperties().clone().toString());
	}
	
}
