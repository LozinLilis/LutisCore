package org.lozin.lutiscore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.lozin.lutiscore.coreHandlers.Commander;
import org.lozin.lutiscore.coreHandlers.Taber;
import org.lozin.lutiscore.coreHandlers.UiHandler;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.cache.FilePathCache;
import org.lozin.tools.dependencies.DependencyService;
import org.lozin.tools.gui.UiCache;
import org.lozin.tools.string.ConsoleFix;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class MAIN extends JavaPlugin {
	public static MAIN instance;
	public static Map<Map<Plugin, String>, BukkitTask> tasks;
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		saveResource("ex.yml", false);
		saveDefaultConfig();
		Objects.requireNonNull(Bukkit.getPluginCommand("lutis_core")).setExecutor(new Commander());
		Objects.requireNonNull(Bukkit.getPluginCommand("lutis_core")).setTabCompleter(new Taber());
		Bukkit.getPluginManager().registerEvents(new UiHandler(), this);
		instance = this;
		try {
			for (Plugin p : DependencyService.getDependencies(this)){
				Cache.init((JavaPlugin) p);
				FilePathCache.init((JavaPlugin) p);
				ConsoleFix.log(this, "&a√ &7已加载 &e" + p.getName());
			}
		} catch (IOException e) {
			Bukkit.getLogger().severe(e.getMessage());
		}
		DependencyService.registerTask(this, new BukkitRunnable() {
			@Override
			public void run() {
				try {
					DependencyService.reload(instance);
				} catch (IOException ignored) {
				}
			}
		}.runTaskTimerAsynchronously(instance, 20*60*5, 20*60*5));
	}
	
	@Override
	public void onDisable() {
		Cache.mapper.clear();
		FilePathCache.cache.clear();
		UiCache.trash();
		DependencyService.unregisterAllTask();
	}
}
