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
import com.github.lukesky19.skytrials.data.config.entity.EntityConfig;
import com.github.lukesky19.skytrials.data.config.trial.LevelTrialConfig;
import com.github.lukesky19.skytrials.trial.impl.LevelTrial;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This record contains the data that is used to create a {@link LevelTrial}.
 * @param trialId The id of the trial.
 * @param timeLimitSeconds The trial's time limit in seconds. -1 to disable.
 * @param cooldownSeconds The trial's cooldown in seconds to apply to players when the trial ends. -1 to disable.
 * @param gracePeriodSeconds The trial's grace period in seconds to apply to players that log out within a trial. -1 to disable.
 * @param trialWorld The trial's {@link World}.
 * @param trialRegion The trial's {@link ProtectedRegion}.
 * @param joinLocation The trial's join or lobby {@link Location}.
 * @param endLocation The trial's end {@link Location}. This is where the player is teleported when a trial ends.
 * @param lobbyBossBar The {@link BossBarData} for the boss bar when in a trial's lobby.
 * @param trialBossBar The {@link BossBarData} for the boss bar when in an active trial.
 * @param clearEffectsOnTrialEnd Whether to clear potion effects on trial end.
 * @param rewardOnTimeEnd Whether to apply the following rewards when the trial's time limit ends.
 * @param playerEffects The {@link List} of {@link PotionEffect}s to apply to the player on trial start.
 * @param trialRewardItemStacks A {@link List} of {@link ItemStack}s to reward.
 * @param trialRewardCommands A {@link List} of commands as a {@link String} to execute on trial end.
 * @param levels A {@link List} of {@link LevelTrialConfig.Level}s for the trial.
 */
@ConfigSerializable
public record LevelTrialData(
        @NotNull String trialId,
        int timeLimitSeconds,
        int cooldownSeconds,
        int gracePeriodSeconds,
        @NotNull World trialWorld,
        @NotNull ProtectedRegion trialRegion,
        @NotNull Location joinLocation,
        @NotNull Location endLocation,
        @NotNull BossBarData lobbyBossBar,
        @NotNull BossBarData trialBossBar,
        boolean clearEffectsOnTrialEnd,
        boolean rewardOnTimeEnd,
        @NotNull List<PotionEffect> playerEffects,
        @NotNull List<ItemStack> trialRewardItemStacks,
        @NotNull List<String> trialRewardCommands,
        @NotNull List<LevelData> levels) {
    /**
     * This record contains the data for a single level.
     * @param startLocation The trial's start {@link Location}.
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
     * @param playerEffects A {@link List} of {@link PotionEffect}s to apply to the player.
     * @param rewardItemStacks A {@link List} {@link ItemStack}s to reward.
     * @param rewardCommands A {@link List} of commands as a {@link String} to execute on trial end.
     * @param mobSpawnList A {@link List} of {@link MobSpawn} for the level.
     */
    @ConfigSerializable
    public record LevelData(
            @NotNull Location startLocation,
            boolean removeMobsOnLevelEnd,
            boolean clearEffectsOnLevelEnd,
            boolean allowMilkEffectRemoval,
            int baseMobLimit,
            int additionalMobLimitPerPlayer,
            int goalCount,
            int additionalGoalCountPerPlayer,
            int spawnCount,
            int additionalSpawnCountPerPlayer,
            long mobSpawnStartDelay,
            long mobSpawnFrequencySeconds,
            @NotNull List<PotionEffect> playerEffects,
            @NotNull List<ItemStack> rewardItemStacks,
            @NotNull List<String> rewardCommands,
            @NotNull List<MobSpawn> mobSpawnList) {}
    /**
     * The data for the boss bar shown to the player.
     * @param bossBarText The boss bar text to show when in the trial.
     * @param color The color of the boss bar.
     * @param overlay The overlay of the boss bar.
     */
    @ConfigSerializable
    public record BossBarData(
            @NotNull String bossBarText,
            @NotNull BossBar.Color color,
            @NotNull BossBar.Overlay overlay) {}
    /**
     * The {@link EntityConfig} for the mob and the {@link List} of {@link Location} that it can spawn at.
     * @param entityConfig The {@link EntityConfig} for the mob.
     * @param spawnLocations The {@link List} of {@link Location}s that it can spawn at.
     */
    @ConfigSerializable
    public record MobSpawn(
            @NotNull EntityConfig entityConfig,
            @NotNull List<Location> spawnLocations) {}
}
