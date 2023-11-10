package com.gmail.bluballsman.themaze.game.item.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.MazeItems;
import com.gmail.bluballsman.themaze.game.item.UpgradableItem;
import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class GrimReaper extends UpgradableItem {
	private static ItemStack DEATHS_DOOR = new ItemStack(Material.IRON_HOE, 1);
	private static ItemStack REAPERS_HARVEST = new ItemStack(Material.GOLDEN_HOE, 1);
	private static ItemStack GRIM_HELMET = new ItemStack(Material.WITHER_SKELETON_SKULL, 1);
	
	static {
		ItemMeta deathsDoorMeta = DEATHS_DOOR.getItemMeta();
		ItemMeta reapersHarvestMeta = REAPERS_HARVEST.getItemMeta();
		ItemMeta grimHelmetMeta = GRIM_HELMET.getItemMeta();
		
		deathsDoorMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
		deathsDoorMeta.setDisplayName(ChatColor.DARK_PURPLE + "Death's Door");
		DEATHS_DOOR.setItemMeta(deathsDoorMeta);
		reapersHarvestMeta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);
		reapersHarvestMeta.setDisplayName(ChatColor.DARK_PURPLE + "Reaper's Harvest");
		REAPERS_HARVEST.setItemMeta(reapersHarvestMeta);
		grimHelmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
		deathsDoorMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);
		grimHelmetMeta.setDisplayName(ChatColor.DARK_PURPLE + "Grim's Skull");
		GRIM_HELMET.setItemMeta(grimHelmetMeta);
		
		DEATHS_DOOR = MazeItems.setCanDestroyCarpetsAndSigns(DEATHS_DOOR);
		DEATHS_DOOR = setZombieSoulBonus(DEATHS_DOOR, 2);
		REAPERS_HARVEST = MazeItems.setCanDestroyCarpetsAndSigns(REAPERS_HARVEST);
		REAPERS_HARVEST = setPlayerSoulStealPercentage(REAPERS_HARVEST, 1.5);
		REAPERS_HARVEST = setZombieSoulBonus(REAPERS_HARVEST, 3);
	}
	
	public GrimReaper() {
		super("Grim Reaper", DEATHS_DOOR);
		setSkullOwner("MHF_WSkeleton");
	}
	
	@Override
	public String getPurchaseMessage(PlayerItemStandData data) {
		return data.getPlayerGameData().getPlayerName() + " has made the ultimate sacrifice...";
	}
	
	@Override
	public String getUpgradeMessage(PlayerItemStandData data) {
		return data.getPlayerGameData().getPlayerName() + " has become the " + ChatColor.DARK_PURPLE + "Grim Reaper.";
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayerGameData().getTeam().getPlayers().forEach(p -> {
			p.playSound(p.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, .4F, .2F);
			p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, .7F, .8F);
		});
	}
	
	@Override
	public void playUpgradeSound(PlayerItemStandData data) {
		data.getPlayerGameData().getTeam().getPlayers().forEach(p -> {
			p.playSound(p.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, .4F, .2F);
			p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_DEATH, .7F, .8F);
			p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.5F, .2F);
			p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, .5F, 1.0F);
		});
	}
	
	@Override
	public void purchaseLevelOne(PlayerItemStandData data) {
		int woodenSwordIndex = data.getPlayerGameData().getFirstMatchingInventorySlot(Material.WOODEN_SWORD);
		int stoneSwordIndex = data.getPlayerGameData().getFirstMatchingInventorySlot(Material.STONE_SWORD);
		int swordIndex = woodenSwordIndex >= 0 ? woodenSwordIndex : stoneSwordIndex;
		
		data.getPlayer().getInventory().remove(Material.LEATHER_HELMET);
		data.getPlayer().getInventory().remove(Material.BOW);
		data.getPlayer().getInventory().setItem(swordIndex, DEATHS_DOOR);
		data.getPlayer().getEquipment().setHelmet(GRIM_HELMET);
		
		for(Skeleton itemStand : data.getGame().getItemStands(MazeItems.LEATHER_HELMET, MazeItems.SWORD_UPGRADE, MazeItems.BOW)) {
			PlayerItemStandData forbiddenStand = data.getPlayerGameData().getItemStandData(itemStand);
			forbiddenStand.setPurchasable(false);
			forbiddenStand.updateArmorStand();
		}
		data.getTwin().setItem(EquipmentSlot.HAND, REAPERS_HARVEST);
		data.getPlayerGameData().setReaperEyes(true);
	}

	@Override
	public void purchaseLevelTwo(PlayerItemStandData data) {
		int hoeSlot = data.getPlayerGameData().getFirstMatchingInventorySlot(Material.IRON_HOE);
		data.getPlayerGameData().getPlayer().getInventory().setItem(hoeSlot, REAPERS_HARVEST);
	}
	
	public static ItemStack setPlayerSoulStealPercentage(ItemStack item, double percentage) {
		net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound itemCompound = nmsStack.getOrCreateTag();
		itemCompound.setDouble("MazePlayerSoulSteal", percentage);
		nmsStack.setTag(itemCompound);
		
		return CraftItemStack.asBukkitCopy(nmsStack);
	}
	
	public static ItemStack setZombieSoulBonus(ItemStack item, int bonus) {
		net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound itemCompound = nmsStack.getOrCreateTag();
		itemCompound.setInt("MazeZombieSoulBonus", bonus);
		nmsStack.setTag(itemCompound);
		
		return CraftItemStack.asBukkitCopy(nmsStack);
	}
	
	public static double getPlayerSoulStealPercentage(ItemStack item) {
		net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		if(!nmsStack.hasTag() || !nmsStack.getTag().hasKey("MazePlayerSoulSteal")) {
			return 0;
		}
		return nmsStack.getTag().getDouble("MazePlayerSoulSteal");
	}
	
	public static int getZombieSoulBonus(ItemStack item) {
		net.minecraft.server.v1_15_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		if(!nmsStack.hasTag() || !nmsStack.getTag().hasKey("MazeZombieSoulBonus")) {
			return 0;
		}
		return nmsStack.getTag().getInt("MazeZombieSoulBonus");
	}
	
}
