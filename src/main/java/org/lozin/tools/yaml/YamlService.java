package org.lozin.tools.yaml;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.cache.MapperKey;
import org.lozin.tools.enumrator.MapperType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class YamlService {
	private Map<String, Object> mapper;
	private YamlFactory yamlFactory;
	
	public YamlService(YamlFactory yamlFactory) {
		this.yamlFactory = yamlFactory;
		this.mapper = yamlFactory.getMapper();
		if (this.mapper == null) {
			this.mapper = new ConcurrentHashMap<>();
		}
		MapperKey key = new MapperKey(yamlFactory.getPlugin(), MapperType.YamlCache, yamlFactory.getPathInFolder());
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
	public void write(String param, Object value) {
		if (param == null || param.isEmpty()) return;
		
		String[] keys = param.split("\\.", -1);
		Map<String, Object> current = mapper;
		
		synchronized (Cache.mapper) {
			try {
				for (int i = 0; i < keys.length - 1; i++) {
					String key = keys[i];
					current = (Map<String, Object>) current.compute(key, (k, v) -> {
						if (v != null && !(v instanceof Map)) {
							return new ConcurrentHashMap<>();
						}
						return v != null ? v : new ConcurrentHashMap<>();
					});
				}
				
				String finalKey = keys[keys.length - 1];
				if (value == null) {
					current.remove(finalKey);
				} else {
					current.put(finalKey, value);
				}
				yamlFactory.flush(mapper);
				Cache.mapper.put(new MapperKey(yamlFactory.getPlugin(), MapperType.YamlCache, yamlFactory.getPathInFolder()), mapper);
			} catch (ClassCastException e) {
				throw new IllegalStateException("Path structure conflict: " + e.getMessage());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public Object get(String param, Object defaultValue) {
		Object value = get(param);
		return value != null ? value : defaultValue;
	}
	
}
