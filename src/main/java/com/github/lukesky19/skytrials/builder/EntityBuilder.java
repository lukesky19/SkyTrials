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
package com.github.lukesky19.skytrials.builder;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.itemstack.ItemStackBuilder;
import com.github.lukesky19.skylib.api.registry.RegistryUtil;
import com.github.lukesky19.skytrials.data.config.entity.AttributeConfig;
import com.github.lukesky19.skytrials.data.config.entity.EffectConfig;
import com.github.lukesky19.skytrials.data.config.entity.EntityConfig;
import com.github.lukesky19.skytrials.data.config.entity.EquipmentConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This class is used to create {@link LivingEntity} and {@link EntitySnapshot}.
 */
public class EntityBuilder {
    private final @NotNull ComponentLogger logger;
    private final @NotNull EntityType entityType;
    private final @NotNull World world;
    private final @NotNull Location location;
    private final int playerCount;

    private final @NotNull EntityConfig.Options options;
    private final @NotNull EquipmentConfig equipmentConfig;
    private final @NotNull List<EffectConfig> effectConfigList;
    private final @NotNull List<AttributeConfig> attributeConfigList;
    private final @Nullable String lootTableName;

    /**
     * Constructor
     * @param logger A {@link ComponentLogger}.
     * @param entityType An {@link EntityType}.
     * @param world A {@link World} to spawn the initial entity in.
     * @param location A {@link Location} to spawn the initial entity at.
     * @param playerCount The number of players to scale attributes to.
     * @param options The {@link EntityConfig.Options} to apply to the entity.
     * @param equipmentConfig The {@link EquipmentConfig} to apply to the entity.
     * @param effectConfigList A {@link List} of {@link EffectConfig}s to apply to the entity.
     * @param attributeConfigList A {@link List} of {@link AttributeConfig}s to apply to the entity.
     * @param lootTableName The {@link NamespacedKey} as a {@link String} for the loot table to apply to the entity.
     */
    public EntityBuilder(
            @NotNull ComponentLogger logger,
            @NotNull EntityType entityType,
            @NotNull World world,
            @NotNull Location location,
            int playerCount,
            @NotNull EntityConfig.Options options,
            @NotNull EquipmentConfig equipmentConfig,
            @NotNull List<EffectConfig> effectConfigList,
            @NotNull List<AttributeConfig> attributeConfigList,
            @Nullable String lootTableName) {
        this.logger = logger;
        this.entityType = entityType;
        this.world = world;
        this.location = location;
        this.playerCount = playerCount;
        this.options = options;
        this.equipmentConfig = equipmentConfig;
        this.effectConfigList = effectConfigList;
        this.attributeConfigList = attributeConfigList;
        this.lootTableName = lootTableName;
    }

