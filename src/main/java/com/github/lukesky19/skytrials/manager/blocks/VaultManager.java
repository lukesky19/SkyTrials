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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.block.VaultConfig;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Vault;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Manages the placing of vaults inside trials.
 */
public class VaultManager {
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull ComponentLogger logger;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public VaultManager(@NotNull SkyTrials skyTrials) {
        this.skyTrials = skyTrials;
        this.logger = skyTrials.getComponentLogger();
    }

    /**
     * Spawns the vault blocks for the {@link List} of {@link VaultConfig} provided.
     * @param vaultConfigList A {@link List} of {@link VaultConfig}s.
     */
    public void placeVaultBlocks(@NotNull List<VaultConfig> vaultConfigList) {
        Registry<@NotNull LootTables> lootTablesRegistry = Registry.LOOT_TABLES;

        for(VaultConfig vaultConfig : vaultConfigList) {
            if(vaultConfig.location().world() == null) {
                logger.warn(AdventureUtil.serialize("The world name for a vault config is invalid."));
                continue;
            }

            World vaultWorld = skyTrials.getServer().getWorld(vaultConfig.location().world());
            if(vaultWorld == null) {
                logger.warn(AdventureUtil.serialize("A world for a vault config is invalid."));
                continue;
            }

            LocationConfig locationConfig = vaultConfig.location();
            if(locationConfig.x() == null) {
                logger.warn(AdventureUtil.serialize("The x coordinate for a vault config is invalid."));
                continue;
            }

            if(locationConfig.y() == null) {
                logger.warn(AdventureUtil.serialize("The y coordinate for a vault config is invalid."));
                continue;
            }

            if(locationConfig.z() == null) {
                logger.warn(AdventureUtil.serialize("The z coordinate for a vault config is invalid."));
                continue;
            }

            // Create the Location for the vault
            Location vaultLocation = new Location(vaultWorld, vaultConfig.location().x(), vaultConfig.location().y(), vaultConfig.location().z());

            // Get the Block to place the trial spawner at.
            Block block = vaultWorld.getBlockAt(vaultLocation);
            // Set the Block to a Vault
            block.setType(Material.VAULT);
            // Get the BlockState for the Block.
            BlockState state = block.getState(false);

            if(state instanceof Vault vaultState) {
                if(vaultState.getBlockData() instanceof org.bukkit.block.data.type.Vault vaultBlockData) {
                    if(vaultConfig.ominous() != null) {
                        vaultBlockData.setOminous(vaultConfig.ominous());
                        state.update();
                    }
                }

                if(vaultConfig.lootTable() != null) {
                    NamespacedKey key = NamespacedKey.fromString(vaultConfig.lootTable());
                    if(key != null) {
                        LootTables lootTables = lootTablesRegistry.get(key);
                        if(lootTables != null) {
                            LootTable lootTable = lootTables.getLootTable();
                            vaultState.setLootTable(lootTable);
                        }
                    }
                }

                if(vaultConfig.activationRange() != null) {
                    vaultState.setActivationRange(vaultConfig.activationRange());
                }

                if(vaultConfig.deactivationRange() != null) {
                    vaultState.setDeactivationRange(vaultConfig.deactivationRange());
                }

                // Extra null check to avoid the ItemStackBuilder spamming errors for non-configured keys
                if(vaultConfig.keyItem().itemType() != null) {
                    Optional<ItemStack> optionalKeyItem = new ItemStackBuilder(logger).fromItemStackConfig(vaultConfig.keyItem(), null, null, List.of()).buildItemStack();
                    optionalKeyItem.ifPresent(vaultState::setKeyItem);
                }

                state.update();
            }
        }
    }

    /**
     * Removes the vault blocks that were placed for the {@link List} of {@link VaultConfig} provided.
     * @param vaultConfigList A {@link List} of {@link VaultConfig}s.
     */
    public void removeVaultBlocks(@NotNull List<VaultConfig> vaultConfigList) {
        for(VaultConfig vaultConfig : vaultConfigList) {
            if(vaultConfig.location().world() == null) {
                logger.warn(AdventureUtil.serialize("The world name for a vault config is invalid."));
                continue;
            }

            World vaultWorld = skyTrials.getServer().getWorld(vaultConfig.location().world());
            if(vaultWorld == null) {
                logger.warn(AdventureUtil.serialize("A world for a vault config is invalid."));
                continue;
            }

            LocationConfig locationConfig = vaultConfig.location();
            if(locationConfig.x() == null) {
                logger.warn(AdventureUtil.serialize("The x coordinate for a vault config is invalid."));
                continue;
            }

            if(locationConfig.y() == null) {
                logger.warn(AdventureUtil.serialize("The y coordinate for a vault config is invalid."));
                continue;
            }

            if(locationConfig.z() == null) {
                logger.warn(AdventureUtil.serialize("The z coordinate for a vault config is invalid."));
                continue;
            }

            // Create the Location for the vault
            Location vaultLocation = new Location(vaultWorld, vaultConfig.location().x(), vaultConfig.location().y(), vaultConfig.location().z());

            // Set the block to air
            Block block = vaultWorld.getBlockAt(vaultLocation);
            block.setType(Material.AIR);
        }
    }
}
