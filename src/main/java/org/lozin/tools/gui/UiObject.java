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

@Data
public class UiObject {
	public static final String ACTION_KEY = "LutisCore.action";
	public static final String FILE_TYPE_KEY = "LutisCore.file.type";
	private  final ItemStack DECORATION = new ItemFactory(Material.BLACK_STAINED_GLASS_PANE, " ", null, 1,null, null).build();
	private  final ItemStack ADD_OBJECT_BUTTON = new ItemFactory(Material.ICE, "&6&l创建对象", Arrays.asList("&7置入 &f<普通对象> &7类型值"), 1,null, null).setCompound(ACTION_KEY, "create_object").build();
	private  final ItemStack ADD_LIST_BUTTON = new ItemFactory(Material.CHEST, "&6&l创建列表", Arrays.asList("&7置入 &f<列表> &7类型值"), 1,null, null).setCompound(ACTION_KEY, "create_list").build();
	private  final ItemStack PREVIOUS_OBJECT_BUTTON = new ItemFactory(Material.ARROW, "&6&l返回上一层级", null, 1,null, null).setCompound(ACTION_KEY, "previous").build();
	private  final ItemStack FOLDER_OBJECT_BUTTON = new ItemFactory(Material.CHEST, "&6&l创建文件夹", Arrays.asList("&7置入 &f<文件夹> &7类型值"), 1,null, null).setCompound(ACTION_KEY, "create_folder").build();
	private  final ItemStack EDITABLE_OBJECT_BUTTON = new ItemFactory(Material.PAPER, "&6&l编辑对象", Arrays.asList("&7编辑此 &f<普通对象>"), 1,null, null).setCompound(ACTION_KEY, "edit_object").build();
	private  final ItemStack EDITABLE_LIST_BUTTON = new ItemFactory(Material.PAPER, "&6&l编辑列表", Arrays.asList("&7编辑此 &f<列表>"), 1,null, null).setCompound(ACTION_KEY, "edit_list").build();
	
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
		if (paths.isEmpty()) {
			plugin.getLogger().warning("无法获取资源文件");
			return null;
		}
		for (String path : paths) {
			Object entry = FilePathCache.cache.get(plugin.getName()).get(path);
			if (entry instanceof FileService.FileEntry) {
				items.add(new ItemFactory(Material.PAPER, "&f[&e&l文件&f] &6&l"+FileService.getThisPath(path), null, 1,null, null)
						          .setCompound(ACTION_KEY, "open_file").setCompound(FILE_TYPE_KEY, entry.getClass().getSimpleName()).build());
			}
			if (entry instanceof FileService.FolderEntry) {
				items.add(new ItemFactory(Material.CHEST, "&f[&b&l文件夹&f] &6&l"+FileService.getThisPath(path), null, 1,null, null)
						          .setCompound(ACTION_KEY, "open_folder").setCompound(FILE_TYPE_KEY, entry.getClass().getSimpleName()).build());
			}
		}
		return items;
	}
	private Set<Integer> registeredSlots = new HashSet<>();
}
