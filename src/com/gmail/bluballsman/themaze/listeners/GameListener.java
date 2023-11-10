package com.gmail.bluballsman.themaze.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftZombie;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.bluballsman.themaze.MazeMapRenderer;
import com.gmail.bluballsman.themaze.TheMaze;
import com.gmail.bluballsman.themaze.entity.pathfindergoals.MazePathfinderGoalHurtByTarget;
import com.gmail.bluballsman.themaze.entity.pathfindergoals.MazePathfinderGoalNearestAttackableTarget;
import com.gmail.bluballsman.themaze.game.Game;
import com.gmail.bluballsman.themaze.game.PlayerGameData;
import com.gmail.bluballsman.themaze.game.PlayerItemStandData;
import com.gmail.bluballsman.themaze.game.PreviousPlayerState;
import com.gmail.bluballsman.themaze.game.camp.CampEntities;
import com.gmail.bluballsman.themaze.game.item.MazeItem;
import com.gmail.bluballsman.themaze.game.item.TowerTeleporterEntities;
import com.gmail.bluballsman.themaze.game.item.items.GrimReaper;
import com.gmail.bluballsman.themaze.metadata.MetadataKeys;
import com.gmail.bluballsman.themaze.game.Game.GamePhase;

import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityZombie;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_15_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_15_R1.PathfinderGoalSelector;

public class GameListener implements Listener {

	@EventHandler (priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			PreviousPlayerState prevState = new PreviousPlayerState(p);
			
			TheMaze.getMetadataHelper().setMetadata(p, MetadataKeys.PREVIOUS_PLAYER_STATE, prevState);
			prevState.normalizePlayerStats();
			p.teleport(pGameData.getTeam().getSpawnLocation());
			p.setHealth(0);
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);

