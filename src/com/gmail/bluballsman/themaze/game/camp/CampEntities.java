package com.gmail.bluballsman.themaze.game.camp;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.bluballsman.themaze.MetadataFakeEntity;
import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import net.md_5.bungee.api.ChatColor;

public class CampEntities<T extends LivingEntity> {
	private CampBoss<T> boss;
	private T campEntity;
	private MetadataFakeEntity<T> fakeEntityData;
	private ArmorStand armorStand;
	private Location spawnLoc;
	private BukkitRunnable respawnRunnable;
	private int respawnTime = 0;
	
	public CampEntities(CampBoss<T> boss, Location spawnLoc) {
		this.boss = boss;
		this.spawnLoc = spawnLoc;
		armorStand = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
		armorStand.setVisible(false);
		armorStand.setInvulnerable(true);
		armorStand.setGravity(false);
		armorStand.setCollidable(false);
		armorStand.setCustomNameVisible(true);
	}
	
	public CampBoss<?> getBossInfo() {
		return boss;
	}
	
	public T getBoss() {
		return campEntity;
	}
	
	public ArmorStand getArmorStand() {
		return armorStand;
	}
	
	public boolean isRespawning() {
		return respawnTime > 0;
	}
	
	public MetadataFakeEntity<T> getFakeEntityData() {
		return fakeEntityData;
	}
	
	@SuppressWarnings("unchecked")
	public void spawnNewBoss() {
		campEntity = (T) spawnLoc.getWorld().spawnEntity(spawnLoc, boss.getEntityType());
		campEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(boss.getMaxHealth());
		campEntity.setHealth(boss.getMaxHealth());
		TheMaze.getMetadataHelper().setMetadata(campEntity, MetadataKeys.CAMP, this);
		fakeEntityData = new MetadataFakeEntity<T>(campEntity);
		armorStand.addPassenger(campEntity);
		armorStand.setCustomName(campEntity.getName());
		if(campEntity instanceof Boss) {
			Boss b = (Boss) campEntity;
			b.getBossBar().setVisible(false);
		}
		cancelRespawnTimer();
	}
	
	public void setRespawnTimer(int time) {
		respawnTime = time;
		if(respawnRunnable == null) {
			respawnRunnable = new BukkitRunnable() {

				@Override
				public void run() {
					respawnTime--;
					
					if(respawnTime == 0) {
						spawnNewBoss();
					} else {
						String timeString = String.format("%02d:%02d", respawnTime / 60, respawnTime % 60);
						armorStand.setCustomName("Respawns in "+ timeString);
					}
				}
			};
			
			respawnRunnable.runTaskTimer(TheMaze.getInstance(), 0L, 20L);
		}
	}
	
	public void cancelRespawnTimer() {
		if(respawnRunnable != null) {
			respawnRunnable.cancel();
			respawnRunnable = null;
		}
		respawnTime = 0;
	}
	
	public void updateHealth() {
		double percentageOfMax = campEntity.getHealth() / boss.getMaxHealth();
		int greenBars = (int) Math.ceil(percentageOfMax * 10);
		String customName = "";
		customName += "" + ChatColor.GREEN + ChatColor.BOLD + StringUtils.repeat("|", (int) greenBars) + ChatColor.RESET;
		customName += "" + ChatColor.RED + ChatColor.BOLD + StringUtils.repeat("|", 10 - greenBars); 
		fakeEntityData.getFakeEntity().setCustomName(customName);
	}
}
