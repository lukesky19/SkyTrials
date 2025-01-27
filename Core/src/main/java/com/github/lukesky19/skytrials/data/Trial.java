/*
    SkyTrials is a mob arena plugin inspired by the Minecraft 1.21 Trial Chambers using Trial Spawners and Vault blocks.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skytrials.data;

import com.github.lukesky19.skytrials.configuration.record.TrialConfig;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Trial {
    private final String trialId;
    private final TrialConfig.LocationData joinLocation;
    private final TrialConfig.LocationData startLocation;
    private final TrialConfig.LocationData exitLocation;
    private final ProtectedRegion region;
    private final Long playerCooldownSeconds;
    private final Long timeLimitSeconds;
    private Long endTime;
    private final HashMap<UUID, Boolean> playerMap = new HashMap<>();
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, BukkitTask> gracePeriods = new HashMap<>();
    private boolean status = false;
    private BukkitTask timeLimitTask;
    private final HashMap<Integer, TrialConfig.TrialSpawnerData> trialSpawners;
    private final HashMap<Integer, TrialConfig.VaultData> vaultBlocks;

    public Trial(
            String trialId,
            TrialConfig.LocationData joinLocation,
            TrialConfig.LocationData startLocation,
            TrialConfig.LocationData exitLocation,
            ProtectedRegion region,
            Long playerCooldownSeconds,
            Long timeLimitSeconds,
            HashMap<Integer, TrialConfig.TrialSpawnerData> trialSpawners,
            HashMap<Integer, TrialConfig.VaultData> vaultBlocks) {
        this.trialId = trialId;
        this.joinLocation = joinLocation;
        this.startLocation = startLocation;
        this.exitLocation = exitLocation;
        this.region = region;
        this.playerCooldownSeconds = playerCooldownSeconds;
        this.timeLimitSeconds = timeLimitSeconds;
        this.trialSpawners = trialSpawners;
        this.vaultBlocks = vaultBlocks;
    }

    // Getters
    public String getTrialId() {
        return trialId;
    }

    public TrialConfig.LocationData getJoinLocation() {
        return joinLocation;
    }

    public TrialConfig.LocationData getStartLocation() {
        return startLocation;
    }

    public TrialConfig.LocationData getExitLocation() {
        return exitLocation;
    }

    public ProtectedRegion getRegion() {
        return region;
    }

    // Player(s)
    public void addPlayer(UUID uuid) {
        playerMap.put(uuid, false);
    }

    public void removePlayer(UUID uuid) {
        playerMap.remove(uuid);
    }

    public Boolean isPlayerInTrial(UUID uuid) {
        return playerMap.containsKey(uuid);
    }

    // Cooldowns Start
    /**
     * @return A HashMap of Players in the Trial and their ready status
     */
    public @NotNull HashMap<UUID, Boolean> getPlayerMap() {
        return playerMap;
    }

    public void addCooldown(@NotNull UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis() + (playerCooldownSeconds * 1000));
    }

    public void removeCooldown(@NotNull UUID uuid) {
        cooldowns.remove(uuid);
    }

    @CheckForNull
    public Long getPlayerCooldown(@NotNull UUID uuid) {
        return cooldowns.get(uuid);
    }

    public Boolean isPlayerOnCooldown(@NotNull UUID uuid) {
        return cooldowns.containsKey(uuid);
    }

    public @NotNull HashMap<UUID, Long> getCooldowns() {
        return cooldowns;
    }
    // Cooldowns End

    // Trial Status Start
    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }
    // Trial Status Start


    // Player Status Start
    public void setPlayerReadyStatus(UUID uuid, boolean status) {
        playerMap.put(uuid, status);
    }

    public boolean getPlayerReadyStatus(UUID uuid) {
        return playerMap.get(uuid);
    }

    public boolean arePlayersReady() {
        for(Map.Entry<UUID, Boolean> entry : playerMap.entrySet()) {
            Boolean status = entry.getValue();
            if(!status) {
                return false;
            }
        }

        return true;
    }
    // Player Status End

    // Trial End Time Start
    public void setEndTime(Long time) {
        this.endTime = time;
    }

    @CheckForNull
    public Long getEndTime() {
        return endTime;
    }
    // Trial End Time End

    public Long getTimeLimitSeconds() {
        return timeLimitSeconds;
    }


    // Time Limit Task Start
    @CheckForNull
    public BukkitTask getTimeLimitTask() {
        return timeLimitTask;
    }

    public void setTimeLimitTask(BukkitTask timeLimitTask) {
        this.timeLimitTask = timeLimitTask;
    }
    // Time Limit Task End

    // Grace Period Start
    @CheckForNull
    public BukkitTask getPlayerGracePeriod(UUID uuid) {
        return gracePeriods.get(uuid);
    }

    public void setPlayerGracePeriod(UUID uuid, BukkitTask task) {
        gracePeriods.put(uuid, task);
    }

    public void removePlayerGracePeriod(UUID uuid) {
        gracePeriods.remove(uuid);
    }

    public HashMap<UUID, BukkitTask> getGracePeriods() {
        return gracePeriods;
    }
    // Grace Period End

    public HashMap<Integer, TrialConfig.TrialSpawnerData> getTrialSpawnersConfig() {
        return trialSpawners;
    }

    public HashMap<Integer, TrialConfig.VaultData> getVaultBlocksConfig() {
        return vaultBlocks;
    }
}
