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
package com.github.lukesky19.skytrials.manager.player;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.data.player.PlayerData;
import com.github.lukesky19.skytrials.database.DatabaseManager;
import com.github.lukesky19.skytrials.database.table.PlayerCooldownsTable;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class manages player cooldowns for trials.
 */
public class CooldownManager {
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull DatabaseManager databaseManager;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public CooldownManager(@NotNull SkyTrials skyTrials, @NotNull LocaleManager localeManager, @NotNull PlayerDataManager playerDataManager, @NotNull DatabaseManager databaseManager) {
        this.skyTrials = skyTrials;
        this.localeManager = localeManager;
        this.playerDataManager = playerDataManager;
        this.databaseManager = databaseManager;
    }

    /**
     * Adds the cooldown time in seconds for the player id and the trial id provided.
     * @param playerId The {@link UUID} of the player.
     * @param trialId The id of the trial.
     * @param cooldownTimeSeconds The cooldown time in seconds to add.
     */
    public void addCooldown(@NotNull UUID playerId, @NotNull String trialId, long cooldownTimeSeconds) {
        PlayerData playerData = playerDataManager.getPlayerData(playerId);

        Map<String, Long> cooldownMap = playerData.getCooldownsMap();

        cooldownMap.put(trialId, cooldownTimeSeconds);

        playerDataManager.savePlayerData(playerId);
    }

    /**
     * Removes the cooldown for the player id and trial id provided.
     * @param playerId The {@link UUID} of hte player.
     * @param trialId The id of the trial.
     * @return true if a cooldown was removed, otherwise false.
     */
    public boolean removeCooldown(@NotNull UUID playerId, @NotNull String trialId) {
        PlayerData playerData = playerDataManager.getPlayerData(playerId);

        Map<String, Long> cooldownMap = playerData.getCooldownsMap();

        if(cooldownMap.containsKey(trialId)) {
            cooldownMap.remove(trialId);

            playerDataManager.savePlayerData(playerId);

            return true;
        }

        return false;
    }

    /**
     * Get the cooldown time in seconds for the player id and trial id provided.
     * @param playerId The {@link UUID} of the player.
     * @param trialId The id of the trial.
     * @return The cooldown time in seconds as a {@link Long} or null.
     */
    public @Nullable Long getTrialCooldown(@NotNull UUID playerId, @NotNull String trialId) {
        PlayerData playerData = playerDataManager.getPlayerData(playerId);

        return playerData.getCooldownsMap().get(trialId);
    }

    /**
     * Decrement all cooldowns.
     */
    public void decrementCooldowns() {
        Locale locale = localeManager.getLocale();

        playerDataManager.getPlayerDataMap().forEach((uuid, playerData) -> {
            Map<String, Long> cooldownsMap = playerData.getCooldownsMap();

            Iterator<Map.Entry<String, Long>> iterator = cooldownsMap.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, Long> entry = iterator.next();
                long newCooldownTime = entry.getValue() - 1;

                if(newCooldownTime <= 0) {
                    iterator.remove();

                    PlayerCooldownsTable playerCooldownsTable = databaseManager.getPlayerCooldownsTable();
                    playerCooldownsTable.removeCooldown(uuid, entry.getKey());

                    Player player = skyTrials.getServer().getPlayer(uuid);
                    if(player != null && player.isOnline() && player.isConnected()) {
                        List<TagResolver.Single> placeholders = List.of(Placeholder.parsed("trial_id", entry.getKey()));
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.cooldownEnded(), placeholders));
                    }
                } else {
                    cooldownsMap.put(entry.getKey(), newCooldownTime);
                }
            }
        });
    }
}
