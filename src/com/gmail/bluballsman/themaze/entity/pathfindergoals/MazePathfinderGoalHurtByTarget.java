package com.gmail.bluballsman.themaze.entity.pathfindergoals;

import java.util.function.Predicate;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.PathfinderGoalHurtByTarget;

public class MazePathfinderGoalHurtByTarget extends PathfinderGoalHurtByTarget{
	Predicate<? super EntityLiving> predicate = null;
	
	public MazePathfinderGoalHurtByTarget(EntityCreature entitycreature, Predicate<? super EntityLiving> predicate, Class<?>... excludedHelperTypes) {
		super(entitycreature, excludedHelperTypes);
		this.predicate = predicate;
	}
	
	@Override
	public boolean b() {
		return super.b() && (predicate == null || predicate.test(e.getGoalTarget()));
	}
	
}
