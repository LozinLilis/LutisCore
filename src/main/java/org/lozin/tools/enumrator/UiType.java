package org.lozin.tools.enumrator;

import org.bukkit.entity.Player;
import org.lozin.tools.event.LutisCoreUIEvent;
import org.lozin.tools.string.MessageSender;

public enum UiType {
	DEFAULT,
	READ_ONLY{
		@Override
		public void handle(LutisCoreUIEvent event) {
			Player player = event.getPlayer();
			MessageSender.sendColorizedMessage(player, "&c&l该界面为只读界面，无法进行任何操作！");
			event.setCancelled(true);
		}
	},
	EDITABLE;
	
	public void handle(LutisCoreUIEvent event){}
}
