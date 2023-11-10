package com.gmail.bluballsman.themaze.game.camp;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class CampBoss<T extends LivingEntity> {
	public static double COMBAT_RADIUS = 6;
	
	private EntityType entityType;
	private double maxHealth;
	
	public CampBoss(Class<T> clazz, double maxHealth) {
		this.maxHealth = maxHealth;
		
		for(EntityType type : EntityType.values()) {
			if(clazz.isAssignableFrom(type.getEntityClass())) {
				entityType = type;
				break;
			}
		}
	}
	
	public EntityType getEntityType() {
		return entityType;
	}
	
	public double getMaxHealth() {
		return maxHealth;
	}
	
	public CampEntities<T> getNewCampEntities(Location l) {
		return new CampEntities<T>(this, l);
	}

}
