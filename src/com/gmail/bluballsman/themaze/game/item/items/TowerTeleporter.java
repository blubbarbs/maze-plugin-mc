package com.gmail.bluballsman.themaze.game.item.items;

import org.bukkit.Material;
import org.bukkit.Sound;

import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.item.ConsumableItem;

public class TowerTeleporter extends ConsumableItem {

	public TowerTeleporter() {
		super("Tower Teleporter", 15, Material.ENDER_EYE);
	}
	
	@Override
	public void playPurchaseSound(PlayerItemStandData data) {
		data.getPlayer().playSound(data.getPlayer().getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.5F, 1.5F);
	}
	
}
