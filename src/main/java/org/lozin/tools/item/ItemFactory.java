package org.lozin.tools.item;


import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ItemFactory {
	private Material material;
	private String name;
	private List<String> lore;
	private Integer amount;
	private Short durability;
	private Integer customModelData;
	private ItemStack itemStack;
	private Map<String, Object> nbt;
	public ItemFactory(Material material, String name, List<String> lore, Integer amount, Short durability, Integer customModelData){
		this.material = material;
		this.name = name;
		this.lore = lore;
		this.amount = amount;
		this.durability = durability;
		this.customModelData = customModelData;
	}
	public ItemFactory(){}
	public ItemFactory parserFactory(ItemStack itemStack){
		if (itemStack == null || itemStack.getType() == Material.AIR) return null;
		this.itemStack = itemStack;
		if (itemStack.getItemMeta() != null) {
			ItemMeta meta = itemStack.getItemMeta();
			if (meta.hasDisplayName()) {
				name = meta.getDisplayName();
			}
			if (meta.hasLore()) {
				lore = meta.getLore();
			}
			if (meta.hasCustomModelData()) {
				customModelData = meta.getCustomModelData();
			}
		}
		this.material = itemStack.getType();
		durability = itemStack.getDurability();
		amount = itemStack.getAmount();
		return this;
	}
	public ItemStack build(){
		if (itemStack != null) return itemStack;
		if(material == null) material = Material.STONE;
		if(name == null) name = "None Name itemStack";
		if(lore == null) lore = Collections.emptyList();
		if(amount == null) amount = 1;
		if(durability == null) durability = 0;
		if(customModelData == null) customModelData = 0;
		itemStack = new ItemStack(material, amount);
		itemStack.setDurability(durability);
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(name.replace("&", "§"));
		meta.setLore(lore.stream().map(s -> s.replace("&", "§")).collect(Collectors.toList()));
		meta.setCustomModelData(customModelData);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	public Object getCompound(String path){
		if (itemStack == null) {try{build();}catch (Exception e){return null;}}
		List<String> list = Arrays.asList(path.split("\\."));
		NBTItem nbtItem = new NBTItem(itemStack);
		NBTCompound current = nbtItem.getCompound(list.get(0));
		if (current == null) return null;
		if (list.size() == 1) return current;
		for (int i = 1; i < list.size(); i++) {
			if (current == null) return null;
			String key = list.get(i);
			if (i == list.size() - 1) {
				return current.getObject(key, Object.class);
			}
			current = current.getCompound(key);
		}
		return current;
	}
	
	public ItemFactory setCompound(String path, Object value){
		if (itemStack == null) {try{build();}catch (Exception e){return this;}}
		List<String> list = Arrays.asList(path.split("\\."));
		NBTItem nbtItem = new NBTItem(itemStack);
		NBTCompound current = nbtItem.getOrCreateCompound(list.get(0));
		if (list.size() == 1) {
			current.setObject(list.get(0), value);
		}
		for (int i = 1; i < list.size(); i++) {
			String key = list.get(i);
			if (i == list.size() - 1) {
				current.setObject(key, value);
			}
			current = current.getOrCreateCompound(key);
		}
		itemStack = nbtItem.getItem();
		return this;
	}
	public Object getAllNBT(){
		if (itemStack == null) return null;
		NBTItem nbtItem = new NBTItem(itemStack);
		return ((NBTCompound) nbtItem).getCompound();
	}
}
