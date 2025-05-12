package org.lozin.tools.item;


import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lozin.tools.gui.UiObject;

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
		if (itemStack == null || itemStack.getType() == Material.AIR) return this;
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
	
	public boolean hasCompound(String path){
		if (itemStack == null || itemStack.getType() == Material.AIR) return false;
		List<String> paths = Arrays.stream(path.split("\\."))
				                     .filter(p -> !p.trim().isEmpty())
				                     .collect(Collectors.toList());
		NBTItem nbt = new NBTItem(itemStack);
		NBTCompound current = nbt.getCompound(paths.get(0));
		if (current == null) {
			return paths.size() == 1 && nbt.hasTag(paths.get(0));
		}
		for (int i = 1; i < paths.size(); i++) {
			String key = paths.get(i);
			if (current == null || !current.hasTag(key)) {
				return false;
			}
			current = current.getCompound(key);
		}
		return current != null;
	}
	public ItemFactory setCompound(String path, Object value) {
		if (itemStack == null || itemStack.getType() == Material.AIR) build();
		List<String> paths = Arrays.stream(path.split("\\."))
				                     .filter(p -> !p.trim().isEmpty())
				                     .collect(Collectors.toList());
		NBTItem nbt = new NBTItem(itemStack);
		NBTCompound current = nbt.getOrCreateCompound((paths.get(0)));
		for (int i = 1; i < paths.size() -1; i++){
			String key = paths.get(i);
			current = current.getOrCreateCompound(key);
		}
		current.setString(paths.get(paths.size() - 1), value.toString());
		itemStack =  nbt.getItem();
		return this;
	}
	public String getCompound(String path) {
		if (itemStack == null || itemStack.getType() == Material.AIR) {
			return null;
		}
		List<String> paths = Arrays.stream(path.split("\\."))
				                     .filter(p -> !p.trim().isEmpty())
				                     .collect(Collectors.toList());
		NBTItem nbt = new NBTItem(itemStack);
		NBTCompound current = nbt.getCompound(paths.get(0));
		if (current == null) {
			if (paths.size() == 1 && nbt.hasTag(paths.get(0))) {
				return nbt.getString(paths.get(0));
			}else{
				return null;
			}
		}
		for (int i = 1; i < paths.size() -1; i++) {
			String key = paths.get(i);
			if (current == null || !current.hasTag(key)) {
				return null;
			}
			current = current.getCompound(key);
		}
		if (current != null) {
			return current.getString(paths.get(paths.size() - 1));
		}
		return null;
	}
	public Object getAllNBT(){
		if (itemStack == null) return null;
		NBTItem nbtItem = new NBTItem(itemStack);
		return ((NBTCompound) nbtItem).getCompound();
	}
	public String getAction(){
		if (notValid()) return null;
		return getCompound(UiObject.ACTION_KEY);
	}
	public String getFileType(){
		if (notValid()) return null;
		return getCompound(UiObject.FILE_TYPE_KEY);
	}
	public String getPath(){
		if (notValid()) return null;
		return getCompound(UiObject.PATH_KEY);
	}
	public boolean notValid(){
		return itemStack == null || itemStack.getType() == Material.AIR;
	}
	public ItemFactory setEnchant(Enchantment enchantment, int level, Boolean unsafe, Boolean hide){
		if (itemStack == null || itemStack.getType() == Material.AIR) build();
		if (unsafe == null) unsafe = false;
		if (hide == null) hide = false;
		if (enchantment == null) return this;
		if (unsafe) {
			itemStack.addUnsafeEnchantment(enchantment, level);
		}else{
			itemStack.addEnchantment(enchantment, level);
		}
		if (hide) {
			ItemMeta meta = itemStack.getItemMeta();
			if (meta != null) {
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			itemStack.setItemMeta(meta);
		}
		return this;
	}
	public ItemFactory shine() {
		setEnchant(Enchantment.PROTECTION, 1, true, true);
		return this;
	}
}
