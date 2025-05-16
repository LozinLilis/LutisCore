package org.lozin.lutiscore.coreHandlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.lutiscore.MAIN;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.dependencies.DependencyService;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.gui.UiBuilder;
import org.lozin.tools.gui.UiCache;
import org.lozin.tools.string.JavaPluginParser;
import org.lozin.tools.string.MessageSender;
import org.lozin.tools.yaml.YamlFactory;
import org.lozin.tools.yaml.YamlService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commander implements org.bukkit.command.CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
		if (command.getName().equals("lutis_core") && sender.isOp()){
			if (args.length == 1){
				if (args[0].equals("reload")){
					try {
						DependencyService.reload(sender, MAIN.instance);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			if (args.length >= 2 && args[1].equals("edit")){
				Player player = (Player) sender;
				UiBuilder builder = UiCache.getBuilder(player);
				JavaPlugin plugin = JavaPluginParser.getPlugin(args[0]);
				if (args.length == 2){
					if (builder == null) builder = new UiBuilder(
							player,
							Bukkit.createInventory(null, 54, "§0§l"+args[0]),
							UiType.READ_ONLY,
							plugin
					);
					try {
						builder.basicWindow();
						builder.putObjects("");
						builder.fleshAll("");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				}
			}
			if (args.length >= 3){
				if (args[1].equals("cache")){
					if (args.length == 3) MessageSender.sendCachedMessage(sender, Cache.mapper, JavaPluginParser.getPlugin(args[0]), args[2], Collections.emptyList());
					else {
						List<String> paths = new ArrayList<>(Arrays.asList(args).subList(3, args.length));
						MessageSender.sendCachedMessage(sender, Cache.mapper, JavaPluginParser.getPlugin(args[0]), args[2], paths);
					}
				}
				if (args.length == 4 && args[1].equals("get")){
					String pluginName = args[0];
					JavaPlugin p = (JavaPlugin) Bukkit.getPluginManager().getPlugin(pluginName);
					String filePath = args[2];
					String param = args[3];
					if (p != null){
						try {
							YamlFactory yamlFactory = new YamlFactory(p, filePath);
							YamlService yamlService = new YamlService(yamlFactory);
							Object value = yamlService.get(param);
							sender.sendMessage("§7获取到位于 §e" + pluginName + " §7的 §e" + filePath + " §7中的 §e" + param + " §7的值为 §e" + value);
						}catch (Exception e){
							sender.sendMessage("§c获取失败: " + e.getMessage());
							e.printStackTrace();
						}
					}else{
						sender.sendMessage("§c插件 §e"+ pluginName + "§c 不存在或未加载");
					}
					return true;
				}
				if (args.length == 5 && args[1].equals("write")){
					String pluginName = args[0];
					JavaPlugin p = (JavaPlugin) Bukkit.getPluginManager().getPlugin(pluginName);
					String filePath = args[2];
					String param = args[3];
					Object value = args[4];
					if (p != null){
						try {
							YamlFactory yamlFactory = new YamlFactory(p, filePath);
							YamlService yamlService = new YamlService(yamlFactory);
							yamlService.write(param, value);
							sender.sendMessage("§7成功将 §e" + value + " §7写入 §e" + pluginName + " §7的 §e" + filePath + " §7中的 §e" + param);
						}catch (Exception e){
							sender.sendMessage("§c写入失败: " + e.getMessage());
							e.printStackTrace();
						}
					}else{
						sender.sendMessage("§c插件 §e"+ pluginName + "§c 不存在或未加载");
					}
					return true;
				}
			}
		}
		return false;
	}
}
