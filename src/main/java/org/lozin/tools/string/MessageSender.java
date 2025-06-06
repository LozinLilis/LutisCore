package org.lozin.tools.string;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.cache.MapperKey;
import org.lozin.tools.enumrator.MapperType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageSender{
	public static void sendCachedMessage(CommandSender sender, Map<MapperKey, Object> mapper,
	                                     JavaPlugin plugin, String mapperType, List<String> paths) {
		
		MapperType t = MapperType.getByName(mapperType);
		if (t == null) {
			sender.sendMessage("§7Type §c§l" + mapperType + "§7 not found!");
			return;
		}
		
		Map<String, Object> content = new LinkedHashMap<>();
		
		mapper.entrySet().stream()
				.filter(entry -> entry.getKey().getPlugin() == plugin && entry.getKey().getMapperType() == t && (paths.isEmpty() || paths.contains(entry.getKey().getPath()))) // 直接匹配路径列表
				.forEach(entry -> content.put(entry.getKey().getPath(), entry.getValue()));
		
		if (content.isEmpty()) {
			sender.sendMessage("§7No data found for the specified paths");
			return;
		}
		
		sender.sendMessage("§6§l" + mapperType + "§7 in §e§l" + plugin.getName());
		for (Map.Entry<String, Object> entry : content.entrySet()) {
			sender.sendMessage("\n§f[ §e§l" + entry.getKey() + "§f ]");
			sender.sendMessage(colorizeYaml(toYaml((Map<?, ?>) entry.getValue())));
		}
	}
	public static String toYaml(Map<?, ?> data) {
		DumperOptions options = new DumperOptions();
		options.setIndent(4);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		return new Yaml(options).dump(data);
	}
	public static String colorizeYaml(String rawYaml) {
		return rawYaml.replaceAll("(\\s*[a-zA-Z0-9_]+)(:)", "§f$1§r$2§7")
				       .replaceAll("§7\\s*\n", "§r\n")
				       .replaceAll("§7(\\s+)(- )", "§r$1§7$2");
	}
	public static void sendColorizedMessage(CommandSender sender, String... message) {
		Arrays.stream(message).collect(Collectors.toList()).forEach(s -> sender.sendMessage(s.replaceAll("&", "§")));
	}
}
