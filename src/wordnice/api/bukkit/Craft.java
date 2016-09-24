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

package wordnice.api.bukkit;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import wordnice.coll.ImmWeakArray;
import wordnice.utils.NiceStrings;

public class Craft {
	
	/**
	 * Bukkit-only utilities
	 */
	public static String NMS = null;
	
	protected static Method onlinePlayers = null;
	protected static Method onlineWorlds = null;
	protected static Method onlinePlugins = null;
	
	/**
	 * Get net.minecraft.server.v___.* class name
	 * 
	 * @param clz Class name
	 * 
	 * @return Class name
	 */
	public static String getNMS(String clz) {
		return "net.minecraft.server." + Craft.NMS + "." + clz;
	}
	
	/**
	 * Get org.bukkit.craftbukkit.v___ class name
	 * 
	 * @param clz Class name
	 * 
	 * @return Class name
	 */
	public static String getCB(String clz) {
		return "org.bukkit.craftbukkit." + Craft.NMS + "." + clz;
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<Player> getPlayers() {
		return (Collection<Player>) Bukkit.getOnlinePlayers();
	}
	
	public static Collection<World> getWorlds() {
		return (Collection<World>) Bukkit.getWorlds();
	}
	
	public static Collection<Plugin> getPlugins() {
		return new ImmWeakArray<Plugin>(Bukkit.getPluginManager().getPlugins());
	}
	
	
	/*** API PLAYER ***/
	
	public static Player getPlayer(String name) {
		Iterator<Player> it = Craft.getPlayers().iterator();
		while(it.hasNext()) {
			Player p = it.next();
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public static Player getPlayerIgnoreCase(String name) {
		Iterator<Player> it = Craft.getPlayers().iterator();
		while(it.hasNext()) {
			Player p = it.next();
			if(p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}
	
	public static Player getPlayerUUID(String name) {
		Iterator<Player> it = Craft.getPlayers().iterator();
		UUID id = null;
		try {
			id = UUID.fromString(name);
		} catch(IllegalArgumentException ex) {
			return null;
		}
		while(it.hasNext()) {
			Player p = it.next();
			if(p.getUniqueId().equals(id)) {
				return p;
			}
		}
		return null;
	}
	
	
	
	public static void getPlayers(Collection<Player> out, Collection<String> comp) {
		Iterator<Player> it = Craft.getPlayers().iterator();
		while(it.hasNext()) {
			Player p = it.next();
			String nam = p.getName();
			Iterator<String> req = comp.iterator();
			while(req.hasNext()) {
				String c = req.next();
				if(c.equals(nam)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	public static void getPlayersIgnoreCase(Collection<Player> out, Collection<String> comp) {
		Iterator<Player> it = Craft.getPlayers().iterator();
		while(it.hasNext()) {
			Player p = it.next();
			String nam = p.getName();
			Iterator<String> req = comp.iterator();
			while(req.hasNext()) {
				String c = req.next();
				if(c.equalsIgnoreCase(nam)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	public static void getPlayersUUID(Collection<Player> out, Collection<String> comp) {
		Collection<Player> players = getPlayers();
		UUID id = null;
		Iterator<String> req = comp.iterator();
		while(req.hasNext()) {
			try {
				id = UUID.fromString(req.next());
			} catch(IllegalArgumentException ign) {
				continue;
			}
			for(Player p : players) {
				if(p == null) continue;
				UUID cid = p.getUniqueId();
				if(cid.equals(id)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	
	
	
	/*** WORLD ***/
	
	public static World getWorld(String name) {
		Iterator<World> it = Craft.getWorlds().iterator();
		while(it.hasNext()) {
			World p = it.next();
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public static World getWorldIgnoreCase(String name) {
		Iterator<World> it = Craft.getWorlds().iterator();
		while(it.hasNext()) {
			World p = it.next();
			if(p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}
	
	public static World getWorldUUID(String name) {
		Iterator<World> it = Craft.getWorlds().iterator();
		UUID id = null;
		try {
			id = UUID.fromString(name);
		} catch(IllegalArgumentException ex) {
			return null;
		}
		while(it.hasNext()) {
			World p = it.next();
			if(p.getUID().equals(id)) {
				return p;
			}
		}
		return null;
	}
	
	
	
	public static void getWorlds(Collection<World> out, Collection<String> comp) {
		Iterator<World> it = Craft.getWorlds().iterator();
		while(it.hasNext()) {
			World p = it.next();
			String nam = p.getName();
			Iterator<String> req = comp.iterator();
			while(req.hasNext()) {
				String c = req.next();
				if(c.equals(nam)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	public static void getWorldsIgnoreCase(Collection<World> out, Collection<String> comp) {
		Iterator<World> it = Craft.getWorlds().iterator();
		while(it.hasNext()) {
			World p = it.next();
			String nam = p.getName();
			Iterator<String> req = comp.iterator();
			while(req.hasNext()) {
				String c = req.next();
				if(c.equalsIgnoreCase(nam)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	public static void getWorldsUUID(Collection<World> out, Collection<String> comp) {
		Collection<World> worlds = getWorlds();
		UUID id = null;
		Iterator<String> req = comp.iterator();
		while(req.hasNext()) {
			try {
				id = UUID.fromString(req.next());
			} catch(IllegalArgumentException ign) {
				continue;
			}
			for(World p : worlds) {
				if(p == null) continue;
				UUID cid = p.getUID();
				if(cid.equals(id)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	
	
	
	/*** PLUGIN ***/
	
	public static Plugin getPlugin(String name) {
		Iterator<Plugin> it = Craft.getPlugins().iterator();
		while(it.hasNext()) {
			Plugin p = it.next();
			if(p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	public static Plugin getPluginIgnoreCase(String name) {
		Iterator<Plugin> it = Craft.getPlugins().iterator();
		while(it.hasNext()) {
			Plugin p = it.next();
			if(p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}
	
	
	
	public static void getPlugins(Collection<Plugin> out, Collection<String> comp) {
		Iterator<Plugin> it = Craft.getPlugins().iterator();
		while(it.hasNext()) {
			Plugin p = it.next();
			String nam = p.getName();
			Iterator<String> req = comp.iterator();
			while(req.hasNext()) {
				String c = req.next();
				if(c.equals(nam)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	public static void getPluginsIgnoreCase(Collection<Plugin> out, Collection<String> comp) {
		Iterator<Plugin> it = Craft.getPlugins().iterator();
		while(it.hasNext()) {
			Plugin p = it.next();
			String nam = p.getName();
			Iterator<String> req = comp.iterator();
			while(req.hasNext()) {
				String c = req.next();
				if(c.equalsIgnoreCase(nam)) {
					out.add(p);
					req.remove();
					break;
				}
			}
		}
	}
	
	/***************
	 * Placeholders
	 */

	protected static Object[] _replaceColors = null;
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	public static String colorize(String input) {
		if(input == null) return null;
		if(_replaceColors == null) {
			ChatColor[] colors = ChatColor.values();
			_replaceColors = new Object[colors.length*4]; //&code:replace + {name}:replace
			int i, arrayIndex = 0, l = colors.length;
			for(i = 0; i < l; i++) {
				ChatColor col = colors[i];
				String colStr = col.toString();
				_replaceColors[arrayIndex++] = "{"+col.name()+"}";
				_replaceColors[arrayIndex++] = colStr;
				_replaceColors[arrayIndex++] = "&"+col.getChar();
				_replaceColors[arrayIndex++] = colStr;
			}
		}
		return NiceStrings.multireplace(input, _replaceColors);
	}
	
	
	/**
	 * Replace placeholders
	 * @param sender Command sender to process. 
	 * 		If null, custom placeholders are ignored
	 * @param nev String to process. If null, null is returned
	 * @return Formated string with processed placeholders, 
	 * 		or null if null was passed
	 */
	public static String placeholder(CommandSender sender, String nev) {
		if(nev == null) return null;
		//nev = ChatColor.translateAlternateColorCodes('&', nev);
		if(Bukkit.getServer() != null)
			nev = NiceStrings.multireplace(nev, new Object[] {
				"{n}",                      "\n",
				"{server_ip}",              Bukkit.getIp(),
				"{server_port}",            Bukkit.getPort(),
				"{server_motd}",            Bukkit.getMotd(),
				"{server_maxonline}",       Bukkit.getMaxPlayers(),
				"{server_online}",          Bukkit.getOnlinePlayers().size(),
				"{server_worlds}",          Bukkit.getWorlds().size(),
				"{server_plugins}",         Bukkit.getPluginManager().getPlugins().length,
				"{server_date}",            new Date().toString(),
		});
		
		nev = colorize(nev);
		
		//int befend = 0;
		//Designed to have max. one {server_date{...}} for performance, but definitely can handle more
		/*while(true) {
			int start = nev.indexOf("{server_date{", befend);
			if(start == -1) {
				break;
			}
			int end = nev.indexOf("}}", start + 13);
			if(end == -1) {
				break;
			}
			if(end == start + 13) {
				nev = nev.substring(0, start) + nev.substring(end + 2, nev.length());
				continue;
			}
			String format = Nice.dateFormat(nev.substring(start+13, end));
			nev = nev.substring(0, start) + format + nev.substring(end + 2, nev.length());
			befend = start + format.length();
		}*/
		if(sender != null) {
			nev = NiceStrings.multireplace(nev, new Object[] {
					"{player_name}",            sender.getName(),
					"{player_op}",              sender.isOp(),
			});
			if(sender instanceof Player) {
				Player p = (Player) sender;
				try {
					nev = me.clip.placeholderapi.
							PlaceholderAPI.setBracketPlaceholders(p, nev);
				} catch(Throwable t) {
					nev = NiceStrings.multireplace(nev, new Object[] {
							"{player_displayname}",     p.getDisplayName(),
							"{player_health}",          p.getHealth(),
							"{player_xp}",              p.getExp(),
							"{player_xptolevel}",       p.getExpToLevel(),
							"{player_xplevel}",         p.getLevel(),
							"{player_uuid}",            p.getUniqueId(),
							"{player_ip}",              p.getAddress().getAddress().getHostAddress(),
							"{player_host}",            p.getAddress().getAddress().getHostName()
					});
				}
			}
		}
		return nev;
	}
	
}
