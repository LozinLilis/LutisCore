package org.lozin.tools.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonService {
	public static String formatMapToJson(Map<String, Object> map) {
		StringBuilder sb = new StringBuilder();
		formatMap(map, sb, 1);
		return sb.toString();
	}
	
	private static void formatMap(Map<String, Object> map, StringBuilder sb, int indentLevel) {
		Set<Map.Entry<String, Object>> entries = map.entrySet();
		
		sb.append("{\n");
		for (Map.Entry<String, Object> entry : entries) {
			appendIndent(sb, indentLevel + 1);
			sb.append("\"").append(escape(entry.getKey())).append("\": ");
			
			Object value = entry.getValue();
			if (value instanceof Map) {
				formatMap((Map<String, Object>) value, sb, indentLevel + 1);
			}
			else if (value instanceof List){
				sb.append("[\n");
				formatMap((Map<String, Object>) value, sb, indentLevel + 1);
				sb.append("\n]");
			}
			else {
				sb.append(value);
			}
			
			sb.append(",\n");
		}
		removeLastComma(sb);
		appendIndent(sb, indentLevel);
		sb.append("}");
	}
	
	private static void appendIndent(StringBuilder sb, int indentLevel) {
		for (int i = 0; i < indentLevel; i++) {
			sb.append("  ");
		}
	}
	
	private static void removeLastComma(StringBuilder sb) {
		int length = sb.length();
		if (length > 0 && sb.charAt(length - 1) == ',') {
			sb.deleteCharAt(length - 1);
		}
	}
	
	private static String escape(String str) {
		return str.replace("\"", "\\\"")
				       .replace("\n", "\\n")
				       .replace("\t", "\\t");
	}
}
