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
package com.github.lukesky19.skytrials.data.config.trial;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skytrials.data.config.block.TrialSpawnerConfig;
import com.github.lukesky19.skytrials.data.config.block.VaultConfig;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import com.github.lukesky19.skytrials.data.config.misc.RegionConfig;
import com.github.lukesky19.skytrials.data.trial.ChamberTrialData;
import com.github.lukesky19.skytrials.trial.impl.ChamberTrial;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This record contains the configuration to create {@link ChamberTrialData} that is used to create a {@link ChamberTrial}.
 * @param configVersion The version of the config file.
 * @param trialId The id of the trial.
 * @param timeLimitSeconds The trial's time limit in seconds.
 * @param cooldownSeconds The trial's cooldown in seconds to apply to players when the trial ends.
 * @param gracePeriodSeconds The trial's grace period in seconds to apply to players that log out within a trial.
 * @param region The {@link RegionConfig} for the trial.
 * @param joinLocation The {@link LocationConfig} for the join or lobby area.
 * @param startLocation The {@link LocationConfig} for the start area.
 * @param endLocation The {@link LocationConfig} for the end area. This is where the player is teleported when a trial ends.
 * @param lobbyBossBar The {@link LobbyBossBarConfig} for the boss bar.
 * @param trialBossBar The {@link TrialBossBarConfig} for the boss bar.
 * @param trialSpawners A {@link List} of {@link TrialSpawnerConfig}s for the trial.
 * @param vaults A {@link List} of {@link VaultConfig}s for the trial.
 */
@ConfigSerializable
public record ChamberTrialConfig(
        @Nullable String configVersion,
        @Nullable String trialId,
        @Nullable Integer timeLimitSeconds,
        @Nullable Integer cooldownSeconds,
        @Nullable Integer gracePeriodSeconds,
        @NotNull RegionConfig region,
        @NotNull LocationConfig joinLocation,
        @NotNull LocationConfig startLocation,
        @NotNull LocationConfig endLocation,
        @NotNull LobbyBossBarConfig lobbyBossBar,
        @NotNull TrialBossBarConfig trialBossBar,
        @NotNull List<TrialSpawnerConfig> trialSpawners,
        @NotNull List<VaultConfig> vaults) {
    /**
     * The config for the boss bar shown to the player during the trial.
     * @param timeLimitText The boss bar text to show when the trial has a time limit.
     * @param noTimeLimitText The text to show when the trial has no time limit.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record TrialBossBarConfig(
            @Nullable String timeLimitText,
            @Nullable String noTimeLimitText,
            @Nullable BossBar.Color color,
            @Nullable BossBar.Overlay overlay) {}
    /**
     * The config for the boss bar shown to the player while waiting for the trial to start.
     * @param bossBarText The boss bar text to show.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record LobbyBossBarConfig(
            @Nullable String bossBarText,
            @Nullable BossBar.Color color,
            @Nullable BossBar.Overlay overlay) {}
}
