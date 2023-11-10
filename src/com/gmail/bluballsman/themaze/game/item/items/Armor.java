package com.gmail.bluballsman.themaze.game.item.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.UpgradableItem;

public class Armor extends UpgradableItem {

	public Armor(ItemStack armor) {
		super(armor);
	}
	
	public Armor(Material armor) {
		super(armor);
	}
	
	@Override
	public void resetItemStandData(PlayerItemStandData data) {
		super.resetItemStandData(data);
		ItemStack armor = getDefaultItemInHand();
		LeatherArmorMeta armorMeta = (LeatherArmorMeta) armor.getItemMeta();
		armorMeta.setColor(data.getPlayerGameData().getTeam().getArmorColor());
		armor.setItemMeta(armorMeta);
		data.setItem(EquipmentSlot.HAND, armor);
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.5F, .9F);
	}
	
	@Override
	public void purchaseLevelOne(PlayerItemStandData data) {
		data.getPlayer().getInventory().addItem(data.getItem(EquipmentSlot.HAND));
	}

	@Override
	public void purchaseLevelTwo(PlayerItemStandData data) {
		data.getPlayerGameData().getFirstMatchingItem(getDefaultItemInHand().getType()).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
	}

}
