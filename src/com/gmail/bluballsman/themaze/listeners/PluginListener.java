package com.gmail.bluballsman.themaze.listeners;

import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import com.gmail.bluballsman.themaze.CustomStructureCreationWizard;
import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.game.PlayerGameData;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

public class PluginListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		
		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD)) {
			CustomStructureCreationWizard structureCreationWizard = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD);
			structureCreationWizard.refreshPlayerInstance();
		}
		
		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			pGameData.refreshPlayerInstance();
			p.setScoreboard(pGameData.getGame().getScoreboard());
		}
	}

	@EventHandler
	public void onPlayerSwitchWorld(PlayerChangedWorldEvent event) {
		TheMaze.getMetadataHelper().removeMetadata(event.getPlayer(), MetadataKeys.CUSTOM_STRUCTURE_WIZARD);
		event.getPlayer().getWorld().setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, event.getPlayer().getWorld().getGameRuleValue(GameRule.DO_IMMEDIATE_RESPAWN));
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		TheMaze.getMetadataHelper().removeMetadata(event.getEntity(), MetadataKeys.CUSTOM_STRUCTURE_WIZARD);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		
		if(!TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD) || event.getHand() != EquipmentSlot.HAND || event.getMaterial() != Material.STICK) {
			return;
		}
		
		CustomStructureCreationWizard structureCreationWizard = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.CUSTOM_STRUCTURE_WIZARD);
		
		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			structureCreationWizard.updateClickedBlock(event.getClickedBlock());
			event.setCancelled(true);
		} else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			structureCreationWizard.proceedToNextStep();
		}

	}
}
