package org.lozin.tools.gui;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.string.ArraysHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class UiBuilder {
	private Inventory inventory;
	private Player player;
	private UiType uiType = UiType.DEFAULT;
	private JavaPlugin plugin;
	private Set<Integer> registeredSlots;
	private Map<Player, Integer> pager = new HashMap<>();
	private String folder;
	private Map<Integer, List<ItemStack>> mapper = new WeakHashMap<>();
	private Integer maxPage = 1;
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
		pager.put(player, 1);
	}
	public UiBuilder(){}
	public boolean handlerPageCalc(String folder) throws IOException {
		List<ItemStack> items = UiObject.pathToItems(plugin, folder);
		if (items == null || items.isEmpty()) {
			Bukkit.getLogger().warning("未找到物品 位于: " + folder);
			return false;
		}
		AtomicInteger page = new AtomicInteger(0);
		AtomicInteger index = new AtomicInteger(0);
		int restSize = inventory.getSize() - registeredSlots.size();
		if (restSize > 0) {
			List<ItemStack> subList;
			while (index.get() < items.size() - 1) {
				page.incrementAndGet();
				subList = items.subList(index.get(), Math.min(index.get() + restSize, items.size()));
				mapper.put(page.get(), subList);
				index.addAndGet(restSize);
			}
		}
		maxPage = page.get();
		return true;
	}
	public void putObjects(String folder) throws IOException {
		if (!handlerPageCalc(folder)) return;
		for (ItemStack item : mapper.get(pager.get(player))) {
			inventory.addItem(item);
		}
	}
	public void basicWindow() throws IOException {
		UiObject o = new UiObject();
		build(ImmutableMap.of(
				ArraysHandler.getList("0-8", "45-53"), o.getDECORATION(),
				Collections.singletonList(46), o.getPRE_PAGE(),
				Collections.singletonList(52), o.getNEXT_PAGE(),
				Collections.singletonList(0), o.getPREVIOUS_OBJECT_BUTTON(),
				Collections.singletonList(48), o.getCREATE_FOLDER_BUTTON(),
				Collections.singletonList(50), o.getCREATE_FILE_BUTTON()
		));
	}
	public void fleshPreviousObjectButton(String path, int slot) {
		ItemStack item = UiObject.fleshPreviousObjectButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshCreateFolderButton(String path, int slot) {
		ItemStack item = UiObject.formCreateFolderButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshPreviousPageButton(String path, int slot) {
		ItemStack item = UiObject.fleshPreviousPageButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshNextPageButton(String path, int slot) {
		ItemStack item = UiObject.fleshNextPageButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshPreviousObjectButton(String path){
		fleshPreviousObjectButton(path, 0);
	}
	public void fleshCreateFolderButton(String path){
		fleshCreateFolderButton(path, 48);
	}
	public void fleshNextPageButton(String path){
		fleshNextPageButton(path, 52);
	}
	public void fleshPreviousPageButton(String path){
		fleshPreviousPageButton(path, 46);
	}
	public void fleshAll(String path){
		fleshPreviousObjectButton(path);
		fleshCreateFolderButton(path);
		fleshNextPageButton(path);
		fleshPreviousPageButton(path);
	}
}
