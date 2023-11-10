package com.gmail.bluballsman.themaze.metadata;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.metadata.PlayerMetadataStore;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

import com.gmail.bluballsman.themaze.TheMaze;

public class PluginMetadataHelper {
	private Plugin owningPlugin;
	
	public PluginMetadataHelper(Plugin p) {
		this.owningPlugin = p;
	}
	
	public <T> void setMetadata(Metadatable obj, Key<T> key, T value) {
		obj.setMetadata(key.getKey(), new FixedMetadataValue(owningPlugin, value));
	}
	
	public void removeMetadata(Metadatable obj, Key<?> key) {
		obj.removeMetadata(key.getKey(), TheMaze.getInstance());
	}
	
	public boolean hasMetadata(Metadatable obj, Key<?> key) {
		return obj.hasMetadata(key.getKey());
	}
	
	public <T> T getMetadata(Metadatable obj, Key<T> key) {
		@SuppressWarnings("unchecked")
		T value = (T) obj.getMetadata(key.getKey()).get(0).value();		
		
		return value;
	}
	
	public <T> void setMetadataOfflinePlayer(OfflinePlayer offlinePlayer, Key<T> key, T value) {
		PlayerMetadataStore playerMetadataStore = ((CraftServer) Bukkit.getServer()).getPlayerMetadata();
		playerMetadataStore.setMetadata(offlinePlayer, key.getKey(), new FixedMetadataValue(owningPlugin, value));
	}
	
	public void removeMetadataOfflinePlayer(OfflinePlayer offlinePlayer, Key<?> key) {
		PlayerMetadataStore playerMetadataStore = ((CraftServer) Bukkit.getServer()).getPlayerMetadata();
		playerMetadataStore.removeMetadata(offlinePlayer, key.getKey(), owningPlugin);
	}
	
	public boolean hasMetadataOfflinePlayer(OfflinePlayer offlinePlayer, Key<?> key) {
		PlayerMetadataStore playerMetadataStore = ((CraftServer) Bukkit.getServer()).getPlayerMetadata();
		return playerMetadataStore.hasMetadata(offlinePlayer, key.getKey());
	}
	
	public <T> T getMetadataOfflinePlayer(OfflinePlayer offlinePlayer, Key<T> key) {
		PlayerMetadataStore playerMetadataStore = ((CraftServer) Bukkit.getServer()).getPlayerMetadata();
		@SuppressWarnings("unchecked")
		T value = (T) playerMetadataStore.getMetadata(offlinePlayer, key.getKey()).get(0).value();
		
		return value;
	}
}
