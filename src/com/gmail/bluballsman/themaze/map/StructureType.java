package com.gmail.bluballsman.themaze.map;

import com.gmail.bluballsman.themaze.mazegen.Tile;

import net.minecraft.server.v1_15_R1.MinecraftKey;

public enum StructureType {
	STRAIGHT_WALL(false, Tile.Type.STRAIGHT),
	STRAIGHT_GROUND(true, Tile.Type.STRAIGHT),
	CORNER_WALL(false, Tile.Type.CORNER),
	CORNER_GROUND(true, Tile.Type.CORNER),
	END_WALL(false, Tile.Type.END),
	END_GROUND(true, Tile.Type.END),
	T_WALL(false, Tile.Type.T),
	T_GROUND(true, Tile.Type.T),
	CROSS_WALL(false, Tile.Type.CROSS),
	CROSS_GROUND(true, Tile.Type.CROSS),
	STANDALONE_WALL(false, Tile.Type.STANDALONE),
	STANDALONE_GROUND(true, Tile.Type.STANDALONE),
	TOWER(false, null);
	
	public static StructureType getByTileType(boolean isGround, Tile.Type tType) {
		switch(tType) {
		case CORNER:
			return isGround ? CORNER_GROUND : CORNER_WALL; 
		case CROSS:
			return isGround ? CROSS_GROUND : CROSS_WALL; 
		case END:
			return isGround ? END_GROUND : END_WALL; 
		case STANDALONE:
			return isGround ? STANDALONE_GROUND : STANDALONE_WALL; 
		case STRAIGHT:
			return isGround ? STRAIGHT_GROUND : STRAIGHT_WALL; 
		case T:
			return isGround ? T_GROUND : T_WALL; 
		default:
			return null;
		}
	}
	
	public static StructureType getFromTile(Tile t) {		
		return getByTileType(t.isGround(), Tile.Type.getTypeByTileID(t.getTileID()));
	}
	
	private boolean isGround;
	private Tile.Type tileType;

	StructureType(boolean isGround, Tile.Type tType) {
		this.isGround = isGround;
		tileType = tType;
	}
	
	public boolean isGround() {
		return isGround;
	}
	
	public MinecraftKey getMinecraftKey() {
		return new MinecraftKey("maze", name().toLowerCase());
	}
	
	public Tile.Type getTileType() {
		return tileType;
	}
	
}