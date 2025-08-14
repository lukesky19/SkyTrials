/*
    SkyTrials is a plugin that offers different challenges or trials to tackle. Inspired by the Minecraft Trial Chambers and mob arenas.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skytrials.trial.impl;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.placeholderapi.PlaceholderAPIUtil;
import com.github.lukesky19.skylib.api.player.PlayerUtil;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.data.trial.LevelTrialData;
import com.github.lukesky19.skytrials.manager.entity.EntityManager;
import com.github.lukesky19.skytrials.manager.player.CooldownManager;
import com.github.lukesky19.skytrials.manager.player.GracePeriodManager;
import com.github.lukesky19.skytrials.trial.AbstractTrial;
import com.github.lukesky19.skytrials.util.TrialEndReason;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is used to create a level-based trial.
 */
public class LevelTrial extends AbstractTrial {
    // Plugin Classes
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull EntityManager entityManager;
    private final @NotNull CooldownManager cooldownManager;
    private final @NotNull GracePeriodManager gracePeriodManager;

    // Config Data
    private final @NotNull LevelTrialData trialData;
    private LevelTrialData.LevelData levelData;

    // Controls whether the trial is started or not// Trial Status & timer
    private boolean status = false;

    // Trial time limit
    private int remainingTimeSeconds = -1;

    // Active Trial Data
    private int level = 0;
    private int mobCount = 0;
    private int mobLimit = -1;
    private int goalCount = 0;
    private int goalLimit = -1;

    private final @NotNull BossBar bossBar;

    // Player ready statuses
    private final @NotNull Map<Player, Boolean> playerStatuses = new HashMap<>();

    // Tasks
    private @Nullable BukkitTask mobSpawnTask;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param entityManager An {@link EntityManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     * @param gracePeriodManager A {@link GracePeriodManager} instance.
     * @param trialData The {@link LevelTrialData} for the trial.
     */
    public LevelTrial(
            @NotNull SkyTrials skyTrials,
            @NotNull LocaleManager localeManager,
            @NotNull EntityManager entityManager,
            @NotNull CooldownManager cooldownManager,
            @NotNull GracePeriodManager gracePeriodManager,
            @NotNull LevelTrialData trialData) {
        this.skyTrials = skyTrials;
        this.localeManager = localeManager;
        this.entityManager = entityManager;
        this.cooldownManager = cooldownManager;
        this.gracePeriodManager = gracePeriodManager;
        this.trialData = trialData;

        // Create the initial boss bar
        bossBar = BossBar.bossBar(AdventureUtil.serialize(""), 1, trialData.lobbyBossBar().color(), trialData.lobbyBossBar().overlay());
    }

    /**
     * Handles a player attempting to join the trial.
     * @param player The {@link Player} joining the trial.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void join(@NotNull Player player, @NotNull UUID uuid) {
        Locale locale = localeManager.getLocale();

        // If the trial has already started, send a message to the player that they can't join a trial that has already started.
        if(status) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.joinTrialInTrial()));
            return;
        }

        // If the player has a cooldown for the trial, send a message that the trial is on cooldown and how much time is left on their cooldown.
        @Nullable Long playerCooldown = cooldownManager.getTrialCooldown(uuid, trialData.trialId());
        if(playerCooldown != null) {
            List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()), Placeholder.parsed("time", localeManager.getTimeMessage(playerCooldown)));

            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.trialOnCooldown(), placeholders));
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.cooldownTime(), placeholders));

            return;
        }

        // Add the player to the player statuses map
        playerStatuses.put(player, false);

        // Teleport the player to the join area
        player.teleportAsync(trialData.joinLocation());

        // Send a message that to player that they joined the trial
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()), Placeholder.parsed("player_name", player.getName()));
        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.joinTrial(), placeholders));

        // Get and filter online players
        List<Player> onlinePlayers = ImmutableList.copyOf(skyTrials.getServer().getOnlinePlayers());
        List<Player> filteredOnlinePlayers = onlinePlayers.stream().filter(onlinePlayer -> onlinePlayer.getUniqueId() != uuid).toList();

        // Send a message to all players except the joining player that the player joined the trial
        for(Player p : filteredOnlinePlayers) {
            p.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.playerJoinedTrial(), placeholders));
        }
    }

    /**
     * Handles the actual starting of the trial.
     */
    @Override
    public void start() {
        Locale locale = localeManager.getLocale();

        // Set the trial status to true
        status = true;

        // Update the boss bra color and overlay
        bossBar.color(trialData.trialBossBar().color());
        bossBar.overlay(trialData.trialBossBar().overlay());

        // Get the first level's data.
        levelData = trialData.levels().getFirst();

        // Create a list of placeholders
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()));

