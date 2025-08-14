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
package com.github.lukesky19.skytrials.data.config.block;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skytrials.data.config.entity.EntityConfig;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import com.github.lukesky19.skytrials.data.config.misc.LootTableConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration for a trial spawner.
 * @param location The {@link LocationConfig} for where the trial spawner should be placed.
 * @param normal The {@link SpawnerConfig} for the normal spawner config.
 * @param ominous The {@link SpawnerConfig} for the ominous spawner config.
 */
@ConfigSerializable
public record TrialSpawnerConfig(
        @NotNull LocationConfig location,
        @NotNull SpawnerConfig normal,
        @NotNull SpawnerConfig ominous) {
    /**
     * The configuration for the trial spawner.
     * @param simultaneousMobs How many mobs should be spawned at once?
     * @param simultaneousMobsAddedPerPlayer How many additional mobs per player should be spawned at once?
     * @param playerRange The range the player must be in for mobs to spawn.
     * @param spawnRange How far away from the spawner mobs can spawn.
     * @param baseSpawnsBeforeCooldown The base number of entities spawned before going into cooldown.
     * @param additionalSpawnsBeforeCooldown The number of additional entities spawned per player before going into cooldown.
     * @param spawnPotentials A {@link List} of {@link SpawnPotential}s.
     * @param lootTables A {@link List} of {@link LootTableConfig}s.
     */
    @ConfigSerializable
    public record SpawnerConfig(
            @Nullable Float simultaneousMobs,
            @Nullable Float simultaneousMobsAddedPerPlayer,
            @Nullable Integer playerRange,
            @Nullable Integer spawnRange,
            @Nullable Float baseSpawnsBeforeCooldown,
            @Nullable Float additionalSpawnsBeforeCooldown,
            @NotNull List<SpawnPotential> spawnPotentials,
            @NotNull List<LootTableConfig> lootTables) {}
    /**
     * This record contains the configuration for an entity the trial spawner may spawn.
     * @param entityConfig The {@link EntityConfig} for the entity.
     * @param weight The weight. Determines the chance this entity can spawn.
     */
    @ConfigSerializable
    public record SpawnPotential(
            @NotNull EntityConfig entityConfig,
            @Nullable Integer weight) {}
}
