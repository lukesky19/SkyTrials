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
package com.github.lukesky19.skytrials.data.config.misc;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import javax.annotation.Nullable;

/**
 * This record contains the loot table configuration to apply to trial spawners and vault blocks.
 * @param name The name of the loot table.
 * @param weight The weight for the loot table.
 */
@ConfigSerializable
public record LootTableConfig(@Nullable String name, @Nullable Integer weight) {}
