package com.gmail.bluballsman.themaze.entity.pathfindergoals;

import java.util.function.Predicate;

import net.minecraft.server.v1_15_R1.EntityInsentient;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalNearestAttackableTarget;

public class MazePathfinderGoalNearestAttackableTarget <T extends EntityLiving> extends PathfinderGoalNearestAttackableTarget<T> {
	private Predicate<EntityLiving> predicate;
	
	public MazePathfinderGoalNearestAttackableTarget(EntityInsentient entity, Class<T> targetClass,
			boolean checkSight) {
		this(entity, targetClass, checkSight, true);
	}
	
	public MazePathfinderGoalNearestAttackableTarget(EntityInsentient entity, Class<T> targetClass,
			boolean checkSight, boolean nearbyOnly) {
		this(entity, targetClass, 10, checkSight, nearbyOnly, null);
	}
	
	public MazePathfinderGoalNearestAttackableTarget(EntityInsentient entity, Class<T> targetClass, int targetChance,
			boolean checkSight, boolean nearbyOnly, Predicate<EntityLiving> predicate) {
		super(entity, targetClass, targetChance, false, nearbyOnly, predicate);
		this.predicate = predicate;
		
		if(!checkSight) {
			d.c(); // entity predicate set check sight ignore flag
		}
	}
	
	@Override // should continue executing
	public boolean b() {
		return super.b() && (predicate == null || predicate.test(e.getGoalTarget()));
	}
	
	
}
