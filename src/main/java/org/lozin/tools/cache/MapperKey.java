package org.lozin.tools.cache;

import lombok.Data;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.enumrator.MapperType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

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
	
	private static final LinkedHashSet<MapperKey> mapperKeys = new LinkedHashSet<>();
	public static void add(MapperKey mapperKey) {
		mapperKeys.add(mapperKey);
	}
	public static void remove(MapperKey mapperKey) {
		mapperKeys.remove(mapperKey);
	}
	public static MapperKey get(JavaPlugin plugin, MapperType mapperType, String path) {
		List<MapperKey> ma = mapperKeys.stream().filter(mapperKey -> mapperKey.getPlugin().equals(plugin) && mapperKey.getMapperType().equals(mapperType) && mapperKey.getPath().equals(path)).collect(Collectors.toList());
		if (!ma.isEmpty()) return ma.get(0);
		else {
			MapperKey mapperKey = new MapperKey(plugin, mapperType, path);
			mapperKeys.add(mapperKey);
			return mapperKey;
		}
	}
}
