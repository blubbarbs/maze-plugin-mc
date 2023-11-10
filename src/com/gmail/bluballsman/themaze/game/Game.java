package com.gmail.bluballsman.themaze.game;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCursor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.camp.CampBoss;
import com.gmail.bluballsman.themaze.game.camp.CampBosses;
import com.gmail.bluballsman.themaze.game.camp.CampEntities;
import com.gmail.bluballsman.themaze.game.item.MazeItem;
import com.gmail.bluballsman.themaze.game.item.MazeItems;
import com.gmail.bluballsman.themaze.game.item.TowerTeleporterEntities;
import com.gmail.bluballsman.themaze.game.item.items.Potion;
import com.gmail.bluballsman.themaze.game.item.items.Wool;
import com.gmail.bluballsman.themaze.map.MazeMap;
import com.gmail.bluballsman.themaze.map.StructureType;
import com.gmail.bluballsman.themaze.mazegen.SymmetricMaze;
import com.gmail.bluballsman.themaze.mazegen.Tile;
import com.gmail.bluballsman.themaze.mazegen.Tile.Type;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;
import com.gmail.bluballsman.themaze.metadata.PluginMetadataHelper;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DefinedStructure;
import net.minecraft.server.v1_15_R1.DefinedStructureInfo;
import net.minecraft.server.v1_15_R1.EnumBlockMirror;
import net.minecraft.server.v1_15_R1.EnumBlockRotation;
import net.minecraft.server.v1_15_R1.StructureBoundingBox;

public class Game {
	public static enum GamePhase {
		LOBBY,
		INGAME,
		WON,
		DONE
	}
	public static final int MAZE_LENGTH = 61;
	public static final int MAZE_WIDTH = 31;
	public static final int CENTER_RADIUS = 6;
	public static final float WALLS_DELETED_PERCENTAGE = .09F;
	public static final int PLAYER_RESPAWN_INVUL_TICKS = 60;
	public static final int SECONDS_TILL_AUTO_WOOL_DISCOVERY = 480;
	public static final ItemStack STARTING_WOODEN_SWORD;
	public static final ItemStack STARTING_POTIONS;
	
	private World world = null;
	private Location worldStartingLoc;
	private Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	private Objective gameObjective = scoreboard.registerNewObjective("game_info", "dummy", "" + ChatColor.GRAY + ChatColor.UNDERLINE + "Game");
	private Objective respawnObjective = scoreboard.registerNewObjective("respawn_timer", "dummy", "Respawn Time");
	private Objective soulsObjective = scoreboard.registerNewObjective("souls", "dummy", ChatColor.GOLD + "Souls");
	private GamePhase phase = GamePhase.LOBBY;
	private MazeMap map = null;
	private SymmetricMaze mazeBlueprint = null;
	private UUID gameID = UUID.randomUUID();
	private HashSet<UUID> playerUUIDs = new HashSet<UUID>();
	private HashMap<String, MazeTeam> registeredTeams = new HashMap<String, MazeTeam>();
	private Stack<Wool> undiscoveredWool = new Stack<Wool>();
	private HashMap<Skeleton, Skeleton> itemStands = new HashMap<Skeleton, Skeleton>();
	private HashMap<CampEntities<?>, CampEntities<?>> camps = new HashMap<CampEntities<?>, CampEntities<?>>();
	private BukkitRunnable actionBarRunnable;
	private BukkitRunnable campRunnable;
	private BukkitRunnable autoDiscoverWoolRunnable;
	private BossBar autoDiscoverBar = Bukkit.createBossBar("Time Till Next Wool: ", BarColor.WHITE, BarStyle.SOLID, new BarFlag[0]);
	private int autoDiscoverTime = SECONDS_TILL_AUTO_WOOL_DISCOVERY;
	
