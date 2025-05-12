package org.lozin.tools.file;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
	public static void createFile(JavaPlugin plugin, String folder, String name) throws IOException {
		File file = new File(plugin.getDataFolder(), folder + "/" + name);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
	}
	public static void createFolder(JavaPlugin plugin, String folder, String name){
		File f = new File(plugin.getDataFolder(), folder + "/" + name);
		if (!f.exists()) {
			f.mkdirs();
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
	public static List<String> getInferiorSurface(JavaPlugin plugin, String path) throws IOException {
		File dataFolder = plugin.getDataFolder();
		Path dataFolderPath = dataFolder.toPath();
		File folder = new File(dataFolder, path);
		File[] files = folder.listFiles();
		if (files == null) {
			Bukkit.getLogger().warning("无法获取插件文件列表：" + path);
			return null;
		}
		List<String> filePaths = new ArrayList<>();
		for (File file : files) {
			Path relativePath = dataFolderPath.relativize(file.toPath());
			String filePath = relativePath.toString().replace("\\", "/");
			filePaths.add(filePath);
		}
		System.out.println(filePaths);
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
	@Getter
	@Setter
	public static abstract class Entry {
		protected final File parent;
		protected String realPath;
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
		//private final List<Entry> children = Collections.synchronizedList(new ArrayList<>());
		public FolderEntry(File parent) {
			super(parent);
		}
	}
	public static File getParent(JavaPlugin plugin, String path){
		File dataFolder = plugin.getDataFolder();
		return new File(dataFolder, path).getParentFile();
	}
	public static String getThisPath(String relativePath){
		relativePath = relativePath.replace("\\", "/");
		return relativePath.substring(relativePath.lastIndexOf("/") + 1);
	}
	public static String optimizePath(String path){
		return path.replace("\\", "/");
	}
	public static String filterRootPath(JavaPlugin plugin, String origin){
		return optimizePath(origin).replace(getRootFolder(plugin), "");
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
					fileEntry.setRealPath(relativePath);
					String finalPath = getThisPath(relativePath);
					entries.put(finalPath, fileEntry);
  				}
				else if (Files.isDirectory(path)) {
					String relativePath = dataFolder.toPath().relativize(path).toString().replace("\\", "/");
					FolderEntry folderEntry = new FolderEntry(getParent(plugin, relativePath));
					folderEntry.setRealPath(relativePath);
					String finalPath = getThisPath(relativePath);
					entries.put(finalPath, folderEntry);
				}
			});
		}
		return entries;
	}
	public static String getRootFolder(JavaPlugin plugin){
		return plugin.getDataFolder().getPath().replace("\\", "/");
	}
}