    /**
     * Create and return the {@link LivingEntity}
     * @return A {@link LivingEntity}.
     */
    public @NotNull LivingEntity createEntity() {
        LivingEntity entity = (LivingEntity) world.spawnEntity(location, entityType);

        Optional<ItemStack> optionalHelmet = new ItemStackBuilder(logger).fromItemStackConfig(equipmentConfig.helmet(), null, null, List.of()).buildItemStack();
        Optional<ItemStack> optionalChestplate = new ItemStackBuilder(logger).fromItemStackConfig(equipmentConfig.chestplate(), null, null, List.of()).buildItemStack();
        Optional<ItemStack> optionalLeggings = new ItemStackBuilder(logger).fromItemStackConfig(equipmentConfig.leggings(), null, null, List.of()).buildItemStack();
        Optional<ItemStack> optionalBoots = new ItemStackBuilder(logger).fromItemStackConfig(equipmentConfig.boots(), null, null, List.of()).buildItemStack();
        Optional<ItemStack> optionalMainHand = new ItemStackBuilder(logger).fromItemStackConfig(equipmentConfig.mainHand(), null, null, List.of()).buildItemStack();
        Optional<ItemStack> optionalOffHand = new ItemStackBuilder(logger).fromItemStackConfig(equipmentConfig.offHand(), null, null, List.of()).buildItemStack();

        EntityEquipment entityEquipment = entity.getEquipment();
        if(entityEquipment != null) {
            optionalHelmet.ifPresent(entityEquipment::setHelmet);
            optionalChestplate.ifPresent(entityEquipment::setChestplate);
            optionalLeggings.ifPresent(entityEquipment::setLeggings);
            optionalBoots.ifPresent(entityEquipment::setBoots);
            optionalMainHand.ifPresent(entityEquipment::setItemInMainHand);
            optionalOffHand.ifPresent(entityEquipment::setItemInOffHand);

            if(equipmentConfig.helmetDropChance() != null) {
                entityEquipment.setHelmetDropChance(calculateDropChance(equipmentConfig.helmetDropChance()));
            }
            if(equipmentConfig.chestplateDropChance() != null) {
                entityEquipment.setChestplateDropChance(calculateDropChance(equipmentConfig.chestplateDropChance()));
            }
            if(equipmentConfig.leggingsDropChance() != null) {
                entityEquipment.setLeggingsDropChance(calculateDropChance(equipmentConfig.leggingsDropChance()));
            }
            if(equipmentConfig.bootsDropChance() != null) {
                entityEquipment.setBootsDropChance(calculateDropChance(equipmentConfig.bootsDropChance()));
            }
            if(equipmentConfig.mainHandDropChance() != null) {
                entityEquipment.setItemInMainHandDropChance(calculateDropChance(equipmentConfig.mainHandDropChance()));
            }
            if(equipmentConfig.offHandDropChance() != null) {
                entityEquipment.setItemInOffHandDropChance(calculateDropChance(equipmentConfig.offHandDropChance()));
            }
        }

        // Apply potion effects
        effectConfigList.stream()
                .filter(effectConfig ->
                        effectConfig.effectName() != null
                                && effectConfig.durationInSeconds() != null
                                && effectConfig.durationInSeconds() > 0
                                && effectConfig.amplifier() != null
                                && effectConfig.amplifier() >= 0)
                .forEach(effectConfig -> {
                    Optional<PotionEffectType> optionalPotionEffectType = RegistryUtil.getPotionEffectType(logger, effectConfig.effectName());
                    if(optionalPotionEffectType.isPresent()) {
                        PotionEffect potionEffect = optionalPotionEffectType.get().createEffect(effectConfig.durationInSeconds(), effectConfig.amplifier());
                        entity.addPotionEffect(potionEffect);
                    }
                });

        // Apply attributes
        for(AttributeConfig attributeConfig : attributeConfigList) {
            if(attributeConfig.name() == null) {
                logger.warn(AdventureUtil.serialize("Unable to apply attribute due to an invalid attribute name."));
                continue;
            }

            if(attributeConfig.baseValue() == null) {
                logger.warn(AdventureUtil.serialize("Unable to apply attribute due to an invalid base value."));
                continue;
            }

            if(attributeConfig.additionalValuePerPlayer() == null) {
                logger.warn(AdventureUtil.serialize("Unable to apply attribute due to an invalid additional value per player."));
                continue;
            }

            Optional<Attribute> optionalAttribute = RegistryUtil.getAttribute(logger, attributeConfig.name());
            if(optionalAttribute.isEmpty()) {
                logger.warn(AdventureUtil.serialize("Unable to apply attribute due to an invalid attribute for " + attributeConfig.name()));
                continue;
            }
            Attribute attribute = optionalAttribute.get();
            AttributeInstance attributeInstance = entity.getAttribute(attribute);

            // Either modify the existing attribute or register a new one
            if(attributeInstance != null) {
                attributeInstance.setBaseValue(attributeConfig.baseValue());

                double modifier = attributeConfig.additionalValuePerPlayer() * playerCount;
                AttributeModifier attributeModifier = new AttributeModifier(attribute.getKey(), modifier, AttributeModifier.Operation.ADD_NUMBER);
                attributeInstance.addModifier(attributeModifier);
            } else {
                entity.registerAttribute(attribute);
                AttributeInstance newInstance = entity.getAttribute(attribute);

                if(newInstance != null) {
                    newInstance.setBaseValue(attributeConfig.baseValue());

                    double modifier = attributeConfig.additionalValuePerPlayer() * playerCount;
                    AttributeModifier attributeModifier = new AttributeModifier(attribute.getKey(), modifier, AttributeModifier.Operation.ADD_NUMBER);
                    newInstance.addModifier(attributeModifier);
                }
            }
        }

        // Apply loot table
        if(lootTableName != null) {
            Registry<@NotNull LootTables> lootTablesRegistry = Registry.LOOT_TABLES;

            NamespacedKey key = NamespacedKey.fromString(lootTableName);
            if(key != null) {
                LootTables lootTables = lootTablesRegistry.get(key);
                if(lootTables != null) {
                    LootTable lootTable = lootTables.getLootTable();
                    Mob mob = (Mob) entity;
                    mob.setLootTable(lootTable);
                }
            }
        }

        if(options.isBaby() != null) {
            if(entity instanceof Ageable ageable) {
                ageable.setBaby();
            }
        }

        if(options.isCharged() != null) {
            if(entity instanceof Creeper creeper) {
                creeper.setPowered(options.isCharged());
            }
        }

        if(options.canPickupItems() != null) {
            entity.setCanPickupItems(options.canPickupItems());
        }

        if(options.persistent() != null) {
            entity.setPersistent(options.persistent());
        }

        if(options.removeWhenFarAway() != null) {
            entity.setRemoveWhenFarAway(options.removeWhenFarAway());
        }

        if(options.glowing() != null) {
            entity.setGlowing(options.glowing());
        }

        return entity;
    }

    /**
     * Runs {@link #createEntity()}, creates the {@link EntitySnapshot}, removes the entity, and returns the {@link EntitySnapshot}.
     * @return The {@link EntitySnapshot} of the created entity. May be null.
     */
    public @Nullable EntitySnapshot createEntitySnapshot() {
        LivingEntity entity = createEntity();

        EntitySnapshot entitySnapshot = entity.createSnapshot();

        entity.remove();

        return entitySnapshot;
    }

    private float calculateDropChance(double dropChance) {
        if (dropChance < 0) {
            dropChance = 0;
        } else if (dropChance > 100) {
            dropChance = 100;
        }

        return (float) (dropChance / 100);
    }
}
