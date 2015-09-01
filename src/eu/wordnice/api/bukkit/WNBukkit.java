package eu.wordnice.api.bukkit;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

import eu.wordnice.api.Api;
import eu.wordnice.api.Array;
import eu.wordnice.api.Handler;

public class WNBukkit {
	
	/**
	 * Get all online players
	 * 
	 * @see Bukkit#getOnlinePlayers()
	 * 
	 * @return All online players
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Player> getPlayers() {
		try {
			Method m = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
			m.setAccessible(true);
			Object ret = m.invoke(null);
			if(ret instanceof Player[]) {
				return new Array<Player>((Player[]) ret);
			} else if(ret instanceof Collection<?>) {
				return (Collection<Player>) ret;
			}
		} catch(Throwable t) {
			throw new RuntimeException("Cannot get online players!", t);
		}
		throw new RuntimeException("Cannot get online players!");
	}
	
	/**
	 * Get player by name
	 * 
	 * @param name Name of player to found
	 * @param sensitive Match players case sensitive
	 * 
	 * @return If found, returns player. Otherwise `null`
	 */
	public static Player getPlayer(String name, boolean sensitive) {
		Iterator<Player> it = WNBukkit.getPlayers().iterator();
		if(sensitive) {
			while(it.hasNext()) {
				Player cur = it.next();
				if(cur.getName().equalsIgnoreCase(name)) {
					return cur;
				}
			}
		} else {
			while(it.hasNext()) {
				Player cur = it.next();
				if(cur.getName().equals(name)) {
					return cur;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get player by UUID
	 * 
	 * @param uid UUID of player to found
	 * @return If found, returns player. Otherwise `null`
	 */
	public static Player getPlayer(UUID uid) {
		Iterator<Player> it = WNBukkit.getPlayers().iterator();
		while(it.hasNext()) {
			Player cur = it.next();
			if(cur.getUniqueId().equals(uid)) {
				return cur;
			}
		}
		return null;
	}
	
	/**
	 * Get all loaded worlds
	 * 
	 * @see Bukkit#getWorlds()
	 * 
	 * @return All loaded worlds
	 */
	@SuppressWarnings("unchecked")
	public static Collection<World> getWorlds() {
		try {
			Method m = Bukkit.class.getDeclaredMethod("getWorlds");
			m.setAccessible(true);
			Object ret = m.invoke(null);
			if(ret instanceof World[]) {
				return new Array<World>((World[]) ret);
			} else if(ret instanceof Collection<?>) {
				return (Collection<World>) ret;
			}
		} catch(Throwable t) {
			throw new RuntimeException("Cannot get worlds!", t);
		}
		throw new RuntimeException("Cannot get worlds!");
	}
	
	/**
	 * Get world by name
	 * 
	 * @param name Name of world to found
	 * @param sensitive Match players case sensitive
	 * 
	 * @return If found, returns world. Otherwise `null`
	 */
	public static World getWorld(String name, boolean sensitive) {
		Iterator<World> it = WNBukkit.getWorlds().iterator();
		if(sensitive) {
			while(it.hasNext()) {
				World cur = it.next();
				if(cur.getName().equalsIgnoreCase(name)) {
					return cur;
				}
			}
		} else {
			while(it.hasNext()) {
				World cur = it.next();
				if(cur.getName().equals(name)) {
					return cur;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get world by UUID
	 * 
	 * @param uid UUID of world to found
	 * 
	 * @return If found, returns world. Otherwise `null`
	 */
	public static World getWorld(UUID uid) {
		Iterator<World> it = WNBukkit.getWorlds().iterator();
		while(it.hasNext()) {
			World cur = it.next();
			if(cur.getUID().equals(uid)) {
				return cur;
			}
		}
		return null;
	}
	
	/**
	 * Get all plugins
	 * 
	 * @see {@link org.bukkit.plugin.PluginManager#getPlugins()}
	 * 
	 * @return All loaded worlds
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Plugin> getPlugins() {
		try {
			Object inst = Bukkit.getPluginManager();
			Class<?> clz = inst.getClass();
			Object ret = null;
			while(clz != null) {
				try {
					Method m = clz.getDeclaredMethod("getPlugins");
					m.setAccessible(true);
					ret = m.invoke(inst);
					break;
				} catch(Throwable t2) {}
				clz = clz.getSuperclass();
			}
			if(ret instanceof Plugin[]) {
				return new Array<Plugin>((Plugin[]) ret);
			} else if(ret instanceof Collection<?>) {
				return (Collection<Plugin>) ret;
			}
		} catch(Throwable t) {
			throw new RuntimeException("Cannot get plugins!", t);
		}
		throw new RuntimeException("Cannot get plugins!");
	}
	
	
	
	
	
	/***************
	 * Placeholders
	 */
	
	/**
	 * Replace boolean placeholders with name `placeName` for given `in` string
	 * 
	 * @param in String to process
	 * @param placeName Placeholder name to find
	 * @param defNo Default no message
	 * @param defYes Default yes message
	 * @param get Handler to get value
	 * @param get_arg Additional argument to `get` handler
	 * 
	 * @return Formatted string
	 */
	public static String placeholderBoolean(String in, String placeName,
			String defNo, String defYes,
			Handler.OneHandler<Boolean, Object> get, Object get_arg) {
		placeName = "{" + placeName;
		
		int index = 0;
		//int status = -1;
		while((index = in.indexOf(placeName, index)) > -1) {
			//todo
		}
		return in;
	}
	
	/**
	 * Replace available placeholders for entered command sender 
	 * and call other available placeholder plugins
	 * 
	 * @param sender Command sender to process (may be null)
	 * @param nev String to process
	 * 
	 * @see {@link WNBukkit#placeholders(CommandSender, String, boolean)}
	 * @return Formated string with processed placeholders
	 */
	public static String placeholders(CommandSender sender, String nev) {
		return WNBukkit.placeholders(sender, nev, true);
	}
	
	/**
	 * Replace placeholders
	 * 
	 * @param sender Command sender to process (may be null)
	 * @param nev String to process
	 * @param place_api Call other available placeholder plugins
	 * 
	 * @return Formated string with processed placeholders
	 */
	public static String placeholders(CommandSender sender, String nev, boolean place_api) {
		nev = Api.replace(ChatColor.translateAlternateColorCodes('&', nev), new Object[] {
				"{n}",                      "\n",
				"{server_ip}",              Bukkit.getIp(),
				"{server_port}",            Bukkit.getPort(),
				"{server_motd}",            Bukkit.getMotd(),
				"{server_maxonline}",       Bukkit.getMaxPlayers(),
				"{server_online}",          WNBukkit.getPlayers().size(),
				"{server_worlds}",          WNBukkit.getWorlds().size(),
				"{server_plugins}",         WNBukkit.getPlugins().size(),
				"{server_date}",            new Date().toString(),
		}, true);
		
		ChatColor[] colors = ChatColor.values();
		int i = 0;
		for(; i < colors.length; i++) {
			ChatColor cur = colors[i];
			nev = Api.replace(nev, ("{" + cur.name() + "}"), cur.toString(), false);
		}
		
		int befend = 0;
		while(true) {
			int start = nev.indexOf("{server_date{", befend);
			if(start != -1) {
				int end = nev.indexOf("}}", start + 13);
				if(end != -1) {
					if(end == start + 13) {
						nev = nev.substring(0, start) + nev.substring(end + 2, nev.length());
						continue;
					}
					try {
						DateFormat tf = new SimpleDateFormat(nev.substring(start + 13, end));
						String formatdate = tf.format(new Date());
						befend = start + formatdate.length();
						nev = nev.substring(0, start) + formatdate + nev.substring(end + 2, nev.length());
					} catch(Throwable t) {
						t.printStackTrace();
						String formatdate = new Date().toString();
						befend = start + formatdate.length();
						nev = nev.substring(0, start) + formatdate + nev.substring(end + 2, nev.length());
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}
		if(sender != null) {
			nev = Api.replace(nev, new Object[] {
					"{player_name}",            sender.getName(),
					"{player_op}",              sender.isOp(),
			}, true);
			if(sender instanceof Player) {
				Player p = (Player) sender;
				nev = Api.replace(nev, new Object[] {
						"{player_displayname}",     p.getDisplayName(),
						"{player_health}",          p.getHealth(),
						"{player_xp}",              p.getExp(),
						"{player_xptolevel}",       p.getExpToLevel(),
						"{player_xplevel}",         p.getLevel(),
						"{player_ip}",              p.getAddress().getAddress().getHostAddress(),
						"{player_host}",            p.getAddress().getAddress().getHostName()
				}, true);
			}
		}
		if(place_api) {
			try {
				Player p = null;
				if(sender instanceof Player) {
					p = (Player) sender;
				}
				nev = me.clip.placeholderapi.PlaceholderAPI.setBracketPlaceholders(p, nev);
			} catch(Throwable t) {}
		}
		return nev;
	}
	
}
