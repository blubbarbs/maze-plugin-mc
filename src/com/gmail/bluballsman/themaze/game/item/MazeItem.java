package com.gmail.bluballsman.themaze.game.item;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;

public abstract class MazeItem {
	private int defaultPrice;
	private ItemStack itemInHand;
	private String name;
	private NBTBase skullOwnerNBT;
	
	public MazeItem(String name, int defaultPrice, ItemStack itemInHand)  {
		this.itemInHand = itemInHand;
		this.defaultPrice = defaultPrice;
		this.name = name;
	}
	
	public MazeItem(String name, int defaultPrice, Material itemInHandType) {
		this(name, defaultPrice, new ItemStack(itemInHandType, 1));
	}
	
	public MazeItem(int defaultPrice, ItemStack itemInHand) {
		this(MazeItems.getItemName(itemInHand), defaultPrice, itemInHand);
	}
	
	public MazeItem(int defaultPrice, Material itemInHandType) {
		this(defaultPrice, new ItemStack(itemInHandType, 1));
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack getCustomSkull() {
		net.minecraft.server.v1_15_R1.ItemStack nmsSkull = CraftItemStack.asNMSCopy(new ItemStack(Material.PLAYER_HEAD, 1));
		nmsSkull.getOrCreateTag().set("SkullOwner", skullOwnerNBT);
		return CraftItemStack.asBukkitCopy(nmsSkull);
	}
	
	public ItemStack getDefaultItemInHand() {
		return itemInHand.clone();
	}
	
	public int getDefaultPrice() {
		return defaultPrice;
	}
	
	public String getArmorStandName(int price) {
		return price == 0 ? name : name + " : " + price;
	}
	
	public Skeleton getNewItemStand(Location l) {
		Skeleton stand = (Skeleton) l.getWorld().spawnEntity(l, EntityType.SKELETON);
		stand.getEquipment().setItemInMainHand(itemInHand);
		stand.getEquipment().setHelmet(getCustomSkull());
		stand.setAI(false);
		stand.setSilent(true);
		stand.setRemoveWhenFarAway(false);
		TheMaze.getMetadataHelper().setMetadata(stand, MetadataKeys.ITEMSTAND_ITEM, this);
		
		return stand;
	}
	
	public boolean canPurchase(PlayerItemStandData data) {
		return !data.getPlayerGameData().hasWool();
	}

	
	public String getPurchaseMessage(PlayerItemStandData data) {
		String purchaseMessage = data.getPlayerGameData().getPlayerName() + " has purchased " + getName() + ".";
		return purchaseMessage;
	}
	
	public void setSkullOwner(String skullOwner) {
		skullOwnerNBT = NBTTagString.a(skullOwner);
	}
	
	public void setSkullTexture(String texture) { 
		NBTTagCompound skullOwnerTag = new NBTTagCompound();
		NBTTagCompound skullPropertiesTag = new NBTTagCompound();
		NBTTagList texturesTag = new NBTTagList();
		NBTTagCompound textureTag = new NBTTagCompound();
		
		skullOwnerTag.setString("Id", UUID.randomUUID().toString());
		textureTag.setString("Value", texture);
		texturesTag.add(textureTag);
		skullPropertiesTag.set("textures", texturesTag);
		skullOwnerTag.set("Properties", skullPropertiesTag);
		skullOwnerNBT = skullOwnerTag;
	}
	
	public abstract void resetItemStandData(PlayerItemStandData data);
	
	public abstract void playPurchaseSound(PlayerItemStandData data);
	
	public abstract void purchase(PlayerItemStandData data);
}
