package com.gmail.bluballsman.themaze.game.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;

public abstract class UpgradableItem extends MazeItem {
	public static String ENDER_CHEST_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc0ZWUxNTQyYzQ1NjNmZDZlN2Q3MmRlMjZlNzM3Y2YxOGZiZDA0Y2NhYjFiOGIyODM1M2RhODczNDhlY2ZiIn19fQ==";
	
	public UpgradableItem(String name, ItemStack itemInHand) {
		super(name, 0, MazeItems.setUnbreakable(itemInHand));
		setSkullTexture(ENDER_CHEST_SKULL);
	}
	
	public UpgradableItem(String name, Material itemInHandType) {
		this(name, new ItemStack(itemInHandType, 1));
	}
	
	public UpgradableItem(ItemStack itemInHand) {
		this(MazeItems.getItemName(itemInHand), itemInHand);
	}
	
	public UpgradableItem(Material itemInHandType) {
		this(new ItemStack(itemInHandType, 1));
	}
	
	@Override
	public String getPurchaseMessage(PlayerItemStandData data) {
		return data.getPlayerGameData().getPlayerName() + " has obtained " + getName() + ".";
	}

	
	public String getUpgradeMessage(PlayerItemStandData data) {
		return data.getPlayerGameData().getPlayerName() + " has upgraded " + getName() + ".";
	}
	
	public void playUpgradeSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.5F, 2.3F);
	}
	
	@Override
	public void resetItemStandData(PlayerItemStandData data) {
		data.setLevel(0);
		data.setPurchasable(true);
		data.setItem(EquipmentSlot.HAND, getDefaultItemInHand());
		data.setName(getName() + " I");
	}
	
	
	@Override
	public void purchase(PlayerItemStandData data) {
		if(data.getLevel() == 0) {
			purchaseLevelOne(data);
			data.getTwin().setLevel(1);
			data.getTwin().setName(getName() + " II");
			data.getTwin().updateArmorStand();
			data.getPlayerGameData().getTeam().broadcastMessage(getPurchaseMessage(data));
			playPurchaseSound(data);
		} else {
			purchaseLevelTwo(data);
			data.getPlayerGameData().getTeam().broadcastMessage(getUpgradeMessage(data));
			playUpgradeSound(data);
		}
		data.setPurchasable(false);
		data.updateArmorStand();
	}
	
	public abstract void purchaseLevelOne(PlayerItemStandData data);
	
	public abstract void purchaseLevelTwo(PlayerItemStandData data);
}
