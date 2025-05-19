package org.lozin.tools.yaml;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.cache.MapperKey;
import org.lozin.tools.enumrator.MapperType;
import org.lozin.tools.string.ConsoleFix;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class YamlService {
	private Map<String, Object> mapper;
	private YamlFactory yamlFactory;
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+$");
	
	public YamlService(YamlFactory yamlFactory) {
		this.yamlFactory = yamlFactory;
		this.mapper = yamlFactory.getMapper();
		if (this.mapper == null) {
			this.mapper = new ConcurrentHashMap<>();
		}
		MapperKey key = MapperKey.get(yamlFactory.getPlugin(), MapperType.YamlCache, yamlFactory.getPathInFolder());
		Cache.mapper.put(key, mapper);
		Cache.yamlService.put(ImmutableMap.of(yamlFactory.getPlugin(), yamlFactory.getPathInFolder()), this);
	}
	public void getAll() {
		if (mapper == null) {
			System.out.println("YAML 数据未加载！");
			return;
		}
		for (Map.Entry<?, ?> entry : mapper.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
	public List<String> getTopLevelKeys() {
		return new ArrayList<>(mapper.keySet());
	}
	public Map<String, Object> getTopLevelMap() {
		Map<String, Object> map = new HashMap<>();
		for (Map.Entry<?, ?> entry : mapper.entrySet()) {
			map.put(entry.getKey().toString(), entry.getValue());
		}
		return map;
	}
	
	public Object get(String param) {
		if (yamlFactory.getYaml() == null) return null;
		String[] params = param.split("\\.");
		Object current = mapper;
		for (String key : params) {
			if (current instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) current;
				if (! map.containsKey(key)) return null;
				current = map.get(key);
			} else if (current instanceof List) {
				List<Object> list = (List<Object>) current;
				try {
					int index = Integer.parseInt(key);
					if (index < 0 || index >= list.size()) return null;
					current = list.get(index);
				} catch (NumberFormatException e) {
					return null; // Key 不是数字索引，无法处理 List
				}
			} else {
				return null; // 非 Map/List 类型直接返回
			}
		}
		return current;
	}
	// 获取 Map 类型节点
	public Map<String, Object> getMap(String param) {
		Object value = get(param, null);
		if (value == null) return null;
		return value instanceof Map ? (Map<String, Object>) value : new HashMap<String, Object>(){{
			put(value.toString(), null);
		}};
	}
	
	// 获取 List 类型节点
	public List<Object> getList(String param) {
		Object value = get(param, null);
		if (value == null) return null;
		return value instanceof List ? (List<Object>) value : Collections.singletonList(value);
	}
	public boolean write(String param, Object value) {
		if (param == null || param.isEmpty()) return false;
		
		String[] rawKeys = param.split("\\.", -1);
		List<String> processedKeys = new ArrayList<>();
		Map<String, Object> context = new HashMap<>();
		context.put("currentPath", mapper);
		
		//synchronized (Cache.mapper) {
			try {
				for (int i = 0; i < rawKeys.length; i++) {
					String key = rawKeys[i];
					Object currentNode = context.get("currentPath");
					
					// 列表尾部自动扩展检测
					if (currentNode instanceof List && !isListIndex(key)) {
						List<Object> list = (List<Object>) currentNode;
						int newIndex = list.size();
						ensureListSize(list, newIndex + 1);
						
						// 创建新元素容器
						Map<String, Object> newElement = new LinkedHashMap<>();
						list.set(newIndex, newElement);
						currentNode = newElement;
						
						// 更新路径上下文
						context.put("currentPath", currentNode);
						processedKeys.add(String.valueOf(newIndex));
					}
					
					if (i == rawKeys.length - 1) { // 最后一个键直接设置值
						if (currentNode instanceof Map) {
							if (value == null) ((Map<?, ?>) currentNode).remove(key);
							else ((Map<String, Object>) currentNode).put(key, value);
						} else if (currentNode instanceof List) {
							int index = Integer.parseInt(key);
							if (value == null) ((List<?>) currentNode).remove(index);
							else {
								ensureListSize((List<?>) currentNode, index + 1);
								((List<Object>) currentNode).set(index, value);
							}
						}
					} else { // 处理中间节点
						if (key.matches("\\d+")) { // 列表索引
							int index = Integer.parseInt(key);
							List<Object> list = (List<Object>) currentNode;
							ensureListSize(list, index + 1);
							Object item = list.get(index);
							
							if (item == null) {
								list.set(index, new LinkedHashMap<>());
								item = list.get(index);
							}
							
							if (!(item instanceof Map)) {
								throw new IllegalStateException("List element is not a map");
							}
							currentNode = item;
						} else { // 普通键
							Map<String, Object> map = (Map<String, Object>) currentNode;
							map.computeIfAbsent(key, k -> new LinkedHashMap<>());
							currentNode = map.get(key);
						}
						context.put("currentPath", currentNode);
						processedKeys.add(key);
					}
				}
				
				yamlFactory.flush(mapper);
				updateCache();
				return true;
			} catch (Exception e) {
				handleWriteError(param, e);
				return false;
			}
		//}
	}
	
	private boolean isListIndex(String key) {
		try {
			Integer.parseInt(key);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private void ensureListSize(List<?> list, int size) {
		while (list.size() < size) {
			list.add(null);
		}
	}
	
	private void handleWriteError(String path, Exception e) {
		String errorMsg = String.format("&cWrite error in path '%s': %s", path, e.getMessage());
		if (e instanceof IllegalArgumentException) {
			ConsoleFix.log(yamlFactory.getPlugin(), errorMsg);
		} else if (e instanceof IndexOutOfBoundsException) {
			ConsoleFix.log(yamlFactory.getPlugin(), "&c" + errorMsg);
		} else {
			ConsoleFix.log(yamlFactory.getPlugin(), "&cUnexpected error: " + e.getCause());
		}
	}
	
	private void updateCache() {
		JavaPlugin plugin = yamlFactory.getPlugin();
		String pathInFolder = yamlFactory.getPathInFolder();
		Cache.mapper.put(
				MapperKey.get(plugin, MapperType.YamlCache, pathInFolder),
				mapper
		);
	}
	public Object get(String param, Object defaultValue) {
		Object value = get(param);
		return value != null ? value : defaultValue;
	}
	public void delete(String param){
		write(param, null);
	}
	public String generateUniqueKeyName(String basePath, String baseName) {
		int counter = 0;
		String key = baseName;
		while (exists(basePath + key)) {
			Matcher matcher = NUMBER_PATTERN.matcher(key);
			if (matcher.find() && matcher.start() == key.length() - matcher.group().length()) {
				String prefix = key.substring(0, matcher.start());
				String numberPart = key.substring(matcher.start());
				try {
					counter = Integer.parseInt(numberPart) + 1;
				} catch (NumberFormatException e) {
					counter = 1;
				}
				key = prefix + counter;
			} else {
				key = baseName + counter;
				counter++;
			}
		}
		return key;
	}
	private boolean exists(String path) {
		return get(path) != null;
	}
	
	// 重构创建方法
	public boolean createMap(String folder) {
		return createNamedNode(folder, "new_map", new LinkedHashMap<>());
	}
	
	public boolean createList(String folder) {
		return createNamedNode(folder, "new_list", new LinkedList<>());
	}
	
	public boolean createObject(String folder) {
		return createNamedNode(folder, "new_object", "");
	}
	public boolean createData(String folder) {
		return createNamedNode(folder, null, "new_data");
	}
	
	private boolean createNamedNode(String folder, String keyName, Object defaultValue) {
		String basePath = folder.isEmpty() ? "" : folder + ".";
		String uniqueKey;
		if (keyName == null || keyName.isEmpty()) {
			Object value = get(folder);
			if (value instanceof List) {
				List<Object> list = getList(folder);
				int size = list.size();
				uniqueKey = Integer.toString(size);
			}
			else return false;
		}
		else uniqueKey = generateUniqueKeyName(basePath, keyName);
		return write(basePath + uniqueKey, defaultValue);
	}
}
