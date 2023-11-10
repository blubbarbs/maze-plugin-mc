package com.gmail.bluballsman.themaze.game;

import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

public class PreviousPlayerState {
	private Player player;
	private double health;
	private float exhaustion;
	private float saturation;
	private float exp;
	private int experienceLevel;
	private int hunger;
	private int fire;
	private Collection<PotionEffect> potionEffects;
	private ItemStack[] previousInventory;
	private boolean isFlying;
	private GameMode gamemode;
	private Location previousSpawnLocation;
	private Location previousLocation;
	private Scoreboard previousBoard;
	
	public PreviousPlayerState(Player p) {
		player = p;
		savePlayerData();
	}
	
	public void refreshPlayerInstance(Player p) {
		player = p;
	}
	
	public void savePlayerData() {
		health = player.getHealth();
		exhaustion = player.getExhaustion();
		saturation= player.getSaturation();
		exp = player.getExp();
		experienceLevel = player.getLevel();
		hunger = player.getFoodLevel();
		fire = player.getFireTicks();
		potionEffects = player.getActivePotionEffects();
		previousInventory = player.getInventory().getContents();
		isFlying = player.isFlying();
		gamemode = player.getGameMode();
		previousSpawnLocation = player.getBedSpawnLocation();
		previousLocation = player.getLocation();
		previousBoard = player.getScoreboard();
	}
	
	public void normalizePlayerStats() {
		for(PotionEffect pe : player.getActivePotionEffects()) {
			player.removePotionEffect(pe.getType());
		}
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
		player.setExhaustion(0);
		player.setSaturation(20F);
		player.setExhaustion(0F);
		player.setExp(0);
		player.setLevel(0);
		player.setFoodLevel(20);
		player.setFireTicks(0);
		player.leaveVehicle();
		player.getInventory().clear();
		player.setFlying(false);
		player.setGameMode(GameMode.ADVENTURE);
	}
	
	public void loadPlayerSaveData() {
		for(PotionEffect pe : player.getActivePotionEffects()) {
			player.removePotionEffect(pe.getType());
		}
		player.addPotionEffects(potionEffects);
		player.setHealth(health);
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setExp(exp);
		player.setLevel(experienceLevel);
		player.setFoodLevel(hunger);
		player.setFireTicks(fire);
		player.getInventory().setContents(previousInventory);
		player.setGameMode(gamemode);
		player.setFlying(isFlying);
		player.setBedSpawnLocation(previousSpawnLocation, false);
		player.setNoDamageTicks(0);
		player.setScoreboard(previousBoard);
		player.teleport(previousLocation);
		TheMaze.getMetadataHelper().removeMetadata(player, MetadataKeys.PREVIOUS_PLAYER_STATE);
	}
	
}
