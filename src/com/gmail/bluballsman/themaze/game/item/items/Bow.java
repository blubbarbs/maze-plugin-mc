package com.gmail.bluballsman.themaze.game.item.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.UpgradableItem;

public class Bow extends UpgradableItem {
	
	public Bow() {
		super("Bow", Material.BOW);
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.BLOCK_BAMBOO_HIT, 1.5F, 1.0F);
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ITEM_CROSSBOW_QUICK_CHARGE_3, 1.5F, 1.0F);
	}
	
	@Override
	public void purchaseLevelOne(PlayerItemStandData data) {
		data.getPlayer().getInventory().addItem(getDefaultItemInHand());
	}

	@Override
	public void purchaseLevelTwo(PlayerItemStandData data) {
		ItemStack bow = data.getPlayerGameData().getFirstMatchingItem(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
		bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
	}

}
