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
package com.github.lukesky19.skytrials.manager.entity;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.builder.EntityBuilder;
import com.github.lukesky19.skytrials.data.config.entity.EntityConfig;
import com.github.lukesky19.skytrials.data.trial.LevelTrialData;
import com.github.lukesky19.skytrials.trial.impl.LevelTrial;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This class handles spawning of entities created by the {@link EntityBuilder}.
 */
public class EntityManager {
    private final @NotNull ComponentLogger logger;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public EntityManager(@NotNull SkyTrials skyTrials) {
        this.logger = skyTrials.getComponentLogger();
    }

    /**
     * Spawns a random entity from the mob spawn list and at a random location configured for the randomly selected mob.
     * @param levelTrial The {@link LevelTrial} to spawn the entity in.
     * @param mobSpawnList The {@link List} of {@link LevelTrialData.MobSpawn}s.
     * @param playerCount The number of players in the trial.
     */
    public void spawnEntity(@NotNull LevelTrial levelTrial, @NotNull List<LevelTrialData.MobSpawn> mobSpawnList, int playerCount) {
        if(mobSpawnList.isEmpty()) return;
        Random random = new Random();

        int randomEntityKey;
        if(mobSpawnList.size() > 1) {
            randomEntityKey = random.nextInt(0, mobSpawnList.size());
        } else {
            randomEntityKey = 0;
        }

        LevelTrialData.MobSpawn mobSpawnConfig = mobSpawnList.get(randomEntityKey);
        List<Location> mobSpawnLocationList = mobSpawnConfig.spawnLocations();

        int randomLocationKey;
        if(mobSpawnLocationList.size() > 1) {
            randomLocationKey = random.nextInt(0, (mobSpawnLocationList.size() - 1));
        } else {
            randomLocationKey = 0;
        }

        Location location = mobSpawnLocationList.get(randomLocationKey);

        EntityConfig entityConfig = mobSpawnConfig.entityConfig();

        if(entityConfig.entityType() == null) {
            logger.warn(AdventureUtil.serialize("Unable to create Entity due to an invalid EntityType name."));
            return;
        }

        Optional<EntityType> optionalEntityType = RegistryUtil.getEntityType(logger, entityConfig.entityType());
        if(optionalEntityType.isEmpty()) {
            logger.warn(AdventureUtil.serialize("Unable to create SpawnerEntry due to an invalid EntityType for key " + entityConfig.entityType()));
            return;
        }

        EntityBuilder entityBuilder = new EntityBuilder(logger, optionalEntityType.get(), location.getWorld(), location, playerCount, entityConfig.options(), entityConfig.equipment(), entityConfig.effects(), entityConfig.attributes(), entityConfig.lootTable());
        entityBuilder.createEntity();

        levelTrial.incrementMobCount();
    }
}
