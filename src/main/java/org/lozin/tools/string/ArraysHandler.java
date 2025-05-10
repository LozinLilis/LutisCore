package org.lozin.tools.string;

import java.util.ArrayList;
import java.util.List;

public class ArraysHandler {
	public static List<Integer> getList(String... regex) {
		List<Integer> list = new ArrayList<>();
		for (String s : regex) {
			if (s == null || s.isEmpty()) continue;
			if (s.matches("\\d+")) list.add(Integer.parseInt(s));
			else if (s.matches("\\d+-\\d+")) {
				String[] split = s.split("-");
				int start = Integer.parseInt(split[0]);
				int end = Integer.parseInt(split[1]);
				if (start > end) {
					int temp = start;
					start = end;
					end = temp;
				}
				for (int i = start; i <= end; i++) list.add(i);
			}
		}
		return list;
	}
}
