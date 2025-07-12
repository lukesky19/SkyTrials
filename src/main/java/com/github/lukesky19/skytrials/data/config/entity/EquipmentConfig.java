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

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This record contains the configuration to apply equipment to entities.
 * @param helmet The {@link ItemStackConfig} for the entity's helmet.
 * @param helmetDropChance The drop chance for the helmet.
 * @param chestplate The {@link ItemStackConfig} for the entity's chestplate.
 * @param chestplateDropChance The drop chance for the chestplate.
 * @param leggings The {@link ItemStackConfig} for the entity's leggings.
 * @param leggingsDropChance The drop chance for the leggings.
 * @param boots The {@link ItemStackConfig} for the entity's boots.
 * @param bootsDropChance The drop chance for the boots.
 * @param mainHand The {@link ItemStackConfig} for the entity's main hand.
 * @param mainHandDropChance The drop chance for the main hand.
 * @param offHand The {@link ItemStackConfig} for the entity's offhand.
 * @param offHandDropChance The drop chance for the offhand.
 */
@ConfigSerializable
public record EquipmentConfig(
        @NotNull ItemStackConfig helmet,
        @Nullable Double helmetDropChance,
        @NotNull ItemStackConfig chestplate,
        @Nullable Double chestplateDropChance,
        @NotNull ItemStackConfig leggings,
        @Nullable Double leggingsDropChance,
        @NotNull ItemStackConfig boots,
        @Nullable Double bootsDropChance,
        @NotNull ItemStackConfig mainHand,
        @Nullable Double mainHandDropChance,
        @NotNull ItemStackConfig offHand,
        @Nullable Double offHandDropChance) {}
