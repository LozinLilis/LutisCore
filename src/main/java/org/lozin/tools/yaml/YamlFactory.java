package org.lozin.tools.yaml;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Getter
@Setter
public class YamlFactory {
	private Map<String, Object> mapper;
	private File yamlFile;
	private JavaPlugin plugin;
	private String pathInFolder;
	private Yaml yaml;
	
	public YamlFactory(JavaPlugin plugin, String pathInFolder) throws IOException {
		Yaml yaml = new Yaml();
		this.plugin = plugin;
		this.pathInFolder = pathInFolder;
		this.yamlFile = new File(plugin.getDataFolder(), pathInFolder);
		if (!yamlFile.exists()) {
			yamlFile.getParentFile().mkdirs();
			yamlFile.createNewFile();
		}
		try (FileInputStream inputStream = new FileInputStream(yamlFile)) {
			this.mapper = yaml.load(inputStream);
			this.yaml = yaml;
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void flush(Map<String, Object> data) throws IOException {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setIndent(2);
		
		try (FileWriter writer = new FileWriter(yamlFile)) {
			new Yaml(options).dump(data, writer);
		}
	}
}