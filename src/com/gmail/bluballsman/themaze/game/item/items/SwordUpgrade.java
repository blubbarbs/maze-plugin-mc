package com.gmail.bluballsman.themaze.game.item.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.MazeItems;
import com.gmail.bluballsman.themaze.game.item.UpgradableItem;

public class SwordUpgrade extends UpgradableItem {

	public SwordUpgrade() {
		super("Sword Upgrade", Material.STONE_SWORD);
	}

	@Override
	public String getUpgradeMessage(PlayerItemStandData data) {
		return data.getPlayerGameData().getPlayerName() + " has obtained Sword Upgrade II.";
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1.5F, 1.2F);
	}
	
	@Override
	public void purchaseLevelOne(PlayerItemStandData data) {
		ItemStack stoneSword = MazeItems.setCanDestroyCarpetsAndSigns(new ItemStack(Material.STONE_SWORD, 1));
		stoneSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		int swordSlot = data.getPlayerGameData().getFirstMatchingInventorySlot(Material.WOODEN_SWORD);
		
		data.getTwin().setItem(EquipmentSlot.HAND, new ItemStack(Material.IRON_SWORD));
		data.getPlayer().getInventory().setItem(swordSlot, stoneSword);
	}

	@Override
	public void purchaseLevelTwo(PlayerItemStandData data) {
		ItemStack ironSword = MazeItems.setCanDestroyCarpetsAndSigns(new ItemStack(Material.IRON_SWORD, 1));
		ironSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		int swordSlot = data.getPlayerGameData().getFirstMatchingInventorySlot(Material.STONE_SWORD);
		
		data.getPlayer().getInventory().setItem(swordSlot, ironSword);
	}

}
