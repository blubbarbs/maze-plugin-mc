package com.gmail.bluballsman.themaze.metadata;

import com.gmail.bluballsman.themaze.CustomStructureCreationWizard;
import com.gmail.bluballsman.themaze.game.Game;
import com.gmail.bluballsman.themaze.game.MazeTeam;
import com.gmail.bluballsman.themaze.game.PlayerGameData;
import com.gmail.bluballsman.themaze.game.PreviousPlayerState;
import com.gmail.bluballsman.themaze.game.camp.CampEntities;
import com.gmail.bluballsman.themaze.game.item.MazeItem;
import com.gmail.bluballsman.themaze.game.item.TowerTeleporterEntities;

public class MetadataKeys {
	public static Key<CustomStructureCreationWizard> CUSTOM_STRUCTURE_WIZARD = new Key<CustomStructureCreationWizard>("maze_structurecreationwizard");
	public static Key<PreviousPlayerState> PREVIOUS_PLAYER_STATE = new Key<PreviousPlayerState>("maze_previous_playerstate");
	public static Key<PlayerGameData> PLAYER_GAME_DATA = new Key<PlayerGameData>("maze_game_playerdata");
	public static Key<Game> WORLD_GAME_DATA = new Key<Game>("maze_world_data");
	public static Key<MazeTeam> SIGN_GAME_DATA = new Key<MazeTeam>("maze_sign_data");
	public static Key<MazeItem> ITEMSTAND_ITEM = new Key<MazeItem>("maze_itemstand_item");
	public static Key<CampEntities<?>> CAMP = new Key<CampEntities<?>>("maze_camp");
	public static Key<TowerTeleporterEntities> TOWER_TELEPORTER = new Key<TowerTeleporterEntities>("maze_towerteleporter");
	public static Key<Boolean> MAZE_HUSK_SPAWNER = new Key<Boolean>("maze_huskspawner");
}
