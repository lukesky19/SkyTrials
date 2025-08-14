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

import com.github.lukesky19.skylib.api.itemstack.ItemStackConfig;
import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * This record contain the configuration for a vault block.
 * @param location The {@link LocationConfig} for where the vault should be placed.
 * @param ominous Is the vault ominous or not?
 * @param activationRange The activation range for the vault.
 * @param deactivationRange The deactivation range for the vault.
 * @param keyItem The {@link ItemStackConfig} for the item stack that unlocks the vault.
 * @param lootTable The loot table key for the loot that is dropped by the vault.
 */
@ConfigSerializable
public record VaultConfig(
        @NotNull LocationConfig location,
        @Nullable Boolean ominous,
        @Nullable Double activationRange,
        @Nullable Double deactivationRange,
        @NotNull ItemStackConfig keyItem,
        @Nullable String lootTable) {}
