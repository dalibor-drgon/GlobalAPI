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
import eu.wordnice.api.Set;

public class MainApi extends org.bukkit.plugin.java.JavaPlugin {
	
	/*
	 * Bukkit only utils
	 */
	public static String NMS = null;
	
	public static String getNMS(String clz) {
		return "net.minecraft.server." + MainApi.NMS + "." + clz;
	}
	
	public static String getCB(String clz) {
		return "org.bukkit.craftbukkit." + MainApi.NMS + "." + clz;
	}
	
	
	/*
	 * Enable, Disable
	 */
	@Override
	public void onEnable() {
		try {
			Set<String> clzs = Api.getClasses(Api.getClassesLocation(org.bukkit.Bukkit.class));
			this.getLogger().info("Bukkit classes: " + clzs.size());
			this.getLogger().info("Bukkit packages: " + Api.filterPackages(clzs, null).size());
		} catch(Throwable t) {}
		
		String cpkg = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
		MainApi.NMS = cpkg.substring(cpkg.lastIndexOf('.') + 1);
		this.getLogger().info("NMS version: " + MainApi.NMS);
		
		this.getLogger().info("MainAPI by wordnice for Bukkit was enabled! Hello!");
	}
	
	@Override
	public void onDisable() {
		this.getLogger().info("MainAPI by wordnice was disabled! Bye!");
	}
	
}
