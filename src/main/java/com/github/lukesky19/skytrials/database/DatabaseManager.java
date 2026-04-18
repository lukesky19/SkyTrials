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
package com.github.lukesky19.skytrials.database;

import com.github.lukesky19.skylib.common.api.database.AbstractDatabaseManager;
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
