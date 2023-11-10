package com.gmail.bluballsman.themaze.mazegen;

import java.util.HashMap;

public class Tile {
	
	public static enum Type {
		STRAIGHT(0b1010),
		CORNER(0b1100),
		END(0b1000),
		T(0b1101),
		CROSS(0b1111),
		STANDALONE(0b0000);
		
		private static HashMap<Integer, Type> TYPE_REGISTRY = new HashMap<Integer, Type>();
			
		static {
			for(Type t : values()) {
				TYPE_REGISTRY.put(t.typeID, t);
			}
		}
		
		public static Type getTypeByTileID(int tileID) {
			return TYPE_REGISTRY.get(tileID & 0b001111);
		}
		
		private int typeID;
		
		Type(int typeID) {
			this.typeID = typeID;
		}
		
		public int getTypeID() {
			return typeID;
		}
	}
	
	private byte tileData = 0;
	/**
	 * Class representing a tile in the maze. Each tile is represented by a byte, with 5 bits
	 * actually storing information. The bit in the 4th index from the right is whether the
	 * tile is ground or not, while the next 4 bits (positions 0-3) store the surrounding tiles'
	 * values. These 4 bits are read in order of (Up, Right, Down, Left) from left to right, 
	 * with a 1 representing ground and a 0 representing a wall.
	 */
	public Tile() {}
	
	/**
	 * Constructs a tile with pre-determined tile data.
	 * @param tileData the tile data of the new tile
	 */
	public Tile(byte tileData) {
		this.tileData = tileData;
	}
	
	/**
	 * Returns the tile's byte of data.
	 */
	public byte getTileData() {
		return tileData;
	}
	
	/**
	 * Returns a byte with the rightmost 4 bits representing the surrounding tiles in (U,R,D,L)
	 * order. 
	 */
	public byte getSurroundingTileBits() {
		return (byte) (tileData & 0b01111);
	}
	
	/**
	 * Returns whether the tile is ground or not.
	 */
	public boolean isGround() {
		return (tileData >> 4) == 1;
	}
	
	/**
	 * Calculates the "tile ID" of this tile. The Tile ID is calculated using the surrounding
	 * tiles, and is represented in 6 bits. The rightmost 4 bits are the type ID, which is going
	 * to be the same for identical tiles regardless of rotation. The type ID is calculated by
	 * finding the simplified form of the surrounding tiles, where a 1 represents a tile with
	 * the same ground bit and a 0 represents a tile with a different ground bit. The left 2 bits
	 * are the amount of counter-clockwise rotations that the tile required to get to the
	 * type ID. This creates a unique ID for each possible rotation and type of tile.
	 * @return the Tile's ID
	 */
	public int getTileID() {
		int relativeSurroundingTiles = isGround() ? tileData & 0b1111 : ~tileData & 0b1111;
		int tileID = 0;
		
		for(int ccwRotations = 0; ccwRotations < 4; ccwRotations++) {
			int rotatedTypeIDBits = (relativeSurroundingTiles >> ccwRotations) | (relativeSurroundingTiles << (4 - ccwRotations)) & 0b1111;
			Type potentialTypeMatch = Type.TYPE_REGISTRY.get(rotatedTypeIDBits);
			
			if(potentialTypeMatch != null) {
				tileID = (ccwRotations << 4) | (rotatedTypeIDBits);
				break;
			}
		}
		
		return tileID;
	}
	
	/**
	 * Calculates the amount of counter-clockwise rotations it took to get to the type ID.
	 */
	public int getClockwiseRotations() {
		return (getTileID() >> 4);
	}
		
	/**
	 * Calculates the amount of clockwise rotations it took to get to the type ID.
	 */
	public int getCounterClockwiseRotations() {
		return (4 - getClockwiseRotations()) % 4;
	}
	
	/**
	 * Calculates the tile ID and then returns the tile type using that ID.
	 */
	public Type getTileType() {
		return Type.getTypeByTileID(getTileID());
	}
	
	/**
	 * Sets the full tile data.
	 * @param tileData new tile data
	 */
	public void setTileData(byte tileData) {
		this.tileData = tileData;
	}
	
	/**
	 * Sets whether the ground bit is 0 or 1, with a 0 representing a wall and 1 being ground.
	 * @param isGround whether the tile will be ground or not
	 */
	public void setGround(boolean isGround) {
		tileData = (byte) (isGround ? (tileData | 0b10000) : (tileData & 0b01111));
	}
	
	/**
	 * Sets the bits of the surrounding tiles.
	 * @param surroundingTileBits
	 */
	public void setSurroundingTiles(int surroundingTileBits) {
		int groundBit = tileData & 0b10000;
		
		tileData = (byte) (groundBit | (surroundingTileBits));
	}
	
	public void rotate(int ccwRotations) {
		int groundBit = tileData & 0b10000;
		int surroundingTileBits = tileData & 0b1111;
		int rotatedSurroundingTileBits = (surroundingTileBits >> ccwRotations | surroundingTileBits << (4 - ccwRotations));
		
		tileData = (byte) (groundBit | (rotatedSurroundingTileBits & 0b1111));
	}
	
	@Override
	public Tile clone() {
		return new Tile(tileData);
	}
	
}
