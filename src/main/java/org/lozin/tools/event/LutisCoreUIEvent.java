package org.lozin.tools.event;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lozin.tools.cache.Cache;
import org.lozin.tools.cache.FilePathCache;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.file.FileService;
import org.lozin.tools.gui.UiBuilder;
import org.lozin.tools.gui.UiCache;
import org.lozin.tools.gui.UiObject;
import org.lozin.tools.item.ItemFactory;
import org.lozin.tools.string.MessageSender;
import org.lozin.tools.yaml.YamlFactory;
import org.lozin.tools.yaml.YamlService;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class LutisCoreUIEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private Inventory inventory;
	private String invType;
	private Integer clickedSlot;
	private ItemStack clickedItem;
	private Player player;
	private UiType uiType;
	private UiBuilder builder;
	private boolean safe = true;
	private Set<Integer> cancelSlots;
	private boolean lockBuilder = false;
	private boolean async;
	private static Map<Player, ItemFactory> submitItemFact = new WeakHashMap<>();
	public LutisCoreUIEvent(UiBuilder builder, Integer clickedSlot, ItemStack clickedItem) {
		if (builder == null) {
			safe = false;
			return;
		}
		this.builder = builder;
		this.inventory = builder.getInventory();
		this.clickedSlot = clickedSlot;
		this.clickedItem = clickedItem;
		this.player = builder.getPlayer();
		this.uiType = builder.getUiType();
		cancelSlots = builder.getRegisteredSlots();
		async = false;
	}
	
	public LutisCoreUIEvent(UiBuilder builder) {
		this(builder, null, null);
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public void getItemNBT() {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		//if (builder.getRegisteredSlots().contains(clickedSlot)) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		String action = itemFactory.getAction();
		String fileType = itemFactory.getFileType();
		String path = itemFactory.getPath();
		String kether = itemFactory.getKether();
		String handlerInvType = itemFactory.getHandlerInvType();
		MessageSender.sendColorizedMessage(player,
				" ",
				"&f动作: &e" + action,
				"&f文件类型: &e" + fileType,
				"&f文件路径: &e" + path,
				"&f内部键: &e" + kether,
				"&f容器操作类型: &e" + handlerInvType,
				" "
		);
		//Bukkit.broadcastMessage(itemFactory.getAllNBT().toString());
	}
	
	@Override
	@NonNull
	public HandlerList getHandlers() {
		return handlers;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean b) {
		cancelled = b;
	}
	
	public void typeHandleClick() throws IOException {
		if (async) {
			Bukkit.getScheduler().runTaskAsynchronously(builder.getPlugin(), () -> {
				try {
					typeHandleClickOri();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
		else typeHandleClickOri();
	}
	public void typeHandleClickOri() throws IOException {
		switch (uiType) {
			case DEFAULT:
				if (cancelSlots.contains(clickedSlot)) setCancelled(true);
				getItemNBT();
				break;
			case READ_ONLY:
				//getItemNBT();
				setCancelled(true);
				emitCreator();
				break;
			case EDITABLE:
				break;
		}
		switchHandlerInvAction();
		pageHandler();
		traverseFolder();
		traverseFile();
		traverseInnerValue();
		handleCreateInnerKey();
		back();
		
	}
	
	public void typeHandleCloseOri() {
		if (lockBuilder) return;
		switch (uiType) {
			case DEFAULT:
			case EDITABLE:
				for (int i = 0; i < inventory.getSize(); i++) {
					if (cancelSlots.contains(i)) continue;
					ItemStack itemStack = inventory.getItem(i);
					if (itemStack == null || itemStack.equals(new ItemStack(Material.AIR))) continue;
					if (player.getInventory().firstEmpty() != - 1) player.getInventory().addItem(itemStack);
					else player.getWorld().dropItem(player.getLocation(), itemStack);
				}
				break;
		}
		UiCache.unregister(player);
	}
	public void switchHandlerInvAction(){
		if (!builder.getRegisteredSlots().contains(clickedSlot)) return;
		if (builder.reloadStatusButton(clickedItem, clickedSlot)) builder.setActionMode(inventory.getItem(clickedSlot));
	}
	public void traverseFolder() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		if (builder.getRegisteredSlots().contains(clickedSlot)) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		UiBuilder.ActionMode mode = builder.getActionMode();
		if (itemFactory.getAction().equals(UiObject.Actions.OPEN_FOLDER.toString())) {
			Inventory inv = Bukkit.createInventory(null, 54, "§0§l" + FileService.getThisPath(itemFactory.getPath()));
			builder = new UiBuilder(player, inv, UiType.READ_ONLY, builder.getPlugin());
			builder.basicWindow();
			builder.putObjects(itemFactory.getPath());
			builder.fleshAll(itemFactory.getPath());
		}
	}
	public boolean handleCreateInnerKey() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return false;
		if (!builder.getRegisteredSlots().contains(clickedSlot)) return false;
		ItemFactory itemFactory = new ItemFactory(clickedItem);
		if (itemFactory.notValid()) return false;
		if (itemFactory.getAction() == null) return false;
		if (itemFactory.getKether() == null) return false;
		YamlService service = Cache.yamlService.get(ImmutableMap.of(builder.getPlugin(), itemFactory.getPath()));
		if (service == null) service = new YamlService(new YamlFactory(builder.getPlugin(), itemFactory.getPath()));
		String kether = itemFactory.getKether();
		String path = itemFactory.getPath();
		if (itemFactory.getAction().equals(UiObject.Actions.CREATE_MAP.name())) {
			//player.sendMessage("创建对象包");
			service.createMap(kether);
			afterInnerKey(path, kether, false);
			return true;
		}
		else if (itemFactory.getAction().equals(UiObject.Actions.CREATE_LIST.name())) {
			//player.sendMessage("创建列表");
			service.createList(kether);
			afterInnerKey(path, kether,false);
			return true;
		}
		else if (itemFactory.getAction().equals(UiObject.Actions.CREATE_OBJECT.name())) {
			//player.sendMessage("创建对象");
			service.createObject(kether);
			afterInnerKey(path, kether,false);
			return true;
		}
		else if (itemFactory.getAction().equals(UiObject.Actions.CREATE_DATA.name())){
			//player.sendMessage("创建数据");
			if (!service.createData(kether)) MessageSender.sendColorizedMessage(player, "&c无法在此处创建 data");
			afterInnerKey(path, kether,false);
			return true;
		}
		return false;
	}
	public void clearInv(){builder.getInventory().clear();}
	public void traverseFile() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		if (builder.getRegisteredSlots().contains(clickedSlot)) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		if (itemFactory.getPath() == null) return;
		if (itemFactory.getAction().equals(UiObject.Actions.OPEN_FILE.toString())) {
			Map<JavaPlugin, String> ke = ImmutableMap.of(builder.getPlugin(), itemFactory.getPath());
			String path = itemFactory.getPath();
			if (Cache.yamlService.containsKey(ke)) {
				//YamlService yamlService = Cache.yamlService.get(ke);
				builder = new UiBuilder(player, Bukkit.createInventory(null, 54, "§0§l" + FileService.getThisPath(itemFactory.getPath())), UiType.READ_ONLY, builder.getPlugin());
				afterInnerKey(path, "", false);
			}
		}
	}
	/**
	 * @param path 文件路径
	 * @param ke 配置的当前路径
	 * @param par 是否要使用父级路径 ( delete时为true, edit时为false )
	 */
	public void afterInnerKey(String path, String ke, Boolean par) throws IOException {
		clearInv();
		builder.fileControllerWindow();
		if (par) {
			String parentKe = ItemFactory.getParentKether(ke);
			builder.putObjects(path,parentKe);
			loadInnerHandlerButtons(path, parentKe);
		}
		else {
			builder.putObjects(path,ke);
			loadInnerHandlerButtons(path, ke);
		}
	}
	
	public void traverseInnerValue() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		if (builder.getRegisteredSlots().contains(clickedSlot)) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		String ke = itemFactory.getKether();
		String path = itemFactory.getPath();
		if (itemFactory.getAction().equals(UiObject.Actions.OPEN_COMPOUND.toString())) {
			if (itemFactory.getPath() == null) return;
			if (builder.getActionMode().equals(UiBuilder.ActionMode.EDIT_MODE)){
				afterInnerKey(path, ke, false);
			}
			else if (builder.getActionMode().equals(UiBuilder.ActionMode.DELETE_MODE)){
				YamlService service = Cache.yamlService.get(ImmutableMap.of(builder.getPlugin(), path));
				service.delete(ke);
				afterInnerKey(path, ke, true);
			}
		}
		else if (itemFactory.getAction().equals(UiObject.Actions.EDIT_OBJECT.toString()) || itemFactory.getAction().equals(UiObject.Actions.EDIT_DATA.toString())) {
			if (builder.getActionMode().equals(UiBuilder.ActionMode.EDIT_MODE)){
				player.sendMessage("§a触发编辑");
			}
			else if (builder.getActionMode().equals(UiBuilder.ActionMode.DELETE_MODE)){
				YamlService service = Cache.yamlService.get(ImmutableMap.of(builder.getPlugin(), path));
				service.delete(ke);
				afterInnerKey(path, ke, true);
			}
		}
	}
	public void loadInnerHandlerButtons(String path, String ke){
		builder.renderKetherInfo(ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.KETHER_INFO).getFirst());
		builder.fleshNextPageButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.NEXT_PAGE).getFirst());
		builder.fleshPreviousPageButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.PREVIOUS_PAGE).getFirst());
		builder.fleshPreviousObjectButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.PREVIOUS_OBJECT).getFirst());
		builder.fleshCreateMapButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.CREATE_MAP).getFirst());
		builder.fleshCreateListButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.CREATE_LIST).getFirst());
		builder.fleshCreateObjectButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.CREATE_OBJECT).getFirst());
		builder.fleshAddDataButton(path, ke, builder.getRegisteredSlotsAction().get(UiObject.Actions.CREATE_DATA).getFirst());
	}
	
	/**
	 * 获取上一级的界面 (总方法)
	 */
	public void back() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		if (itemFactory.getAction().equals(UiObject.Actions.PREVIOUS_OBJECT.toString())) {
			if (itemFactory.getKether() != null && ! itemFactory.getKether().equals("")) {
				innerBack(itemFactory);
			} else fileBack(itemFactory);
		}
	}
	
	/**
	 * 传入的是 itemFactory 的当前 kether
	 */
	public void innerBack(ItemFactory itemFactory) throws IOException {
		String parentKe = itemFactory.getKether();
		String path = itemFactory.getPath();
		// 获取上一级的kether
		if (itemFactory.getKether().lastIndexOf(".") != - 1)
			parentKe = parentKe.substring(0, parentKe.lastIndexOf("."));
		else if (itemFactory.getKether().lastIndexOf(".") == - 1) parentKe = "";
		builder.getInventory().clear();
		builder.fileControllerWindow();
		builder.putObjects(itemFactory.getPath(), parentKe);
		loadInnerHandlerButtons(path, parentKe);
	}
	
	public void fileBack(ItemFactory itemFactory) throws IOException {
		String path = itemFactory.getPath();
		if (path == null || path.isEmpty() || path.equals(FileService.getRootFolder(builder.getPlugin()))) return;
		File f = FileService.getParent(builder.getPlugin(), path);
		if (f == null) return;
		String relativePath = FileService.filterRootPath(builder.getPlugin(), f.getPath());
		builder = new UiBuilder(player, Bukkit.createInventory(null, 54, "§0§l" + FileService.getThisPath(f.getPath())), UiType.READ_ONLY, builder.getPlugin());
		builder.basicWindow();
		builder.putObjects(relativePath);
		builder.fleshAll(relativePath);
	}
	
	public boolean emitCreator() {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return false;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return false;
		if (itemFactory.getAction() == null) return false;
		JavaPlugin plugin = builder.getPlugin();
		if (itemFactory.getAction().equals("CREATE_FOLDER")) {
			lockBuilder = true;
			player.closeInventory();
			MessageSender.sendColorizedMessage(player,
					" ",
					"&f&l请输入文件夹名称 &c(输入cancel取消)",
					" "
			);
		} else if (itemFactory.getAction().equals("CREATE_FILE")) {
			lockBuilder = true;
			player.closeInventory();
			MessageSender.sendColorizedMessage(player,
					" ",
					"&f&l请输入文件名称 &c(输入cancel取消)",
					" "
			);
		}
		else return false;
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onChat(AsyncPlayerChatEvent event) {
				if (event.getPlayer() != player) return;
				event.setCancelled(true);
				String folder = itemFactory.getPath();
				Bukkit.getScheduler().runTask(plugin, () -> {
					if (event.getMessage().equals("cancel")) {
						MessageSender.sendColorizedMessage(player,
								" ",
								"&c&l已取消创建文件/文件夹",
								" "
						);
						try {
							builder.build();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						lockBuilder = false;
					} else {
						lockBuilder = false;
						typeHandleCloseOri();
						if (itemFactory.getAction().equals("CREATE_FOLDER")){
							if (! FileService.createFolder(plugin, folder, event.getMessage())) {
								MessageSender.sendColorizedMessage(player,
										" ",
										"&c&l创建文件/文件夹失败 &f(原因: 文件名 &e" + event.getMessage() + " &f存在非法字符  " + FileService.invalidFileArgs.stream().map(s -> "&7" + s).collect(Collectors.joining("  ")) + " &f)",
										"&f&l请重新输入 &c(输入cancel取消)",
										" "
								);
								return;
							}
						}
						else if (itemFactory.getAction().equals(UiObject.Actions.CREATE_FILE.name())){
							try {
								FileService.createFile(plugin,folder,event.getMessage());
								player.sendMessage("创建成功");
							} catch (IOException e) {
								player.sendMessage("创建失败");
								throw new RuntimeException(e);
							}
						}
						MessageSender.sendColorizedMessage(player,
								" ",
								"&f&l已创建文件/文件夹 §e" + event.getMessage(),
								" "
						);
						FilePathCache.reload(plugin);
						try {
							builder.getInventory().clear();
							builder.basicWindow();
							builder.putObjects(folder);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						builder.fleshAll(folder);
					}
					event.getHandlers().unregister(this);
				});
			}
		}, plugin);
		return true;
	}
	
	public void checkFileCache() {
		FilePathCache.cache.forEach((path, map) -> {
			Bukkit.broadcastMessage("§e" + path + ": ");
			map.forEach((key, value) -> {
				Bukkit.broadcastMessage(" · " + key + ": ");
				if (value instanceof FileService.FileEntry) {
					Bukkit.broadcastMessage("      §7" + ((FileService.FileEntry) value).getParent() + ": §b" + ((FileService.FileEntry) value).getRealPath());
				} else if (value instanceof FileService.FolderEntry) {
					Bukkit.broadcastMessage("      §7" + ((FileService.FolderEntry) value).getParent() + ": §b" + ((FileService.FolderEntry) value).getRealPath());
				}
			});
		});
	}
	
	public void previousPage(ItemFactory itemFactory) throws IOException {
		if (builder.getPager().get(player) == 1) return;
		builder.getPager().put(player, builder.getPager().get(player) - 1);
		if (itemFactory.getHandlerInvType() == null) return;
		builder.getInventory().clear();
		if (Objects.equals(itemFactory.getHandlerInvType(), ItemFactory.HandlerInventoryType.FILE_SYSTEM.toString())) {
			builder.basicWindow();
			builder.putObjects(itemFactory.getPath());
			builder.fleshAll(itemFactory.getPath());
		} else {
		
		}
	}
	
	public void nextPage(ItemFactory itemFactory) throws IOException {
		if (builder.getPager().get(player).equals(builder.getMaxPage())) return;
		builder.getPager().put(player, builder.getPager().get(player) + 1);
		if (itemFactory.getHandlerInvType() == null) return;
		builder.getInventory().clear();
		if (Objects.equals(itemFactory.getHandlerInvType(), ItemFactory.HandlerInventoryType.FILE_SYSTEM.toString())) {
			builder.basicWindow();
			builder.putObjects(itemFactory.getPath());
			builder.fleshAll(itemFactory.getPath());
		} else {
		
		}
	}
	
	public void pageHandler() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		if (itemFactory.getAction().equals(UiObject.Actions.PREVIOUS_PAGE.toString())) {
			previousPage(itemFactory);
		} else if (itemFactory.getAction().equals(UiObject.Actions.NEXT_PAGE.toString())) {
			nextPage(itemFactory);
		}
	}
}
