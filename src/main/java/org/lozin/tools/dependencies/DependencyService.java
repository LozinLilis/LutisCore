package org.lozin.tools.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.cache.Cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DependencyService {
	public static List<Plugin> getDependencies(Plugin plugin) {
		List<Plugin> inferior = new ArrayList<>();
		String pluginName = plugin.getName();
		for (Plugin p : Bukkit.getServer().getPluginManager().getPlugins()) {
			if (p.getName().equals(pluginName)) inferior.add(p);
			if (p.getDescription().getDepend().contains(pluginName)) {
				inferior.add(p);
			}
		}
		return inferior;
	}
	public static void reload(CommandSender sender, Plugin plugin) throws IOException {
		for (Plugin p : getDependencies(plugin)) {
			Cache.mapper.clear();
			Cache.init((JavaPlugin) p);
			p.saveConfig();
			p.reloadConfig();
			if (sender != null){
				sender.sendMessage("§7已重载 §e" + p.getName());
			}
		}
	}
	public static void reload(Plugin plugin) throws IOException {
		reload(null, plugin);
	}
}
