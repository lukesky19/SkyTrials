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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.misc.RegionConfig;
import com.github.lukesky19.skytrials.data.config.trial.ChamberTrialConfig;
import com.github.lukesky19.skytrials.data.config.trial.LevelTrialConfig;
import com.github.lukesky19.skytrials.data.trial.ChamberTrialData;
import com.github.lukesky19.skytrials.data.trial.LevelTrialData;
import com.github.lukesky19.skytrials.util.LocationUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class manages the creation and storage of trial data.
 */
public class TrialDataManager {
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull ComponentLogger logger;
    private final @NotNull TrialConfigManager trialConfigManager;

    private final @NotNull List<ChamberTrialData> chamberTrialDataList = new ArrayList<>();
    private final @NotNull List<LevelTrialData> levelTrialDataList = new ArrayList<>();

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param trialConfigManager A {@link TrialConfigManager} instance.
     */
    public TrialDataManager(@NotNull SkyTrials skyTrials, @NotNull TrialConfigManager trialConfigManager) {
        this.skyTrials = skyTrials;
        this.logger = skyTrials.getComponentLogger();
        this.trialConfigManager = trialConfigManager;
    }

    /**
     * Get the {@link List} of {@link ChamberTrialData}.
     * @return A {@link List} of {@link ChamberTrialData}.
     */
    public @NotNull List<ChamberTrialData> getChamberTrialDataList() {
        return chamberTrialDataList;
    }

    /**
     * Get the {@link List} of {@link LevelTrialData}.
     * @return A {@link List} of {@link LevelTrialData}.
     */
    public @NotNull List<LevelTrialData> getLevelTrialDataList() {
        return levelTrialDataList;
    }

    /**
     * Create the trial data from trial configs.
     */
    public void createTrialData() {
        chamberTrialDataList.clear();
        levelTrialDataList.clear();

        trialConfigManager.getChamberTrialConfigList().forEach(this::createChamberTrialData);

        trialConfigManager.getLevelTrialConfigList().forEach(this::createLevelTrialData);
    }

