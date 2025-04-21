package org.lozin.tools.enumrator;

import lombok.Getter;

@Getter
public enum MapperType {
	YamlCache("yaml");
	
	private final String name;
	MapperType(String name) {
		this.name = name;
	}
	public static MapperType getByName(String name) {
		for (MapperType mapperType : MapperType.values()) {
			if (mapperType.getName().equals(name)) {
				return mapperType;
			}
		}
		return null;
	}
}
