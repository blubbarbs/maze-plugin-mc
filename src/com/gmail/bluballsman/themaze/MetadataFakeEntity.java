package com.gmail.bluballsman.themaze;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DataWatcher;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;

public class MetadataFakeEntity<T extends Entity> {
	private int blueprintEntityID;
	private T fakeEntity;
	
	@SuppressWarnings("unchecked")
	public <E> MetadataFakeEntity(T blueprintEntity) {
		blueprintEntityID = blueprintEntity.getEntityId();
		net.minecraft.server.v1_15_R1.Entity nmsEntity = ((CraftEntity) blueprintEntity).getHandle();
		net.minecraft.server.v1_15_R1.Entity fakeNMSEntity = nmsEntity.getEntityType().createCreature(nmsEntity.world, null, null, null, new BlockPosition(0, 0, 0), EnumMobSpawn.EVENT, false, false);
		fakeEntity = (T) fakeNMSEntity.getBukkitEntity();

		for(DataWatcher.Item<?> dataWatcherItem : nmsEntity.getDataWatcher().c()) {
			DataWatcherObject<E> obj = (DataWatcherObject<E>) dataWatcherItem.a();
			E val = nmsEntity.getDataWatcher().get(obj);
			fakeNMSEntity.getDataWatcher().set(obj, val);
		}
		
		if(blueprintEntity instanceof LivingEntity) {
			EntityLiving nmsEntityLiving = (EntityLiving) nmsEntity;
			EntityLiving fakeNMSEntityLiving = (EntityLiving) fakeNMSEntity;
			
			for(EnumItemSlot s : EnumItemSlot.values()) {
				fakeNMSEntityLiving.setSlot(s, nmsEntityLiving.getEquipment(s));
			}
		}
	}
	
	public T getFakeEntity() {
		return fakeEntity;
	}
	
	public net.minecraft.server.v1_15_R1.Entity getNMSFakeEntity() {
		return ((CraftEntity) fakeEntity).getHandle();
	}
	
	public PacketPlayOutEntityMetadata getMetadataPacket() {
		net.minecraft.server.v1_15_R1.Entity fakeNMSEntity = ((CraftEntity) fakeEntity).getHandle();
		return new PacketPlayOutEntityMetadata(blueprintEntityID, fakeNMSEntity.getDataWatcher(), true);
	}
	
	public PacketPlayOutEntityEquipment getEquipmentPacket(EnumItemSlot slot) {
		if(fakeEntity instanceof LivingEntity) {
			net.minecraft.server.v1_15_R1.EntityLiving fakeEntityNMS = ((CraftLivingEntity) fakeEntity).getHandle();
			
			return new PacketPlayOutEntityEquipment(blueprintEntityID, slot, fakeEntityNMS.getEquipment(slot));
		} else {
			return null;
		}
		
	}
}
