package com.github.lukesky19.skytrials.manager.player;

import com.github.lukesky19.skytrials.data.player.PlayerData;
import com.github.lukesky19.skytrials.database.DatabaseManager;
import com.github.lukesky19.skytrials.database.table.PlayerCooldownsTable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class manages the storage, loading, and saving of {@link PlayerData}.
 */
public class PlayerDataManager {
    private final @NotNull DatabaseManager databaseManager;
    private final @NotNull Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    /**
     * Default Constructor.
     * You should use {@link #PlayerDataManager(DatabaseManager)} instead.
     * @deprecated You should use {@link #PlayerDataManager(DatabaseManager)} instead.
     */
    @Deprecated
    public PlayerDataManager() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Constructor
     * @param databaseManager A {@link DatabaseManager} instance.
     */
    public PlayerDataManager(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Get the {@link Map} mapping {@link UUID} to {@link PlayerData}.
     * @return A {@link Map} mapping {@link UUID} to {@link PlayerData}.
     */
    public @NotNull Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    /**
     * Get the {@link PlayerData} for the {@link UUID} provided. If no player data exists for the player, one will be created.
     * @param uuid The {@link UUID} of the player.
     * @return The {@link PlayerData} for the player.
     */
    public @NotNull PlayerData getPlayerData(@NotNull UUID uuid) {
        if(playerDataMap.containsKey(uuid)) return playerDataMap.get(uuid);

        PlayerData playerData = new PlayerData();
        playerDataMap.put(uuid, playerData);

        return playerData;
    }

    /**
     * Loads player data for all data stored in the database.
     */
    public void loadPlayerData() {
        PlayerCooldownsTable playerCooldownsTable = databaseManager.getPlayerCooldownsTable();

        playerCooldownsTable.loadPlayerCooldowns().thenAccept(cooldownsMap ->
                cooldownsMap.forEach((uuid, playerCooldowns) -> {
                    Map<String, Long> updatedPlayerCooldowns = playerCooldowns.entrySet().stream()
                            .filter(entry -> entry.getValue() > 0)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    PlayerData playerData = new PlayerData(updatedPlayerCooldowns);
                    playerDataMap.put(uuid, playerData);
                }));
    }

    /**
     * Saves player data for the {@link UUID} provided.
     * @param uuid The {@link UUID} of the player.
     */
    public void savePlayerData(@NotNull UUID uuid) {
        PlayerData playerData = playerDataMap.get(uuid);
        if(playerData == null) return;

        savePlayerData(uuid, playerData);
    }

    /**
     * Saves player data for the {@link UUID} and {@link PlayerData} provided.
     * @param uuid The {@link UUID} of the player.
     * @param playerData The {@link PlayerData} to save.
     * @return A {@link CompletableFuture} that completes when all data is saved.
     */
    public @NotNull CompletableFuture<Void> savePlayerData(@NotNull UUID uuid, @NotNull PlayerData playerData) {
        PlayerCooldownsTable playerCooldownsTable = databaseManager.getPlayerCooldownsTable();

        return playerCooldownsTable.saveCooldowns(uuid, playerData.getCooldownsMap()).thenAccept(result -> {});
    }

    /**
     * Saves all player data to the database.
     * @return A {@link CompletableFuture} that completes when all data is saved.
     */
    public @NotNull CompletableFuture<Void> savePlayerData() {
        List<CompletableFuture<Void>> completableFutureList = playerDataMap.entrySet().stream().map(entry -> savePlayerData(entry.getKey(), entry.getValue())).toList();

        return CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0]));
    }
}
