package org.lozin.lutiscore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.lutiscore.coreHandlers.Commander;
import org.lozin.lutiscore.coreHandlers.Taber;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.dependencies.DependencyService;

import java.io.IOException;
import java.util.Objects;

public final class MAIN extends JavaPlugin {
	public static MAIN instance;
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		saveResource("ex.yml", false);
		saveDefaultConfig();
		Objects.requireNonNull(Bukkit.getPluginCommand("lutis_core")).setExecutor(new Commander());
		Objects.requireNonNull(Bukkit.getPluginCommand("lutis_core")).setTabCompleter(new Taber());
		instance = this;
		try {
			for (Plugin p : DependencyService.getDependencies(this)){
				System.out.println(p.getName());
				Cache.init((JavaPlugin) p);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
