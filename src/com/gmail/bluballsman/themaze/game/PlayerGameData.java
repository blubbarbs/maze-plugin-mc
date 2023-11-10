package com.gmail.bluballsman.themaze.game;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.item.TowerTeleporterEntities;
import com.gmail.bluballsman.themaze.game.item.items.Wool;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_15_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_15_R1.ScoreboardServer;

public class PlayerGameData {
	private Player player;
	private Game game;
	private MazeTeam team;
	private Wool possessedWool = null;
	private ItemStack[] preWoolInventory;
	private int souls = 0;
	private boolean hasReaperEyes = false;
	private int respawnTimer = 0;
	private HashMap<Skeleton, PlayerItemStandData> standPlayerData = new HashMap<Skeleton, PlayerItemStandData>();
	private BukkitRunnable respawnRunnable;
	private BukkitRunnable potionRechargeRunnable;

	public PlayerGameData(Player player, Game game) {
		this.player = player;
		this.game = game;
	}

	public Player getPlayer() {
		return player;
	}

	public Game getGame() {
		return game;
	}

	public MazeTeam getTeam() {
		return team;
	}

	public String getPlayerName() {
		return team.getTeamColor() + player.getName() + ChatColor.RESET;
	}

	public boolean isRespawning() {
		return respawnRunnable != null;
	}
	
	public boolean hasReaperEyes() {
		return hasReaperEyes;
	}
	
	public boolean hasWool() {
		return possessedWool != null;
	}

	public Wool getPossessedWool() {
		return possessedWool;
	}

	public int getSouls() {
		return souls;
	}

	public int getFirstMatchingInventorySlot(Material m) {
		for(int i = 0; i < player.getInventory().getContents().length; i++) {
			ItemStack stack = player.getInventory().getItem(i);

			if(stack != null && stack.getType() == m) {
				return i;
			}
		}
		return -1;
	}
	
	public ItemStack getFirstMatchingItem(Material m) {
		int firstMatchingSlot = getFirstMatchingInventorySlot(m);
		return firstMatchingSlot > -1 ? player.getInventory().getItem(firstMatchingSlot) : null;
	}
	
	public int count(Material m) {
		int count = 0;
		
		for(int i = 0; i < player.getInventory().getContents().length; i++) {
			ItemStack stack = player.getInventory().getItem(i);
			
			if(stack != null && stack.getType() == m) {
				count += stack.getAmount();
			}
		}
		
		return count;
	}
	
	
	public PlayerItemStandData getItemStandData(Skeleton stand) {
		return standPlayerData.computeIfAbsent(stand, s -> new PlayerItemStandData(s, this));
	}
	
	
	public void refreshPlayerInstance() {
		player = Bukkit.getPlayer(player.getUniqueId());
	}

	public void setSouls(int souls) {
		this.souls = souls = souls > 0 ? souls : 0;
		game.getSoulsObjective().getScore(player.getName()).setScore(this.souls);
	}

	public void setTeam(MazeTeam team) {
		this.team = team;
	}
	
	public void setReaperEyes(boolean hasReaperEyes) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		net.minecraft.server.v1_15_R1.Scoreboard nmsScoreboard = ((CraftScoreboard) game.getScoreboard()).getHandle();
		net.minecraft.server.v1_15_R1.ScoreboardObjective nmsSoulsObjective = nmsScoreboard.getObjective("souls");
		
