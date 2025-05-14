package org.lozin.tools.event;

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
import org.lozin.tools.cache.FilePathCache;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.file.FileService;
import org.lozin.tools.gui.UiBuilder;
import org.lozin.tools.gui.UiCache;
import org.lozin.tools.gui.UiObject;
import org.lozin.tools.item.ItemFactory;
import org.lozin.tools.string.MessageSender;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class LutisCoreUIEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private Inventory inventory;
	private Integer clickedSlot;
	private ItemStack clickedItem;
	private Player player;
	private UiType uiType;
	private UiBuilder builder;
	private boolean safe = true;
	private Set<Integer> cancelSlots;
	private boolean lockBuilder = false;
	private long time;
	
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
	}
	public LutisCoreUIEvent(UiBuilder builder) {
		this(builder, null, null);
	}
	
	public void getItemNBT(){
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		if (builder.getRegisteredSlots().contains(clickedSlot)) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		String action = itemFactory.getAction();
		String fileType = itemFactory.getFileType();
		String path = itemFactory.getPath();
		MessageSender.sendColorizedMessage(player,
				" ",
				"&f此物品的动作: &e"+ action,
				"&f此物品的文件类型: &e"+ fileType,
				"&f此物品的路径: &e"+ path,
				" "
		);
	}
	
	@Override
	@NonNull
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList() {
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
		pageHandler();
		traverseFolder();
		back();
	}
	public void typeHandleClose(){
		if (lockBuilder) return;
		switch (uiType) {
			case DEFAULT:
			case EDITABLE:
				for (int i = 0; i < inventory.getSize(); i++){
					if (cancelSlots.contains(i)) continue;
					ItemStack itemStack = inventory.getItem(i);
					if (itemStack == null || itemStack.equals(new ItemStack(Material.AIR))) continue;
					if (player.getInventory().firstEmpty() != -1) player.getInventory().addItem(itemStack);
					else player.getWorld().dropItem(player.getLocation(), itemStack);
				}
				break;
		}
		UiCache.unregister(player);
	}
	public void traverseFolder() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		if (itemFactory.getAction().equals(UiObject.Actions.OPEN_FOLDER.toString())){
			Inventory inv = Bukkit.createInventory(null, 54, "§0§l"+FileService.getThisPath(itemFactory.getPath()));
			builder = new UiBuilder(player, inv, UiType.READ_ONLY, builder.getPlugin());
			builder.basicWindow();
			builder.putObjects(itemFactory.getPath());
			builder.fleshAll(itemFactory.getPath());
		}
	}
	public void back() throws IOException {
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		if (itemFactory.getAction().equals(UiObject.Actions.PREVIOUS_OBJECT.toString())){
			String path = itemFactory.getPath();
			if (path == null || path.equals("") || path.equals(FileService.getRootFolder(builder.getPlugin()))) return;
			File f = FileService.getParent(builder.getPlugin(), path);
			if (f == null) return;
			String relativePath = FileService.filterRootPath(builder.getPlugin(), f.getPath());
			builder = new UiBuilder(player, Bukkit.createInventory(null, 54, "§0§l"+FileService.getThisPath(f.getPath())), UiType.READ_ONLY, builder.getPlugin());
			builder.basicWindow();
			builder.putObjects(relativePath);
			builder.fleshAll(relativePath);
		}
	}
	public boolean emitCreator(){
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
		}else return false;
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onChat(AsyncPlayerChatEvent event){
				
				if (event.getPlayer() != player) return;
				event.setCancelled(true);
				String folder = itemFactory.getPath();
				Bukkit.getScheduler().runTask(plugin, () -> {
					if (event.getMessage().equals("cancel")){
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
					}else {
						lockBuilder = false;
						typeHandleClose();
						if (!FileService.createFolder(plugin, folder, event.getMessage())){
							MessageSender.sendColorizedMessage(player,
									" ",
									"&c&l创建文件/文件夹失败 &f(原因: 文件名 &e"+ event.getMessage() +" &f存在非法字符  " +FileService.invalidFileArgs.stream().map(s -> "&7"+s).collect(Collectors.joining("  ")) + " &f)",
									"&f&l请重新输入 &c(输入cancel取消)",
									" "
							);
							return;
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
	public void checkFileCache(){
		FilePathCache.cache.forEach((path, map) -> {
				Bukkit.broadcastMessage("§e"+path + ": ");
				map.forEach((key, value) -> {
					Bukkit.broadcastMessage(" · "+key + ": ");
					if (value instanceof FileService.FileEntry) {
						Bukkit.broadcastMessage("      §7"+((FileService.FileEntry) value).getParent() + ": §b" + ((FileService.FileEntry) value).getRealPath());
					} else if (value instanceof FileService.FolderEntry) {
						Bukkit.broadcastMessage("      §7"+((FileService.FolderEntry) value).getParent() + ": §b" + ((FileService.FolderEntry) value).getRealPath());
					}
				});
			});
	}
	public void previousPage(ItemFactory itemFactory) throws IOException{
		builder.getPager().put(player, (builder.getPager().get(player) - 1) <= 0 ? 1 : builder.getPager().get(player) - 1);
		builder.getInventory().clear();
		builder.basicWindow();
		builder.putObjects(itemFactory.getPath());
		builder.fleshAll(itemFactory.getPath());
	
	}
	public void nextPage(ItemFactory itemFactory) throws IOException{
		builder.getPager().put(player, (builder.getPager().get(player) + 1) > builder.getMaxPage() ? builder.getMaxPage() : builder.getPager().get(player) + 1);
		builder.getInventory().clear();
		builder.basicWindow();
		builder.putObjects(itemFactory.getPath());
		builder.fleshAll(itemFactory.getPath());
	}
	public void pageHandler() throws IOException{
		if (clickedItem == null || clickedItem.equals(new ItemStack(Material.AIR))) return;
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		if (itemFactory.notValid()) return;
		if (itemFactory.getAction() == null) return;
		if (itemFactory.getAction().equals(UiObject.Actions.PREVIOUS_PAGE.toString())){
			previousPage(itemFactory);
		}else if (itemFactory.getAction().equals(UiObject.Actions.NEXT_PAGE.toString())){
			nextPage(itemFactory);
		}
	}
}
