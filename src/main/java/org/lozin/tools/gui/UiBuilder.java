package org.lozin.tools.gui;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.string.ArraysHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class UiBuilder {
	private Inventory inventory;
	private Player player;
	private UiType uiType = UiType.DEFAULT;
	private JavaPlugin plugin;
	private Set<Integer> registeredSlots;
	public void build() throws IOException {
		build(null);
	}
	public void build(Map<List<Integer>, ItemStack> mapper) throws IOException {
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
	public void putObjects(String folder) throws IOException {
		List<ItemStack> items = UiObject.pathToItems(plugin, folder);
		if (items == null || items.isEmpty()) return;
		for (ItemStack item : items) {
			inventory.addItem(item);
		}
	}
	public void basicWindow() throws IOException {
		UiObject o = new UiObject();
		build(ImmutableMap.of(
				ArraysHandler.getList("0-8", "45-53"), o.getDECORATION(),
				Collections.singletonList(46), o.getPRE_PAGE(),
				Collections.singletonList(52), o.getNEXT_PAGE(),
				Collections.singletonList(0), o.getPREVIOUS_OBJECT_BUTTON()
		));
	}
}
