package org.lozin.tools.gui;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lozin.tools.item.ItemFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class UiObject {
	private static final String ACTION_KEY = "LutisCore.action";
	private final ItemStack ADD_OBJECT_BUTTON = new ItemFactory(Material.ICE, "&6&lObject", Arrays.asList("&7置入 &f<普通对象> &7类型值"), 1,null, null).build();
	private final ItemStack ADD_LIST_BUTTON = new ItemFactory(Material.CHEST, "&6&lList", Arrays.asList("&7置入 &f<列表> &7类型值"), 1,null, null).build();
	private final ItemStack INFERIOR_OBJECT_BUTTON = new ItemFactory(Material.ARROW, "&6&l返回上一层级", null, 1,null, null).setCompound(ACTION_KEY, "inferior").build();
	
	public void insertItem(Inventory inventory, Map<Integer, ItemStack> mapper) {
		if (mapper == null || mapper.isEmpty()) return;
		Set<Integer> slots = new HashSet<>();
		for (Integer slot : mapper.keySet()) {
			inventory.setItem(slot, mapper.get(slot));
			slots.add(slot);
		}
		registeredSlots = slots;
	}
	private Set<Integer> registeredSlots = new HashSet<>();
}
