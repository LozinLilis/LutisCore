package org.lozin.tools.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lozin.tools.enumrator.UiType;
import org.lozin.tools.gui.UiBuilder;
import org.lozin.tools.item.ItemFactory;
import org.lozin.tools.string.MessageSender;

import java.util.Set;

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
		ItemFactory itemFactory = new ItemFactory();
		itemFactory.parserFactory(clickedItem);
		String action = itemFactory.getAction();
		String fileType = itemFactory.getFileType();
		MessageSender.sendColorizedMessage(player,
				"&f此物品的动作: &e"+ action,
				"&f此物品的文件类型: &e"+ fileType
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
	public void typeHandleClick(){
		switch (uiType) {
			case DEFAULT:
				if (cancelSlots.contains(clickedSlot)) setCancelled(true);
				getItemNBT();
				break;
			case READ_ONLY:
				getItemNBT();
				setCancelled(true);
				break;
			case EDITABLE:
				break;
		}
	}
	public void typeHandleClose(){
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
	}
}