        // Send a message to all players in the trial that the trial is starting, teleport them to the start area, and show the boss bar.
        playerStatuses.keySet().forEach(player -> {
            player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.startTrial(), placeholders));

            player.teleportAsync(levelData.startLocation());

            player.showBossBar(bossBar);
        });

        // Get and filter online players
        List<Player> onlinePlayers = ImmutableList.copyOf(skyTrials.getServer().getOnlinePlayers());
        List<Player> filteredOnlinePlayers = onlinePlayers.stream().filter(onlinePlayer -> !playerStatuses.containsKey(onlinePlayer)).toList();

        // Send a message to all players except the players in the trial that the trial is starting
        filteredOnlinePlayers.forEach(onlinePlayer ->
                onlinePlayer.sendMessage(AdventureUtil.serialize(onlinePlayer,locale.prefix() + locale.broadcastTrialStart(), placeholders)));

        // Update trial limits
        updateLimits();
        // Update boss bar
        updateBossBar();

        // Set the trial time limit
        remainingTimeSeconds = trialData.timeLimitSeconds();

        // Start the mob spawn task
        startMobSpawnTask();
    }

    /**
     * Handles when a player attempts to leave the trial.
     * @param player The {@link Player} leaving the trial.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void leave(@NotNull Player player, @NotNull UUID uuid) {
        Locale locale = localeManager.getLocale();

        // Create a list of placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trialData.trialId()));
        placeholders.add(Placeholder.parsed("player_name", player.getName()));

        // Remove the player from the players in the trial
        playerStatuses.remove(player);

        // Remove the boss bar
        player.hideBossBar(bossBar);

        // Send a message to the player leaving the trial that they have left the trial.
        player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.leaveTrial(), placeholders));

        // If the trial is active, apply the cooldown if configured.
        if(status) {
            // If a cooldown is configured, apply it and send a message with their cooldown.
            if(trialData.cooldownSeconds() != -1) {
                placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(trialData.cooldownSeconds())));

                // Add the cooldown for the player and trial.
                cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds());

                // Send a message to the player leaving the trial that they have left the trial with their cooldown.
                player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.cooldownApplied(), placeholders));
            }

            if(playerStatuses.isEmpty()) {
                // Remove blocks and entities before teleporting the last player out
                removeBlocks();
                removeEntities();
            }
        }

        // Teleport the player to the end location of the trial
        player.teleportAsync(trialData.endLocation());

        // If the trial is active, apply the cooldown if configured.
        if(status) {
            // If a cooldown is configured, apply it and send a message with their cooldown.
            if(trialData.cooldownSeconds() != -1) {
                placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(trialData.cooldownSeconds())));

                // Add the cooldown for the player and trial.
                cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds());

                // Send a message to the player leaving the trial that they have left the trial with their cooldown.
                player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.cooldownApplied(), placeholders));
            }

            if(playerStatuses.isEmpty()) {
                // Remove entities before teleporting the last player out
                removeEntities();
            }
        }

        // Get and filter online players
        List<Player> onlinePlayers = ImmutableList.copyOf(skyTrials.getServer().getOnlinePlayers());
        List<Player> filteredOnlinePlayers = onlinePlayers.stream().filter(onlinePlayer -> onlinePlayer.getUniqueId() != uuid).toList();

        // Send a message to all players except the leaving player that the player left the trial
        for(Player p : filteredOnlinePlayers) {
            p.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.playerLeaveTrial(), placeholders));
        }

        if(status) {
            // End the trial if there are no more players
            if(playerStatuses.isEmpty()) {
                // End the trial
                end(TrialEndReason.EMPTY);
            } else {
                updateLimits();
                updateBossBar();
            }
        }
    }

    /**
     * Handles the ending of a trial.
     * @param trialEndReason The {@link TrialEndReason} for why the trial is ending.
     */
    @Override
    public void end(@NotNull TrialEndReason trialEndReason) {
        Locale locale = localeManager.getLocale();

        // Stop the mob spawn task
        stopMobSpawnTask();

        // Remove grace periods if necessary and get a list of UUIDs to apply cooldowns to if necessary
        List<UUID> playersWithGracePeriods = gracePeriodManager.removeGracePeriods(trialData.trialId());

        switch(trialEndReason) {
            case TIMEOUT -> {
                removeEntities();

                // Apply cooldown if configured
                if(trialData.cooldownSeconds() != -1) {
                    // Apply cooldown to players with grace periods
                    playersWithGracePeriods.forEach(uuid ->
                            cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds()));

                    // Apply cooldown to players in trial
                    playerStatuses.keySet().forEach(player -> {
                        if(trialData.clearEffectsOnTrialEnd()) player.clearActivePotionEffects();
                        if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();

                        UUID playerId = player.getUniqueId();
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()), Placeholder.parsed("time", localeManager.getTimeMessage(trialData.cooldownSeconds())));

                        // Add cooldown
                        cooldownManager.addCooldown(playerId, trialData.trialId(), trialData.cooldownSeconds());

                        // Send a trial end message and a message with the cooldown time
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.trialEnd(), placeholders));
                        player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.cooldownApplied(), placeholders));

                        // Teleport the player to the end location and once that is complete, give end rewards if configured to do so
                        player.teleportAsync(trialData.endLocation()).whenComplete((b, t) -> {
                            if(trialData.rewardOnTimeEnd()) {
                                givePlayerRewards(player, trialData.trialRewardItemStacks(), trialData.trialRewardCommands());
                            }
                        });

                        // Remove the boss bar from the player
                        player.hideBossBar(bossBar);
                    });
                } else {
                    playerStatuses.keySet().forEach(player -> {
                        if(trialData.clearEffectsOnTrialEnd()) player.clearActivePotionEffects();
                        if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();

                        // Send a trial end message
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.trialEnd()));

                        // Teleport the player to the end location and once that is complete, give end rewards if configured to do so
                        player.teleportAsync(trialData.endLocation()).whenComplete((b, t) -> {
                            if(trialData.rewardOnTimeEnd()) {
                                givePlayerRewards(player, trialData.trialRewardItemStacks(), trialData.trialRewardCommands());
                            }
                        });

                        // Remove the boss bar from the player
                        player.hideBossBar(bossBar);
                    });
                }

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()));
                for(Player player : skyTrials.getServer().getOnlinePlayers()) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.broadcastTrialEnd(), placeholders));
                }
            }

            case COMPLETED -> {
                removeEntities();

                // Apply cooldown if configured
                if(trialData.cooldownSeconds() != -1) {
                    // Apply cooldown to players with grace periods
                    playersWithGracePeriods.forEach(uuid ->
                            cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds()));

                    // Apply cooldown to players in trial
                    playerStatuses.keySet().forEach(player -> {
                        if(trialData.clearEffectsOnTrialEnd()) player.clearActivePotionEffects();
                        if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();

                        UUID playerId = player.getUniqueId();
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("time", localeManager.getTimeMessage(trialData.cooldownSeconds())));

                        // Add cooldown
                        cooldownManager.addCooldown(playerId, trialData.trialId(), trialData.cooldownSeconds());

                        // Send a trial end message and a message with the cooldown time
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.trialEnd(), placeholders));
                        player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.cooldownApplied(), placeholders));

                        // Teleport the player to the end location and once that is complete, give trial end rewards and level end rewards
                        player.teleportAsync(trialData.endLocation()).whenComplete((b, t) -> {
                            givePlayerRewards(player, levelData.rewardItemStacks(), levelData.rewardCommands());
                            givePlayerRewards(player, trialData.trialRewardItemStacks(), trialData.trialRewardCommands());

                        });

                        // Remove the boss bar from the player
                        player.hideBossBar(bossBar);
                    });
                } else {
                    playerStatuses.keySet().forEach(player -> {
                        // Send a trial end message
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.trialEnd()));

                        // Teleport the player to the end location and once that is complete, give trial end rewards and level end rewards
                        player.teleportAsync(trialData.endLocation()).whenComplete((b, t) -> {
                            givePlayerRewards(player, levelData.rewardItemStacks(), levelData.rewardCommands());
                            givePlayerRewards(player, trialData.trialRewardItemStacks(), trialData.trialRewardCommands());
                        });

                        // Remove the boss bar from the player
                        player.hideBossBar(bossBar);
                    });
                }

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()));
                for(Player player : skyTrials.getServer().getOnlinePlayers()) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.broadcastTrialEnd(), placeholders));
                }
            }

            case EMPTY, DEATH -> {
                // Apply cooldown if configured
                if(trialData.cooldownSeconds() != -1) {
                    // Apply cooldown to players with grace periods
                    playersWithGracePeriods.forEach(uuid ->
                            cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds()));
                }

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()));
                for(Player player : skyTrials.getServer().getOnlinePlayers()) {
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.broadcastTrialEnd(), placeholders));
                }
            }

            case RELOAD -> {
                removeBlocks();
                removeEntities();

                List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", trialData.trialId()));

                playerStatuses.keySet().forEach(player -> {
                    if(trialData.clearEffectsOnTrialEnd()) player.clearActivePotionEffects();
                    if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();

                    // Send a trial end message
                    player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.trialEnd(), placeholders));

                    // Teleport the player to the end location
                    player.teleportAsync(trialData.endLocation());

                    // Remove the boss bar from the player
                    player.hideBossBar(bossBar);
                });
            }
        }

        playerStatuses.clear();

        mobLimit = -1;
        goalLimit = -1;
        mobCount = 0;
        goalCount = 0;
        level = 0;

        remainingTimeSeconds = -1;
        status = false;

        // Reset boss bar back to lobby boss bar
        bossBar.color(trialData.lobbyBossBar().color());
        bossBar.overlay(trialData.lobbyBossBar().overlay());
        updateBossBar();
    }

    /**
     * This method updates trial data to the next level, updates the boss bar, teleports the player to the start area, and sends a trial level up message.
     */
    public void levelUp() {
        if(levelData.removeMobsOnLevelEnd()) removeEntities();

        giveRewards(levelData.rewardItemStacks(), levelData.rewardCommands());

        playerStatuses.keySet().forEach(player -> {
            if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();
        });

        goalCount = 0;
        mobCount = 0;
        level++;

        levelData = trialData.levels().get(level);

        updateLimits();
        updateBossBar();

        Locale locale = localeManager.getLocale();
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("current_level", String.valueOf(level)), Placeholder.parsed("max_level", String.valueOf(trialData.levels().size())));
        for(Player player : getPlayers()) {
            player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.levelUp(), placeholders));

            player.teleportAsync(levelData.startLocation());
        }

        stopMobSpawnTask();
        startMobSpawnTask();
    }

    /**
     * Handles when a player attempts to toggle their ready status.
     * @param player The {@link Player} toggling their ready status.
     * @param uuid The {@link UUID} of the player.
     */
    @Override
    public void togglePlayerStatus(@NotNull Player player, @NotNull UUID uuid) {
        Locale locale = localeManager.getLocale();

        // Send a message to the player if the trial is already started.
        if(status) {
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.startTrialActive()));
            return;
        }

        // Create placeholders
        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("player_name", player.getName()));

        // Toggle player status
        playerStatuses.put(player, !playerStatuses.get(player));
        boolean playerStatus = playerStatuses.get(player);

        if(playerStatus) {
            // Send a message that the player is now ready to start the trial.
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.ready()));

            // Send a message to all other players in the trial that the player is ready.
            playerStatuses.keySet().stream()
                    .filter(trialPlayer -> player.getUniqueId() != uuid)
                    .forEach(trialPlayer ->
                            trialPlayer.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.playerIsReady(), placeholders)));
        } else {
            // Send a message that the player is no longer ready to start the trial.
            player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.notReady()));

            // Send a message to all other players in the trial that the player is not ready.
            playerStatuses.keySet().stream()
                    .filter(trialPlayer -> player.getUniqueId() != uuid)
                    .forEach(trialPlayer ->
                            trialPlayer.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.playerIsNotReady(), placeholders)));
        }

        // If all players are ready, start the trial
        if(areAllPlayersReady()) {
            start();
        }
    }

    /**
     * If the player has a grace period, re-add them to the trial.
     * If not, teleport them out of the trial if they are in the trial region using the end location.
     * @param playerJoinEvent A {@link PlayerJoinEvent}.
     */
    @Override
    public void handlePlayerJoinEvent(@NotNull PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(gracePeriodManager.doesPlayerHaveGracePeriod(uuid, trialData.trialId())) {
            gracePeriodManager.removeGracePeriod(uuid, trialData.trialId());

            playerStatuses.put(player, true);

            trialData.playerEffects().forEach(player::addPotionEffect);
            levelData.playerEffects().forEach(player::addPotionEffect);

            player.teleportAsync(levelData.startLocation());

            player.showBossBar(bossBar);
        } else {
            Location playerLocation = player.getLocation();
            if(trialData.trialRegion().contains(playerLocation.getBlockX(), playerLocation.getBlockY(), playerLocation.getBlockZ())) {
                player.teleportAsync(trialData.endLocation());
            }
        }
    }

    /**
     * Apply a grace period if configured or add a cooldown if configured.
     * If the trial has no players, end the trial
     * @param playerQuitEvent A {@link PlayerQuitEvent}
     */
    @Override
    public void handlePlayerQuitEvent(@NotNull PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        if(trialData.gracePeriodSeconds() != -1) {
            gracePeriodManager.addGracePeriod(uuid, trialData.trialId(), trialData.gracePeriodSeconds());
        } else {
            if(trialData.cooldownSeconds() != -1) {
                cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds());
            }
        }

        if(trialData.clearEffectsOnTrialEnd()) player.clearActivePotionEffects();
        if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();

        playerStatuses.remove(player);

        if(playerStatuses.isEmpty()) {
            // Remove blocks and entities
            removeBlocks();
            removeEntities();

            end(TrialEndReason.EMPTY);
        }
    }

    /**
     * Handles when a player dies in the trial.
     * @param playerDeathEvent A {@link PlayerDeathEvent}.
     */
    @Override
    public void handlePlayerDeath(@NotNull PlayerDeathEvent playerDeathEvent) {
        Locale locale = localeManager.getLocale();
        Player player = playerDeathEvent.getPlayer();
        UUID uuid = player.getUniqueId();

        playerDeathEvent.setKeepInventory(true);
        playerDeathEvent.setKeepLevel(true);
        playerDeathEvent.getDrops().clear();
        playerDeathEvent.setCancelled(true);

        if(trialData.clearEffectsOnTrialEnd()) player.clearActivePotionEffects();
        if(levelData.clearEffectsOnLevelEnd()) player.clearActivePotionEffects();

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trialData.trialId()));
        placeholders.add(Placeholder.parsed("player_name", player.getName()));

        // Send a message that a player died in a trial and fake their actual death message
        Component deathMessage = playerDeathEvent.deathMessage();
        if(deathMessage != null) {
            for(Player p : skyTrials.getServer().getOnlinePlayers()) {
                p.sendMessage(deathMessage);

                p.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.playerDiedInTrial(), placeholders));
            }
        } else {
            for(Player p : skyTrials.getServer().getOnlinePlayers()) {
                p.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.playerDiedInTrial(), placeholders));
            }
        }

        // Remove the player from the players in the trial
        playerStatuses.remove(player);

        // If the trial is active, apply the cooldown if configured.
