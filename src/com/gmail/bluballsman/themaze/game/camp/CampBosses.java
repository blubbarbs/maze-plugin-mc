package com.gmail.bluballsman.themaze.game.camp;

import java.util.HashSet;

import org.bukkit.entity.ElderGuardian;

public class CampBosses {
	public static HashSet<CampBoss<?>> ALL_BOSSES = new HashSet<CampBoss<?>>();
	
	public static CampBoss<ElderGuardian> ELDER_GUARDIAN = new CampBoss<ElderGuardian>(ElderGuardian.class, 200);
	
	static {
		//ALL_BOSSES.add(ELDER_GUARDIAN);
	}
}