	static {
		STARTING_WOODEN_SWORD = MazeItems.setCanDestroyCarpetsAndSigns(new ItemStack(Material.WOODEN_SWORD));
		STARTING_POTIONS = Potion.getPotion(Material.SPLASH_POTION, "Healing Potion", Color.RED, new PotionEffect(PotionEffectType.HEAL, 0, 1));
		STARTING_WOODEN_SWORD.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		STARTING_POTIONS.setAmount(2);
	}
	
	public Game(MazeMap map) {
		this.map = map;
		mazeBlueprint = new SymmetricMaze(MAZE_LENGTH, MAZE_WIDTH);
		mazeBlueprint.fill();
		mazeBlueprint.knockDownRandomWalls(WALLS_DELETED_PERCENTAGE);
		mazeBlueprint.openUpCenter(CENTER_RADIUS);
		mazeBlueprint.validateCenterAccessibility();
		mazeBlueprint.updateAllTiles();

		MazeTeam redTeam = new MazeTeam("RED", this).setArmorColor(Color.fromRGB(240, 65, 65)).setTeamColor(ChatColor.RED)
				.setCarpet(Material.RED_CARPET).setSignItem(Material.ACACIA_SIGN).setMapCursor(MapCursor.Type.RED_POINTER).setPedestalMaterial(Material.RED_CONCRETE);
		MazeTeam blueTeam = new MazeTeam("BLUE", this).setArmorColor(Color.fromRGB(65, 65, 240)).setTeamColor(ChatColor.BLUE)
				.setCarpet(Material.BLUE_CARPET).setSignItem(Material.BIRCH_SIGN).setMapCursor(MapCursor.Type.BLUE_POINTER).setPedestalMaterial(Material.BLUE_CONCRETE);
		MazeTeam spectatorTeam = new SpectatorTeam(this);
		registeredTeams.put("RED", redTeam);
		registeredTeams.put("BLUE", blueTeam);
		registeredTeams.put("SPECTATOR", spectatorTeam);
		respawnObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		gameObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		soulsObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
	}

	public World getWorld() {
		return world;
	}
	