    /**
     * Create the {@link ChamberTrialData} from a {@link ChamberTrialConfig}.
     * @param config The {@link ChamberTrialConfig}.
     */
    private void createChamberTrialData(@NotNull ChamberTrialConfig config) {
        if(config.trialId() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the trial id is invalid."));
            return;
        }

        int timeLimitSeconds = -1;
        if(config.timeLimitSeconds() != null && config.timeLimitSeconds() > 0) {
            timeLimitSeconds = config.timeLimitSeconds();
        }

        int cooldownSeconds = -1;
        if(config.cooldownSeconds() != null && config.cooldownSeconds() > 0) {
            cooldownSeconds = config.cooldownSeconds();
        }

        int gracePeriodSeconds = -1;
        if(config.gracePeriodSeconds() != null && config.gracePeriodSeconds() > 0) {
            gracePeriodSeconds = config.gracePeriodSeconds();
        }

        RegionConfig regionConfig = config.region();
        if(regionConfig.worldName() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the world name in the region config is invalid."));
            return;
        }

        World world = skyTrials.getServer().getWorld(regionConfig.worldName());
        if(world == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the world was not found for " + regionConfig.worldName() + " in the region config is invalid."));
            return;
        }

        if(regionConfig.regionName() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the region name in the region config is invalid."));
            return;
        }

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if(regionManager == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the region manager for world " + regionConfig.worldName() + " is null."));
            return;
        }

        ProtectedRegion protectedRegion = regionManager.getRegion(regionConfig.regionName());
        if(protectedRegion == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the region was not found for " + regionConfig.regionName() + " in the region config is invalid."));
            return;
        }

        Location joinLocation = LocationUtil.getLocation(skyTrials, config.joinLocation());
        if(joinLocation == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the join location is invalid."));
            return;
        }

        Location startLocation = LocationUtil.getLocation(skyTrials, config.startLocation());
        if(startLocation == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the start location is invalid."));
            return;
        }

        Location endLocation = LocationUtil.getLocation(skyTrials, config.endLocation());
        if(endLocation == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the end location is invalid."));
            return;
        }

        ChamberTrialConfig.LobbyBossBarConfig lobbyBossBarConfig = config.lobbyBossBar();
        if(lobbyBossBarConfig.bossBarText() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the lobby boss bar text is invalid."));
            return;
        }

        if(lobbyBossBarConfig.color() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the lobby boss bar color is invalid."));
            return;
        }

        if(lobbyBossBarConfig.overlay() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the lobby boss bar overlay is invalid."));
            return;
        }

        ChamberTrialData.LobbyBossBarData lobbyBossBarData = new ChamberTrialData.LobbyBossBarData(lobbyBossBarConfig.bossBarText(), lobbyBossBarConfig.color(), lobbyBossBarConfig.overlay());

        ChamberTrialConfig.TrialBossBarConfig trialBossBarConfig = config.trialBossBar();
        if(trialBossBarConfig.timeLimitText() == null || trialBossBarConfig.noTimeLimitText() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the trial boss bar text is invalid."));
            return;
        }

        if(trialBossBarConfig.color() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the trial boss bar color is invalid."));
            return;
        }

        if(trialBossBarConfig.overlay() == null) {
            logger.error(AdventureUtil.serialize("Unable to create chamber trial data as the trial boss bar overlay is invalid."));
            return;
        }

        ChamberTrialData.TrialBossBarData trialBossBarData = new ChamberTrialData.TrialBossBarData(trialBossBarConfig.timeLimitText(), trialBossBarConfig.noTimeLimitText(), trialBossBarConfig.color(), trialBossBarConfig.overlay());

        ChamberTrialData chamberTrialData = new ChamberTrialData(
                config.trialId(), timeLimitSeconds, cooldownSeconds, gracePeriodSeconds, world, protectedRegion,
                joinLocation, startLocation, endLocation, lobbyBossBarData, trialBossBarData, config.trialSpawners(), config.vaults());

        chamberTrialDataList.add(chamberTrialData);
    }

