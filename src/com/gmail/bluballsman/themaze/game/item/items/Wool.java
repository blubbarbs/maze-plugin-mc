package com.gmail.bluballsman.themaze.game.item.items;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.MazeItem;

public class Wool extends MazeItem {
	public static String MAGENTA_WOOL_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJiNDM4NmJjZGE4NGUzNTNjMzFkNzc4ZDNiMTFiY2QyNmZlYTQ5NGRkNjM0OTZiOGE4MmM3Yzc4YTRhZCJ9fX0=";
	public static String LIME_WOOL_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY3NDcwYTBjMThmNjg1MWU5MTQzNTM3MTllNzk1ODc3ZDI5YjMyNTJmN2U2YmQ0YTFiODY1NzY1YmQ3NGZlYiJ9fX0=";
	public static String YELLOW_WOOL_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjdiYmQwYjI5MTFjOTZiNWQ4N2IyZGY3NjY5MWE1MWI4YjEyYzZmZWZkNTIzMTQ2ZDhhYzVlZjFiOGVlIn19fQ==";
	
	private DyeColor woolColor;
	private BarColor barColor;
	private ChatColor chatColor;
	
	public Wool(DyeColor woolColor, BarColor barColor, ChatColor chatColor, String woolSkullTexture) {
		super(10, Material.valueOf(woolColor.name() + "_WOOL"));
		this.woolColor = woolColor;
		this.barColor = barColor;
		this.chatColor = chatColor;
		setSkullTexture(woolSkullTexture);
	}
	
	public DyeColor getWoolColor() {
		return woolColor;
	}
	
	public BarColor getBarColor() {
		return barColor;
	}
	
	public ChatColor getChatColor() {
		return chatColor;
	}
	
	@Override
	public String getName() {
		return chatColor + super.getName() + ChatColor.RESET;
	}
	
	@Override
	public boolean canPurchase(PlayerItemStandData data) {
		return super.canPurchase(data) && !data.getPlayerGameData().getTeam().hasWoolPossession();
	}
	
	@Override
	public void resetItemStandData(PlayerItemStandData data) {}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5F, 1.5F);
	}
	
	@Override
	public void purchase(PlayerItemStandData data) {
		if(data.getPlayerGameData().getGame().isWoolUndiscovered(this)) {
			data.getPlayerGameData().getGame().discoverWool(this);
		}
		data.getPlayerGameData().setWoolPossession(this);
		data.getPlayerGameData().getGame().broadcastMessage("" + data.getPlayerGameData().getPlayerName() + " has taken possession of the " + getName() + "!");
		playPurchaseSound(data);
	}
	
}
