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
import com.github.lukesky19.skylib.paper.api.itemstack.ItemStackConfig;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
        @NonNull ItemStackConfig helmet,
        @Nullable Double helmetDropChance,
        @NonNull ItemStackConfig chestplate,
        @Nullable Double chestplateDropChance,
        @NonNull ItemStackConfig leggings,
        @Nullable Double leggingsDropChance,
        @NonNull ItemStackConfig boots,
        @Nullable Double bootsDropChance,
        @NonNull ItemStackConfig mainHand,
        @Nullable Double mainHandDropChance,
        @NonNull ItemStackConfig offHand,
        @Nullable Double offHandDropChance) {}
