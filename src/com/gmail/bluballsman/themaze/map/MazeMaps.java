package com.gmail.bluballsman.themaze.map;

import java.io.File;
import java.util.HashMap;

import com.gmail.bluballsman.themaze.TheMaze;

public class MazeMaps {
	private static final HashMap<String, MazeMap> LOADED_MAPS = new HashMap<String, MazeMap>();
	
	public static void reloadMaps() {
		File mapsFolder = new File(TheMaze.getInstance().getDataFolder(), "maps");
		
		if(!mapsFolder.exists()) {
			return;
		}
		try {
			for(File f : mapsFolder.listFiles()) {
				if(!f.isDirectory()) {
					continue;
				}	
				MazeMap map = new MazeMap(f);
				if(map.isMapValid()) {
					LOADED_MAPS.put(f.getName(), map);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static MazeMap getMap(String mapName) {
		return LOADED_MAPS.get(mapName);
	}
	
}
