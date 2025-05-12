package org.lozin.lutiscore.coreHandlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.lozin.lutiscore.MAIN;
import org.lozin.tools.dependencies.DependencyService;
import org.lozin.tools.file.FileService;
import org.lozin.tools.string.JavaPluginParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Taber implements TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		if (command.getName().equals("lutis_core")){
			if (strings.length == 1){
				List<String> listA = Arrays.asList("reload");
				List<String> listB = DependencyService.getDependencies(MAIN.instance).stream().map(Plugin::getName).collect(Collectors.toList());
				return new ArrayList<String>(){{
					addAll(listA);
					addAll(listB);
				}};
			}
			else if (strings.length == 2){
				return Arrays.asList("write", "get", "cache", "edit");
			}
			else if (strings.length == 3 && !strings[1].equals("cache")){
				return FileService.getInferiorFiles(MAIN.instance);
			}
			else if (strings[1].equals("cache")){
				if (strings.length == 3){
					return Arrays.asList("yaml");
				}
				else if (strings.length >= 4){
					switch (strings[2]){
						case "yaml":
							return FileService.getInferiorFiles(JavaPluginParser.getPlugin(strings[0])).stream().filter(s1 -> s1.endsWith(".yml")).collect(Collectors.toList());
					}
				}
			}
			else{
				return Collections.emptyList();
			}
		}
		return Collections.emptyList();
	}
}
