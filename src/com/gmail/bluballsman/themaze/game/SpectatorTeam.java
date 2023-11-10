package com.gmail.bluballsman.themaze.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

public class SpectatorTeam extends MazeTeam {

	public SpectatorTeam(Game game) {
		super("SPECTATOR", game);
	}
	
	@Override
	public void giveStartingLoadout(Player p) {
		PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
		p.setGameMode(GameMode.SPECTATOR);
		pGameData.getGame().getItemStands().forEach(stand -> {
			PlayerItemStandData standData = pGameData.getItemStandData(stand);
			
			standData.setVisible(false);
			standData.updateArmorStand();
			
		});
	}
}
