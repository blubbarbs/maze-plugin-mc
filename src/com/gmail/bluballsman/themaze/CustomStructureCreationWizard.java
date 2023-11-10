package com.gmail.bluballsman.themaze;

import java.io.File;
import java.io.FileOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.gmail.bluballsman.themaze.metadata.MetadataKeys;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.BlockPropertyStructureMode;
import net.minecraft.server.v1_15_R1.DefinedStructure;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.TileEntityStructure;

public class CustomStructureCreationWizard {
	private static enum Step {
		CORNER_1,
		CORNER_2,
		DONE
	}
	
	private Player player;
	private String structureName;
	private World world;
	private BlockVector firstCorner = null;
	private BlockVector secondCorner = null;
	private BlockVector currentlyClickedBlockVector = null;
	private Step currentStep = Step.CORNER_1;
	private boolean isOptimized = false;
	
	public CustomStructureCreationWizard(Player player, String structureName, boolean isOptimized) {
		this.player = player;
		this.structureName = structureName;
		this.isOptimized = isOptimized;
		world = player.getWorld();
		
	}
	
	public boolean isDone() {
		return currentStep == Step.DONE;
	}
	
	public boolean isOptimized() {
		return isOptimized;
	}
	
	public void refreshPlayerInstance() {
		player = Bukkit.getPlayer(player.getUniqueId());
	}
	
	public void updateClickedBlock(Block b) {
		currentlyClickedBlockVector = b.getLocation().toVector().toBlockVector();
		player.sendMessage("Updated location.");
	}

	public void proceedToNextStep() {
		if(currentlyClickedBlockVector == null) {
			player.sendMessage("You must click a block in order to proceed.");
			return;
		}

		switch(currentStep) {
		case CORNER_1:
			firstCorner = currentlyClickedBlockVector;
			player.sendMessage("Corner 1 set. Now do corner 2 loser");
			currentStep = Step.CORNER_2;
			break;
		case CORNER_2:
			secondCorner = currentlyClickedBlockVector;
			player.sendMessage("Corner 2 set. You're done now.");
			TheMaze.getMetadataHelper().removeMetadata(player, MetadataKeys.CUSTOM_STRUCTURE_WIZARD);
			exportCustomStructure();
			currentStep = Step.DONE;
			break;
		case DONE:
		default:
			return;
		}
		currentlyClickedBlockVector = null;
	}

	private void exportCustomStructure() {
		BlockVector minimum = new BlockVector(Vector.getMinimum(firstCorner, secondCorner));
		BlockVector maximum = new BlockVector(Vector.getMaximum(firstCorner, secondCorner));
		BlockVector size = maximum.clone().subtract(minimum).add(new Vector(1, 1, 1)).toBlockVector();
		Block structureBlock = world.getBlockAt(minimum.toLocation(world).add(-1, 0, -1));
		final BlockData previousData = structureBlock.getBlockData();
		net.minecraft.server.v1_15_R1.WorldServer nmsWorld = ((CraftWorld) world).getHandle();
		structureBlock.setType(Material.STRUCTURE_BLOCK);
		
		if(isOptimized) {
			replaceRegionBlocks(minimum, maximum, Material.STRUCTURE_VOID, Material.AIR);
		}
		
		TileEntityStructure nmsStructureTileEntity = (TileEntityStructure) nmsWorld.getTileEntity(new BlockPosition(structureBlock.getX(), structureBlock.getY(), structureBlock.getZ()));
		nmsStructureTileEntity.setUsageMode(BlockPropertyStructureMode.SAVE);
		nmsStructureTileEntity.setStructureName(structureName);
		nmsStructureTileEntity.size = new BlockPosition(size.getBlockX(), size.getBlockY(), size.getBlockZ());
		nmsStructureTileEntity.relativePosition = new BlockPosition(1, 0, 1);
		nmsStructureTileEntity.ignoreEntities = true;
		nmsStructureTileEntity.b(false); //save function
		MinecraftKey structureKey = new MinecraftKey(structureName);
		DefinedStructure definedStructure = nmsWorld.r().a(structureKey); //retrieve defined structure that was just saved
		NBTTagCompound structureCompound = definedStructure.a(new NBTTagCompound()); //save to NBT
		
		try {
			String folderName;
			String fileName;
			int lastIndexSlash = structureKey.getKey().lastIndexOf("/");
			
			if(lastIndexSlash > -1) {
				fileName = structureKey.getKey().substring(lastIndexSlash) + ".nbt";
				folderName = "generated/" + structureKey.getNamespace() + "/structures/" + structureKey.getKey().substring(0, lastIndexSlash);
				
			} else {
				fileName = structureKey.getKey() + ".nbt";
				folderName = "generated/" + structureKey.getNamespace() + "/structures/";
			}
			
			File structureFolder = new File(world.getWorldFolder(), folderName);
			File compoundFile = new File(structureFolder, fileName);
			compoundFile.createNewFile();
			NBTCompressedStreamTools.a(structureCompound, new FileOutputStream(compoundFile));
		} catch(Exception e) {
			e.printStackTrace();
		}
		structureBlock.setBlockData(previousData);
		if(isOptimized) {
			replaceRegionBlocks(minimum, maximum, Material.AIR, Material.STRUCTURE_VOID);
		}
	}
	
	private void replaceRegionBlocks(BlockVector minimum, BlockVector maximum, Material replacing, Material beingReplaced) {
		for(int z = minimum.getBlockZ(); z <= maximum.getBlockZ(); z++) {
			for(int y = minimum.getBlockY(); y <= maximum.getBlockY(); y++) {
				for(int x = minimum.getBlockX(); x <= maximum.getBlockX(); x++) {
					Block block = world.getBlockAt(x, y, z);
					
					if(block.getType() == beingReplaced) {
						block.setType(replacing);
					}
				}
			}
		}
	}
}