		this.hasReaperEyes = hasReaperEyes;
		if(hasReaperEyes) {
			Packet<?> createScoreboardPacket = new PacketPlayOutScoreboardObjective(nmsSoulsObjective, 0);	
			Packet<?> setDisplayPacket = new PacketPlayOutScoreboardDisplayObjective(2, nmsSoulsObjective);
			
			nmsPlayer.playerConnection.sendPacket(createScoreboardPacket);
			nmsPlayer.playerConnection.sendPacket(setDisplayPacket);
			for(PlayerGameData pGameData : game.getAllPlayerGameData()) {
				Packet<?> scoreboardScorePacket = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "souls", pGameData.getPlayer().getName(), pGameData.getSouls());
				nmsPlayer.playerConnection.sendPacket(scoreboardScorePacket);
			}
		} else {
			Packet<?> removeScoreboardPacket = new PacketPlayOutScoreboardObjective(nmsSoulsObjective, 1);	
			
			nmsPlayer.playerConnection.sendPacket(removeScoreboardPacket);
		}
	}
	
	public void sendMessageToPlayer(String message) {
		player.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + "[MAZE]" + " " + ChatColor.RESET + message);
	}

	public void setRespawnTimer(int time) {
		respawnTimer = time;
		player.setNoDamageTicks(time * 20);

		if(respawnRunnable == null) {
			respawnRunnable = new BukkitRunnable() {
				@Override
				public void run() {
					game.getRespawnObjective().getScore(player.getName()).setScore(respawnTimer);
					if(respawnTimer == 0) {
						player.setTicksLived(1);
						player.setNoDamageTicks(Game.PLAYER_RESPAWN_INVUL_TICKS);
						team.giveStartingLoadout(player);
						cancelRespawnTimer();
					}
					respawnTimer--;
				}
			};
			respawnRunnable.runTaskTimer(TheMaze.getInstance(), 0L, 20L);
		}
	}
	
	public void setPotionRechargeTimer(int time) {
		if(potionRechargeRunnable == null) { 
			player.setCooldown(Material.SPLASH_POTION, time * 20);
			
			potionRechargeRunnable = new BukkitRunnable() {

				@Override
				public void run() {
					int potionSlot = getFirstMatchingInventorySlot(Material.SPLASH_POTION);
					ItemStack firstSplashPotion = player.getInventory().getItem(potionSlot);
					PotionMeta potMeta = (PotionMeta) firstSplashPotion.getItemMeta();
					
					potionRechargeRunnable = null;
					if(!potMeta.hasCustomEffects()) {
						ItemStack singleHealingPotion = Game.STARTING_POTIONS.clone();
						singleHealingPotion.setAmount(1);
						player.getInventory().setItem(potionSlot, singleHealingPotion);
						setPotionRechargeTimer(20);
					} else {
						firstSplashPotion.setAmount(2);
					}
				}
			};
			
			potionRechargeRunnable.runTaskLater(TheMaze.getInstance(), time * 20);
		}		
	}
	
	public void cancelRespawnTimer() {
		if(respawnRunnable != null) {
			respawnRunnable.cancel();
			respawnRunnable = null;
		}
		game.getRespawnObjective().getScore(player.getName()).setScore(0);	
		respawnTimer = 0;
	}
	
	public void cancelPotionRechargeTimer() {
		if(potionRechargeRunnable != null) {
			player.setCooldown(Material.SPLASH_POTION, 0);
			potionRechargeRunnable.cancel();
			potionRechargeRunnable = null;
		}
	}
	
	public void resetStats() {
		setSouls(0);
		standPlayerData.forEach((k, v) -> v.resetArmorStand());
		setWoolPossession(null);
		player.getInventory().clear();
		cancelPotionRechargeTimer();
		setReaperEyes(false);
		
		if(TheMaze.getMetadataHelper().hasMetadata(player, MetadataKeys.TOWER_TELEPORTER)) {
			TowerTeleporterEntities tTeleporter = TheMaze.getMetadataHelper().getMetadata(player, MetadataKeys.TOWER_TELEPORTER);
			tTeleporter.remove();
		}
	}

	public void setWoolPossession(Wool wool) {
		if(wool == null && possessedWool != null) {
			possessedWool = null;
			player.getInventory().setContents(preWoolInventory);
		} else if(wool != null && possessedWool == null) {
			possessedWool = wool;
			preWoolInventory = new ItemStack[player.getInventory().getContents().length];
			for(int i = 0; i < preWoolInventory.length; i++) {
				preWoolInventory[i] = player.getInventory().getItem(i);
			}
			player.getInventory().clear();
			player.getActivePotionEffects().forEach(pe -> player.removePotionEffect(pe.getType()));
			if(TheMaze.getMetadataHelper().hasMetadata(player, MetadataKeys.TOWER_TELEPORTER)) {
				TowerTeleporterEntities tTeleporter = TheMaze.getMetadataHelper().getMetadata(player, MetadataKeys.TOWER_TELEPORTER);
				tTeleporter.remove();
			}
			ItemStack woolStack = team.getWoolItem(wool);
			for(int i = 0; i < 9; i++) {
				player.getInventory().setItem(i, woolStack);
			}
		}
		team.getAllPlayerGameData().forEach(pGameData -> pGameData.updateAllItemStands());
		game.updateGameInfoObjective();
	}

	public void updateAllItemStands() {
		standPlayerData.forEach((k, v) -> v.updateArmorStand());
	}
	
	public void updateSoulActionbar() {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		PacketPlayOutTitle timingPacket = new PacketPlayOutTitle(20, 10, 20);
		String individualSoulString = "" + ChatColor.GOLD + ChatColor.BOLD + "Individual Souls: " + souls + ChatColor.RESET;
		String teamSoulString = "" + ChatColor.AQUA + ChatColor.BOLD + "Team Souls: " + team.getCollectiveSoulCount();
		int rawTextLength = individualSoulString.length() + teamSoulString.length();
		String actionBarString = individualSoulString + StringUtils.repeat(" ", 55 - rawTextLength) + teamSoulString;
		IChatBaseComponent actionBarText = CraftChatMessage.fromStringOrNull(actionBarString);
		PacketPlayOutTitle actionBarPacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.ACTIONBAR, actionBarText);
		
		nmsPlayer.playerConnection.sendPacket(timingPacket);
		nmsPlayer.playerConnection.sendPacket(actionBarPacket);
	}
}
