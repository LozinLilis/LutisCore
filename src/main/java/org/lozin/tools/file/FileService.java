package org.lozin.tools.file;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
	public static List<String> getInferiorFiles(JavaPlugin plugin, String path) throws IOException {
		File dataFolder = plugin.getDataFolder();
		List<String> filePaths = new ArrayList<>();
		try (Stream<Path> pathStream = Files.walk(dataFolder.toPath().resolve(path))) {
			pathStream.filter(Files::isRegularFile)
					.map(dataFolder.toPath()::relativize) // 转换为插件目录的相对路径
					.map(p -> p.toString().replace("\\", "/")) // 强制统一为UNIX路径
					.forEach(filePaths::add);
		}catch (IOException e) {
			plugin.getLogger().warning("无法获取插件文件列表：" + e.getMessage());
		}
		return filePaths;
	}
	//TODO 修理 NullPointerException
	public static List<String> getInferiorSurface(JavaPlugin plugin, String path) throws IOException {
		File dataFolder = plugin.getDataFolder();
		List<String> filePaths = new ArrayList<>();
		try (Stream<Path> pathStream = Files.walk(dataFolder.toPath().resolve(path), 1)) {
			pathStream.map(dataFolder.toPath()::relativize)
					.map(p -> p.toString().replace("\\", "/"))
					.forEach(filePaths::add);
		}
		return filePaths;
	}
	public static List<String> getInferiorSurface(JavaPlugin plugin) throws IOException {
		File dataFolder = plugin.getDataFolder();
		try (Stream<Path> pathStream = Files.walk(dataFolder.toPath(), 1)){
			return pathStream.map(dataFolder.toPath()::relativize)
					.map(p -> p.toString().replace("\\", "/"))
					.collect(Collectors.toList());
		}
	}
	public static abstract class Entry {
		protected final File parent;
		protected Entry(File parent) {
			this.parent = parent;
		}
	}
	@Getter
	public static class FileEntry extends Entry {
		public FileEntry(File parent) {
			super(parent);
		}
	}
	@Getter
	public static class FolderEntry extends Entry {
		private final List<Entry> children = Collections.synchronizedList(new ArrayList<>());
		public FolderEntry(File parent) {
			super(parent);
		}
	}
	public static File getParent(JavaPlugin plugin, String path){
		File dataFolder = plugin.getDataFolder();
		return new File(dataFolder, path).getParentFile();
	}
	public static String getThisPath(String relativePath){
		return relativePath.substring(relativePath.lastIndexOf("/") + 1);
	}
	public static Map<String, Object> mappedFiles(JavaPlugin plugin) throws IOException {
		File dataFolder = plugin.getDataFolder();
		Path dataFolderPath = dataFolder.toPath();
		Map<String, Object> entries = new java.util.concurrent.ConcurrentHashMap<>();
		try (Stream<Path> pathStream = Files.walk(dataFolder.toPath()).parallel()) {
			pathStream.forEach(path -> {
				if (path.equals(dataFolderPath)) return;
				if  (Files.isRegularFile(path)) {
					String relativePath = dataFolder.toPath().relativize(path).toString().replace("\\", "/");
					FileEntry fileEntry = new FileEntry(getParent(plugin, relativePath));
					String finalPath = getThisPath(relativePath);
					entries.put(finalPath, fileEntry);
  				}
				else if (Files.isDirectory(path)) {
					String relativePath = dataFolder.toPath().relativize(path).toString().replace("\\", "/");
					FolderEntry folderEntry = new FolderEntry(getParent(plugin, relativePath));
					String finalPath = getThisPath(relativePath);
					entries.put(finalPath, folderEntry);
				}
			});
		}
		return entries;
	}
}
