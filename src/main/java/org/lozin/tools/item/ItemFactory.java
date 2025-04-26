package org.lozin.tools.item;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ItemFactory {
	private Material material;
	private String name;
	private List<String> lore;
	private Integer amount;
	private Short durability;
	private Integer customModelData;
	public ItemFactory(Material material, String name, List<String> lore, Integer amount, Short durability, Integer customModelData){
		this.material = material;
		this.name = name;
		this.lore = lore;
		this.amount = amount;
		this.durability = durability;
		this.customModelData = customModelData;
	}
	public ItemStack build(){
		if(material == null) material = Material.STONE;
		if(name == null) name = "None Name Item";
		if(lore == null) lore = Collections.emptyList();
		if(amount == null) amount = 1;
		if(durability == null) durability = 0;
		if(customModelData == null) customModelData = 0;
		ItemStack item = new ItemStack(material, amount);
		item.setDurability(durability);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name.replace("&", "§"));
		meta.setLore(lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList()));
		meta.setCustomModelData(customModelData);
		item.setItemMeta(meta);
		return item;
	}
}
