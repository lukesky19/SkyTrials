package com.github.lukesky19.skytrials.database;

import com.github.lukesky19.skylib.api.database.connection.AbstractConnectionManager;
import com.github.lukesky19.skylib.libs.hikaricp.HikariConfig;
import com.github.lukesky19.skylib.libs.hikaricp.HikariDataSource;
import com.github.lukesky19.skytrials.SkyTrials;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * This class manages connections to the database.
 */
public class ConnectionManager extends AbstractConnectionManager {
    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public ConnectionManager(@NotNull SkyTrials skyTrials) {
        super(skyTrials);
    }

    /**
     * Creates the required {@link HikariConfig} to access the database and returns the {@link HikariDataSource}.
     * @param plugin The {@link Plugin} implementing and making use of this class.
     * @return A {@link HikariDataSource} object.
     */
    @Override
    protected @NotNull HikariDataSource createHikariDataSource(@NotNull Plugin plugin) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" +  plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db");
        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }
}
