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
package com.github.lukesky19.skytrials.manager.player;

import com.github.lukesky19.skytrials.data.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class manages grace periods for players that have logged out inside a trial.
 */
public class GracePeriodManager {
    private final @NotNull PlayerDataManager playerDataManager;

    /**
     * Constructor
     * @param playerDataManager A {@link PlayerDataManager} instance.
     */
    public GracePeriodManager(@NotNull PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    /**
     * Adds a new grace period time to the player id and trial id provided.
     * @param playerId The {@link UUID} of the player.
     * @param trialId The id of the trial.
     * @param gracePeriodTimeSeconds The grace period time in seconds to apply.
     */
    public void addGracePeriod(@NotNull UUID playerId, @NotNull String trialId, long gracePeriodTimeSeconds) {
        PlayerData playerData = playerDataManager.getPlayerData(playerId);

        Map<String, Long> gracePeriodMap = playerData.getGracePeriodsMap();

        gracePeriodMap.put(trialId, gracePeriodTimeSeconds);
    }

    /**
     * Removes the grace period for the player id and trial id provided.
     * @param playerId The {@link UUID} of the player.
     * @param trialId The id of the trial.
     */
    public void removeGracePeriod(@NotNull UUID playerId, @NotNull String trialId) {
        PlayerData playerData = playerDataManager.getPlayerData(playerId);

        Map<String, Long> gracePeriodMap = playerData.getGracePeriodsMap();

        gracePeriodMap.remove(trialId);
    }

    /**
     * Removes any grace periods stored for the trial id provided.
     * @param trialId The id of the trial.
     * @return A {@link List} of {@link UUID} that had their grace periods removed.
     */
    public @NotNull List<UUID> removeGracePeriods(@NotNull String trialId) {
        List<UUID> playersWithGracePeriods = new ArrayList<>();

        playerDataManager.getPlayerDataMap().forEach((uuid, playerData) -> {
            Map<String, Long> gracePeriodMap = playerData.getGracePeriodsMap();

            gracePeriodMap.remove(trialId);

            playersWithGracePeriods.add(uuid);
        });

        return playersWithGracePeriods;
    }

    /**
     * Checks if the player has a grace period for the provided trial id.
     * @param playerId The {@link UUID} of the player.
     * @param trialId The id of the trial.
     * @return true if they have a grace period, otherwise false.
     */
    public boolean doesPlayerHaveGracePeriod(@NotNull UUID playerId, @NotNull String trialId) {
        PlayerData playerData = playerDataManager.getPlayerData(playerId);

        return playerData.getGracePeriodsMap().containsKey(trialId);
    }

    /**
     * Decrement the grace period times for all players and trials.
     */
    public void decrementGracePeriods() {
        playerDataManager.getPlayerDataMap().forEach((uuid, playerData) -> {
            Map<String, Long> gracePeriodsMap = playerData.getGracePeriodsMap();

            Iterator<Map.Entry<String, Long>> iterator = gracePeriodsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                long newGracePeriodTime = entry.getValue() - 1;

                if(newGracePeriodTime <= 0) {
                    iterator.remove();
                } else {
                    gracePeriodsMap.put(entry.getKey(), newGracePeriodTime);
                }
            }
        });
    }
}
