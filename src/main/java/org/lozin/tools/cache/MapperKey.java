package org.lozin.tools.cache;

import lombok.Data;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.enumrator.MapperType;

@Data
public class MapperKey {
	private MapperType mapperType;
	private JavaPlugin plugin;
	private String path;
	
	public MapperKey(JavaPlugin plugin, MapperType mapperType, String path) {
		this.mapperType = mapperType;
		this.plugin = plugin;
		this.path = path;
	}
}
