package org.lozin.lutiscore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.lutiscore.coreHandlers.Commander;
import org.lozin.lutiscore.coreHandlers.Taber;
import org.lozin.tools.cache.Cache;

import java.io.IOException;
import java.util.Objects;

public final class MAIN extends JavaPlugin {
	public static MAIN instance;
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		saveResource("ex.yml", false);
		Objects.requireNonNull(Bukkit.getPluginCommand("lutis_core")).setExecutor(new Commander());
		Objects.requireNonNull(Bukkit.getPluginCommand("lutis_core")).setTabCompleter(new Taber());
		instance = this;
		try {
			Cache.init(this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
