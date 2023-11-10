package com.gmail.bluballsman.themaze.network;

import java.util.UUID;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.plugin.Plugin;

import com.comphenix.tinyprotocol.Reflection;
import com.comphenix.tinyprotocol.Reflection.FieldAccessor;
import com.comphenix.tinyprotocol.TinyProtocol;
import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.PlayerGameData;
import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.camp.CampEntities;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import io.netty.channel.Channel;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_15_R1.WorldServer;

public class TinyProtocolAdapter extends TinyProtocol {
	private FieldAccessor<UUID> spawnEntityLivingPacketEntityUID = Reflection.getField(PacketPlayOutSpawnEntityLiving.class, UUID.class, 0);
	private FieldAccessor<Integer> entityMetadataPacketEntityID = Reflection.getField(PacketPlayOutEntityMetadata.class, int.class, 0);
	private FieldAccessor<Integer> entityEquipmentPacketEntityID = Reflection.getField(PacketPlayOutEntityEquipment.class, int.class, 0);
	private FieldAccessor<EnumItemSlot> entityEquipmentPacketItemSlot = Reflection.getField(PacketPlayOutEntityEquipment.class, EnumItemSlot.class, 0);
	private FieldAccessor<String> scoreboardObjectivePacketObjective = Reflection.getField(PacketPlayOutScoreboardObjective.class, String.class, 0);
	private FieldAccessor<Integer> scoreboardObjectivePacketAction = Reflection.getField(PacketPlayOutScoreboardObjective.class, int.class, 0);
	
	public TinyProtocolAdapter(Plugin plugin) {
		super(plugin);
	}
	
	@Override
	public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
		if(receiver != null && TheMaze.getMetadataHelper().hasMetadata(receiver, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData receiverGameData = TheMaze.getMetadataHelper().getMetadata(receiver, MetadataKeys.PLAYER_GAME_DATA);
			EntityPlayer nmsReceiver = ((CraftPlayer) receiver).getHandle();
			WorldServer nmsWorld = nmsReceiver.getWorldServer();
			
			if(packet instanceof PacketPlayOutSpawnEntityLiving) {
				UUID entityUUID = spawnEntityLivingPacketEntityUID.get(packet);
				Entity entity = nmsWorld.getEntity(entityUUID).getBukkitEntity();
				
				if(TheMaze.getMetadataHelper().hasMetadata(entity, MetadataKeys.ITEMSTAND_ITEM)) {
					Skeleton itemStand = (Skeleton) entity;
					PlayerItemStandData standData = receiverGameData.getItemStandData(itemStand);
					if(!standData.isVisible()) {
						return null;
					}
				}
			} else if(packet instanceof PacketPlayOutEntityMetadata) {
				int entityID = entityMetadataPacketEntityID.get(packet);
				Entity entity = nmsWorld.getEntity(entityID).getBukkitEntity();				
				
				if(TheMaze.getMetadataHelper().hasMetadata(entity, MetadataKeys.ITEMSTAND_ITEM)) {
					Skeleton itemStand = (Skeleton) entity;
					PlayerItemStandData standData = receiverGameData.getItemStandData(itemStand);
					PacketPlayOutEntityMetadata newMetadataPacket;
					
					if(!standData.isPurchasable()) {
						String previousName = standData.getFakeEntityData().getFakeEntity().getCustomName();
						standData.getFakeEntityData().getFakeEntity().setCustomName("---");
						newMetadataPacket = standData.getFakeEntityData().getMetadataPacket();
						standData.getFakeEntityData().getFakeEntity().setCustomName(previousName);
					} else {
						newMetadataPacket = standData.getFakeEntityData().getMetadataPacket();
					}
					
					return super.onPacketOutAsync(receiver, channel, newMetadataPacket);
				} else if(TheMaze.getMetadataHelper().hasMetadata(entity, MetadataKeys.CAMP)) {
					CampEntities<?> campEntities = TheMaze.getMetadataHelper().getMetadata(entity, MetadataKeys.CAMP);
					PacketPlayOutEntityMetadata newMetadataPacket = campEntities.getFakeEntityData().getMetadataPacket();
					
					return super.onPacketOutAsync(receiver, channel, newMetadataPacket);
				}
			} else if(packet instanceof PacketPlayOutEntityEquipment) {
				int entityID = entityEquipmentPacketEntityID.get(packet);
				Entity entity = nmsWorld.getEntity(entityID).getBukkitEntity();
				
				if(TheMaze.getMetadataHelper().hasMetadata(entity, MetadataKeys.ITEMSTAND_ITEM)) {
					Skeleton itemStand = (Skeleton) entity;
					PlayerItemStandData standData = receiverGameData.getItemStandData(itemStand);
					EnumItemSlot slot = entityEquipmentPacketItemSlot.get(packet);
					PacketPlayOutEntityEquipment newEquipmentPacket = standData.getFakeEntityData().getEquipmentPacket(slot);
					
					return super.onPacketOutAsync(receiver, channel, newEquipmentPacket);
				}				
			} else if(packet instanceof PacketPlayOutScoreboardObjective) {
				if(!receiverGameData.hasReaperEyes() && scoreboardObjectivePacketObjective.get(packet).equals("souls")) {
					scoreboardObjectivePacketAction.set(packet, 1);
				}
			}
		} 
		
		return super.onPacketOutAsync(receiver, channel, packet);
	}
}
