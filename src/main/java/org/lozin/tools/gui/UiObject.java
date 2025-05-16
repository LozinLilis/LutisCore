package org.lozin.tools.gui;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.cache.FilePathCache;
import org.lozin.tools.file.FileService;
import org.lozin.tools.item.ItemFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class UiObject {
	public static final String ACTION_KEY = "LutisCore.action";
	public static final String FILE_TYPE_KEY = "LutisCore.file.type";
	public static final String PATH_KEY = "LutisCore.path";
	public static final String INNER_KETHER = "LutisCore.inner.kether";
	public static final String HANDLER_INV_TYPE  = "LutisCore.handler.inv.type";
	
	
	public enum Actions{
		OPEN_FOLDER,
		OPEN_FILE,
		CREATE_OBJECT,
		CREATE_LIST,
		CREATE_FOLDER,
		CREATE_FILE,
		EDIT_OBJECT,
		EDIT_LIST,
		PREVIOUS_PAGE,
		NEXT_PAGE,
		PREVIOUS_OBJECT,
		OPEN_COMPOUND;
		public enum Status{
			DELETE, EDIT;
			public Status getStatus(String name) {
				for (Status s : values()) {
					if (s.toString().equals(name)) return s;
				}
				return null;
			}
		}
		public Actions getAction(String name) {
			for (Actions a : values()) {
				if (a.toString().equals(name)) return a;
			}
			return null;
		}
	}
	private final ItemStack DECORATION = new ItemFactory(Material.BLACK_STAINED_GLASS_PANE, " ", null, 1,null, null).build();
	private final ItemStack ADD_OBJECT_BUTTON = new ItemFactory(Material.CHEST, "&6&l创建对象", Arrays.asList("&7置入 &f<普通对象> &7类型值"), 1,null, null).setCompound(ACTION_KEY, Actions.CREATE_OBJECT).build();
	private final ItemStack ADD_LIST_BUTTON = new ItemFactory(Material.BOOKSHELF, "&6&l创建列表", Arrays.asList("&7置入 &f<列表> &7类型值"), 1,null, null).setCompound(ACTION_KEY, Actions.CREATE_LIST).build();
	private ItemStack PREVIOUS_OBJECT_BUTTON = new ItemFactory(Material.REDSTONE_TORCH, "&6&l返回上一层级", null, 1,null, null).shine().setCompound(ACTION_KEY, Actions.PREVIOUS_OBJECT).build();
	private final ItemStack CREATE_FOLDER_BUTTON = new ItemFactory(Material.CHEST, "&6&l创建文件夹", Arrays.asList("&7置入 &f<文件夹> &7类型"), 1,null, null).shine().setCompound(ACTION_KEY, Actions.CREATE_FOLDER).build();
	private final ItemStack CREATE_FILE_BUTTON = new ItemFactory(Material.PAPER, "&6&l创建文件", Arrays.asList("&7置入 &f<文件> &7类型"), 1,null, null).shine().setCompound(ACTION_KEY, Actions.CREATE_FILE).build();
	private final ItemStack EDITABLE_OBJECT_BUTTON = new ItemFactory(Material.PAPER, "&6&l编辑对象", Arrays.asList("&7编辑此 &f<普通对象>"), 1,null, null).setCompound(ACTION_KEY, Actions.EDIT_OBJECT).build();
	private final ItemStack EDITABLE_LIST_BUTTON = new ItemFactory(Material.PAPER, "&6&l编辑列表", Arrays.asList("&7编辑此 &f<列表>"), 1,null, null).setCompound(ACTION_KEY, Actions.EDIT_LIST).build();
	private final ItemStack PRE_PAGE = new ItemFactory(Material.ARROW, "&e&l上一页", null, 1,null, null).setCompound(ACTION_KEY, Actions.PREVIOUS_PAGE).build();
	private final ItemStack NEXT_PAGE = new ItemFactory(Material.ARROW, "&e&l下一页", null, 1,null, null).setCompound(ACTION_KEY, Actions.NEXT_PAGE).build();
	private ItemStack STATUS_SWITCH_BUTTON = new ItemFactory(Material.MINECART, "&a&l编辑状态", Arrays.asList("&7[点击切换]"), 1,null, null).setCompound(ACTION_KEY, Actions.Status.EDIT).shine().build();
	
	public void insertItem(Inventory inventory, Map<List<Integer>, ItemStack> mapper) {
		if (mapper == null || mapper.isEmpty()) return;
		Set<Integer> s = new HashSet<>();
		for (List<Integer> slots : mapper.keySet()) {
			for (Integer slot : slots){
				inventory.setItem(slot, mapper.get(slots));
				s.add(slot);
			}
		}
		registeredSlots = s;
	}
	public static List<ItemStack> pathToItems(JavaPlugin plugin, String folder) throws IOException {
		if (!plugin.isEnabled()) return null;
		List<ItemStack> items = new ArrayList<>();
		List<String> paths = FileService.getInferiorSurface(plugin, folder);
		if (paths == null || paths.isEmpty()) {
			plugin.getLogger().warning("无法获取资源文件");
			return null;
		}
		for (String p : paths) {
			String path = FileService.getThisPath(p);
			Object entry = FilePathCache.cache.get(plugin.getName()).get(path);
			if (entry instanceof FileService.FileEntry) {
				items.add(new ItemFactory(Material.PAPER, "&f[&e&l文件&f] &6&l"+FileService.getThisPath(path), null, 1,null, null)
						          .setCompound(ACTION_KEY, Actions.OPEN_FILE)
						          .setCompound(FILE_TYPE_KEY, entry.getClass().getSimpleName())
						          .setCompound(PATH_KEY, p)
						          .build());
			}
			if (entry instanceof FileService.FolderEntry) {
				items.add(new ItemFactory(Material.CHEST, "&f[&b&l文件夹&f] &6&l"+FileService.getThisPath(path), null, 1,null, null)
						          .setCompound(ACTION_KEY, Actions.OPEN_FOLDER)
						          .setCompound(FILE_TYPE_KEY, entry.getClass().getSimpleName())
						          .setCompound(PATH_KEY, p)
						          .build());
			}
		}
		return items;
	}
	private Set<Integer> registeredSlots = new HashSet<>();
	public static ItemStack fleshPreviousObjectButton(String path) {
		UiObject uiObject = new UiObject();
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(uiObject.PREVIOUS_OBJECT_BUTTON);
		if (itemFactory.notValid()) return null;
		itemFactory.setCompound(PATH_KEY, path);
		return itemFactory.build();
	}
	public static ItemStack fleshPreviousObjectButton(String path, String ke){
		ItemStack i = fleshPreviousObjectButton(path);
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(i);
		if (itemFactory.notValid()) return null;
		itemFactory.setCompound(INNER_KETHER, ke);
		return itemFactory.build();
	}
	public static ItemStack formCreateFolderButton(String path){
		UiObject uiObject = new UiObject();
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(uiObject.CREATE_FOLDER_BUTTON);
		if (itemFactory.notValid()) return null;
		itemFactory.setCompound(PATH_KEY, path);
		return itemFactory.build();
	}
	public static ItemStack formCreateFileButton(String path){
		UiObject uiObject = new UiObject();
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(uiObject.CREATE_FILE_BUTTON);
		if (itemFactory.notValid()) return null;
		itemFactory.setCompound(PATH_KEY, path);
		return itemFactory.build();
	}
	/**
	 * 文件类
	 * @param path 文件路径
	 */
	public static ItemStack fleshPreviousPageButton(String path){
		UiObject uiObject = new UiObject();
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(uiObject.PRE_PAGE);
		if (itemFactory.notValid()) return null;
		itemFactory.setCompound(PATH_KEY, path);
		itemFactory.setHandlerInvType(ItemFactory.HandlerInventoryType.FILE_SYSTEM);
		return itemFactory.build();
	}
	/**
	 * 文件类
	 * @param path 文件路径
	 */
	public static ItemStack fleshNextPageButton(String path){
		UiObject uiObject = new UiObject();
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(uiObject.NEXT_PAGE);
		if (itemFactory.notValid()) return null;
		itemFactory.setCompound(PATH_KEY, path);
		itemFactory.setHandlerInvType(ItemFactory.HandlerInventoryType.FILE_SYSTEM);
		return itemFactory.build();
	}
	public static ItemStack fleshPreviousPageButton(String path, String ke){
		ItemStack item = UiObject.fleshPreviousPageButton(path);
		if (item == null) return null;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(item);
		itemFactory.setCompound(INNER_KETHER, ke);
		itemFactory.setHandlerInvType(ItemFactory.HandlerInventoryType.INNER_SYSTEM);
		return itemFactory.build();
	}
	public static ItemStack fleshNextPageButton(String path, String ke){
		ItemStack item = UiObject.fleshNextPageButton(path);
		if (item == null) return null;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(item);
		itemFactory.setCompound(INNER_KETHER, ke);
		itemFactory.setHandlerInvType(ItemFactory.HandlerInventoryType.INNER_SYSTEM);
		return itemFactory.build();
	}
	public static ItemStack fullKetherItem(String ke) {
		ItemFactory itemFactory = new ItemFactory(Material.COMPASS, "&6&l当前的路径", null, 1, null, null);
		List<String> lore = new ArrayList<>();
		if (ke == null || ke.isEmpty()) {
			return null;
		}
		List<String> segments = Arrays.stream(ke.split("\\."))
				                        .filter(s -> !s.isEmpty())
				                        .collect(Collectors.toList());
		if (segments.isEmpty()) {
			itemFactory.setLore(Collections.singletonList("&c无效路径格式"));
			return itemFactory.build();
		}
		List<String> formattedLines = new ArrayList<>();
		int totalSegments = segments.size();
		int maxPerRow = 5;
		for (int i = 0; i < totalSegments; i += maxPerRow) {
			int end = Math.min(i + maxPerRow, totalSegments);
			List<String> segmentGroup = segments.subList(i, end);
			StringBuilder pathBuilder = new StringBuilder();
			for (String seg : segmentGroup) {
				pathBuilder.append(seg).append("/");
			}
			String rawPath = pathBuilder.length() > 0
					                 ? pathBuilder.substring(0, pathBuilder.length() - 1)
					                 : "";
			String line = rawPath;
			if (end < totalSegments) {
				line += "...";
			}
			if (i > 0) {
				line = "..." + line.replaceFirst("^/+", "");
			}
			formattedLines.add(line);
		}
		itemFactory.setLore(formattedLines.stream().map(line -> "&7" + line).collect(Collectors.toList()));
		return itemFactory.shine().build();
	}
}
