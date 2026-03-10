package com.github.lukesky19.skytrials.database;

import com.github.lukesky19.skylib.api.database.AbstractDatabaseManager;
import com.github.lukesky19.skytrials.database.table.PlayerCooldownsTable;
import org.jspecify.annotations.NonNull;

/**
 * This class manages access to the database table classes.
 */
public class DatabaseManager extends AbstractDatabaseManager {
    private final @NonNull PlayerCooldownsTable playerCooldownsTable;

    /**
     * Constructor
     * @param connectionManager Α {@link ConnectionManager} instance.
     * @param queueManager A {@link QueueManager} instance.
     */
    public DatabaseManager(@NonNull ConnectionManager connectionManager, @NonNull QueueManager queueManager) {
        super(connectionManager, queueManager);

        playerCooldownsTable = new PlayerCooldownsTable(queueManager);
        playerCooldownsTable.createTable();
    }

    /**
     * Get the {@link PlayerCooldownsTable}.
     * @return The {@link PlayerCooldownsTable}.
     */
    public @NonNull PlayerCooldownsTable getPlayerCooldownsTable() {
        return playerCooldownsTable;
    }
}
