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
package com.github.lukesky19.skytrials.data.config.entity;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This record contains the configuration to create an {@link Entity}.
 * @param entityType The {@link EntityType}.
 * @param options The {@link Options} with extra options for the entity.
 * @param effects A {@link List} of {@link EffectConfig}s.
 * @param equipment The {@link EquipmentConfig} for the entity.
 * @param attributes A {@link List} of {@link AttributeConfig}s.
 * @param lootTable The loot table key name for the loot table to apply to the entity.
 */
@ConfigSerializable
public record EntityConfig(
        @Nullable String entityType,
        @NotNull Options options,
        @NotNull List<EffectConfig> effects,
        @NotNull EquipmentConfig equipment,
        @NotNull List<AttributeConfig> attributes,
        @NotNull String lootTable) {
    /**
     * Extra options to apply to the entity.
     * @param isCharged Is the entity charged? For creepers only.
     * @param isBaby Is the entity a baby?
     * @param canPickupItems Can the entity pick up items?
     * @param persistent Should the entity persistent?
     * @param removeWhenFarAway Should the entity be removed when far away?
     * @param glowing Should the entity be glowing?
     */
    @ConfigSerializable
    public record Options(
            @Nullable Boolean isCharged,
            @Nullable Boolean isBaby,
            @Nullable Boolean canPickupItems,
            @Nullable Boolean persistent,
            @Nullable Boolean removeWhenFarAway,
            @Nullable Boolean glowing) {}
}
