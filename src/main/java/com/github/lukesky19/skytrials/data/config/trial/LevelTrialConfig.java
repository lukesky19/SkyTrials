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

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skytrials.data.config.entity.EffectConfig;
import com.github.lukesky19.skytrials.data.config.entity.EntityConfig;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import com.github.lukesky19.skytrials.data.config.misc.RegionConfig;
import com.github.lukesky19.skytrials.data.trial.LevelTrialData;
import com.github.lukesky19.skytrials.trial.impl.LevelTrial;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This record contains the configuration to create the {@link LevelTrialData} that is used to create a {@link LevelTrial}.
 * @param configVersion The version of the config file.
 * @param trialId The id of the trial.
 * @param timeLimitSeconds The trial's time limit in seconds.
 * @param cooldownSeconds The trial's cooldown in seconds to apply to players when the trial ends.
 * @param gracePeriodSeconds The trial's grace period in seconds to apply to players that log out within a trial.
 * @param region The {@link RegionConfig} for the trial.
 * @param joinLocation The {@link LocationConfig} for the join or lobby area.
 * @param endLocation The {@link LocationConfig} for the end area. This is where the player is teleported when a trial ends.
 * @param lobbyBossBar The {@link BossBarConfig} for the boss bar to show when in the lobby.
 * @param trialBossBar The {@link BossBarConfig} for the boss bar to show when in the trial.
 * @param clearEffectsOnTrialEnd Whether to clear potion effects when the trial ends or not.
 * @param rewardOnTimeEnd Whether to apply the following rewards when the trial's time limit ends.
 * @param playerEffects The {@link List} of {@link EffectConfig}s to apply to the player on trial start.
 * @param endItemRewards A {@link List} of {@link ItemStackConfig}s for the {@link ItemStack}s to reward.
 * @param endCommandsRewards A {@link List} of commands as a {@link String} to execute on trial end.
 * @param levels A {@link List} of {@link Level}s for the trial.
 */
@ConfigSerializable
public record LevelTrialConfig(
        @Nullable String configVersion,
        @Nullable String trialId,
        @Nullable Integer timeLimitSeconds,
        @Nullable Integer cooldownSeconds,
        @Nullable Integer gracePeriodSeconds,
        @NotNull RegionConfig region,
        @NotNull LocationConfig joinLocation,
        @NotNull LocationConfig endLocation,
        @NotNull BossBarConfig lobbyBossBar,
        @NotNull BossBarConfig trialBossBar,
        @Nullable Boolean clearEffectsOnTrialEnd,
        @Nullable Boolean rewardOnTimeEnd,
        @NotNull List<EffectConfig> playerEffects,
        @NotNull List<ItemStackConfig> endItemRewards,
        @NotNull List<String> endCommandsRewards,
        @NotNull List<Level> levels) {
    /**
     * This record contains the configuration for a single level.
     * @param startLocation The {@link LocationConfig} for the start area.
     * @param removeMobsOnLevelEnd Should mobs in the trial be removed at the end of the level?
     * @param clearEffectsOnLevelEnd Should potion effects be cleared at the end of the level?
     * @param allowMilkEffectRemoval Should potion effects allowed to be removed by milk?
     * @param baseMobLimit The base mob limit. -1 to disable.
     * @param additionalMobLimitPerPlayer The additional mob limit per player. -1 to disable.
     * @param goalCount The base goal count. -1 to disable.
     * @param additionalGoalCountPerPlayer The additional goal count per player. -1 to disable.
     * @param spawnCount The base spawn count. -1 to disable.
     * @param additionalSpawnCountPerPlayer The additional spawn count per player. -1 to disable.
     * @param mobSpawnStartDelay The initial mob spawn start delay in seconds. -1 to disable.
     * @param mobSpawnFrequencySeconds How frequent to spawn mobs in seconds. -1 to disable.
     * @param playerEffects A {@link List} of {@link EffectConfig}s to apply to the player.
     * @param rewardItems A {@link List} of {@link ItemStackConfig}s for the {@link ItemStack}s to reward.
     * @param rewardCommands A {@link List} of commands as a {@link String} to execute on trial end.
     * @param mobSpawns A {@link List} of {@link MobSpawn}s for the level.
     */
    @ConfigSerializable
    public record Level(
            @NotNull LocationConfig startLocation,
            @Nullable Boolean removeMobsOnLevelEnd,
            @Nullable Boolean clearEffectsOnLevelEnd,
            @Nullable Boolean allowMilkEffectRemoval,
            @Nullable Integer baseMobLimit, // -1 to disable
            @Nullable Integer additionalMobLimitPerPlayer, // -1 to disable
            @Nullable Integer goalCount, // -1 to disable
            @Nullable Integer additionalGoalCountPerPlayer, // -1 to disable
            @Nullable Integer spawnCount, // -1 to disable
            @Nullable Integer additionalSpawnCountPerPlayer, // - 1 to disable
            @Nullable Long mobSpawnStartDelay,
            @Nullable Long mobSpawnFrequencySeconds,
            @NotNull List<EffectConfig> playerEffects,
            @NotNull List<ItemStackConfig> rewardItems,
            @NotNull List<String> rewardCommands,
            @NotNull List<MobSpawn> mobSpawns) {}
    /**
     * The data for the boss bar shown to the player.
     * @param bossBarText The boss bar text to show when in the lobby or trial.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record BossBarConfig(
            @Nullable String bossBarText,
            @Nullable BossBar.Color color,
            @Nullable BossBar.Overlay overlay) {}
    /**
     * The {@link EntityConfig} for the mob and the {@link List} of {@link LocationConfig} that it can spawn at.
     * @param entityConfig The {@link EntityConfig}  for the mob.
     * @param spawnLocations The {@link List} of {@link LocationConfig}s that it can spawn at.
     */
    @ConfigSerializable
    public record MobSpawn(
            @NotNull EntityConfig entityConfig,
            @NotNull List<LocationConfig> spawnLocations) {}
}