// If a cooldown is configured, apply it and send a message with their cooldown.
        if(trialData.cooldownSeconds() != -1) {
            placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(trialData.cooldownSeconds())));

            // Add the cooldown for the player and trial.
            cooldownManager.addCooldown(uuid, trialData.trialId(), trialData.cooldownSeconds());

            // Send a message to the player who died in the trial with their cooldown.
            player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.diedInTrial(), placeholders));
            player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.cooldownApplied(), placeholders));
        } else {
            // Send a message to the player who died in the trial
            player.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.diedInTrial(), placeholders));
        }

        if(playerStatuses.isEmpty()) {
            // Remove blocks and entities before teleporting the last player out
            removeBlocks();
            removeEntities();
        }

        // Teleport the player to the end location of the trial
        player.teleportAsync(trialData.endLocation());

        // Get and filter online players
        List<Player> onlinePlayers = ImmutableList.copyOf(skyTrials.getServer().getOnlinePlayers());
        List<Player> filteredOnlinePlayers = onlinePlayers.stream().filter(onlinePlayer -> onlinePlayer.getUniqueId() != uuid).toList();

        // Send a message to all players except the player that died.
        for(Player p : filteredOnlinePlayers) {
            p.sendMessage(AdventureUtil.serialize(player,locale.prefix() + locale.playerDiedInTrial(), placeholders));
        }

        if(status) {
            // End the trial if there are no more players
            if(playerStatuses.isEmpty()) {
                // End the trial
                end(TrialEndReason.DEATH);
            }
        }
    }

    /**
     * Handles when an entity dies and updates the counters and boss bar.
     * @param entityDeathEvent An {@link EntityDeathEvent}.
     */
    @Override
    public void handleEntityDeath(@NotNull EntityDeathEvent entityDeathEvent) {
        if(mobLimit == -1) return;
        if(goalLimit == -1) return;
        mobCount--;
        goalCount++;

        updateBossBar();

        if(level >= trialData.levels().size() - 1 && goalCount >= goalLimit) {
            this.end(TrialEndReason.COMPLETED);
        } else if(goalCount >= goalLimit){
            this.levelUp();
        }
    }

    /**
     * This handles when an entity spawns and updates the counters and boss bar.
     * @param spawnerSpawnEvent A {@link SpawnerSpawnEvent}.
     */
    @Override
    public void handleEntitySpawn(@NotNull SpawnerSpawnEvent spawnerSpawnEvent) {
        if(mobCount >= mobLimit) {
            spawnerSpawnEvent.setCancelled(true);
        } else {
            mobCount++;

            updateBossBar();
        }
    }

    /**
     * Handles when a Player removes a potion effect through milk and cancels it if effect removal via milk is not allowed.
     * @param entityPotionEffectEvent An {@link EntityPotionEffectEvent}.
     */
    @Override
    public void handleEntityPotionEffect(@NotNull EntityPotionEffectEvent entityPotionEffectEvent) {
        Entity entity = entityPotionEffectEvent.getEntity();
        if(entity instanceof Player) {
            if(entityPotionEffectEvent.getCause().equals(EntityPotionEffectEvent.Cause.MILK)) {
                if(!levelData.allowMilkEffectRemoval()) {
                    entityPotionEffectEvent.setCancelled(true);
                }
            }
        }
    }

    /**
     * If the trial has a time limit, decrement the time by one and update the boss bar.
     */
    @Override
    public void decrementTime() {
        if(remainingTimeSeconds != -1) {
            remainingTimeSeconds--;
            updateBossBar();

            if(remainingTimeSeconds <= 0) end(TrialEndReason.TIMEOUT);
        }
    }

    /**
     * Get the world the trial is in.
     * @return A {@link World}.
     */
    @Override
    public @NotNull World getWorld() {
        return trialData.trialWorld();
    }

    /**
     * Get the {@link ProtectedRegion} for the trial.
     * @return A {@link ProtectedRegion}.
     */
    @Override
    public @NotNull ProtectedRegion getRegion() {
        return trialData.trialRegion();
    }

    /**
     * Get a {@link List} of {@link UUID}s for the players in the trial.
     * @return A {@link List} of {@link UUID}s for the players in the trial.
     */
    @Override
    public @NotNull List<UUID> getPlayerIds() {
        return playerStatuses.keySet().stream().map(Player::getUniqueId).toList();
    }

    /**
     * Get a {@link List} of {@link Player}s in the trial.
     * @return A {@link List} of {@link Player}s in the trial.
     */
    @Override
    public @NotNull List<Player> getPlayers() {
        return playerStatuses.keySet().stream().toList();
    }

    /**
     * This trial doesn't place blocks so this method does nothing.
     */
    @Override
    public void placeBlocks() {}

    /**
     * This trial doesn't have blocks to remove so this method does nothing.
     */
    @Override
    public void removeBlocks() {}

    /**
     * Remove all non-player entities from the trial.
     */
    @Override
    protected void removeEntities() {
        List<Entity> entityList = trialData.trialWorld().getEntities().stream()
                .filter(entity -> trialData.trialRegion().contains(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ()))
                .filter(entity -> !(entity instanceof Player))
                .toList();

        entityList.forEach(Entity::remove);
    }

    /**
     * Increments the mob count for the trial.
     */
    public void incrementMobCount() {
        mobCount++;

        updateBossBar();
    }

    /**
     * Start the task to spawn mobs.
     */
    private void startMobSpawnTask() {
        if(levelData.mobSpawnStartDelay() == -1 || levelData.mobSpawnFrequencySeconds() == -1) return;

        mobSpawnTask = skyTrials.getServer().getScheduler().runTaskTimer(skyTrials, () -> {
            if(mobLimit != -1) {
                for(int i = 0; i <= levelData.spawnCount(); i++) {
                    if(mobCount < mobLimit) {
                        entityManager.spawnEntity(this, levelData.mobSpawnList(), this.getPlayerIds().size());

                        this.updateBossBar();
                    } else {
                        return;
                    }
                }
            } else {
                for(int i = 0; i <= levelData.spawnCount(); i++) {
                    entityManager.spawnEntity(this, levelData.mobSpawnList(), this.getPlayerIds().size());

                    this.updateBossBar();
                }
            }
        }, levelData.mobSpawnStartDelay() * 20L, levelData.mobSpawnFrequencySeconds() * 20L);
    }

    /**
     * Stop the task that spawns mobs.
     */
    private void stopMobSpawnTask() {
        if(mobSpawnTask != null && !mobSpawnTask.isCancelled()) {
            mobSpawnTask.cancel();
            mobSpawnTask = null;
        }
    }

    /**
     * Updates the limits for the trial.
     */
    private void updateLimits() {
        if(levelData.baseMobLimit() != -1) {
            if(levelData.additionalMobLimitPerPlayer() != -1) {
                mobLimit = levelData.baseMobLimit() + (levelData.additionalMobLimitPerPlayer() * playerStatuses.size());
            } else {
                mobLimit = levelData.baseMobLimit();
            }
        } else {
            mobLimit = -1;
        }

        if(levelData.goalCount() != -1) {
            if(levelData.additionalGoalCountPerPlayer() != -1) {
                goalLimit = levelData.goalCount() + (levelData.additionalGoalCountPerPlayer() * playerStatuses.size());
            } else {
                goalLimit = levelData.goalCount();
            }
        } else {
            goalLimit = -1;
        }
    }

    /**
     * Are all players ready to start the trial?
     * @return true if all players are ready, otherwise false.
     */
    private boolean areAllPlayersReady() {
        for(boolean status : playerStatuses.values()) {
            if(!status) return false;
        }

        return true;
    }

    /**
     * Update the boss bar shown to the players.
     */
    private void updateBossBar() {
        if(status) {
            LevelTrialData.BossBarData bossBarData = trialData.trialBossBar();

            List<TagResolver.Single> placeholders = new ArrayList<>();
            if(mobLimit != -1) {
                placeholders.add(Placeholder.parsed("mob_count", String.valueOf(mobCount)));
                placeholders.add(Placeholder.parsed("mob_limit", String.valueOf(mobLimit)));
            }
            if(goalLimit != -1) {
                placeholders.add(Placeholder.parsed("goal_count", String.valueOf(goalCount)));
                placeholders.add(Placeholder.parsed("goal_limit", String.valueOf(goalLimit)));
            }

            if(remainingTimeSeconds != -1) {
                @NotNull String timeMessage = localeManager.getTimeMessage(remainingTimeSeconds);
                placeholders.add(Placeholder.parsed("time", timeMessage));
            }

            int timeValue = 0;
            if(remainingTimeSeconds != -1) {
                timeValue = remainingTimeSeconds;
            }

            @NotNull String timeMessage = localeManager.getTimeMessage(timeValue);
            placeholders.add(Placeholder.parsed("time", timeMessage));

            bossBar.name(AdventureUtil.serialize(bossBarData.bossBarText(), placeholders));
        } else {
            LevelTrialData.BossBarData lobbyBossBarData = trialData.lobbyBossBar();

            List<TagResolver.Single> placeholders = List.of(
                    Placeholder.parsed("ready_count",
                            String.valueOf(playerStatuses.entrySet().stream().filter(Map.Entry::getValue).count())),
                    Placeholder.parsed("player_count", String.valueOf(playerStatuses.size())));

            bossBar.name(AdventureUtil.serialize(lobbyBossBarData.bossBarText(), placeholders));
        }
    }

    /**
     * Give a {@link List} of {@link ItemStack}s to the players in the trial and execute the {@link List} of commands in console.
     * @param rewardItems A {@link List} of {@link ItemStack}s.
     * @param rewardCommands A {@link List} of commands as a {@link String}.
     */
    private void giveRewards(@NotNull List<ItemStack> rewardItems, @NotNull List<String> rewardCommands) {
        playerStatuses.keySet().forEach(player -> givePlayerRewards(player, rewardItems, rewardCommands));
    }

    /**
     * Give a {@link List} of {@link ItemStack}s to the player provided and execute the {@link List} of commands in console.
     * @param player The {@link Player} to give rewards to.
     * @param rewardItems A {@link List} of {@link ItemStack}s.
     * @param rewardCommands A {@link List} of commands as a {@link String}.
     */
    private void givePlayerRewards(@NotNull Player player, @NotNull List<ItemStack> rewardItems, @NotNull List<String> rewardCommands) {
        ConsoleCommandSender commandSender = skyTrials.getServer().getConsoleSender();

        for(ItemStack itemStack : rewardItems) {
            PlayerUtil.giveItem(player.getInventory(), itemStack, itemStack.getAmount(), player.getLocation());
        }

        for(String cmd : rewardCommands) {
            skyTrials.getServer().dispatchCommand(commandSender, PlaceholderAPIUtil.parsePlaceholders(player, cmd));
        }
    }
}