package com.github.lukesky19.skytrials.database;

import com.github.lukesky19.skylib.api.database.queue.MultiThreadQueueManager;
import org.jspecify.annotations.NonNull;

/**
 * This class manages queuing reads and writes to the database.
 */
public class QueueManager extends MultiThreadQueueManager {
    /**
     * Constructor
     * @param connectionManager A {@link ConnectionManager} instance.
     */
    public QueueManager(@NonNull ConnectionManager connectionManager) {
        super(connectionManager);
    }
}
