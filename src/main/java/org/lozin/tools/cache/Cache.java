package org.lozin.tools.cache;

import lombok.Data;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.lutiscore.MAIN;
import org.lozin.tools.file.FileService;
import org.lozin.tools.yaml.YamlFactory;
import org.lozin.tools.yaml.YamlService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Cache {
	public static final Map<MapperKey, Object> mapper = new ConcurrentHashMap<>();
	public static Map<Map<JavaPlugin, String>, YamlService> yamlService = new ConcurrentHashMap<>();
	public static void init(JavaPlugin plugin) throws IOException {
		List<String> files = FileService.getInferiorFiles(plugin);
		for(String file : files){
			if(file.endsWith(".yml")){
				YamlFactory yamlFactory = new YamlFactory(plugin, file);
				new YamlService(yamlFactory);
				if (MAIN.instance.getConfig().getBoolean("debug.cache")) System.out.println(mapper);
			}
		}
		//ConsoleFix.log(plugin, mapper.entrySet().toString());
	}
}