    /**
     * Create the {@link LevelTrialData} from a {@link LevelTrialConfig}.
     * @param config The {@link LevelTrialConfig}.
     */
    private void createLevelTrialData(@NotNull LevelTrialConfig config) {
        if(config.trialId() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the trial id is invalid."));
            return;
        }

        int timeLimitSeconds = -1;
        if(config.timeLimitSeconds() != null && config.timeLimitSeconds() > 0) {
            timeLimitSeconds = config.timeLimitSeconds();
        }

        int cooldownSeconds = -1;
        if(config.cooldownSeconds() != null && config.cooldownSeconds() > 0) {
            cooldownSeconds = config.cooldownSeconds();
        }

        int gracePeriodSeconds = -1;
        if(config.gracePeriodSeconds() != null && config.gracePeriodSeconds() > 0) {
            gracePeriodSeconds = config.gracePeriodSeconds();
        }

        RegionConfig regionConfig = config.region();
        if(regionConfig.worldName() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the world name in the region config is invalid."));
            return;
        }

        World world = skyTrials.getServer().getWorld(regionConfig.worldName());
        if(world == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the world was not found for " + regionConfig.worldName() + " in the region config is invalid."));
            return;
        }

        if(regionConfig.regionName() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the region name in the region config is invalid."));
            return;
        }

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if(regionManager == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the region manager for world " + regionConfig.worldName() + " is null."));
            return;
        }

        ProtectedRegion protectedRegion = regionManager.getRegion(regionConfig.regionName());
        if(protectedRegion == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the region was not found for " + regionConfig.regionName() + " in the region config is invalid."));
            return;
        }

        Location joinLocation = LocationUtil.getLocation(skyTrials, config.joinLocation());
        if(joinLocation == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the join location is invalid."));
            return;
        }

        Location endLocation = LocationUtil.getLocation(skyTrials, config.endLocation());
        if(endLocation == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the end location is invalid."));
            return;
        }

        LevelTrialConfig.BossBarConfig lobbyBossBarConfig = config.lobbyBossBar();
        if(lobbyBossBarConfig.bossBarText() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the lobby boss bar text is invalid."));
            return;
        }

        if(lobbyBossBarConfig.color() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the lobby boss bar color is invalid."));
            return;
        }

        if(lobbyBossBarConfig.overlay() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the lobby boss bar overlay is invalid."));
            return;
        }

        LevelTrialData.BossBarData lobbyBossBarData = new LevelTrialData.BossBarData(lobbyBossBarConfig.bossBarText(), lobbyBossBarConfig.color(), lobbyBossBarConfig.overlay());

        LevelTrialConfig.BossBarConfig trialBossBarConfig = config.trialBossBar();
        if(trialBossBarConfig.bossBarText() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the trial boss bar text is invalid."));
            return;
        }

        if(trialBossBarConfig.color() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the trial boss bar color is invalid."));
            return;
        }

        if(trialBossBarConfig.overlay() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the trial boss bar overlay is invalid."));
            return;
        }

        LevelTrialData.BossBarData trialBossBarData = new LevelTrialData.BossBarData(trialBossBarConfig.bossBarText(), trialBossBarConfig.color(), trialBossBarConfig.overlay());

        if(config.clearEffectsOnTrialEnd() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the clear effects on trial end boolean is invalid."));
            return;
        }

        if(config.rewardOnTimeEnd() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the reward on time end boolean is invalid."));
            return;
        }

        List<PotionEffect> potionEffectList = config.playerEffects().stream()
                .filter(effectConfig ->
                        effectConfig.effectName() != null
                                && effectConfig.durationInSeconds() != null
                                && effectConfig.durationInSeconds() > 0
                                && effectConfig.amplifier() != null
                                && effectConfig.amplifier() >= 0)
                .map(effectConfig -> {
                    Optional<PotionEffectType> optionalPotionEffectType = RegistryUtil.getPotionEffectType(logger, effectConfig.effectName());
                    return optionalPotionEffectType.map(potionEffectType -> potionEffectType.createEffect(effectConfig.durationInSeconds(), effectConfig.amplifier())).orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        List<ItemStack> rewardItemStacks = config.endItemRewards().stream()
                .map(itemStackConfig -> new ItemStackBuilder(logger).fromItemStackConfig(itemStackConfig, null, null, List.of()).buildItemStack())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<String> rewardCommands = config.endCommandsRewards().stream().filter(Objects::nonNull).filter(command -> !command.isEmpty()).toList();

        List<LevelTrialData.LevelData> levelDataList = new ArrayList<>();

        for(LevelTrialConfig.Level levelConfig : config.levels()) {
            LevelTrialData.LevelData levelData = createLevelData(levelConfig);

            if(levelData == null) {
                logger.error(AdventureUtil.serialize("Unable to create level trial data due to an invalid level config."));
                return;
            }

            levelDataList.add(levelData);
        }

        LevelTrialData levelTrialData = new LevelTrialData(
                config.trialId(),
                timeLimitSeconds,
                cooldownSeconds,
                gracePeriodSeconds,
                world,
                protectedRegion,
                joinLocation,
                endLocation,
                lobbyBossBarData,
                trialBossBarData,
                config.clearEffectsOnTrialEnd(),
                config.rewardOnTimeEnd(),
                potionEffectList,
                rewardItemStacks,
                rewardCommands,
                levelDataList);

        levelTrialDataList.add(levelTrialData);
    }

    /**
     * Create the {@link LevelTrialData.LevelData} from a {@link LevelTrialConfig.Level}.
     * @param config The {@link LevelTrialConfig.Level}.
     * @return A {@link LevelTrialData.LevelData} or null if creation failed.
     */
    private @Nullable LevelTrialData.LevelData createLevelData(@NotNull LevelTrialConfig.Level config) {
        Location startLocation = LocationUtil.getLocation(skyTrials, config.startLocation());
        if(startLocation == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the start location for a level is invalid."));
            return null;
        }

        if(config.removeMobsOnLevelEnd() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the remove mobs on level end boolean is invalid."));
            return null;
        }

        if(config.clearEffectsOnLevelEnd() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the clear effects on level end boolean is invalid."));
            return null;
        }

        if(config.allowMilkEffectRemoval() == null) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the allow milk effect removal boolean is invalid."));
            return null;
        }

        int baseMobLimit = -1;
        if(config.baseMobLimit() != null && config.baseMobLimit() > 0) {
            baseMobLimit = config.baseMobLimit();
        }

        int additionalMobLimitPerPlayer = -1;
        if(config.additionalMobLimitPerPlayer() != null && config.additionalMobLimitPerPlayer() > 0) {
            additionalMobLimitPerPlayer = config.additionalMobLimitPerPlayer();
        }

        int goalCount = -1;
        if(config.goalCount() != null && config.goalCount() > 0) {
            goalCount = config.goalCount();
        }

        int additionalGoalCountPerPlayer = -1;
        if(config.additionalGoalCountPerPlayer() != null && config.additionalGoalCountPerPlayer() > 0) {
            additionalGoalCountPerPlayer = config.additionalGoalCountPerPlayer();
        }

        int spawnCount = -1;
        if(config.spawnCount() != null && config.spawnCount() > 0) {
            spawnCount = config.spawnCount();
        }

        int additionalSpawnCountPerPlayer = -1;
        if(config.additionalSpawnCountPerPlayer() != null && config.additionalSpawnCountPerPlayer() > 0) {
            additionalSpawnCountPerPlayer = config.additionalSpawnCountPerPlayer();
        }

        if(config.mobSpawnStartDelay() == null  || config.mobSpawnStartDelay() < 0) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the mob spawn start delay is invalid."));
            return null;
        }

        if(config.mobSpawnFrequencySeconds() == null || config.mobSpawnFrequencySeconds() < 0) {
            logger.error(AdventureUtil.serialize("Unable to create level trial data as the mob spawn frequency is invalid."));
            return null;
        }

        List<ItemStack> rewardItemStacks = config.rewardItems().stream()
                .map(itemStackConfig -> new ItemStackBuilder(logger).fromItemStackConfig(itemStackConfig, null, null, List.of()).buildItemStack())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        List<String> rewardCommands = config.rewardCommands().stream().filter(Objects::nonNull).filter(command -> !command.isEmpty()).toList();

        List<PotionEffect> potionEffectList = config.playerEffects().stream()
                .filter(effectConfig ->
                        effectConfig.effectName() != null
                                && effectConfig.durationInSeconds() != null
                                && effectConfig.durationInSeconds() > 0
                                && effectConfig.amplifier() != null
                                && effectConfig.amplifier() >= 0)
                .map(effectConfig -> {
                    Optional<PotionEffectType> optionalPotionEffectType = RegistryUtil.getPotionEffectType(logger, effectConfig.effectName());
                    return optionalPotionEffectType.map(potionEffectType -> potionEffectType.createEffect(effectConfig.durationInSeconds(), effectConfig.amplifier())).orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        List<LevelTrialData.MobSpawn> entityList = config.mobSpawns().stream().map(mobSpawn -> {
            List<Location> mobSpawnLocations = mobSpawn.spawnLocations().stream().map(locationConfig -> LocationUtil.getLocation(skyTrials, locationConfig)).filter(Objects::nonNull).toList();

            return new LevelTrialData.MobSpawn(mobSpawn.entityConfig(), mobSpawnLocations);
        }).toList();

        return new LevelTrialData.LevelData(
                startLocation,
                config.removeMobsOnLevelEnd(),
                config.clearEffectsOnLevelEnd(),
                config.allowMilkEffectRemoval(),
                baseMobLimit,
                additionalMobLimitPerPlayer,
                goalCount,
                additionalGoalCountPerPlayer,
                spawnCount,
                additionalSpawnCountPerPlayer,
                config.mobSpawnStartDelay(),
                config.mobSpawnFrequencySeconds(),
                potionEffectList,
                rewardItemStacks,
                rewardCommands,
                entityList);
    }
}
