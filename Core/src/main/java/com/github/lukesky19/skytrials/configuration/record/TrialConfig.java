/*
    SkyTrials is a mob arena plugin inspired by the Minecraft 1.21 Trial Chambers using Trial Spawners and Vault blocks.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skytrials.configuration.record;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.LinkedHashMap;

@ConfigSerializable
public record TrialConfig(
        String configVersion,
        String trialId,
        Integer timeLimitSeconds,
        Integer cooldownSeconds,
        Integer gracePeriodSeconds,
        RegionData regionData,
        LinkedHashMap<Integer, TrialSpawnerData> trialSpawners,
        LinkedHashMap<Integer, VaultData> vaults) {

    @ConfigSerializable
    public record RegionData(
            String world,
            String region,
            LocationData joinArea,
            LocationData startArea,
            LocationData exitArea) {}

    @ConfigSerializable
    public record TrialSpawnerData(
            LocationData locationData,
            SpawnerConfig normal,
            SpawnerConfig ominous) {}

    @ConfigSerializable
    public record VaultData(
            LocationData locationData,
            String type,
            String lootTable) {}

    @ConfigSerializable
    public record LocationData(
            String world,
            Integer x,
            Integer y,
            Integer z) {}

    @ConfigSerializable
    public record SpawnerConfig(
            Float simultaneousMobs,
            Float simultaneousMobsAddedPerPlayer,
            Integer playerRange,
            Integer spawnRange,
            //Integer spawnerCooldown,
            HashMap<Integer, SpawnPotential> spawnPotentials,
            HashMap<Integer, LootTableData> lootTables) {}

    @ConfigSerializable
    public record SpawnPotential(
            String entity,
            Integer weight,
            HashMap<Integer, EffectData> effects,
            EquipmentData equipment) {}

    @ConfigSerializable
    public record EffectData(
            String name,
            Integer level) {}

    @ConfigSerializable
    public record EquipmentData(
            ItemData helmet,
            ItemData chestplate,
            ItemData leggings,
            ItemData boots,
            ItemData mainHand,
            ItemData offHand) {}

    @ConfigSerializable
    public record ItemData(
            String name,
            HashMap<Integer, EnchantmentData> enchantments) {}

    @ConfigSerializable
    public record LootTableData(String name, Integer weight) {}

    @ConfigSerializable
    public record EnchantmentData(String name, Integer level) {}
}
