package org.lozin.tools.yaml;

import lombok.Getter;
import lombok.Setter;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.cache.MapperKey;
import org.lozin.tools.enumrator.MapperType;

import java.io.IOException;
import java.util.Map;
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
		Cache.mapper.put(new MapperKey(yamlFactory.getPlugin(), MapperType.YamlCache, yamlFactory.getPathInFolder()), mapper);
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
	
	public Object get(String param) {
		String[] params = param.split("\\.");
		for (Map.Entry<String, Object> entry : mapper.entrySet()) {
			if (entry.getKey().equals(params[0])) {
				if (params.length == 1) {
					return entry.getValue();
				} else {
					Map<String, Object> map = (Map<String, Object>) entry.getValue();
					for (int i = 1; i < params.length; i++) {
						if (map.containsKey(params[i])) {
							map = (Map<String, Object>) map.get(params[i]);
						} else {
							return null;
						}
					}
					return map;
				}
			}
		}
		return null;
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
}
