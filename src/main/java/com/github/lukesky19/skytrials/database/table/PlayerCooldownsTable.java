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
package com.github.lukesky19.skytrials.database.table;

import com.github.lukesky19.skylib.api.database.parameter.Parameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.LongParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.StringParameter;
import com.github.lukesky19.skylib.api.database.parameter.impl.UUIDParameter;
import com.github.lukesky19.skytrials.database.QueueManager;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This class is used to create and interface with the cooldowns table in the database.
 */
public class PlayerCooldownsTable {
    private final @NotNull QueueManager queueManager;
    private final @NotNull String tableName = "skytrials_cooldowns";

    /**
     * Default Constructor.
     * You should use {@link #PlayerCooldownsTable(QueueManager)} instead.
     * @deprecated You should use {@link #PlayerCooldownsTable(QueueManager)} instead.
     */
    @Deprecated
    public PlayerCooldownsTable() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param queueManager A {@link QueueManager} instance.
     */
    public PlayerCooldownsTable(@NotNull QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    /**
     * Creates the table in the database if it doesn't exist and any indexes that don't exist.
     */
    public void createTable() {
        String tableCreationSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "trial_id TEXT NOT NULL, " + // Unique
                "player_id LONG NOT NULL DEFAULT 0, " + // Unique
                "cooldown LONG NOT NULL DEFAULT 0, " +
                "last_updated LONG NOT NULL DEFAULT 0, " +
                "UNIQUE (trial_id, player_id))";
        String mineIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_trial_ids ON " + tableName + "(mine_id)";
        String playerIdsIndexSql = "CREATE INDEX IF NOT EXISTS idx_" + tableName + "_player_ids ON " + tableName + "(player_id)";

        queueManager.queueBulkWriteTransaction(List.of(tableCreationSql, mineIdsIndexSql, playerIdsIndexSql)).thenAccept(result -> {});
    }

    /**
     * Get a {@link CompletableFuture} containing a {@link Map} mapping player {@link UUID}s to a {@link Map} mapping trials ids to cooldowns as a {@link Long}.
     * @return A {@link CompletableFuture} containing a {@link Map} mapping player {@link UUID}s to a {@link Map} mapping trials ids to cooldowns as a {@link Long}.
     */
    public @NotNull CompletableFuture<@NotNull Map<UUID, Map<String, Long>>> loadPlayerCooldowns() {
        String selectSql = "SELECT trial_id, player_id, cooldown FROM " + tableName;

        return queueManager.queueReadTransaction(selectSql, resultSet -> {
            Map<UUID, Map<String, Long>> cooldownsMap = new HashMap<>();

            try {
                while(resultSet.next()) {
                    UUID playerId = UUID.fromString(resultSet.getString("player_id"));
                    String trialId = resultSet.getString("trial_id");
                    long cooldownTime = resultSet.getLong("cooldown");

                    Map<String, Long> playerCooldowns = cooldownsMap.getOrDefault(playerId, new HashMap<>());
                    playerCooldowns.put(trialId, cooldownTime);

                    cooldownsMap.put(playerId, playerCooldowns);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return cooldownsMap;
        });
    }

    /**
     * Saves all cooldowns for the player to the database.
     * @param playerId The {@link UUID} of the player.
     * @param data A {@link Map} mapping trial ids to cooldown times as a {@link Long}.
     * @return A {@link CompletableFuture} containing a {@link List} of {@link Boolean} with the results.
     * The list will contain false if an operation failed.
     */
    public @NotNull CompletableFuture<List<Boolean>> saveCooldowns(@NotNull UUID playerId, @NotNull Map<String, Long> data) {
        List<List<Parameter<?>>> listOfParameterLists = new ArrayList<>();
        String insertOrUpdateSql = "INSERT INTO " + tableName + " (trial_id, player_id, cooldown, last_updated) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (trial_id, player_id) DO UPDATE SET " +
                "cooldown = ?, last_updated = ? WHERE last_updated < ?";

        data.forEach((trialId, time) -> {
            StringParameter mineIdParameter = new StringParameter(trialId);
            UUIDParameter playerIdParameter = new UUIDParameter(playerId);
            LongParameter cooldownTimeParameter = new LongParameter(time);
            LongParameter lastUpdatedParameter = new LongParameter(System.currentTimeMillis());

            List<Parameter<?>> parameterList = List.of(mineIdParameter, playerIdParameter, cooldownTimeParameter, lastUpdatedParameter, cooldownTimeParameter, lastUpdatedParameter, lastUpdatedParameter);

            listOfParameterLists.add(parameterList);
        });

        return queueManager.queueBulkWriteTransaction(insertOrUpdateSql, listOfParameterLists).thenApply(list -> {
                    List<Boolean> results = new ArrayList<>();

                    list.forEach(rowsUpdated -> {
                        if(rowsUpdated > 0) {
                            results.add(true);
                        } else  {
                            results.add(false);
                        }
                    });

                    return results;
                }
        );
    }

    /**
     * Removes a cooldown stored in the database.
     * @param playerId The {@link UUID} of the player to save the cooldown for.
     * @param trialId The trial id the cooldown is for.
     */
    public void removeCooldown(@NotNull UUID playerId, @NotNull String trialId) {
        String deleteSql = "DELETE FROM " + tableName + " WHERE trial_id = ? AND player_id = ?";

        StringParameter trialIdParameter = new StringParameter(trialId);
        UUIDParameter playerIdParameter = new UUIDParameter(playerId);

        queueManager.queueWriteTransaction(deleteSql, List.of(trialIdParameter, playerIdParameter));
    }
}
