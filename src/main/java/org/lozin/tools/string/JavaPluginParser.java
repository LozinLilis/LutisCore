package org.lozin.tools.string;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class JavaPluginParser {
	public static JavaPlugin getPlugin(String pluginName) {
		return (JavaPlugin) Bukkit.getPluginManager().getPlugin(pluginName);
	}
}
