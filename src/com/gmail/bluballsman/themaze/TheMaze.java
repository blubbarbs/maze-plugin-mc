package com.gmail.bluballsman.themaze;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.tinyprotocol.TinyProtocol;
import com.gmail.bluballsman.themaze.game.Game;
import com.gmail.bluballsman.themaze.game.Game.GamePhase;
import com.gmail.bluballsman.themaze.game.MazeTeam;
import com.gmail.bluballsman.themaze.listeners.GameListener;
import com.gmail.bluballsman.themaze.listeners.PluginListener;
import com.gmail.bluballsman.themaze.map.MazeMaps;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;
import com.gmail.bluballsman.themaze.metadata.PluginMetadataHelper;
import com.gmail.bluballsman.themaze.network.TinyProtocolAdapter;

import net.md_5.bungee.api.ChatColor;

public class TheMaze extends JavaPlugin {
	private static Game ACTIVE_GAME = null;
	private static TinyProtocol TINY_PROTOCOL = null;
	private static PluginMetadataHelper METADATA_HELPER = null;
	
	@Override
	public void onEnable() {
		TINY_PROTOCOL = new TinyProtocolAdapter(this);
		METADATA_HELPER = new PluginMetadataHelper(this);
		Bukkit.getPluginManager().registerEvents(new PluginListener(), this);
		Bukkit.getPluginManager().registerEvents(new GameListener(), this);
		registerCommands();
		MazeMaps.reloadMaps();
	}

	@Override
	public void onDisable() {
		for(World w : Bukkit.getWorlds()) {
			if(METADATA_HELPER.hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
				Game g = METADATA_HELPER.getMetadata(w, MetadataKeys.WORLD_GAME_DATA);
				g.endGame();
			}
		}
	}

	public void registerCommands() {
		getCommand("createcustomstructure").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.DARK_RED + "Only players can use this command.");
					return true;
				}

				Player p = (Player) sender;

				if(METADATA_HELPER.hasMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD)) {
					METADATA_HELPER.removeMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD);
					p.sendMessage("Stopped structure creation.");
					return true;
				}

				if(args.length < 1) {
					return false;
				}
				
				boolean isOptimized = args.length >= 2 && args[1].equals("-o");
				
				METADATA_HELPER.setMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD, new CustomStructureCreationWizard(p, args[0], isOptimized));
				p.sendMessage("Started custom structure creation. Select corner 1 with a stick by left clicking and then right click to confirm. Repeat for corner 2 to finish.");
				return true;
			}
		});

		getCommand("createmazegame").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					return true;
				}
				
				if(args.length < 1) {
					return false;
				}
				
				if(hasActiveGame()) {
					sender.sendMessage(ChatColor.RED + "An active game already exists.");
					return true;
				}
				
				ACTIVE_GAME = new Game(MazeMaps.getMap(args[0].toLowerCase()));
				sender.sendMessage("Game created.");
				return true;
			}
		});
		
		getCommand("joingame").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					return true;
				}
				
				if(args.length < 1) {
					return false;
				}
				
				if(!hasActiveGame()) {
					sender.sendMessage(ChatColor.RED + "There isn't an active game right now.");
					return true;
				}
				
				if(ACTIVE_GAME.hasStarted()) {
					sender.sendMessage(ChatColor.RED + "That game is already in progress.");
					return true;
				}
				
				if(!ACTIVE_GAME.doesTeamExist(args[0])) {
					sender.sendMessage(ChatColor.RED + "That team doesn't exist.");
					return true;
				}
				MazeTeam team = ACTIVE_GAME.getTeam(args[0]);
				ACTIVE_GAME.addPlayer(team, (Player) sender);
				sender.sendMessage("Joined " + team.getName() + " team.");
				return true;
			}
			
		});
		
		getCommand("startgame").setExecutor(new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "Only players can use this command.");
					return true;
				}
				
				if(!hasActiveGame()) {
					sender.sendMessage(ChatColor.RED + "There isn't an active game right now.");
					return true;
				}
				ACTIVE_GAME.broadcastMessage("Starting game - sit tight, it's going to lag a bit");
				ACTIVE_GAME.startGame();
				return true;
			}
			
		});
	}
	
	public static TheMaze getInstance() {
		return (TheMaze) Bukkit.getPluginManager().getPlugin("Maze");
	}
	
	public static TinyProtocol getTinyProtocol() {
		return TINY_PROTOCOL;
	}
	
	public static PluginMetadataHelper getMetadataHelper() {
		return METADATA_HELPER;
	}
	
	public static boolean hasActiveGame() {
		return ACTIVE_GAME != null && ACTIVE_GAME.getGamePhase() != GamePhase.DONE;
	}
	
}
