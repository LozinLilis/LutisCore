package org.lozin.lutiscore.coreHandlers;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.lozin.tools.event.LutisCoreUIEvent;
import org.lozin.tools.gui.UiCache;

import java.io.IOException;

public class UiHandler implements Listener {
	@EventHandler
	public void onUiClick(InventoryClickEvent event) throws IOException {
		LutisCoreUIEvent uiEvent = new LutisCoreUIEvent(UiCache.getBuilder(event.getWhoClicked()), event.getRawSlot(), event.getCurrentItem());
		if (!uiEvent.isSafe()) return;
		Bukkit.getPluginManager().callEvent(uiEvent);
		uiEvent.typeHandleClick();
		if (uiEvent.isCancelled()) event.setCancelled(true);
	}
	@EventHandler
	public void onUiClose(InventoryCloseEvent event) {
		if (UiCache.getBuilder(event.getPlayer()) == null) return;
		LutisCoreUIEvent uiEvent = new LutisCoreUIEvent(UiCache.getBuilder(event.getPlayer()));
		if (!uiEvent.isSafe()) return;
		Bukkit.getPluginManager().callEvent(uiEvent);
		uiEvent.typeHandleCloseOri();
	}
}
