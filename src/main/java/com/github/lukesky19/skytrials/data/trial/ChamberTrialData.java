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
package com.github.lukesky19.skytrials.data.trial;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skytrials.data.config.block.TrialSpawnerConfig;
import com.github.lukesky19.skytrials.data.config.block.VaultConfig;
import com.github.lukesky19.skytrials.trial.impl.ChamberTrial;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record contains the data that is used to create a {@link ChamberTrial}.
 * @param trialId The id of the trial.
 * @param timeLimitSeconds The trial's time limit in seconds. -1 to disable.
 * @param cooldownSeconds The trial's cooldown in seconds to apply to players when the trial ends. -1 to disable.
 * @param gracePeriodSeconds The trial's grace period in seconds to apply to players that log out within a trial. -1 to disable.
 * @param trialWorld The trial's {@link World}.
 * @param trialRegion The trial's {@link ProtectedRegion}.
 * @param joinLocation The trial's join or lobby {@link Location}.
 * @param startLocation The trial's start {@link Location}.
 * @param endLocation The trial's end {@link Location}. This is where the player is teleported when a trial ends.
 * @param lobbyBossBar THe {@link LobbyBossBarData} for the boss bar to show while players are in the lobby.
 * @param trialBossBar The {@link TrialBossBarData} for the boss bar to show while the trial is active.
 * @param trialSpawnerConfigList A {@link List} of {@link TrialSpawnerConfig}s for the trial.
 * @param vaultConfigList A {@link List} of {@link VaultConfig}s for the trial.
 */
public record ChamberTrialData(
        @NotNull String trialId,
        int timeLimitSeconds,
        int cooldownSeconds,
        int gracePeriodSeconds,
        @NotNull World trialWorld,
        @NotNull ProtectedRegion trialRegion,
        @NotNull Location joinLocation,
        @NotNull Location startLocation,
        @NotNull Location endLocation,
        @NotNull LobbyBossBarData lobbyBossBar,
        @NotNull TrialBossBarData trialBossBar,
        @NotNull List<TrialSpawnerConfig> trialSpawnerConfigList,
        @NotNull List<VaultConfig> vaultConfigList) {
    /**
     * The data for the boss bar shown to the player during the trial.
     * @param timeLimitText The boss bar text to show when the trial has a time limit.
     * @param noTimeLimitText The text to show when the trial has no time limit.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record TrialBossBarData(
            @NotNull String timeLimitText,
            @NotNull String noTimeLimitText,
            @NotNull BossBar.Color color,
            @NotNull BossBar.Overlay overlay) {}
    /**
     * The data for the boss bar shown to the player while in the lobby.
     * @param bossBarText The boss bar text to show.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record LobbyBossBarData(
            @NotNull String bossBarText,
            @NotNull BossBar.Color color,
            @NotNull BossBar.Overlay overlay) {}
}
