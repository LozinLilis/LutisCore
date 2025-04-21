package org.lozin.tools.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.lutiscore.MAIN;
import org.lozin.tools.cache.Cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DependencyService {
	public static List<Plugin> getDependencies() {
		List<Plugin> inferior = new ArrayList<>();
		String pluginName = MAIN.instance.getName();
		for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
			if (plugin.getName().equals(pluginName)) inferior.add(plugin);
			if (plugin.getDescription().getDepend().contains(pluginName)) {
				inferior.add(plugin);
			}
		}
		return inferior;
	}
	public static void reload(CommandSender sender) throws IOException {
		for (Plugin plugin : getDependencies()) {
			Cache.mapper.clear();
			Cache.init((JavaPlugin) plugin);
			plugin.saveConfig();
			plugin.reloadConfig();
			if (sender != null){
				sender.sendMessage("§7已重载 §e" + plugin.getName());
			}
		}
	}
	public static void reload() throws IOException {
		reload(null);
	}
}