	public HashSet<UUID> getPlayerUUIDs() {
		return playerUUIDs;
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
	
	public SymmetricMaze getMazeBlueprint() {
		return mazeBlueprint;
	}

	public boolean hasStarted() {
		return phase != GamePhase.LOBBY;
	}

	public GamePhase getGamePhase() {
		return phase;
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	public Objective getRespawnObjective() {
		return respawnObjective;
	}

	public Objective getGameInfoObjective() {
		return gameObjective;
	}
	
	public Objective getSoulsObjective() {
		return soulsObjective;
	}
	
	public MazeMap getMap() {
		return map;
	}

	public int getWorldLength() {
		return map.getTileSize() * mazeBlueprint.getLength();
	}

	public int getWorldWidth() {
		return map.getTileSize() * mazeBlueprint.getWidth();
	}

	public Location getWorldStart() {
		return worldStartingLoc.clone();
	}

	public Location getWorldCenter() {
		return getLocationFromMazeCoordinates(mazeBlueprint.getCenterPoint());
	}

	public UUID getGameID() {
		return gameID;
	}

	public boolean doesTeamExist(String teamName) {
		return registeredTeams.containsKey(teamName.toUpperCase());
	}

	public MazeTeam getTeam(String teamName) {
		return registeredTeams.get(teamName.toUpperCase());
	}

	public Collection<Skeleton> getItemStands() {
		return itemStands.keySet();
	}
	
	public Collection<CampEntities<?>> getCamps() {
		return camps.keySet();
	}
	
	public Collection<Skeleton> getItemStands(Predicate<Skeleton> standPredicate) {
		ArrayList<Skeleton> stands = new ArrayList<Skeleton>();
		for(Skeleton stand : itemStands.keySet()) {
			if(standPredicate.test(stand)) {
				stands.add(stand);
			}
		}
		return stands;
	}
	
	public Collection<Skeleton> getItemStands(MazeItem... items) {
		List<MazeItem> itemTypes = Arrays.asList(items);
		return getItemStands(s -> itemTypes.contains(TheMaze.getMetadataHelper().getMetadata(s, MetadataKeys.ITEMSTAND_ITEM)));
	}
	
	public Skeleton getWoolStand(Wool wool) {
		for(Skeleton stand : itemStands.keySet()) {
			MazeItem item = TheMaze.getMetadataHelper().getMetadata(stand, MetadataKeys.ITEMSTAND_ITEM);
			if(item == wool) {
				return stand;
			}
		}
		return null;
	}
	
	public boolean isWoolUndiscovered(Wool wool) {
		return undiscoveredWool.contains(wool);
	}
	
	public Skeleton getTwinItemStand(Skeleton stand) {
		return itemStands.get(stand);
	}

	public CampEntities<?> getTwinCamp(CampEntities<?> camp) {
		return camps.get(camp);
	}
		
	public Location getLocationFromMazeCoordinates(int mazeX, int mazeY) {
		int distanceFromTileCenter = map.getTileSize() / 2;
		Location worldLocStart = getWorldStart();
		Location locationFromMazeCoords = worldLocStart.add((mazeX * map.getTileSize()) + distanceFromTileCenter, 0, (mazeY * map.getTileSize()) + distanceFromTileCenter);
		Tile t = mazeBlueprint.getTile(mazeX, mazeY);
		locationFromMazeCoords.setYaw(90F * t.getClockwiseRotations());
		return locationFromMazeCoords;
	}

	public Location getLocationFromMazeCoordinates(Point p) {
		return getLocationFromMazeCoordinates(p.x, p.y);
	}

	public Location getWorldMinCorner() {
		return worldStartingLoc.clone();
	}

	public Location getWorldMaxCorner() {
		return worldStartingLoc.clone().add(getWorldLength(), 0, getWorldWidth());
	}

	public void addPlayer(MazeTeam team, Player p) {
		PlayerGameData pGameData;
		if(!TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			pGameData = new PlayerGameData(p, this);
			TheMaze.getMetadataHelper().setMetadata(p, MetadataKeys.PLAYER_GAME_DATA, pGameData);
		} else {
			pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			pGameData.getTeam().unregisterPlayer(p);
		}
		pGameData.setTeam(team);
		team.registerPlayer(p);
		playerUUIDs.add(p.getUniqueId());
		broadcastMessage(p.getName() + " has joined the " + team.getName() + " team.");
	}

	public void removePlayer(Player p) {
		PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
		pGameData.getTeam().unregisterPlayer(p);
		TheMaze.getMetadataHelper().removeMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
		playerUUIDs.remove(p.getUniqueId());
	}

	public void startGame() {
		createWorld();
		buildStructures();
		buildItemsAndZombieSpawners();
		updateGameInfoObjective();
		setupRunnables();
		autoDiscoverBar.setColor(undiscoveredWool.peek().getBarColor());
		for(Player p : getPlayers()) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			PreviousPlayerState previousState = new PreviousPlayerState(p);
			TheMaze.getMetadataHelper().setMetadata(p, MetadataKeys.PREVIOUS_PLAYER_STATE, new PreviousPlayerState(p));
			previousState.normalizePlayerStats();
			autoDiscoverBar.addPlayer(p);
			p.setScoreboard(scoreboard);
			p.teleport(pGameData.getTeam().getSpawnLocation());
			pGameData.getTeam().giveStartingLoadout(p);
			pGameData.setReaperEyes(false);
			pGameData.updateAllItemStands();
		}
		phase = GamePhase.INGAME;
	}

	public void endGame() {
		for(UUID pID : playerUUIDs) {
			Player p = Bukkit.getPlayer(pID);
			if(p != null) {
				PreviousPlayerState previousState = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PREVIOUS_PLAYER_STATE);
				PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
				
				previousState.loadPlayerSaveData();
				pGameData.cancelRespawnTimer();
				TheMaze.getMetadataHelper().removeMetadata(p, MetadataKeys.PREVIOUS_PLAYER_STATE);
				TheMaze.getMetadataHelper().removeMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
				if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.TOWER_TELEPORTER)) {
					TowerTeleporterEntities tTeleporter = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.TOWER_TELEPORTER);
					tTeleporter.remove();
				}
			} else {
				OfflinePlayer offlineP = Bukkit.getOfflinePlayer(pID);
				PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadataOfflinePlayer(offlineP, MetadataKeys.PLAYER_GAME_DATA);

				pGameData.cancelRespawnTimer();
				TheMaze.getMetadataHelper().removeMetadataOfflinePlayer(offlineP, MetadataKeys.PLAYER_GAME_DATA);
			}
		}
		world.getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));
		autoDiscoverBar.removeAll();
		if(!autoDiscoverWoolRunnable.isCancelled()) {
			autoDiscoverWoolRunnable.cancel();
		}
		actionBarRunnable.cancel();
		deleteWorld();
		phase = GamePhase.DONE;
	}


	public void winGame(MazeTeam victor) {
		broadcastMessage("" + victor.getTeamColor() + victor.getName() + ChatColor.RESET + " has won the Maze! Congratulations!");
		phase = GamePhase.WON;
		Bukkit.getScheduler().scheduleSyncDelayedTask(TheMaze.getInstance(), () -> endGame(), 100L);
	}

	public void discoverWool(Wool wool) {
		Skeleton woolStand = getWoolStand(wool);
		Block glassBlock = woolStand.getLocation().getBlock().getRelative(BlockFace.DOWN);
		Block beaconBlock = glassBlock.getRelative(BlockFace.DOWN);
		
		glassBlock.setType(Material.valueOf(wool.getWoolColor().name() + "_STAINED_GLASS"));
		beaconBlock.setType(Material.BEACON);

		Block emeraldCenterBlock = beaconBlock.getRelative(BlockFace.DOWN);
		for(int xOffset = -1; xOffset <= 1; xOffset++)  {
			for(int zOffset = -1; zOffset <= 1; zOffset++) {
				emeraldCenterBlock.getLocation().add(xOffset, 0, zOffset).getBlock().setType(Material.EMERALD_BLOCK);
			}
		}
		
		getAllPlayerGameData().forEach(pGameData -> pGameData.getItemStandData(woolStand).setPriceAndUpdateName(0));
		getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1.5F, 2.0F));
		broadcastMessage("The " + wool.getName() + " has been discovered!");
		undiscoveredWool.remove(wool);
		autoDiscoverTime = SECONDS_TILL_AUTO_WOOL_DISCOVERY;

		if(undiscoveredWool.size() > 0) {
			autoDiscoverBar.setColor(undiscoveredWool.peek().getBarColor());
		} else {
			autoDiscoverWoolRunnable.cancel();
			autoDiscoverBar.removeAll();
		}
	}
	
	public void updateGameInfoObjective() {
		for(String entry : scoreboard.getEntries()) {
			if(gameObjective.getScore(entry).isScoreSet()) {
				scoreboard.resetScores(entry);
			}
		}
		MazeTeam redTeam = getTeam("RED");
		MazeTeam blueTeam = getTeam("BLUE");
		
		gameObjective.getScore("" + redTeam.getTeamColor() + redTeam.getNameClean()).setScore(9);
		gameObjective.getScore(redTeam.getWoolStatus(MazeItems.YELLOW_WOOL)).setScore(8);
		gameObjective.getScore(redTeam.getWoolStatus(MazeItems.LIME_WOOL)).setScore(7);
		gameObjective.getScore(redTeam.getWoolStatus(MazeItems.MAGENTA_WOOL)).setScore(6);
		gameObjective.getScore(" ").setScore(5);
		gameObjective.getScore("" + blueTeam.getTeamColor() + blueTeam.getNameClean()).setScore(4);
		gameObjective.getScore(blueTeam.getWoolStatus(MazeItems.YELLOW_WOOL)).setScore(3);
		gameObjective.getScore(blueTeam.getWoolStatus(MazeItems.LIME_WOOL)).setScore(2);
		gameObjective.getScore(blueTeam.getWoolStatus(MazeItems.MAGENTA_WOOL)).setScore(1);
	}
	
	public void broadcastMessage(String message) {
		for(Player p : getPlayers()) {
			p.sendMessage("" + ChatColor.GRAY + ChatColor.BOLD + "[MAZE]" + " " + ChatColor.RESET + message);
		} 
	}

	private void createWorld() {
		WorldCreator creator = new WorldCreator("maze_game." + gameID.toString());
		creator.generateStructures(false);
		creator.type(WorldType.FLAT);
		creator.generatorSettings(map.getGeneratorSettings());
		world = creator.createWorld();

		TheMaze.getMetadataHelper().setMetadata(world, MetadataKeys.WORLD_GAME_DATA, this);
		world.setGameRule(GameRule.KEEP_INVENTORY, true);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
		world.setGameRule(GameRule.DO_MOB_LOOT, false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
		world.setGameRule(GameRule.DO_TILE_DROPS, false);
		world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		world.setGameRule(GameRule.MOB_GRIEFING, false);
		world.setDifficulty(Difficulty.NORMAL);
		world.setTime(map.getFixedTimeOfDay());

		int distanceFromWorldSpawnX = getWorldLength() / 2;
		int distanceFromWorldSpawnZ = getWorldWidth() / 2;
		worldStartingLoc = world.getSpawnLocation().subtract(distanceFromWorldSpawnX, 0, distanceFromWorldSpawnZ);
		worldStartingLoc.setY(map.getGroundLevel());

		getTeam("RED").setSpawnLocation(getLocationFromMazeCoordinates(1, 1).add(.5, 1, .5));
		getTeam("BLUE").setSpawnLocation(getLocationFromMazeCoordinates(mazeBlueprint.getMirroredPoint(1, 1)).add(.5, 1, .5));
		getTeam("SPECTATOR").setSpawnLocation(world.getSpawnLocation().add(0, 50, 0));
	}

	private void deleteWorld() {
		TheMaze.getMetadataHelper().removeMetadata(world, MetadataKeys.WORLD_GAME_DATA);
		Bukkit.unloadWorld(world, true);
		recursivelyDeleteFile(world.getWorldFolder());				
	}

	private void recursivelyDeleteFile(File f) {
		if(f.isDirectory()) {
			for(File child : f.listFiles()) {
				recursivelyDeleteFile(child);
			}
		}
		f.delete();
	}

	private void buildStructures() {
		long timeStarted = System.currentTimeMillis();

		Bukkit.getLogger().info("Started.");

		for(int mazeY = 0; mazeY < mazeBlueprint.getWidth(); mazeY++) {
			for(int mazeX = 0; mazeX < mazeBlueprint.getLength(); mazeX++) {
				Tile t = mazeBlueprint.getTile(mazeX, mazeY);
				int heightFromGround = t.isGround() ? 0 : 1;
				Location structureBlockLoc = getLocationFromMazeCoordinates(mazeX, mazeY).add(0, heightFromGround, 0);
				StructureType structureType = StructureType.getFromTile(t);
				if(map.getStructure(structureType) == null) {
					structureType = StructureType.getByTileType(structureType.isGround(), Tile.Type.STANDALONE);
				}
				buildStructure(structureBlockLoc, structureType, t.getClockwiseRotations());
			}
		}

		buildStructure(getWorldCenter().add(0, map.getTowerHeightFromGround(), 0), StructureType.TOWER, 0);

		Bukkit.getLogger().info("Ended. Took " + (System.currentTimeMillis() - timeStarted));
	}

	private void buildStructure(Location structureCenterLoc, StructureType structureType, int cwRotations) {
		CraftWorld craftWorld = (CraftWorld) world;
		DefinedStructure structure = map.getStructure(structureType);
		DefinedStructureInfo info = new DefinedStructureInfo();
		BlockPosition structureCenterBlockPos = new BlockPosition(structureCenterLoc.getBlockX(), structureCenterLoc.getBlockY(), structureCenterLoc.getBlockZ());
		BlockPosition structureSize = structure.a(); // get structure size
		BlockPosition offset = new BlockPosition(structureSize.getX() / 2, 0, structureSize.getZ() / 2);
		EnumBlockRotation rotation;

		if(cwRotations == 0) {
			rotation = EnumBlockRotation.NONE;
			offset = new BlockPosition(-offset.getX(), 0, -offset.getZ());
		} else if(cwRotations == 1) {
			offset = new BlockPosition(offset.getZ(), 0, -offset.getX());
			rotation = EnumBlockRotation.CLOCKWISE_90;
		} else if(cwRotations == 2) {
			rotation = EnumBlockRotation.CLOCKWISE_180;
		} else {
			offset = new BlockPosition(-offset.getZ(), 0, offset.getX());
			rotation = EnumBlockRotation.COUNTERCLOCKWISE_90;
		}

		info.a(true); //ignore entities
		info.a(rotation); //rotation
		info.a(EnumBlockMirror.NONE); //mirror
		info.a((StructureBoundingBox) null); 

		BlockPosition structureStartingPos = new BlockPosition(structureCenterBlockPos.getX() + offset.getX(), structureCenterBlockPos.getY(), structureCenterBlockPos.getZ() + offset.getZ());

		structure.a(craftWorld.getHandle(), structureStartingPos, info); //build structure in world
	}

	private void buildItemsAndZombieSpawners() {
		ArrayList<Point> deadEnds = mazeBlueprint.getUnmirroredMatchingPoints(p -> mazeBlueprint.getTile(p).isGround() && mazeBlueprint.getTile(p).getTileType() == Type.END);
		Random r = new Random();
		deadEnds.remove(new Point(1, 1));

		for(MazeItem item : MazeItems.getAllItems()) {
			Point randomPoint = deadEnds.get(r.nextInt(deadEnds.size()));
			Point mirroredPoint = mazeBlueprint.getMirroredPoint(randomPoint);

			if(item instanceof Wool) {
				Location woolStandLoc = Math.random() < .5 ? getLocationFromMazeCoordinates(randomPoint).add(.5, 1, .5) : getLocationFromMazeCoordinates(mirroredPoint).add(.5, 1, .5);
				Skeleton woolStand = item.getNewItemStand(woolStandLoc);
				undiscoveredWool.add((Wool) item);
				itemStands.put(woolStand, null);
			} else {
				Skeleton stand = item.getNewItemStand(getLocationFromMazeCoordinates(randomPoint).add(.5, 1, .5));
				Skeleton mirror = item.getNewItemStand(getLocationFromMazeCoordinates(mirroredPoint).add(.5, 1, .5));
				itemStands.put(stand, mirror);
				itemStands.put(mirror, stand);
			}
			deadEnds.remove(randomPoint);
		}
		
		for(CampBoss<?> boss : CampBosses.ALL_BOSSES) {
			Point randomPoint = deadEnds.get(r.nextInt(deadEnds.size()));
			Point mirroredPoint = mazeBlueprint.getMirroredPoint(randomPoint);
			CampEntities<?> campE = boss.getNewCampEntities(getLocationFromMazeCoordinates(randomPoint).add(.5, -.5, .5));
			CampEntities<?> mirroredCampE = boss.getNewCampEntities(getLocationFromMazeCoordinates(mirroredPoint).add(.5, -.5, .5));
			
			campE.spawnNewBoss();
			mirroredCampE.spawnNewBoss();
			camps.put(campE, mirroredCampE);
			camps.put(mirroredCampE, campE);
			deadEnds.remove(randomPoint);
		}
		
		double zombieDeadEndPercentage = .5;
		int amountOfDeadEndsPopulated = (int) Math.round(deadEnds.size() * zombieDeadEndPercentage);
		for(int i = 0; i < amountOfDeadEndsPopulated; i++) {
			Point randomPoint = deadEnds.get(r.nextInt(deadEnds.size()));
			Point mirroredPoint = mazeBlueprint.getMirroredPoint(randomPoint);
			Block block = getLocationFromMazeCoordinates(randomPoint).getBlock();
			Block mirror = getLocationFromMazeCoordinates(mirroredPoint).getBlock();

			block.setType(Material.SPAWNER);
			mirror.setType(Material.SPAWNER);

			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			CreatureSpawner mirroredSpawner = (CreatureSpawner) mirror.getState();

			spawner.setSpawnedType(EntityType.PIG);
			spawner.setRequiredPlayerRange(50);
			spawner.setMaxNearbyEntities(25);
			spawner.setMaxSpawnDelay(600);
			spawner.setMinSpawnDelay(400);
			TheMaze.getMetadataHelper().setMetadata(spawner, MetadataKeys.MAZE_HUSK_SPAWNER, true);
			spawner.update();
			mirroredSpawner.setSpawnedType(EntityType.PIG);
			mirroredSpawner.setRequiredPlayerRange(50);
			mirroredSpawner.setMaxNearbyEntities(25);
			mirroredSpawner.setMaxSpawnDelay(600);
			mirroredSpawner.setMinSpawnDelay(400);
			TheMaze.getMetadataHelper().setMetadata(mirroredSpawner, MetadataKeys.MAZE_HUSK_SPAWNER, true);
			mirroredSpawner.update();
			deadEnds.remove(randomPoint);
		}
	}
	
	private void setupRunnables() {
		actionBarRunnable = new BukkitRunnable() {

			@Override
			public void run() {
				for(Player p : getPlayers()) {
					PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
					pGameData.updateSoulActionbar();
				}
			}
		};
		actionBarRunnable.runTaskTimer(TheMaze.getInstance(), 0L, 0L);
		autoDiscoverWoolRunnable = new BukkitRunnable() {

			@Override
			public void run() {
				double percentage = (double) autoDiscoverTime / SECONDS_TILL_AUTO_WOOL_DISCOVERY;
				String time = String.format("%02d:%02d", autoDiscoverTime / 60, autoDiscoverTime % 60);				
				String barTitle = "Time Till Next Wool: " + time;
				autoDiscoverBar.setProgress(percentage);
				autoDiscoverBar.setTitle(barTitle);

				if(autoDiscoverTime == 0) {
					discoverWool(undiscoveredWool.peek());
				}
				autoDiscoverTime--;
			}
		};
		autoDiscoverWoolRunnable.runTaskTimer(TheMaze.getInstance(), 0L, 20L);
		campRunnable = new BukkitRunnable() {

			@Override
			public void run() {
				for(CampEntities<?> camp : getCamps()) {
					boolean hasNearbyPlayer = false;
					if(!camp.isRespawning()) {
						camp.updateHealth();
						for(Player p : getPlayers()) {
							if(p.getGameMode() != GameMode.SPECTATOR && camp.getBoss().hasLineOfSight(p) 
									&& camp.getBoss().getLocation().distance(p.getLocation()) <= CampBoss.COMBAT_RADIUS) {
								hasNearbyPlayer = true;
								break;
							}
						}
						if(!hasNearbyPlayer) {
							camp.getBoss().setHealth(camp.getBossInfo().getMaxHealth());
						}
					}
				}
			}
		};
		campRunnable.runTaskTimer(TheMaze.getInstance(), 0L, 0L);
	}
	
}
