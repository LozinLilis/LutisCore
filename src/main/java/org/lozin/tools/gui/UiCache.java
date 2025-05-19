package org.lozin.tools.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class UiCache {
	protected static Map<Player, UiBuilder> builders = new java.util.HashMap<>();
	
	public static UiBuilder getBuilder(HumanEntity player) {
		Player p = (Player) player;
		return builders.getOrDefault(p, null);
	}
	public static void unregister(HumanEntity player) {
		Player p = (Player) player;
		builders.remove(p);
	}
	public static void trash(){
		for (Player p : builders.keySet()) {
			p.closeInventory();
		}
		builders.clear();
	}
}