			if(pGameData.getGame().hasStarted()) {
				PreviousPlayerState previousState = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PREVIOUS_PLAYER_STATE);
				pGameData.resetStats();
				previousState.loadPlayerSaveData();
			} else {
				pGameData.getGame().removePlayer(p);
			}

			if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.TOWER_TELEPORTER)) {
				TowerTeleporterEntities tTeleporter = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.TOWER_TELEPORTER);
				tTeleporter.remove();
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			Location difference = event.getFrom().subtract(event.getTo());

			if(pGameData.isRespawning() && (difference.getX() != 0 || difference.getZ() != 0)) {
				p.teleport(pGameData.getTeam().getSpawnLocation());
			}
		}
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
			if(event.getEntity() instanceof AreaEffectCloud) {
				AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity();
				Player thrower = (Player) cloud.getSource();
				PlayerGameData throwerGameData = TheMaze.getMetadataHelper().getMetadata(thrower, MetadataKeys.PLAYER_GAME_DATA);

				if(cloud.getLocation().getBlock().isEmpty()) {
					Location highestValidLoc = cloud.getLocation();
					while(highestValidLoc.getBlock().isEmpty()) {
						highestValidLoc = highestValidLoc.subtract(0, 1, 0);
						highestValidLoc.setY(highestValidLoc.getBlockY());
					}
					cloud.teleport(highestValidLoc.add(0, 1, 0));
				}
				cloud.setRadius(.5F);
				cloud.setRadiusPerTick(.01F);
				cloud.setDuration(6000);
				cloud.setWaitTime(0);
				cloud.setColor(throwerGameData.getTeam().getArmorColor());
				Bukkit.getScheduler().scheduleSyncDelayedTask(TheMaze.getInstance(), () -> {
					if(cloud != null && !cloud.isDead()) {
						cloud.setRadiusPerTick(0F);
					}
				}, 100L);
				
			}
		}
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
			if(event.getSpawnReason() == SpawnReason.NATURAL) {
				event.setCancelled(true);
			} else if(event.getEntity() instanceof Zombie) {
				Zombie z = (Zombie) event.getEntity();
				setupZombie(z);
			}
		}
	}

	@EventHandler
	public void onSpawnerSpawn(SpawnerSpawnEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA) 
				&& TheMaze.getMetadataHelper().hasMetadata(event.getSpawner(), MetadataKeys.MAZE_HUSK_SPAWNER)) {
			event.getEntity().remove();
			event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.HUSK);
		}
	}

	@EventHandler
	public void onAreaEffectCloud(AreaEffectCloudApplyEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA) && event.getAffectedEntities().size() > 0) {
			AreaEffectCloud cloud = event.getEntity();
			boolean hasVulnerableEntity = false;
			for(LivingEntity le : event.getAffectedEntities()) {
				if(TheMaze.getMetadataHelper().hasMetadata(le, MetadataKeys.PLAYER_GAME_DATA)) {
					PlayerGameData throwerGameData = TheMaze.getMetadataHelper().getMetadata((Player) cloud.getSource(), MetadataKeys.PLAYER_GAME_DATA);
					PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(le, MetadataKeys.PLAYER_GAME_DATA);
					
					if(throwerGameData.getTeam() != pGameData.getTeam()) {
						hasVulnerableEntity = true;
						break;
					}
					
				} else if(!TheMaze.getMetadataHelper().hasMetadata(le, MetadataKeys.ITEMSTAND_ITEM)) {
					hasVulnerableEntity = true;
					break;
				}
			}
			if(hasVulnerableEntity) {
				Player thrower = (Player) cloud.getSource();
				PlayerGameData throwerGameData = TheMaze.getMetadataHelper().getMetadata(thrower, MetadataKeys.PLAYER_GAME_DATA);
				ArrayList<PotionEffect> correctedPotionEffects = new ArrayList<PotionEffect>();
				cloud.remove();
				int ticksLived = cloud.getTicksLived();
				float percentage = ticksLived < 100 ? (ticksLived * ticksLived) / 10000F : 1;
				for(PotionEffect customE : cloud.getCustomEffects()) {
					int duration = (int) (customE.getDuration() * percentage);
					correctedPotionEffects.add(new PotionEffect(customE.getType(), duration, customE.getAmplifier()));
				}
				throwerGameData.sendMessageToPlayer("One of your traps has been triggered!");
				Bukkit.getScheduler().scheduleSyncDelayedTask(TheMaze.getInstance(), () -> {
					for(LivingEntity le : event.getAffectedEntities()) {
						for(PotionEffect e : correctedPotionEffects) {
							le.removePotionEffect(e.getType());
							if(le != cloud.getSource() && !TheMaze.getMetadataHelper().hasMetadata(le, MetadataKeys.ITEMSTAND_ITEM)) {
								le.addPotionEffect(e);
							}
						}
					}
				});
			} else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onSplashDamage(PotionSplashEvent event) {
		World w = event.getEntity().getWorld();
		
		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
			for(LivingEntity e : event.getAffectedEntities()) {
				event.setIntensity(e, 1F);
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
			if(event.getEntity() instanceof Vex && TheMaze.getMetadataHelper().hasMetadata(event.getEntity(), MetadataKeys.TOWER_TELEPORTER)) {
				Vex v = (Vex) event.getEntity();
				TowerTeleporterEntities towerTeleporter = TheMaze.getMetadataHelper().getMetadata(v, MetadataKeys.TOWER_TELEPORTER);
				towerTeleporter.remove();
			} else if(event.getEntity() instanceof Zombie && event.getEntity().getKiller() != null) {
				Zombie z = (Zombie) event.getEntity();
				Player killer = z.getKiller();
				PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(killer, MetadataKeys.PLAYER_GAME_DATA);
				ItemStack item = killer.getInventory().getItemInMainHand();
				int itemBonus = GrimReaper.getZombieSoulBonus(item);
				int soulBonus = z.getLastDamageCause().getCause() == DamageCause.ENTITY_ATTACK ? itemBonus : 0;
				pGameData.setSouls(pGameData.getSouls() + 1 + soulBonus);
			} else if(TheMaze.getMetadataHelper().hasMetadata(event.getEntity(), MetadataKeys.CAMP)) {
				CampEntities<?> campEntities = TheMaze.getMetadataHelper().getMetadata(event.getEntity(), MetadataKeys.CAMP);
				campEntities.setRespawnTimer(15);
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			
			if(p.getKiller() != null && p.getLastDamageCause().getCause() == DamageCause.ENTITY_ATTACK 
					&& GrimReaper.getPlayerSoulStealPercentage(p.getKiller().getInventory().getItemInMainHand()) > 0) {
				PlayerGameData killerGameData = TheMaze.getMetadataHelper().getMetadata(p.getKiller(), MetadataKeys.PLAYER_GAME_DATA);
				ItemStack murderWeapon = p.getKiller().getInventory().getItemInMainHand();
				int soulsStolen = (int) (GrimReaper.getPlayerSoulStealPercentage(murderWeapon) * pGameData.getSouls());
				killerGameData.setSouls(killerGameData.getSouls() + soulsStolen);
				p.getWorld().strikeLightningEffect(p.getLocation());
				pGameData.getGame().broadcastMessage(pGameData.getPlayerName() + " had their souls sapped by " + killerGameData.getPlayerName() + "!");
			}
			pGameData.resetStats();
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			event.setRespawnLocation(pGameData.getTeam().getSpawnLocation());
			pGameData.setRespawnTimer(pGameData.getTeam().getTotalPoints() * 5);
		}
	}

	@EventHandler
	public void onEntityTransform(EntityTransformEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA) && event.getEntity() instanceof Husk) {
			Husk h = (Husk) event.getEntity();
			h.remove();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPotionEffectChange(EntityPotionEffectEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA) && event.getAction() == Action.ADDED) {
			if(event.getEntity() instanceof Vex && event.getNewEffect().getType().equals(PotionEffectType.GLOWING)) {
				event.setCancelled(true);
			} else if(event.getEntity() instanceof Player && event.getNewEffect().getType().equals(PotionEffectType.HUNGER)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onHunger(FoodLevelChangeEvent event) {
		World w = event.getEntity().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		World w = event.getPlayer().getWorld();
		
		if(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
			if(event.getMaterial() == Material.ENDER_EYE && TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)
					&& !TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.TOWER_TELEPORTER)) {
				Game g = (Game) TheMaze.getMetadataHelper().getMetadata(w, MetadataKeys.WORLD_GAME_DATA);
				Location towerPeak = g.getWorld().getHighestBlockAt(g.getWorldCenter()).getLocation().add(.5, 2, .5);
				TowerTeleporterEntities teleporter = new TowerTeleporterEntities(towerPeak, event.getPlayer());
				
				p.playSound(p.getLocation(), Sound.ENTITY_EGG_THROW, 1.0F, 1.0F);
				teleporter.spawn(p.getEyeLocation());
				event.getItem().setAmount(event.getItem().getAmount() - 1);
			} else if(event.getMaterial() == Material.SPLASH_POTION
					&& TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
				PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
				ItemStack thrownPotion = event.getItem();
				PotionMeta thrownPotMeta = (PotionMeta) thrownPotion.getItemMeta();
				
				if(thrownPotMeta.hasCustomEffect(PotionEffectType.HEAL)) {
					ThrownPotion launched = p.launchProjectile(ThrownPotion.class);
					launched.setItem(Game.STARTING_POTIONS);
					pGameData.setPotionRechargeTimer(20);
					
					if(pGameData.count(Material.SPLASH_POTION) == 1) {
						thrownPotMeta.setColor(Color.WHITE);
						thrownPotMeta.removeCustomEffect(PotionEffectType.HEAL);
						thrownPotMeta.setDisplayName("Recharging Health Potion...");
						thrownPotion.setItemMeta(thrownPotMeta);
					} else {
						thrownPotion.setAmount(thrownPotion.getAmount() - 1);
					}
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);

			if(event.getBlockPlaced().getType().name().endsWith("CARPET")) {
				PlayerInventory inventory = event.getPlayer().getInventory();
				ItemStack carpetStack = event.getHand() == EquipmentSlot.HAND ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
				carpetStack.setAmount(2);
			} else if(event.getBlock().getType().name().endsWith("SIGN")) { 
				PlayerInventory inventory = event.getPlayer().getInventory();
				ItemStack signStack = event.getHand() == EquipmentSlot.HAND ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
				signStack.setAmount(2);
			} else if(event.getBlockPlaced().getType().name().endsWith("WOOL") && pData.getGame().getGamePhase() == GamePhase.INGAME) {
				pData.getTeam().captureWool(pData.getPossessedWool());
				pData.setWoolPossession(null);
			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onMap(MapInitializeEvent event) {
		World w = event.getMap().getWorld();

		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA)) {
			event.getMap().getRenderers().clear();
			event.getMap().addRenderer(MazeMapRenderer.MAP_RENDERER);
		}
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractAtEntityEvent event) {
		if(TheMaze.getMetadataHelper().hasMetadata(event.getRightClicked(), MetadataKeys.ITEMSTAND_ITEM) && !event.getPlayer().isSneaking() 
				&& event.getHand() == EquipmentSlot.HAND && event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
			Skeleton stand = (Skeleton) event.getRightClicked();
			Player p = event.getPlayer();
			MazeItem mazeItem = TheMaze.getMetadataHelper().getMetadata(event.getRightClicked(), MetadataKeys.ITEMSTAND_ITEM);
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			PlayerItemStandData itemStandData = pGameData.getItemStandData(stand);
			int price = itemStandData.getPrice();

			if(itemStandData.isPurchasable() && pGameData.getTeam().getCollectiveSoulCount() >= price) {
				pGameData.getTeam().deductSouls(p, price);
				mazeItem.purchase(itemStandData);
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(TheMaze.getMetadataHelper().hasMetadata(event.getEntity(), MetadataKeys.ITEMSTAND_ITEM)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Vex && TheMaze.getMetadataHelper().hasMetadata(event.getEntity(), MetadataKeys.TOWER_TELEPORTER) && !(event.getDamager() instanceof Projectile)) {
			event.setCancelled(true);
		} else if(TheMaze.getMetadataHelper().hasMetadata(event.getEntity(), MetadataKeys.ITEMSTAND_ITEM) && event.getDamager() instanceof Player 
				&& !((Player) event.getDamager()).isSneaking() && event.getEntity().getLocation().getBlock().getType().name().endsWith("SIGN")) {
			event.getEntity().getLocation().getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA) && !event.getAction().name().startsWith("DROP")) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			if(pGameData.hasWool()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		Player p = (Player) event.getWhoClicked();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			if(pGameData.hasWool()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerConsume(PlayerItemConsumeEvent event) {
		Player p = event.getPlayer();

		if(TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) { 
			if(event.getItem().getType() == Material.POTION) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(TheMaze.getInstance(), () -> p.getInventory().remove(Material.GLASS_BOTTLE));
			} else if (event.getItem().getType() == Material.MILK_BUCKET) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(TheMaze.getInstance(), () -> p.getInventory().remove(Material.BUCKET));
			} 
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		World w = event.getBlock().getWorld();
		Player p = event.getPlayer();
		
		if(TheMaze.getMetadataHelper().hasMetadata(w, MetadataKeys.WORLD_GAME_DATA) 
				&& TheMaze.getMetadataHelper().hasMetadata(p, MetadataKeys.PLAYER_GAME_DATA)) {
			PlayerGameData pGameData = TheMaze.getMetadataHelper().getMetadata(p, MetadataKeys.PLAYER_GAME_DATA);
			Sign s = (Sign) event.getBlock().getState();
			for(int i = 0; i < 2; i++) {
				String line = event.getLine(i);
				int maxIndex = line.length() > 5 ? 5 : line.length();
				event.setLine(i, line.substring(0, maxIndex));
				s.setLine(i, event.getLine(i));
			}
			event.setLine(2, "");
			event.setLine(3, "");
			pGameData.getTeam().addMapSign(s);
			TheMaze.getMetadataHelper().setMetadata(s.getBlock(), MetadataKeys.SIGN_GAME_DATA, pGameData.getTeam());
		}
	}
	
	private void setupZombie(Zombie z) {
		EntityZombie nmsZombie = ((CraftZombie) z).getHandle();
		PathfinderGoalSelector newTargetSelector = new PathfinderGoalSelector(nmsZombie.world.getMethodProfiler());
		PathfinderGoalHurtByTarget revengeGoal = new MazePathfinderGoalHurtByTarget(nmsZombie, e -> {
			return (!isEntityInvisibleToZombies((LivingEntity) e.getBukkitEntity()));

		});
		PathfinderGoalNearestAttackableTarget<EntityPlayer> playerHuntingGoal = new MazePathfinderGoalNearestAttackableTarget<EntityPlayer>(nmsZombie, EntityPlayer.class, 10, true, true, e -> {
			return (!isEntityInvisibleToZombies((LivingEntity) e.getBukkitEntity()));
		});
		PathfinderGoalNearestAttackableTarget<EntityLiving> glowPriorityGoal = new MazePathfinderGoalNearestAttackableTarget<EntityLiving>(nmsZombie, EntityLiving.class, -1, false, false, e -> {
			return ((LivingEntity) e.getBukkitEntity()).hasPotionEffect(PotionEffectType.GLOWING);
		});
		
		nmsZombie.setEquipment(EnumItemSlot.MAINHAND, null);
		nmsZombie.teleportAndSync(nmsZombie.locX(), nmsZombie.locY(), nmsZombie.locZ());
		newTargetSelector.a(1, glowPriorityGoal);
		newTargetSelector.a(2, revengeGoal);
		newTargetSelector.a(3, playerHuntingGoal);
		nmsZombie.targetSelector = newTargetSelector;
		nmsZombie.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(1.3);
		nmsZombie.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.34);
		nmsZombie.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(30D);
		nmsZombie.setBaby(false);
	}

	private boolean isEntityInvisibleToZombies(LivingEntity le) {
		boolean isHoldingItems = false;

		if(le instanceof Player) {
			Player p = (Player) le;
			PlayerInventory pInv = p.getInventory();
			isHoldingItems = pInv.getItemInMainHand().getType() != Material.AIR || pInv.getItemInOffHand().getType() != Material.AIR;

			if(!isHoldingItems) {
				for(ItemStack i : pInv.getArmorContents()) {
					if(i != null) {
						isHoldingItems = true;
						break;
					}
				}
			}
		}
		return (!isHoldingItems && le.hasPotionEffect(PotionEffectType.INVISIBILITY)) || le.getTicksLived() < Game.PLAYER_RESPAWN_INVUL_TICKS;
	}
}
