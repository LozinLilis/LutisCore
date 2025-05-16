package org.lozin.tools.gui;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.item.ItemFactory;
import org.lozin.tools.string.ArraysHandler;
import org.lozin.tools.yaml.YamlService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	public void handlerPageCalc(YamlService service, String kether){
		if (service == null) return;
	}
	public void putObjects(String file, String ke) {
		List<ItemStack> items = virtualObject(file, ke);
		if (items == null || items.isEmpty()) return;
		for (ItemStack item : items) {
			inventory.addItem(item);
		}
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
	public void fileControllerWindow() throws IOException {
		UiObject o = new UiObject();
		ItemStack bar = o.getDECORATION().clone();
		bar.setType(Material.LIME_STAINED_GLASS_PANE);
		build(ImmutableMap.of(
				ArraysHandler.getList("0-8", "45-53","9","18","27","36","17","26","35","44"), o.getDECORATION(),
				Arrays.asList(11, 20, 29, 38), bar,
				Collections.singletonList(19), o.getSTATUS_SWITCH_BUTTON(),
				Collections.singletonList(28), o.getADD_OBJECT_BUTTON(),
				Collections.singletonList(37), o.getADD_LIST_BUTTON(),
				Collections.singletonList(49), o.getPRE_PAGE(),
				Collections.singletonList(51), o.getNEXT_PAGE(),
				Collections.singletonList(10), o.getPREVIOUS_OBJECT_BUTTON()
		));
	}
	public void fleshPreviousObjectButton(String path, int slot) {
		ItemStack item = UiObject.fleshPreviousObjectButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshPreviousObjectButton(String path, String ke, int slot){
		ItemStack item = UiObject.fleshPreviousObjectButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshCreateFolderButton(String path, int slot) {
		ItemStack item = UiObject.formCreateFolderButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshCreateFileButton(String path, int slot) {
		ItemStack item = UiObject.formCreateFileButton(path);
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
	public void fleshPreviousPageButton(String path, String ke, int slot){
		ItemStack item = UiObject.fleshPreviousPageButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshNextPageButton(String path, String ke, int slot){
		ItemStack item = UiObject.fleshNextPageButton(path, ke);
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
	public void fleshCreateFileButton(String path){
		fleshCreateFileButton(path, 50);
	}
	public void fleshAll(String path){
		fleshPreviousObjectButton(path);
		fleshCreateFolderButton(path);
		fleshCreateFileButton(path);
		fleshNextPageButton(path);
		fleshPreviousPageButton(path);
	}
	public List<ItemStack> virtualObject(String file, String ke) {
		if  (file == null || ke == null) {
			return null;
		}
		Map<JavaPlugin, String> m = ImmutableMap.of(plugin, file);
		List<ItemStack> items = new ArrayList<>();
		if (Cache.yamlService.containsKey(m)) {
			YamlService service = Cache.yamlService.get(m);
			folder = file;
			if (ke.isEmpty()) service.getTopLevelMap().forEach((k, v) -> {
				items.add(createItem(folder, k, v));
			});
			else {
				Object valueObject = service.get(ke);
				traverseNode(ke, valueObject, items, service);
			}
		}
		System.out.println(items.stream().map(i -> Objects.requireNonNull(i.getItemMeta()).getDisplayName()).collect(Collectors.toSet()));
		return items;
	}
	private void traverseNode(String fullKether, Object rootObject, List<ItemStack> items, YamlService service){
		if (rootObject instanceof Map) {
			((Map<?, ?>) rootObject).forEach((k, v) -> {
				items.add(createItem(folder, fullKether + "." + k, v));
			});
		}
		else if (rootObject instanceof List) {
			IntStream.range(0, ((List<?>) rootObject).size()).forEach(index -> {
				Object value = ((List<?>) rootObject).get(index);
				items.add(createItem(folder, fullKether + "." + index, value));
			});
		}
		else {
			items.add(createItem(folder, fullKether, rootObject));
		}
	}
	
	private ItemStack createItem(String path, String fullKey, Object value) {
		int maxLength = 5;
		Material material = Material.BOOK;
		String key = fullKey.substring(fullKey.lastIndexOf(".") + 1);
		String displayName = "&7[对象] &f" + key + " = &e" + value.toString().substring(0, Math.min(value.toString().length(), maxLength)) + (value.toString().length() > maxLength ? "..." : "");
		
		if (value instanceof Map) {
			material = Material.CHEST;
			displayName = "&a[对象包] &f" + key;
		} else if (value instanceof List) {
			material = Material.BOOKSHELF;
			displayName = "&e[列表] &f" + key;
		}
		
		ItemFactory factory = new ItemFactory(material, displayName, null, 1, null, null)
				       .setKether(fullKey)
				       .setCompound(UiObject.PATH_KEY, path);
		if (value instanceof Map) {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.OPEN_COMPOUND);
		} else if (value instanceof List) {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.OPEN_COMPOUND);
		} else {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.EDIT_OBJECT);
			factory.setLore("&f>> &e"+ value).formattedLore(40);
		}
		return factory.build();
	}
	public ItemStack switchStatusOfStatusButton(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return null;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(item);
		if (itemFactory.notValid()) return null;
		if (itemFactory.getAction() == null) return null;
		ItemStack newItem;
		if (itemFactory.getAction().equals(UiObject.Actions.Status.EDIT.toString())) {
			itemFactory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.Status.DELETE);
			itemFactory.setType(Material.TNT_MINECART);
			itemFactory.setName("&c&l删除状态");
			newItem = itemFactory.build();
		} else if (itemFactory.getAction().equals(UiObject.Actions.Status.DELETE.toString())){
			itemFactory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.Status.EDIT);
			itemFactory.setType(Material.MINECART);
			itemFactory.setName("&a&l编辑状态");
			newItem = itemFactory.build();
		}else return null;
		return newItem;
	}
	public void reloadStatusButton(ItemStack item, Integer slot){
		if (item == null || item.getType() == Material.AIR) return;
		if (slot == null) slot = 19;
		ItemStack newItem = switchStatusOfStatusButton(item);
		if (newItem == null) newItem = item.clone();
		inventory.setItem(slot, newItem);
	}
	public void renderKetherInfo(String kether, Integer slot) {
		ItemStack item = UiObject.fullKetherItem(kether);
		if (item == null || item.getType() == Material.AIR) return;
		if (slot == null) slot = 0;
		inventory.setItem(slot, item);
	}
}
