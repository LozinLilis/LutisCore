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
	private ActionMode actionMode = ActionMode.EDIT_MODE;
	private Map<UiObject.Actions, List<Integer>> registeredSlotsAction = new HashMap<>();
	
	public UiBuilder(Player player, Inventory inventory, UiType uiType, JavaPlugin plugin) {
		this.player = player;
		this.uiType = uiType;
		this.inventory = inventory;
		this.plugin = plugin;
		pager.put(player, 1);
	}
	
	public UiBuilder() {
	}
	
	public void build() throws IOException {
		build(null);
	}
	
	public void build(Map<List<Integer>, ItemStack> mapper) {
		if (inventory == null) return;
		if (mapper == null) mapper = ImmutableMap.of();
		registeredSlots = UiObject.insertItem(inventory, mapper);
		player.openInventory(inventory);
		UiCache.builders.put(player, this);
	}
	
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
	
	public void handlerPageCalc(YamlService service, String kether) {
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
		if (! handlerPageCalc(folder)) return;
		for (ItemStack item : mapper.get(pager.get(player))) {
			inventory.addItem(item);
		}
	}
	
	public void basicWindow() throws IOException {
		UiObject o = new UiObject();
		Map<List<Integer>, ItemStack> mapper = ImmutableMap.of(
				ArraysHandler.getList("0-8", "45-53"), o.getDECORATION(),
				Collections.singletonList(46), o.getPRE_PAGE(),
				Collections.singletonList(52), o.getNEXT_PAGE(),
				Collections.singletonList(0), o.getPREVIOUS_OBJECT_BUTTON(),
				Collections.singletonList(48), o.getCREATE_FOLDER_BUTTON(),
				Collections.singletonList(8), loadStatusButton(o.getSTATUS_SWITCH_BUTTON()),
				Collections.singletonList(50), o.getCREATE_FILE_BUTTON()
		);
		loadRegSlotAction(mapper);
		build(mapper);
	}
	public void loadRegSlotAction(Map<List<Integer>, ItemStack> map) {
		registeredSlotsAction.clear();
		for (Map.Entry<List<Integer>, ItemStack> entry : map.entrySet()){
			ItemFactory item = new ItemFactory(entry.getValue());
			if (item.notValid()) continue;
			UiObject.Actions action = item.getAction() == null ? UiObject.Actions.NONE : (item.getAction(item.getAction()) == null ? UiObject.Actions.NONE : item.getAction(item.getAction()));
			registeredSlotsAction.put(action, entry.getKey());
		}
		//ConsoleFix.log(plugin, "&bRegistered slots: &a" + registeredSlotsAction.toString());
	}
	
	public void fileControllerWindow(){
		UiObject o = new UiObject();
		ItemStack bar = o.getDECORATION().clone();
		bar.setType(Material.LIME_STAINED_GLASS_PANE);
		Map<List<Integer>, ItemStack> map = new LinkedHashMap<>();
		try {
			map.put(ArraysHandler.getList("0-8", "45-53", "9", "18", "27", "36", "17", "26", "35", "44"), o.getDECORATION());
			map.put(Arrays.asList(11, 20, 29, 38), bar);
			map.put(Collections.singletonList(0), o.getINFO_BUTTON());
			map.put(Collections.singletonList(10), o.getADD_MAP_BUTTON());
			map.put(Collections.singletonList(19), o.getADD_LIST_BUTTON());
			map.put(Collections.singletonList(28), o.getADD_OBJECT_BUTTON());
			map.put(Collections.singletonList(37), o.getADD_DATA());
			map.put(Collections.singletonList(49), o.getPRE_PAGE());
			map.put(Collections.singletonList(51), o.getNEXT_PAGE());
			map.put(Collections.singletonList(9), o.getPREVIOUS_OBJECT_BUTTON());
			map.put(Collections.singletonList(8), loadStatusButton(o.getSTATUS_SWITCH_BUTTON()));
		}catch (Exception ignored){}
		loadRegSlotAction(map);
		build(map);
	}
	
	public void fleshAddDataButton(String path, String ke, int slot){
		ItemStack item = UiObject.fleshAddDataButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	
	public void fleshPreviousObjectButton(String path, int slot) {
		ItemStack item = UiObject.fleshPreviousObjectButton(path);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	
	public void fleshPreviousObjectButton(String path, String ke, int slot) {
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
	
	public void fleshPreviousPageButton(String path, String ke, int slot) {
		ItemStack item = UiObject.fleshPreviousPageButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	
	public void fleshNextPageButton(String path, String ke, int slot) {
		ItemStack item = UiObject.fleshNextPageButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshCreateMapButton(String path, String ke, int slot) {
		ItemStack item = UiObject.fleshCreateMapButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshCreateListButton(String path, String ke, int slot) {
		ItemStack item = UiObject.fleshCreateListButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	public void fleshCreateObjectButton(String path, String ke, int slot) {
		ItemStack item = UiObject.fleshCreateObjectButton(path, ke);
		if (item == null) return;
		inventory.setItem(slot, item);
	}
	
	public void fleshPreviousObjectButton(String path) {
		fleshPreviousObjectButton(path, 0);
	}
	
	public void fleshCreateFolderButton(String path) {
		fleshCreateFolderButton(path, 48);
	}
	
	public void fleshNextPageButton(String path) {
		fleshNextPageButton(path, 52);
	}
	
	public void fleshPreviousPageButton(String path) {
		fleshPreviousPageButton(path, 46);
	}
	
	public void fleshCreateFileButton(String path) {
		fleshCreateFileButton(path, 50);
	}
	public void fleshCreateMapButton(String path, String ke) {
		fleshCreateMapButton(path, ke, 19);
	}
	
	public void fleshAll(String path) {
		fleshPreviousObjectButton(path);
		fleshCreateFolderButton(path);
		fleshCreateFileButton(path);
		fleshNextPageButton(path);
		fleshPreviousPageButton(path);
	}
	
	public List<ItemStack> virtualObject(String file, String ke) {
		if (file == null || ke == null) {
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
		//System.out.println(items.stream().map(i -> Objects.requireNonNull(i.getItemMeta()).getDisplayName()).collect(Collectors.toSet()));
		return items;
	}
	
	private void traverseNode(String fullKether, Object rootObject, List<ItemStack> items, YamlService service) {
		if (rootObject instanceof Map) {
			((Map<?, ?>) rootObject).forEach((k, v) -> {
				items.add(createItem(folder, fullKether + "." + k, v));
			});
		} else if (rootObject instanceof List) {
			IntStream.range(0, ((List<?>) rootObject).size()).forEach(index -> {
				Object value = ((List<?>) rootObject).get(index);
				items.add(createItem(folder, fullKether + "." + index, value));
			});
		} else {
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
		else if (isDigit(key) && value instanceof String) {
			String str = (String) value;
			material = Material.PAPER;
			displayName = "&8[数据] &f" + str.substring(0, Math.min(str.length(), maxLength)) + (str.length() > maxLength ? "..." : "");
		}
		
		ItemFactory factory = new ItemFactory(material, displayName, null, 1, null, null)
				                      .setKether(fullKey)
				                      .setCompound(UiObject.PATH_KEY, path);
		if (value instanceof Map) {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.OPEN_COMPOUND);
		} else if (value instanceof List) {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.OPEN_COMPOUND);
		} else if (isDigit(key) && value instanceof String) {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.EDIT_DATA);
			factory.setLore("&f>> &e" + value).formattedLore(40);
		}
		else {
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.EDIT_OBJECT);
			factory.setLore("&f>> &e" + value).formattedLore(40);
		}
		return factory.build();
	}
	public ItemStack loadStatusButton(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) return null;
		ItemFactory factory = new ItemFactory();
		factory.parserFactory(item);
		if (factory.notValid()) return null;
		if (factory.getAction() == null) return null;
		if (actionMode == ActionMode.EDIT_MODE){
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.STATUS_EDIT);
			factory.setType(Material.MINECART);
			factory.setName("&a&l编辑状态");
		}
		else if (actionMode == ActionMode.DELETE_MODE){
			factory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.STATUS_DELETE);
			factory.setType(Material.TNT_MINECART);
			factory.setName("&c&l删除状态");
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
		if (itemFactory.getAction().equals(UiObject.Actions.STATUS_EDIT.toString())) {
			itemFactory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.STATUS_DELETE);
			itemFactory.setType(Material.TNT_MINECART);
			itemFactory.setName("&c&l删除状态");
			newItem = itemFactory.build();
		} else if (itemFactory.getAction().equals(UiObject.Actions.STATUS_DELETE.toString())) {
			itemFactory.setCompound(UiObject.ACTION_KEY, UiObject.Actions.STATUS_EDIT);
			itemFactory.setType(Material.MINECART);
			itemFactory.setName("&a&l编辑状态");
			newItem = itemFactory.build();
		} else return null;
		return newItem;
	}
	
	public UiBuilder setActionMode(ItemStack item) {
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(item);
		if (itemFactory.notValid()) return null;
		if (itemFactory.getAction() == null) return null;
		if (itemFactory.getAction().equals(UiObject.Actions.STATUS_EDIT.toString())) {
			actionMode = ActionMode.EDIT_MODE;
		} else if (itemFactory.getAction().equals(UiObject.Actions.STATUS_DELETE.toString())) {
			actionMode = ActionMode.DELETE_MODE;
		}
		return this;
	}
	
	public boolean reloadStatusButton(ItemStack item, Integer slot) {
		if (item == null || item.getType() == Material.AIR) return false;
		if (slot == null) slot = 18;
		ItemStack newItem = switchStatusOfStatusButton(item);
		if (newItem == null) return false;
		inventory.setItem(slot, newItem);
		return true;
	}
	public void reloadStatusButton(Integer slot) {
		ItemStack item = inventory.getItem(slot);
		reloadStatusButton(item, slot);
	}
	
	public void renderKetherInfo(String kether, Integer slot) {
		ItemStack item = UiObject.fullKetherItem(kether);
		if (item == null || item.getType() == Material.AIR) item = new UiObject().getDECORATION();
		if (slot == null) slot = 0;
		inventory.setItem(slot, item);
	}
	
	public UiBuilder setActionMode(ActionMode mode) {
		this.actionMode = mode;
		return this;
	}
	
	public enum ActionMode {
		DELETE_MODE,
		EDIT_MODE
	}
	
	public static boolean isDigit(String str){
		try {
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException e){
			return false;
		}
	}
}
