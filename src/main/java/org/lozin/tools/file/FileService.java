package org.lozin.tools.file;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileService {
	public static List<String> getInferiorFiles(JavaPlugin plugin){
		File dataFolder = plugin.getDataFolder();
		List<String> filePaths = new ArrayList<>();
		try (Stream<Path> pathStream = Files.walk(dataFolder.toPath())) {
			pathStream.filter(Files::isRegularFile)
					.map(dataFolder.toPath()::relativize) // 转换为插件目录的相对路径
					.map(p -> p.toString().replace("\\", "/")) // 强制统一为UNIX路径
					.forEach(filePaths::add);
		} catch (IOException e) {
			plugin.getLogger().warning("无法获取插件文件列表：" + e.getMessage());
		}
		return filePaths;
	}
	public static void createFile(JavaPlugin plugin, String path){
		File file = new File(plugin.getDataFolder(), path);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().warning("无法创建文件：" + e.getMessage());
			}
		}
	}
}
