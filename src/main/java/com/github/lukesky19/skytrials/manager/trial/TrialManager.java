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
package com.github.lukesky19.skytrials.manager.trial;

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.github.lukesky19.skytrials.manager.player.CooldownManager;
import com.github.lukesky19.skytrials.manager.blocks.SpawnerManager;
import com.github.lukesky19.skytrials.manager.blocks.VaultManager;
import com.github.lukesky19.skytrials.manager.entity.EntityManager;
import com.github.lukesky19.skytrials.manager.player.GracePeriodManager;
import com.github.lukesky19.skytrials.trial.AbstractTrial;
import com.github.lukesky19.skytrials.trial.impl.ChamberTrial;
import com.github.lukesky19.skytrials.trial.impl.LevelTrial;
import com.github.lukesky19.skytrials.util.TrialEndReason;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class manages the actual running trials.
 */
public class TrialManager {
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull TrialDataManager trialDataManager;
    private final @NotNull SpawnerManager spawnerManager;
    private final @NotNull EntityManager entityManager;
    private final @NotNull VaultManager vaultManager;
    private final @NotNull CooldownManager cooldownManager;
    private final @NotNull GracePeriodManager gracePeriodManager;

    private final @NotNull Map<String, AbstractTrial> trialMap = new HashMap<>();

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param trialDataManager A {@link TrialDataManager} instance.
     * @param spawnerManager A {@link SpawnerManager} instance.
     * @param entityManager A {@link EntityManager} instance.
     * @param vaultManager A {@link VaultManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     * @param gracePeriodManager A {@link GracePeriodManager} instance.
     */
    public TrialManager(
            @NotNull SkyTrials skyTrials,
            @NotNull LocaleManager localeManager,
            @NotNull TrialDataManager trialDataManager,
            @NotNull SpawnerManager spawnerManager,
            @NotNull EntityManager entityManager,
            @NotNull VaultManager vaultManager,
            @NotNull CooldownManager cooldownManager,
            @NotNull GracePeriodManager gracePeriodManager) {
        this.skyTrials = skyTrials;
        this.localeManager = localeManager;
        this.trialDataManager = trialDataManager;
        this.spawnerManager = spawnerManager;
        this.entityManager = entityManager;
        this.vaultManager = vaultManager;
        this.cooldownManager = cooldownManager;
        this.gracePeriodManager = gracePeriodManager;
    }

    /**
     * Creates the trials from the trial data.
     */
    public void createTrials() {
        trialDataManager.getChamberTrialDataList().forEach(trialData ->
                trialMap.put(trialData.trialId(), new ChamberTrial(skyTrials, localeManager, spawnerManager, vaultManager, cooldownManager, gracePeriodManager, trialData)));

        trialDataManager.getLevelTrialDataList().forEach(trialData ->
                trialMap.put(trialData.trialId(), new LevelTrial(skyTrials, localeManager, entityManager, cooldownManager, gracePeriodManager, trialData)));
    }

    /**
     * Clears all trials of players and teleports them to exit areas.
     * Is used on reloads.
     */
    public void clearTrials() {
        for(AbstractTrial trial : trialMap.values()) {
            trial.end(TrialEndReason.RELOAD);
        }

        trialMap.clear();
    }

    /**
     * Get the {@link AbstractTrial} for the trial id provided.
     * @param trialId The id of the trial.
     * @return An {@link AbstractTrial} or null.
     */
    public @Nullable AbstractTrial getTrialById(@NotNull String trialId) {
        return trialMap.get(trialId);
    }

    /**
     * Get the {@link AbstractTrial} the player is in if any.
     * @param uuid The {@link UUID} of the player.
     * @return An {@link AbstractTrial} or null.
     */
    public @Nullable AbstractTrial getTrialByPlayerUUID(@NotNull UUID uuid) {
        for(AbstractTrial trial : trialMap.values()) {
            if(trial.getPlayerIds().contains(uuid)) return trial;
        }

        return null;
    }

    /**
     * Checks if a player is in a trial.
     * @param uuid The {@link UUID} of the playe.r
     * @return true if in a trial, otherwise false.
     */
    public boolean isPlayerInTrial(@NotNull UUID uuid) {
        for(AbstractTrial trial : trialMap.values()) {
            if(trial.getPlayerIds().contains(uuid)) return true;
        }

        return false;
    }

    /**
     * Get an {@link AbstractTrial} if the {@link Location} is inside a trial.
     * @param location The {@link Location} to check for a trial for.
     * @return An {@link AbstractTrial} or null.
     */
    public @Nullable AbstractTrial getTrialByLocation(@NotNull Location location) {
        for(AbstractTrial trial : trialMap.values()) {
            World trialWorld = trial.getWorld();
            ProtectedRegion trialRegion = trial.getRegion();

            if(location.getWorld().equals(trialWorld) && trialRegion.contains(location.getBlockX(), location.getBlockY(),location.getBlockZ())) {
                return trial;
            }
        }

        return null;
    }

    /**
     * Get a {@link List} of {@link String} for trial ids.
     * @return A {@link List} of {@link String} for trial ids.
     */
    public @NotNull List<String> getTrialIds() {
        return trialMap.keySet().stream().toList();
    }

    /**
     * Get a {@link List} of {@link AbstractTrial}s for all created trials.
     * @return A {@link List} of {@link AbstractTrial}s
     */
    public @NotNull List<AbstractTrial> getTrials() {
        return trialMap.values().stream().toList();
    }
}
