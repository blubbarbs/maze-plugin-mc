package com.gmail.bluballsman.themaze.game;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.scoreboard.Team;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.item.MazeItems;
import com.gmail.bluballsman.themaze.game.item.items.Wool;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;
import com.gmail.bluballsman.themaze.metadata.PluginMetadataHelper;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;

public class MazeTeam {
	private String teamName;
	private Game game;
	private Team scoreboardTeam;
	private HashSet<UUID> playerUUIDs = new HashSet<UUID>();
	private HashSet<Wool> collectedWools = new HashSet<Wool>();
	private HashSet<Sign> mapSigns = new HashSet<Sign>();
	private Location spawnLocation = null;
	private Color armorColor = null;
	private ChatColor teamColor = ChatColor.GRAY;
	private ItemStack carpet = null;
	private ItemStack signStack = null;
	private Material pedestalMaterial = null;
	private MapCursor.Type mapCursor = null;

	public MazeTeam(String teamName, Game game) {
		this.teamName = teamName;
		this.game = game;
		scoreboardTeam = game.getScoreboard().registerNewTeam(teamName);
		scoreboardTeam.setAllowFriendlyFire(false);
		scoreboardTeam.setCanSeeFriendlyInvisibles(true);
	}

	public String getName() {
		return teamColor + teamName + ChatColor.RESET;
	}

	public String getNameClean() {
		return teamName;
	}
	
	public HashSet<Player> getPlayers() {
		HashSet<Player> players = new HashSet<Player>();
		for(UUID pID : playerUUIDs) {
			Player p = Bukkit.getPlayer(pID);
			if(p != null) {
				players.add(p);
			}
		}
		return players;
	}
	
	public HashSet<PlayerGameData> getAllPlayerGameData() {
		HashSet<PlayerGameData> playerGameData = new HashSet<PlayerGameData>();
		
		for(UUID pID : getPlayerUUIDs()) {
			Player p = Bukkit.getPlayer(pID);
			PluginMetadataHelper helper = TheMaze.getMetadataHelper();
			PlayerGameData pGameData = p != null ? helper.getMetadata(p, MetadataKeys.PLAYER_GAME_DATA) : helper.getMetadataOfflinePlayer(Bukkit.getOfflinePlayer(pID), MetadataKeys.PLAYER_GAME_DATA);
			playerGameData.add(pGameData);
		}
		
		return playerGameData;
	}
	
	public HashSet<UUID> getPlayerUUIDs() { 
		return playerUUIDs;
	}

	public boolean isPlayerRegistered(Player p) {
		return playerUUIDs.contains(p.getUniqueId());
	}

	public Team getScoreboardTeam() {
		return scoreboardTeam;
	}

	public int getCollectiveSoulCount() {
		int soulCount = 0;
		for(PlayerGameData pGameData : getAllPlayerGameData()) {
			soulCount += pGameData.getSouls();
		}
		return soulCount;
	}

	public Location getSpawnLocation() {
		return spawnLocation;
	}

	public int getTotalPoints() {
		return collectedWools.size();
	}

	public HashSet<Wool> getCollectedWools() {
		return collectedWools;
	}

	public Color getArmorColor() {
		return armorColor;
	}

	public ChatColor getTeamColor() {
		return teamColor;
	}

	public ItemStack getCarpet() {
		return carpet;
	}
	
	public ItemStack getSignItem() {
		return signStack;
	}
	
	public ItemStack getWoolItem(Wool wool) {
		ItemStack woolStack = new ItemStack(Material.valueOf(wool.getWoolColor().name() + "_WOOL"), 1);
		net.minecraft.server.v1_15_R1.ItemStack nmsWoolStack = CraftItemStack.asNMSCopy(woolStack);
		NBTTagCompound woolCompound = new NBTTagCompound();
		NBTTagList canPlaceOn = new NBTTagList();
		
		canPlaceOn.add(NBTTagString.a("minecraft:" + pedestalMaterial.name().toLowerCase()));
		woolCompound.set("CanPlaceOn", canPlaceOn);
		woolCompound.setInt("HideFlags", 16);
		nmsWoolStack.setTag(woolCompound);
		
		return CraftItemStack.asBukkitCopy(nmsWoolStack);
	}
	
	public Material getPedestalMaterial() {
		return pedestalMaterial;
	}

	public boolean hasWoolPossession() {
		for(PlayerGameData pGameData : getAllPlayerGameData()) {
			if(pGameData.hasWool()) {
				return true;
			}
		}

		return false;
	}
	
	public String getWoolStatus(Wool wool) {
		String prefix = "" + teamColor + ChatColor.RESET + wool.getChatColor() + wool.getWoolColor().name().substring(0, 1) + "   ";
		if(hasCapturedWool(wool)) {
			return prefix + ChatColor.WHITE + ChatColor.BOLD + "SECURED";
		} else if(hasWoolPossession(wool)) {
			return prefix + ChatColor.YELLOW + ChatColor.BOLD + "NABBED";
		} else {
			return prefix + ChatColor.GREEN + ChatColor.BOLD + "AVAILABLE";
		}
	}
	
