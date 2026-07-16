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
package com.github.lukesky19.skytrials.manager.blocks;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.paper.api.registry.RegistryUtil;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.builder.EntityBuilder;
import com.github.lukesky19.skytrials.data.config.block.TrialSpawnerConfig;
import com.github.lukesky19.skytrials.data.config.entity.EntityConfig;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import com.github.lukesky19.skytrials.data.config.misc.LootTableConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TrialSpawner;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.loot.LootTable;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class manages the placing and removal of trial spawners.
 */
public class SpawnerManager {
    private final @NonNull SkyTrials skyTrials;
    private final @NonNull ComponentLogger logger;
    private final @NonNull Server server;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public SpawnerManager(@NonNull SkyTrials skyTrials) {
        this.skyTrials = skyTrials;
        this.logger = skyTrials.getComponentLogger();
        this.server = skyTrials.getServer();
    }

    /**
     * Spawns the trial spawners for the {@link List} of {@link TrialSpawnerConfig} provided.
     * @param trialSpawnerConfigList A {@link List} of {@link TrialSpawnerConfig}s.
     * @param playerCount The number of players in the trial.
     */
    public void placeTrialSpawners(@NonNull List<TrialSpawnerConfig> trialSpawnerConfigList, int playerCount) {
        for(TrialSpawnerConfig trialSpawnerConfig : trialSpawnerConfigList) {
            if(trialSpawnerConfig.location().world() == null) {
                logger.warn(AdventureUtility.plain("The world name for a trial spawner config is invalid."));
                continue;
            }

            World spawnerWorld = skyTrials.getServer().getWorld(trialSpawnerConfig.location().world());
            if(spawnerWorld == null) {
                logger.warn(AdventureUtility.plain("A world for a trial spawner config is invalid."));
                continue;
            }

            LocationConfig locationConfig = trialSpawnerConfig.location();
            if(locationConfig.x() == null) {
                logger.warn(AdventureUtility.plain("The x coordinate for a trial spawner config is invalid."));
                continue;
            }

            if(locationConfig.y() == null) {
                logger.warn(AdventureUtility.plain("The y coordinate for a trial spawner config is invalid."));
                continue;
            }

            if(locationConfig.z() == null) {
                logger.warn(AdventureUtility.plain("The z coordinate for a trial spawner config is invalid."));
                continue;
            }

            // Create the Location for the trial spawner
            Location spawnerLocation = new Location(spawnerWorld, trialSpawnerConfig.location().x(), trialSpawnerConfig.location().y(), trialSpawnerConfig.location().z());

            // Get the Block to place the trial spawner at.
            Block block = spawnerWorld.getBlockAt(spawnerLocation);
            // Set the Block to a trial spawner
            block.setType(Material.TRIAL_SPAWNER);
            // Get the BlockState for the Block.
            BlockState state = block.getState(false);

            // Apply Trial Spawner settings
            if(state instanceof TrialSpawner trialSpawner) {
                TrialSpawnerConfig.SpawnerConfig normalData = trialSpawnerConfig.normal();
                applyTrialSpawnerConfigurationSettings(spawnerWorld, spawnerLocation, trialSpawner.getNormalConfiguration(), normalData, playerCount);

                TrialSpawnerConfig.SpawnerConfig ominousData = trialSpawnerConfig.ominous();
                applyTrialSpawnerConfigurationSettings(spawnerWorld, spawnerLocation, trialSpawner.getOminousConfiguration(), ominousData, playerCount);

                // Update block state
                state.update();
            }
        }
    }

    /**
     * Removes the trial spawners that were placed for the {@link List} of {@link TrialSpawnerConfig} provided.
     * @param trialSpawnerConfigList A {@link List} of {@link TrialSpawnerConfig}s.
     */
    public void removeTrialSpawners(@NonNull List<TrialSpawnerConfig> trialSpawnerConfigList) {
        for(TrialSpawnerConfig trialSpawnerConfig : trialSpawnerConfigList) {
            if(trialSpawnerConfig.location().world() == null) {
                logger.warn(AdventureUtility.plain("The world name for a trial spawner config is invalid."));
                continue;
            }

            World spawnerWorld = skyTrials.getServer().getWorld(trialSpawnerConfig.location().world());
            if(spawnerWorld == null) {
                logger.warn(AdventureUtility.plain("A world for a trial spawner config is invalid."));
                continue;
            }

            LocationConfig locationConfig = trialSpawnerConfig.location();
            if(locationConfig.x() == null) {
                logger.warn(AdventureUtility.plain("The x coordinate for a trial spawner config is invalid."));
                continue;
            }

            if(locationConfig.y() == null) {
                logger.warn(AdventureUtility.plain("The y coordinate for a trial spawner config is invalid."));
                continue;
            }

            if(locationConfig.z() == null) {
                logger.warn(AdventureUtility.plain("The z coordinate for a trial spawner config is invalid."));
                continue;
            }

            // Create the Location for the trial spawner
            Location spawnerLocation = new Location(spawnerWorld, trialSpawnerConfig.location().x(), trialSpawnerConfig.location().y(), trialSpawnerConfig.location().z());

            // Set the block to air
            Block block = spawnerWorld.getBlockAt(spawnerLocation);
            block.setType(Material.AIR);
        }
    }

