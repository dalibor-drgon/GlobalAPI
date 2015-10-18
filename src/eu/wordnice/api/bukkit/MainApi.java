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

package eu.wordnice.api.bukkit;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import eu.wordnice.api.Api;
import eu.wordnice.api.OnlyOnce;
import eu.wordnice.javaagent.JavaAgent;

public class MainApi extends org.bukkit.plugin.java.JavaPlugin {
	
	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		final Logger lg = this.getLogger();
		
		try {
			Set<String> clzs = Api.getClasses(Api.getClassesLocation(org.bukkit.Bukkit.class));
			lg.info("Bukkit classes: " + clzs.size());
			lg.info("Bukkit packages: " + Api.filterPackagesString(clzs, (String) null).size());
		} catch(Throwable t) {}
				
		String cpkg = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
		WNBukkit.NMS = cpkg.substring(cpkg.lastIndexOf('.') + 1);
		lg.info("NMS version: " + WNBukkit.NMS);
		lg.info("Online players: " + WNBukkit.getPlayers());
		lg.info("Online worlds: " + WNBukkit.getWorlds());
		lg.info("Online plugins: " + WNBukkit.getPlugins());
		
		OnlyOnce.debugAll(new OnlyOnce.OnlyOnceLogger() {
			
			@Override
			public void severe(String str) {
				lg.severe(str);
			}
			
			@Override
			public void info(String str) {
				lg.info(str);
			}
		});
		
		Bukkit.getScheduler().runTask(this, new Runnable() {
			@Override
			public void run() {
				lg.info("Checking instrumentation...");
				JavaAgent.setTryAgain(true);
				lg.info("Instrumentation: " + JavaAgent.get());
			}
		});
		
		lg.info("MainAPI by wordnice for Bukkit was enabled! "
				+ "(waiting for server core to check instrumentation)");
	}
	
	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		this.getLogger().info("MainAPI was disabled!");
	}
	
}
