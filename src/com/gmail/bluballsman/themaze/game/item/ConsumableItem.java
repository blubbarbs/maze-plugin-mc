package com.gmail.bluballsman.themaze.game.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;

public class ConsumableItem extends MazeItem {
	
	public ConsumableItem(String name, int defaultPrice, ItemStack stackToGive) {
		super(name, defaultPrice, stackToGive);
		setSkullOwner("MHF_Chest");
	}
	
	public ConsumableItem(String name, int defaultPrice, Material itemInHandType) {
		this(name, defaultPrice, new ItemStack(itemInHandType, 1));
	}
	
	public ConsumableItem(int defaultPrice, ItemStack itemInHand) {
		this(MazeItems.getItemName(itemInHand), defaultPrice, itemInHand);
	}
	
	public ConsumableItem(int defaultPrice, Material itemInHandType) {
		this(defaultPrice, new ItemStack(itemInHandType, 1));
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.5F, 1.0F);
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.5F, 1.5F);
	}
	
	@Override
	public void resetItemStandData(PlayerItemStandData data) {}
	
	@Override
	public void purchase(PlayerItemStandData data) {
		data.getPlayer().getInventory().addItem(getDefaultItemInHand());
		data.getPlayerGameData().getTeam().broadcastMessage(getPurchaseMessage(data) + " (" + data.getPrice() + " Souls)");
		playPurchaseSound(data);
	}

}