    /**
     * Applies configuration settings to a {@link TrialSpawnerConfiguration}.
     * @param spawnerWorld The {@link World} the spawner will be placed in.
     * @param spawnerLocation The {@link Location} the spawner will be placed at.
     * @param configuration The {@link TrialSpawnerConfiguration} to apply configuration to.
     * @param spawnerConfig The {@link TrialSpawnerConfig.SpawnerConfig} to apply to the {@link TrialSpawnerConfiguration}.
     * @param playerCount The number of players in the trial.
     */
    private void applyTrialSpawnerConfigurationSettings(
            @NonNull World spawnerWorld,
            @NonNull Location spawnerLocation,
            @NonNull TrialSpawnerConfiguration configuration,
            TrialSpawnerConfig.@NonNull SpawnerConfig spawnerConfig,
            int playerCount) {
        if(spawnerConfig.simultaneousMobs() != null) {
            configuration.setBaseSimultaneousEntities(spawnerConfig.simultaneousMobs());
        }

        if(spawnerConfig.simultaneousMobsAddedPerPlayer() != null) {
            configuration.setAdditionalSimultaneousEntities(spawnerConfig.simultaneousMobsAddedPerPlayer());
        }

        if(spawnerConfig.playerRange() != null) {
            configuration.setRequiredPlayerRange(spawnerConfig.playerRange());
        }

        if(spawnerConfig.spawnRange() != null) {
            configuration.setSpawnRange(spawnerConfig.spawnRange());
        }

        if(spawnerConfig.baseSpawnsBeforeCooldown() != null) {
            configuration.setBaseSpawnsBeforeCooldown(spawnerConfig.baseSpawnsBeforeCooldown());
        }

        if(spawnerConfig.additionalSpawnsBeforeCooldown() != null) {
            configuration.setAdditionalSpawnsBeforeCooldown(spawnerConfig.additionalSpawnsBeforeCooldown());
        }

        for(TrialSpawnerConfig.SpawnPotential spawnPotential : spawnerConfig.spawnPotentials()) {
            SpawnerEntry spawnerEntry = createSpawnerEntry(spawnerWorld, spawnerLocation, spawnPotential, playerCount);
            if(spawnerEntry != null) {
                configuration.addPotentialSpawn(spawnerEntry);
            }
        }

        for(LootTableConfig lootTableConfig : spawnerConfig.lootTables()) {
            if(lootTableConfig.name() == null) {
                logger.warn(AdventureUtility.plain("A Trial Spawner configuration has a loot table configuration with an invalid key name."));
                continue;
            }

            NamespacedKey key = NamespacedKey.fromString(lootTableConfig.name());
            if(key == null) {
                logger.warn(AdventureUtility.plain("Failed to create a NamespacedKey for loot table key name " + lootTableConfig.name() + "."));
                continue;
            }

            LootTable lootTable = server.getLootTable(key);
            if(lootTable == null) {
                logger.warn(AdventureUtility.plain("No loot table found for NamespacedKey " + key + "."));
                continue;
            }

            configuration.addPossibleReward(lootTable, Objects.requireNonNullElse(lootTableConfig.weight(), 1));
        }
    }

    /**
     * Attempt to create a {@link SpawnerEntry} for a trial spawner.
     * @param world The {@link World} the spawner will be placed in.
     * @param location The {@link Location} the spawner will be placed at.
     * @param spawnPotential The {@link TrialSpawnerConfig.SpawnPotential} config.
     * @param playerCount The number of players in the trial.
     * @return The created {@link SpawnerEntry} or null if creation failed.
     */
    private @Nullable SpawnerEntry createSpawnerEntry(@NonNull World world, @NonNull Location location, TrialSpawnerConfig.SpawnPotential spawnPotential, int playerCount) {
        EntityConfig entityConfig = spawnPotential.entityConfig();

        if(entityConfig.entityType() == null) {
            logger.warn(AdventureUtility.plain("Unable to create SpawnerEntry due to an invalid EntityType name."));
            return null;
        }

        if(spawnPotential.weight() == null) {
            logger.warn(AdventureUtility.plain("Unable to create SpawnerEntry due to an invalid weight for entity Type: " + entityConfig.entityType()));
            return null;
        }

        Optional<EntityType> optionalEntityType = RegistryUtil.getEntityType(logger, entityConfig.entityType());
        if(optionalEntityType.isEmpty()) {
            logger.warn(AdventureUtility.plain("Unable to create SpawnerEntry due to an invalid EntityType for key " + entityConfig.entityType()));
            return null;
        }

        EntitySnapshot entitySnapshot = new EntityBuilder(
                    logger, optionalEntityType.get(), world, location, playerCount, entityConfig.options(),
                    entityConfig.equipment(), entityConfig.effects(), entityConfig.attributes(), entityConfig.lootTable())
                .createEntitySnapshot();

        if(entitySnapshot != null) {
            SpawnRule spawnRule = new SpawnRule(0, 15, 0, 15);
            return new SpawnerEntry(entitySnapshot, spawnPotential.weight(), spawnRule);
        }

        return null;
    }
}