	public boolean hasWoolPossession(Wool wool) {
		if(collectedWools.contains(wool)) {
			return false;
		}
		
		for(PlayerGameData pGameData : getAllPlayerGameData()) {
			if(pGameData.hasWool() && pGameData.getPossessedWool() == wool) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasCapturedWool(Wool wool)  {
		return collectedWools.contains(wool);
	}

	public MapCursor.Type getMapCursor() {
		return mapCursor;
	}
	
	public HashSet<Sign> getMapSigns() {
		return mapSigns;
	}
	
	public void giveStartingLoadout(Player p) {
		p.getInventory().addItem(Game.STARTING_WOODEN_SWORD);
		p.getInventory().addItem(new ItemStack(Material.MAP, 1));
		p.getInventory().addItem(carpet);
		p.getInventory().addItem(signStack);
		p.getInventory().addItem(Game.STARTING_POTIONS);
	}

	public MazeTeam setSpawnLocation(Location spawnLocation) {
		this.spawnLocation = spawnLocation;
		return this;
	}

	public MazeTeam setArmorColor(Color armorColor) {
		this.armorColor = armorColor;
		return this;
	}

	public MazeTeam setTeamColor(ChatColor teamColor) {
		this.teamColor = teamColor;
		scoreboardTeam.setColor(teamColor);
		return this;
	}

	public MazeTeam setCarpet(Material carpetMaterial) {
		carpet = MazeItems.setCanPlaceOnGround(game.getMap(), new ItemStack(carpetMaterial, 2));
		return this;
	}
	
	public MazeTeam setSignItem(Material signMaterial) {
		signStack = (MazeItems.setCanPlaceOnGround(game.getMap(), new ItemStack(signMaterial, 2)));
		return this;
	}

	public MazeTeam setPedestalMaterial(Material pedestalMaterial) {
		this.pedestalMaterial = pedestalMaterial;
		return this;
	}

	public MazeTeam setMapCursor(MapCursor.Type mapCursor) {
		this.mapCursor = mapCursor;
		return this;
	}

	public void registerPlayer(Player p) {
		playerUUIDs.add(p.getUniqueId());
		scoreboardTeam.addEntry(p.getName());
	}

	public void unregisterPlayer(Player p) {
		playerUUIDs.remove(p.getUniqueId());
		scoreboardTeam.removeEntry(p.getName());
	}

	public void captureWool(Wool wool) {
		Skeleton woolStand = game.getWoolStand(wool);
		
		collectedWools.add(wool);
		if(getTotalPoints() == 3) {
			game.winGame(this);
		}
		game.broadcastMessage(getName() + " team has captured the " + wool.getName() + "!");
		game.getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 2.0F, 1.5F));
		getAllPlayerGameData().forEach(pGameData -> {
			PlayerItemStandData data = pGameData.getItemStandData(woolStand);
			data.setPurchasable(false);
			data.updateArmorStand();
		});
		
	}

	public void deductSouls(Player buyer, int souls) {
		PlayerGameData buyerGameData = TheMaze.getMetadataHelper().getMetadata(buyer, MetadataKeys.PLAYER_GAME_DATA);
		int soulsToDeductFromBuyer = buyerGameData.getSouls() < souls ? buyerGameData.getSouls() : souls;
		int leftoverTotal = souls - soulsToDeductFromBuyer;
		
		if(leftoverTotal > 0) {
			int remainingTeamSouls = getCollectiveSoulCount() - soulsToDeductFromBuyer;
			int leftoverToBeSplit = leftoverTotal;
			
			for(Player p : getPlayers()) {
				if(p != buyer) {
					PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
					double percentageOfSouls = ((double) pGameData.getSouls()) / remainingTeamSouls;
					int deducted = (int) Math.round(percentageOfSouls * leftoverTotal);
					deducted = leftoverToBeSplit - deducted > 0 ? deducted : leftoverToBeSplit;
					leftoverToBeSplit -= deducted;
					pGameData.setSouls(pGameData.getSouls() - deducted);
				}
			}
		}
		buyerGameData.setSouls(buyerGameData.getSouls() - soulsToDeductFromBuyer);
	}

	public void broadcastMessage(String message) {
		for(Player p : getPlayers()) {
			p.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + "[MAZE]" + ChatColor.RESET + getTeamColor() + ChatColor.BOLD + "[" + getNameClean()  + "] " + ChatColor.RESET + message);			
		}
	}
	
	public void addMapSign(Sign s) {
		mapSigns.add(s);
	}
	
	public void removeMapSign(Sign s) {
		mapSigns.remove(s);
	}
}
