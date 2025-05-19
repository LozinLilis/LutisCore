package org.lozin.tools.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.cache.FilePathCache;
import org.lozin.tools.gui.UiCache;
import org.lozin.tools.string.ConsoleFix;
import org.lozin.tools.string.MessageSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DependencyService {
	protected static final Map<Plugin, List<Integer>> tasks = new ConcurrentHashMap<>();
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
		Bukkit.getScheduler().runTask(plugin, () -> {
			Cache.mapper.clear();
			UiCache.trash();
			FilePathCache.trash();
		});
		for (Plugin p : getDependencies(plugin)) {
			try {
				Cache.init((JavaPlugin) p);
				FilePathCache.init((JavaPlugin) p);
				p.saveConfig();
				p.reloadConfig();
				ConsoleFix.log(plugin, "&a√ &7已重载 &e" + p.getName());
				if (sender instanceof Player && sender.isOp()) MessageSender.sendColorizedMessage(sender,"&a√ &7已重载 &e" + p.getName());
			}catch (Exception e) {
				ConsoleFix.log(plugin, "&c× &7重载 &e" + p.getName() + " &7失败 &b" + e.getMessage());
				if (sender instanceof Player && sender.isOp()) MessageSender.sendColorizedMessage(sender,"&c× &7重载 &e" + p.getName() + " &7失败");
			}
		}
	}
	public static void reload(Plugin plugin) throws IOException {
		reload(null, plugin);
	}
	public static void registerTask(JavaPlugin plugin, BukkitTask task) {
		if (!tasks.containsKey(plugin)) tasks.put(plugin, new ArrayList<>(task.getTaskId()));
		tasks.get(plugin).add(task.getTaskId());
	}
	public static void unregisterTask(JavaPlugin plugin, Integer taskId) {
		if (tasks.containsKey(plugin) && tasks.get(plugin).contains(taskId)) {
			plugin.getServer().getScheduler().cancelTask(taskId);
			tasks.get(plugin).remove(taskId);
		}
	}
	public static void unregisterAllTask() {
		tasks.clear();
	}
}
