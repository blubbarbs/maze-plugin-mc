package com.gmail.bluballsman.themaze.game;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftSkeleton;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.gmail.bluballsman.themaze.MetadataFakeEntity;
import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.item.MazeItem;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntitySkeleton;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntityLiving;

public class PlayerItemStandData {
	private PlayerGameData pGameData = null;
	private boolean visible = true;
	private boolean purchasable = true;
	private int price = 0;
	private int level = 0;
	private MazeItem mazeItem;
	private Skeleton itemStand = null;
	private MetadataFakeEntity<Skeleton> fakeEntityData = null;

	public PlayerItemStandData(Skeleton itemStand, PlayerGameData pGameData) {
		this.pGameData = pGameData;
		this.itemStand = itemStand;
		mazeItem = TheMaze.getMetadataHelper().getMetadata(itemStand, MetadataKeys.ITEMSTAND_ITEM);
		fakeEntityData = new MetadataFakeEntity<Skeleton>(itemStand);
		setPriceAndUpdateName(mazeItem.getDefaultPrice());
		setItem(EquipmentSlot.HAND, mazeItem.getDefaultItemInHand());
		mazeItem.resetItemStandData(this);
	}

	public PlayerGameData getPlayerGameData() {
		return pGameData;
	}

	public Player getPlayer() {
		return pGameData.getPlayer();
	}

	public Game getGame() {
		return pGameData.getGame();
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isPurchasable() {
		return purchasable && mazeItem.canPurchase(this);
	}

	public int getPrice() {
		return price;
	}

	public int getLevel() {
		return level;
	}

	public MazeItem getMazeItem() {
		return mazeItem;
	}

	public PlayerItemStandData getTwin() {
		return pGameData.getItemStandData(pGameData.getGame().getTwinItemStand(itemStand));
	}

	public ItemStack getItem(EquipmentSlot slot) {
		switch(slot) {
		case CHEST:
			return fakeEntityData.getFakeEntity().getEquipment().getChestplate();
		case FEET:
			return fakeEntityData.getFakeEntity().getEquipment().getBoots();
		case HAND:
			return fakeEntityData.getFakeEntity().getEquipment().getItemInMainHand();
		case HEAD:
			return fakeEntityData.getFakeEntity().getEquipment().getHelmet();
		case LEGS:
			return fakeEntityData.getFakeEntity().getEquipment().getLeggings();
		case OFF_HAND:
			return fakeEntityData.getFakeEntity().getEquipment().getItemInOffHand();
		default:
			return null;
		}
	}

	public MetadataFakeEntity<Skeleton> getFakeEntityData() {
		return fakeEntityData;
	}

	public void setPurchasable(boolean purchasable) {
		this.purchasable = purchasable;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setPriceAndUpdateName(int price) {
		this.price = price;
		setName(mazeItem.getArmorStandName(price));
	}

	public void setName(String name) {
		fakeEntityData.getFakeEntity().setCustomName(name);
	}

	public void setItem(EquipmentSlot slot, ItemStack item) {
		switch(slot) {
		case CHEST:
			fakeEntityData.getFakeEntity().getEquipment().setChestplate(item);
			break;
		case FEET:
			fakeEntityData.getFakeEntity().getEquipment().setBoots(item);
			break;
		case HAND:
			fakeEntityData.getFakeEntity().getEquipment().setItemInMainHand(item);
			break;
		case HEAD:
			fakeEntityData.getFakeEntity().getEquipment().setHelmet(item);
			break;
		case LEGS:
			fakeEntityData.getFakeEntity().getEquipment().setLeggings(item);
			break;
		case OFF_HAND:
			fakeEntityData.getFakeEntity().getEquipment().setItemInOffHand(item);
			break;
		default:
			break;
		}
	}

	public void setVisible(boolean visible) {
		if(!pGameData.getPlayer().isOnline() || pGameData.getPlayer().getWorld() != pGameData.getGame().getWorld()) {
			return;
		}
		EntityPlayer nmsPlayer = ((CraftPlayer) pGameData.getPlayer()).getHandle();

		this.visible = visible;
		if(visible) {
			PacketPlayOutEntityDestroy destroyEntityPacket = new PacketPlayOutEntityDestroy(itemStand.getEntityId());
			nmsPlayer.playerConnection.sendPacket(destroyEntityPacket);
		} else {
			EntitySkeleton nmsItemStand = (EntitySkeleton) ((CraftSkeleton) itemStand).getHandle();
			PacketPlayOutSpawnEntityLiving spawnEntityPacket = new PacketPlayOutSpawnEntityLiving(nmsItemStand);
			nmsPlayer.playerConnection.sendPacket(spawnEntityPacket);
			updateArmorStand();
		}
	}

	public void resetArmorStand() {
		mazeItem.resetItemStandData(this);
		updateArmorStand();
	}

	public void updateArmorStand() {
		if(!pGameData.getPlayer().isOnline() || pGameData.getPlayer().getWorld() != pGameData.getGame().getWorld()) {
			return;
		}
		EntityPlayer nmsPlayer = ((CraftPlayer) pGameData.getPlayer()).getHandle();

		nmsPlayer.playerConnection.sendPacket(fakeEntityData.getMetadataPacket());
		for(EnumItemSlot s : EnumItemSlot.values()) {
			nmsPlayer.playerConnection.sendPacket(fakeEntityData.getEquipmentPacket(s));
		}
	}

}
