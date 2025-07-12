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
import org.bukkit.attribute.Attribute;

import javax.annotation.Nullable;

/**
 * This record contains the configuration for attributes that are applied to entities.
 * @param name The name of the {@link Attribute}.
 * @param baseValue The base value of the attribute.
 * @param additionalValuePerPlayer The additional value per player.
 */
@ConfigSerializable
public record AttributeConfig(
        @Nullable String name,
        @Nullable Double baseValue,
        @Nullable Double additionalValuePerPlayer) {}
