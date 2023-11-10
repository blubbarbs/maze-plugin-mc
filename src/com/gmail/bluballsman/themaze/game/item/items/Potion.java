package com.gmail.bluballsman.themaze.game.item.items;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.ConsumableItem;

import net.md_5.bungee.api.ChatColor;

public class Potion extends ConsumableItem {

	public Potion(String name, int price, Material type, Color color, PotionEffect... potionEffects) {
		super(name, price, getPotion(type, name, color, potionEffects));
	}
	
	public Potion(String name, int price, Material type, Color color, PotionEffectType... potionEffectTypes) {
		super(name, price, getPotion(type, name, color, potionEffectTypes));
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0F, 1.1F);
	}
	
	public static ItemStack getPotion(Material type, String name, Color color, PotionEffect... potionEffects) {
		ItemStack potionStack = new ItemStack(type, 1);
		PotionMeta potionMeta = (PotionMeta) potionStack.getItemMeta();
		potionMeta.setDisplayName(ChatColor.RESET + name);
		potionMeta.setColor(color);
		for(PotionEffect potionEffect : potionEffects) {
			potionMeta.addCustomEffect(potionEffect, true);
		}
		potionStack.setItemMeta(potionMeta);
		return potionStack;
	}
	
	public static ItemStack getPotion(Material type, String name, Color color, PotionEffectType... potionEffectTypes) {
		ArrayList<PotionEffect> actualEffects = new ArrayList<PotionEffect>();
		for(PotionEffectType potionEffectType : potionEffectTypes) {
			actualEffects.add(new PotionEffect(potionEffectType, 600, 0));
		}
		return getPotion(type, name, color, actualEffects.toArray(new PotionEffect[actualEffects.size()]));
	}
	
}
