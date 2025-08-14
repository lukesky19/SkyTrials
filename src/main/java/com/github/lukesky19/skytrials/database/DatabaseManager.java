package com.github.lukesky19.skytrials.database;

import com.github.lukesky19.skylib.api.database.AbstractDatabaseManager;
import com.github.lukesky19.skytrials.database.table.PlayerCooldownsTable;
import org.jetbrains.annotations.NotNull;

/**
 * This class manages access to the database table classes.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NotNull PlayerCooldownsTable playerCooldownsTable;

    /**
     * Constructor
     * @param connectionManager Α {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NotNull ConnectionManager connectionManager, @NotNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        playerCooldownsTable = new PlayerCooldownsTable(queueManager);
        playerCooldownsTable.createTable();
    }

    /**
     * Get the {@link PlayerCooldownsTable}.
     * @return The {@link PlayerCooldownsTable}.
     */
    public @NotNull PlayerCooldownsTable getPlayerCooldownsTable() {
        return playerCooldownsTable;
    }
}
