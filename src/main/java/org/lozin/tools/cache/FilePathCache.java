package org.lozin.tools.cache;

import org.bukkit.plugin.java.JavaPlugin;
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
}
