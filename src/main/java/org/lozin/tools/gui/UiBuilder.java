package org.lozin.tools.gui;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.enumrator.UiType;

import java.util.Map;
import java.util.Set;

@Data
public class UiBuilder {
	private Inventory inventory;
	private Player player;
	private UiType uiType = UiType.DEFAULT;
	private JavaPlugin plugin;
	private Set<Integer> registeredSlots;
	public void build() {
		build(null);
	}
	public void build(Map<Integer, ItemStack> mapper){
		if (inventory == null) return;
		UiObject uiObject = new UiObject();
		if (mapper == null) mapper = ImmutableMap.of();
		uiObject.insertItem(inventory, mapper);
		registeredSlots = uiObject.getRegisteredSlots();
		player.openInventory(inventory);
		UiCache.builders.put(player, this);
	}
	public UiBuilder(Player player, Inventory inventory, UiType uiType, JavaPlugin plugin) {
		this.player = player;
		this.uiType = uiType;
		this.inventory = inventory;
		this.plugin = plugin;
	}
	public UiBuilder(){}
}
