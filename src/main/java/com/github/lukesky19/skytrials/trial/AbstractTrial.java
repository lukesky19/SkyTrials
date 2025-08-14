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
package com.github.lukesky19.skytrials.trial;

import com.github.lukesky19.skytrials.util.TrialEndReason;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * This abstract class is used to create different types of trials.
 */
public abstract class AbstractTrial {
    /**
     * Constructor
     */
    public AbstractTrial() {}

    /**
     * Handles when a player attempts to join the trial.
     * @param player The {@link Player} joining the trial.
     * @param uuid The {@link UUID} of the player.
     */
    public abstract void join(@NotNull Player player, @NotNull UUID uuid);

    /**
     * This handles the starting of a trial.
     */
    public abstract void start();

    /**
     * Handles when a player attempts to leave the trial.
     * @param player The {@link Player} leaving the trial.
     * @param uuid The {@link UUID} of the player.
     */
    public abstract void leave(@NotNull Player player, @NotNull UUID uuid);

    /**
     * Handles when a trial is ended.
     * @param trialEndReason The {@link TrialEndReason} for why the trial is ending.
     */
    public abstract void end(@NotNull TrialEndReason trialEndReason);

    /**
     * Handles when a player attempts to toggle their ready status.
     * @param player The {@link Player} toggling their ready status.
     * @param uuid The {@link UUID} of the player.
     */
    public abstract void togglePlayerStatus(@NotNull Player player, @NotNull UUID uuid);

    /**
     * Handles when a player dies inside a trial.
     * @param playerDeathEvent A {@link PlayerDeathEvent}.
     */
    public abstract void handlePlayerDeath(@NotNull PlayerDeathEvent playerDeathEvent);

    /**
     * Handles when an {@link Entity} inside a trial dies.
     * @param entityDeathEvent An {@link EntityDeathEvent}.
     */
    public abstract void handleEntityDeath(@NotNull EntityDeathEvent entityDeathEvent);

    /**
     * Handles when a player joins the server and is inside a trial.
     * @param playerJoinEvent A {@link PlayerJoinEvent}
     */
    public abstract void handlePlayerJoinEvent(@NotNull PlayerJoinEvent playerJoinEvent);

    /**
     * Handles when a player quits the server while in a trial.
     * @param playerQuitEvent A {@link PlayerQuitEvent}
     */
    public abstract void handlePlayerQuitEvent(@NotNull PlayerQuitEvent playerQuitEvent);

    /**
     * Handles when a spawner spawns an {@link Entity}.
     * @param spawnerSpawnEvent A {@link SpawnerSpawnEvent}.
     */
    public abstract void handleEntitySpawn(@NotNull SpawnerSpawnEvent spawnerSpawnEvent);

    /**
     * Handles when a potion effect is applied to an {@link Entity}.
     * @param entityPotionEffectEvent An {@link EntityPotionEffectEvent}.
     */
    public abstract void handleEntityPotionEffect(@NotNull EntityPotionEffectEvent entityPotionEffectEvent);

    /**
     * Handles the decrementing of the trial's time by one.
     */
    public abstract void decrementTime();

    /**
     * Get the {@link World} for the trial.
     * @return A {@link World}.
     */
    public abstract @NotNull World getWorld();

    /**
     * Get the {@link ProtectedRegion} for the trial.
     * @return A {@link ProtectedRegion}.
     */
    public abstract @NotNull ProtectedRegion getRegion();

    /**
     * Get a {@link List} of {@link UUID}s that are inside the trial.
     * @return A {@link List} of {@link UUID}s.
     */
    public abstract @NotNull List<UUID> getPlayerIds();

    /**
     * Get a {@link List} of {@link Player}s that are inside the trial.
     * @return A {@link List} of {@link Player}s.
     */
    public abstract @NotNull List<Player> getPlayers();

    /**
     * This method can be run to place blocks.
     */
    protected abstract void placeBlocks();

    /**
     * This method can be run to remove blocks.
     */
    protected abstract void removeBlocks();

    /**
     * This method can be run to remove entities inside the trial.
     */
    protected abstract void removeEntities();
}