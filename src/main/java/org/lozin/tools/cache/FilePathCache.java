package org.lozin.tools.cache;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.lutiscore.MAIN;
import org.lozin.tools.dependencies.DependencyService;
import org.lozin.tools.file.FileService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FilePathCache {
	public static Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();
	public static Map<String, Map<FileService.Entry, FileService.Entry>> entryCache = new ConcurrentHashMap<>();
	public static void init(JavaPlugin plugin) throws IOException {
		if (!cache.containsKey(plugin.getName())) {
			cache.put(plugin.getName(), FileService.mappedFiles(plugin));
		}
	}
	public static void trash() {
		cache.clear();
	}
	public static boolean reload(){
		trash();
		try {
			for (Plugin plugin : DependencyService.getDependencies(MAIN.instance)) {
				init((JavaPlugin) plugin);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	public static boolean reload(Plugin plugin){
		cache.remove(plugin.getName());
		try {
			init((JavaPlugin) plugin);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
