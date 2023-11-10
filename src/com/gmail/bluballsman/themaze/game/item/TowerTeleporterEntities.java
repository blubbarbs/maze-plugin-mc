package com.gmail.bluballsman.themaze.game.item;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.TrigUtils;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EntityVex;
import net.minecraft.server.v1_15_R1.WorldServer;

public class TowerTeleporterEntities {
	public static final PotionEffect INVISIBLE_EFFECT = new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 1, false, false);
	
	private Player thrower;
	private EnderSignal enderEye;
	private Vex vex;
	private World world;
	private Location target;
	private BukkitRunnable logicRunnable;
	private Vector velocityToTarget;
	
	public TowerTeleporterEntities(Location target, Player thrower) {
		world = target.getWorld();
		this.target = target;
		this.thrower = thrower;
	}
	
	public void spawn(Location l) {
		WorldServer nmsWorld = ((CraftWorld) world).getHandle();
		EntityVex nmsVex = new EntityVex(EntityTypes.VEX, ((CraftWorld)world).getHandle());
		vex = (Vex) nmsVex.getBukkitEntity();
		vex.setSilent(true);
		INVISIBLE_EFFECT.apply(vex);
		vex.getEquipment().clear();
		vex.teleport(l);
		nmsWorld.addEntity(nmsVex, SpawnReason.CUSTOM); // had to use nms methods as clearing the equipment doesnt correctly update to players for some reason
		enderEye = (EnderSignal) world.spawnEntity(l, EntityType.ENDER_SIGNAL);
		TheMaze.getMetadataHelper().setMetadata(vex, MetadataKeys.TOWER_TELEPORTER, this);
		TheMaze.getMetadataHelper().setMetadata(thrower, MetadataKeys.TOWER_TELEPORTER, this);
		vex.addPassenger(enderEye);
		enderEye.setDespawnTimer(-9999);
		velocityToTarget = TrigUtils.getFixedDistanceCongruent(vex.getLocation().toVector(), target.toVector(), .75);
		logicRunnable = new BukkitRunnable() {

			@Override
			public void run() {
				doMovement();
			}
			
		};
		
		logicRunnable.runTaskTimer(TheMaze.getInstance(), 0L, 0L);
		
	}
	
	public EnderSignal getEnderEye() {
		return enderEye;
	}
	
	public Vex getVex() {
		return vex;
	}
	
	public BukkitRunnable getLogicRunnable() {
		return logicRunnable;
	}
	
	public void remove() {
		logicRunnable.cancel();
		TheMaze.getMetadataHelper().removeMetadata(thrower, MetadataKeys.TOWER_TELEPORTER);
		TheMaze.getMetadataHelper().removeMetadata(vex, MetadataKeys.TOWER_TELEPORTER);
		vex.remove();
		enderEye.remove();
	}
	
	private void doMovement() {
		if(vex.getLocation().distance(target) <= 1.7) {
			reachTarget();
		} else {
			vex.setVelocity(velocityToTarget);
		}
	}
	
	private void reachTarget() {
		vex.teleport(target);
		TheMaze.getMetadataHelper().removeMetadata(vex, MetadataKeys.TOWER_TELEPORTER);
		enderEye.setTargetLocation(target);
		enderEye.setDespawnTimer(0);
		enderEye.setDropItem(false);
		vex.remove();
		logicRunnable.cancel();
		logicRunnable = new BukkitRunnable() {

			@Override
			public void run() {
				thrower.teleport(target);
				thrower.setNoDamageTicks(15);
				TheMaze.getMetadataHelper().removeMetadata(thrower, MetadataKeys.TOWER_TELEPORTER);
			}
			
		};
		logicRunnable.runTaskLater(TheMaze.getInstance(), 80);
	}
	
}
