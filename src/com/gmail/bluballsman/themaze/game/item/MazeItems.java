package com.gmail.bluballsman.themaze.game.item;

import java.util.ArrayList;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.bluballsman.themaze.game.item.items.*;
import com.gmail.bluballsman.themaze.map.MazeMap;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;

public class MazeItems {
	private static final ArrayList<MazeItem> ALL_ITEMS = new ArrayList<MazeItem>();
	
	public static final MazeItem LEATHER_HELMET = new Armor(Material.LEATHER_HELMET);
	public static final MazeItem LEATHER_CHESTPLATE = new Armor(Material.LEATHER_CHESTPLATE);
	public static final MazeItem LEATHER_LEGGINGS = new Armor(Material.LEATHER_LEGGINGS);
	public static final MazeItem LEATHER_BOOTS = new Armor(Material.LEATHER_BOOTS);
	public static final MazeItem SWORD_UPGRADE = new SwordUpgrade();
	public static final MazeItem BOW = new Bow();
	public static final MazeItem ARROWS = new ConsumableItem("Arrows", 3, new ItemStack(Material.ARROW, 5));
	public static final MazeItem SPECTRAL_ARROWS = new ConsumableItem("Spectral Arrows", 5, new ItemStack(Material.SPECTRAL_ARROW, 2));
	public static final MazeItem INVISIBILITY_POTION = new Potion("Invisibility Potion", 9, Material.POTION, Color.SILVER, PotionEffectType.INVISIBILITY);
	public static final MazeItem SPEED_POTION = new Potion("Speed Potion", 7, Material.POTION, Color.YELLOW, new PotionEffect(PotionEffectType.SPEED, 900, 2));
	public static final MazeItem TRAP = new Potion("Trap", 12, Material.LINGERING_POTION, Color.fromRGB(54, 58, 68), PotionEffectType.CONFUSION, PotionEffectType.WEAKNESS, PotionEffectType.SLOW);
	public static final MazeItem MILK_BUCKET = new ConsumableItem("Milk", 8, Material.MILK_BUCKET);
	public static final MazeItem TOWER_TELEPORTER = new TowerTeleporter();
	public static final MazeItem GRIM_REAPER = new GrimReaper();
	public static final Wool MAGENTA_WOOL = new Wool(DyeColor.MAGENTA, BarColor.PINK, ChatColor.LIGHT_PURPLE, Wool.MAGENTA_WOOL_SKULL);
	public static final Wool LIME_WOOL = new Wool(DyeColor.LIME, BarColor.GREEN, ChatColor.GREEN, Wool.LIME_WOOL_SKULL);
	public static final Wool YELLOW_WOOL = new Wool(DyeColor.YELLOW, BarColor.YELLOW, ChatColor.YELLOW, Wool.YELLOW_WOOL_SKULL);
	
	static {
		ALL_ITEMS.add(LEATHER_HELMET);
		ALL_ITEMS.add(LEATHER_CHESTPLATE);
		ALL_ITEMS.add(LEATHER_LEGGINGS);
		ALL_ITEMS.add(LEATHER_BOOTS);
		ALL_ITEMS.add(SWORD_UPGRADE);
		ALL_ITEMS.add(BOW);
		ALL_ITEMS.add(ARROWS);
		ALL_ITEMS.add(SPECTRAL_ARROWS);
		ALL_ITEMS.add(INVISIBILITY_POTION);
		ALL_ITEMS.add(SPEED_POTION);
		ALL_ITEMS.add(TRAP);
		ALL_ITEMS.add(MILK_BUCKET);
		ALL_ITEMS.add(TOWER_TELEPORTER);
		ALL_ITEMS.add(GRIM_REAPER);
		ALL_ITEMS.add(MAGENTA_WOOL);
		ALL_ITEMS.add(LIME_WOOL);
		ALL_ITEMS.add(YELLOW_WOOL);
	}
	
	public static final ArrayList<MazeItem> getAllItems() {
		return ALL_ITEMS;
	}
	
	public static String getItemName(ItemStack stack) {
		return WordUtils.capitalizeFully(stack.getType().getKey().getKey().replace('_', ' '));
	}
	
	public static ItemStack setUnbreakable(ItemStack stack) {
		ItemMeta itemMeta = stack.getItemMeta();
		itemMeta.setUnbreakable(true);
		stack.setItemMeta(itemMeta);
		
		return stack;
	}
	
	public static ItemStack setCanDestroyCarpetsAndSigns(ItemStack stack) {
		net.minecraft.server.v1_15_R1.ItemStack stackNMS = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound stackCompound = stackNMS.getOrCreateTag();
		NBTTagList canBreak = new NBTTagList();
		
		canBreak.add(NBTTagString.a("#minecraft:carpets"));
		canBreak.add(NBTTagString.a("#minecraft:signs"));
		stackCompound.set("CanDestroy", canBreak);
		stackCompound.setBoolean("Unbreakable", true);
		stackCompound.setInt("HideFlags", 8);
		stackNMS.setTag(stackCompound);
		
		return CraftItemStack.asBukkitCopy(stackNMS);
	}
	
	public static ItemStack setCanPlaceOnGround(MazeMap map, ItemStack stack) {
		net.minecraft.server.v1_15_R1.ItemStack stackNMS = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound stackCompound = new NBTTagCompound();
		
		stackCompound.set("CanPlaceOn", map.getGroundMaterials());
		stackCompound.setInt("HideFlags", 16);
		stackNMS.setTag(stackCompound);
		
		return CraftItemStack.asBukkitCopy(stackNMS);
	}
	
}
