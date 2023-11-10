package com.gmail.bluballsman.themaze.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.server.v1_15_R1.DefinedStructure;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.NBTTagString;

public class MazeMap {
	private long fixedTimeOfDay = 0L;
	private int towerHeightFromGround = 0;
	private int tileSize = 3;
	private int textColor = 32;
	private Biome biome = Biome.PLAINS;
	private HashMap<StructureType, DefinedStructure> loadedStructures = new HashMap<StructureType, DefinedStructure>();
	private NBTTagList groundMaterials = new NBTTagList();
	private File mapFolder;
	
	public MazeMap(File mapFolder) {
		this.mapFolder = mapFolder;
		loadPropertiesFile();
		loadMapStructures();
	}
	
	public boolean isMapValid() {
		return loadedStructures.containsKey(StructureType.STANDALONE_GROUND) && loadedStructures.containsKey(StructureType.STANDALONE_WALL) && loadedStructures.containsKey(StructureType.TOWER);
	}

	public long getFixedTimeOfDay() {
		return fixedTimeOfDay;
	}

	public int getGroundLevel() {
		return towerHeightFromGround < 0 ? (4 - towerHeightFromGround) : 4;
	}
	
	public int getWallLevel() {
		return getGroundLevel() + 1;
	}
	
	public int getTowerLevel() {
		return getGroundLevel() + towerHeightFromGround;
	}
	
	public int getTileSize() {
		return tileSize;
	}
	
	public int getTowerHeightFromGround() {
		return towerHeightFromGround;
	}
	
	public int getWallHeightFromGround() {
		return 1;
	}
	
	public Biome getBiome() {
		return biome;
	}

	public DefinedStructure getStructure(StructureType type) {
		return loadedStructures.get(type);
	}
	
	public HashMap<StructureType, DefinedStructure> getLoadedStructures(){
		return loadedStructures;
	}
	
	public String getGeneratorSettings() {
		JsonObject json = new JsonObject();
		JsonObject airLayer = new JsonObject();
		JsonArray layers = new JsonArray();

		airLayer.addProperty("block", "minecraft:air");
		layers.add(airLayer);
		json.addProperty("biome", "minecraft:" + biome.name().toLowerCase());
		json.add("layers", layers);
		
		return json.toString();
	}
	
	public NBTTagList getGroundMaterials() {
		return groundMaterials;
	}
	
	public String getSignTextColorCode() {
		return "§" + textColor + ";";
	}
	
	private void loadPropertiesFile() {
		File mapProperties = new File(mapFolder, "map.properties");
		
		try {
			if(!mapProperties.exists()) {
				mapProperties.createNewFile();
			}
			
			FileConfiguration mapYML = YamlConfiguration.loadConfiguration(mapProperties);
			if(!mapYML.contains("fixed-time-of-day")) {
				mapYML.set("fixed-time-of-day", 0L);
			}
			
			if(!mapYML.contains("tower-height-from-ground")) {
				mapYML.set("tower-height-from-ground", 0);
			}
			
			if(!mapYML.contains("tile-size")) {
				mapYML.set("tile-size", 3);
			}
			
			if(!mapYML.contains("biome")) {
				mapYML.set("biome", "plains");
			}
			
			if(!mapYML.contains("map-text-color")) {
				mapYML.set("map-text-color", "white");
			}
			
			mapYML.save(mapProperties);
			
			fixedTimeOfDay = mapYML.getLong("fixed-time-of-day");
			towerHeightFromGround = mapYML.getInt("tower-height-from-ground");
			tileSize = mapYML.getInt("tile-size");
			biome = Biome.valueOf(mapYML.getString("biome").toUpperCase());
			textColor = getTextColor(mapYML.getString("map-text-color"));
			if(textColor == -1) {
				textColor = 32;
				throw new IllegalArgumentException("Invalid text color for " + mapFolder.getName() + " using default color of white");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void loadMapStructures() {
		try {
			for(File f : mapFolder.listFiles()) {
				String fileNameClean = f.getName().substring(0, f.getName().indexOf("."));
				String extension = f.getName().substring(fileNameClean.length(), f.getName().length());
				
				if(!extension.equalsIgnoreCase(".nbt") && !extension.equalsIgnoreCase(".dat")) {
					continue;
				}
				
				StructureType structureType = StructureType.valueOf(fileNameClean.toUpperCase());
				
				if(structureType != null) {
					DefinedStructure structure = new DefinedStructure();
					NBTTagCompound structureCompound = NBTCompressedStreamTools.a(new FileInputStream(f));
					
					structure.b(structureCompound);
					loadedStructures.put(structureType, structure);
					if(structureType.isGround()) {
						NBTTagList palette = (NBTTagList) structureCompound.get("palette");
						for(int i = 0; i < palette.size(); i++) {
							NBTTagCompound paletteBlockCompound = palette.getCompound(i);
							NBTTagString paletteMaterial = NBTTagString.a(paletteBlockCompound.getString("Name"));
							groundMaterials.add(paletteMaterial);
						}
					}
				}
			}

			if(!isMapValid()) {
				throw new FileNotFoundException("standalone_ground.nbt, standalone_wall.nbt, and tower.nbt must be present in order for " + mapFolder.getName() + " to be valid.");
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private int getTextColor(String name) {
		if(name == null) {
			return -1;
		} else if(name.equalsIgnoreCase("blue")) {
			return 48;
		} else if(name.equalsIgnoreCase("brown")) {
			return 40;
		} else if(name.equalsIgnoreCase("dark_brown")) {
			return 52;
		} else if(name.equalsIgnoreCase("dark_gray")) {
			return 44;
		} else if(name.equalsIgnoreCase("dark_green")) {
			return 28;
		} else if(name.equalsIgnoreCase("gray") || name.equalsIgnoreCase("gray_1")) {
			return 12;
		} else if(name.equalsIgnoreCase("gray_2")) {
			return 24;
		} else if(name.equalsIgnoreCase("light_brown")) {
			return 8;
		} else if(name.equalsIgnoreCase("light_gray")) {
			return 36;
		} else if(name.equalsIgnoreCase("light_green")) {
			return 4;
		} else if(name.equalsIgnoreCase("light_blue") || name.equalsIgnoreCase("pale_blue")) {
			return 20;
		} else if(name.equalsIgnoreCase("red")) {
			return 16;
		} else if(name.equalsIgnoreCase("clear") || name.equalsIgnoreCase("transparent")) {
			return 0;
		} else if(name.equalsIgnoreCase("white")) {
			return 32;
		} else {
			return -1;
		}
	}
}
