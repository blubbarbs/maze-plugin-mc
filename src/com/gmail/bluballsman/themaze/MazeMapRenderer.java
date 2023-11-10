package com.gmail.bluballsman.themaze;

import java.awt.Point;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

import com.gmail.bluballsman.themaze.game.PlayerGameData;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

public class MazeMapRenderer extends MapRenderer {
	public static int PIXELS_PER_BLOCK = 3;
	public static int CENTER_RADIUS = (int) (64D / (double) PIXELS_PER_BLOCK);
	public static int START_X_Z = (int) (64D - CENTER_RADIUS);
	public static MazeMapRenderer MAP_RENDERER = new MazeMapRenderer();

	public MazeMapRenderer() {
		super(true);
	}

	@Override
	public void initialize(MapView view) {
		view.setTrackingPosition(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(player, MetadataKeys.PLAYER_GAME_DATA);
		MapCursorCollection cursors = new MapCursorCollection();
		Location minCorner = pGameData.getGame().getWorldMinCorner();
		Location maxCorner = pGameData.getGame().getWorldMaxCorner();

		view.setCenterX(clamp(player.getLocation().getBlockX(), minCorner.getBlockX() + CENTER_RADIUS, maxCorner.getBlockX() - CENTER_RADIUS));
		view.setCenterZ(clamp(player.getLocation().getBlockZ(), minCorner.getBlockZ() + CENTER_RADIUS, maxCorner.getBlockZ() - CENTER_RADIUS));

		for(int mapX = 0, baseX = START_X_Z; mapX < 128; mapX += PIXELS_PER_BLOCK, baseX++)  {
			for(int mapZ = 0, baseZ = START_X_Z; mapZ < 128; mapZ += PIXELS_PER_BLOCK, baseZ++) {
				byte pixelColor = (byte) canvas.getBasePixel(baseX, baseZ);

				for(int xOffset = 0; xOffset < PIXELS_PER_BLOCK; xOffset++) {
					for(int zOffset = 0; zOffset < PIXELS_PER_BLOCK; zOffset++) {
						canvas.setPixel(mapX + xOffset, mapZ + zOffset, pixelColor);
					}
				}
			}
		}

		for(Player p : pGameData.getTeam().getPlayers())  {
			MapCursor.Type cursorType = p == player ? MapCursor.Type.WHITE_POINTER : pGameData.getTeam().getMapCursor();
			MapCursor playerCursor = getCursor(p.getEyeLocation(), view, cursorType);
			if(playerCursor != null) {
				cursors.addCursor(playerCursor);
			}
		}
		
		for(Sign s : (HashSet<Sign>) pGameData.getTeam().getMapSigns().clone()) {
			if(!s.getBlock().getType().name().endsWith("SIGN")) {
				pGameData.getTeam().removeMapSign(s);
				continue;
			}
			
			Point p = getCoordsOnMapFromLocation(s.getLocation(), view);
			
			if(p != null) {
				int width = 0;
				int length = 0;
				String fullText = pGameData.getGame().getMap().getSignTextColorCode();
				
				for(int i = 0; i < 2; i++) {
					String line = s.getLine(i).replace("§", "");
					if(!line.isEmpty()) {
						length = MinecraftFont.Font.getWidth(line) > length ? MinecraftFont.Font.getWidth(line) : length;
						width = i * (MinecraftFont.Font.getHeight() + 3);
						fullText += line + "\n";
					}
				}
				int startX = (p.x - (length / 2D)) > 0 ? (int) (p.x - (length / 2D)) : 0;
				int startY = (p.y - (width / 2D)) > 0 ? (int) (p.y - (width /2D)) : 0;
				canvas.drawText(startX, startY, MinecraftFont.Font, fullText);
			}
		}

		canvas.setCursors(cursors);
	}

	private int clamp(int i, int minimum, int maximum) {
		if(i < minimum) {
			return minimum;
			
		} 

		if(i > maximum) {
			return maximum;
		}

		return i;
	}
	
	private Point getCoordsOnMapFromLocation(Location l, MapView view) {
		double differenceX = l.getX() - view.getCenterX();
		double differenceZ = l.getZ() - view.getCenterZ();
		
		if(Math.abs(differenceX) > CENTER_RADIUS || Math.abs(differenceZ) > CENTER_RADIUS) {
			return null;
		}
		
		int coordX = (int) (differenceX * PIXELS_PER_BLOCK) + 64;
		int coordZ = (int) (differenceZ * PIXELS_PER_BLOCK) + 64;
		
		return new Point(coordX, coordZ);
	}
	
	private MapCursor getCursor(Location l, MapView view, MapCursor.Type cursorType) {
		double differenceX = l.getX() - view.getCenterX();
		double differenceZ = l.getZ() - view.getCenterZ();

		if(Math.abs(differenceX) > CENTER_RADIUS || Math.abs(differenceZ) > CENTER_RADIUS) {
			return null;
		}

		byte mapCursorDifferenceX = (byte) (differenceX * PIXELS_PER_BLOCK * 2);
		byte mapCursorDifferenceZ = (byte) (differenceZ * PIXELS_PER_BLOCK * 2);
		int fixedYaw = (int) Math.round(l.getYaw() < 0 ? 360 + (l.getYaw() % 360) : (l.getYaw() % 360));
		byte direction = (byte) (Math.round((fixedYaw / 22.5)) % 16);

		return new MapCursor(mapCursorDifferenceX, mapCursorDifferenceZ, direction, cursorType, true);
	}

}
