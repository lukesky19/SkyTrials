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
package com.github.lukesky19.skytrials.manager.trial;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.platform.PlatformUtils;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.NodeStyle;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.trial.ChamberTrialConfig;
import com.github.lukesky19.skytrials.data.config.trial.LevelTrialConfig;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class manages trial configurations.
 */
public class TrialConfigManager {
    private final @NonNull SkyTrials skyTrials;

    private final @NonNull List<ChamberTrialConfig> chamberTrialConfigList = new ArrayList<>();
    private final @NonNull List<LevelTrialConfig> levelTrialConfigList = new ArrayList<>();

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public TrialConfigManager(@NonNull SkyTrials skyTrials)  {
        this.skyTrials = skyTrials;
    }

    /**
     * Get a {@link List} of {@link ChamberTrialConfig}s.
     * @return A {@link List} of {@link ChamberTrialConfig}s.
     */
    public @NonNull List<ChamberTrialConfig> getChamberTrialConfigList() {
        return chamberTrialConfigList;
    }

    /**
     * Get a {@link List} of {@link LevelTrialConfig}s.
     * @return A {@link List} of {@link LevelTrialConfig}s.
     */
    public @NonNull List<LevelTrialConfig> getLevelTrialConfigList() {
        return levelTrialConfigList;
    }

    /**
     * Reloads the plugin's trial configurations.
     */
    public void reload() {
        ComponentLogger logger = skyTrials.getComponentLogger();
        Path chamberTrialsPath = Path.of(skyTrials.getDataPath() + File.separator + "trials" + File.separator + "chamber");
        Path levelTrialsPath = Path.of(skyTrials.getDataPath() + File.separator + "trials" + File.separator + "level");

        // Create the necessary directories
        if(!Files.exists(chamberTrialsPath)) {
            try {
                Files.createDirectories(chamberTrialsPath);
            } catch (IOException e) {
                logger.error(AdventureUtility.plain("An error occurred while creating the chamber trial config directory: " + e.getMessage()));
                return;
            }
        }

        if(!Files.exists(levelTrialsPath)) {
            try {
                Files.createDirectories(levelTrialsPath);
            } catch (IOException e) {
                logger.error(AdventureUtility.deserialize("An error occurred while creating the level trial config directory: " + e.getMessage()));
                return;
            }
        }

        // Clear any loaded configurations
        chamberTrialConfigList.clear();
        levelTrialConfigList.clear();

        // Load any chamber trial configs
        try(Stream<Path> paths = Files.walk(chamberTrialsPath)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        YamlConfigurationLoader loader = createLoader(path);
                        try {
                            ChamberTrialConfig chamberTrialConfig = loader.load().get(ChamberTrialConfig.class);
                            if(chamberTrialConfig != null) {
                                chamberTrialConfigList.add(chamberTrialConfig);
                            } else {
                                logger.warn(AdventureUtility.plain("Failed to load trial config for " + path.toFile().getName()));
                            }
                        } catch(ConfigurateException e) {
                            logger.warn(AdventureUtility.plain("Failed to load trial config for " + path.toFile().getName() + ". Error: " + e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            logger.error(AdventureUtility.plain("An error occurred while processing chamber trial config files: " + e.getMessage()));
        }

        // Load any level trial configs
        try(Stream<Path> paths = Files.walk(levelTrialsPath)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        YamlConfigurationLoader loader = createLoader(path);
                        try {
                            LevelTrialConfig levelTrialConfig = loader.load().get(LevelTrialConfig.class);
                            if(levelTrialConfig != null) {
                                levelTrialConfigList.add(levelTrialConfig);
                            } else {
                                logger.warn(AdventureUtility.plain("Failed to load trial config for " + path.toFile().getName()));
                            }
                        } catch(ConfigurateException e) {
                            logger.warn(AdventureUtility.plain("Failed to load trial config for " + path.toFile().getName() + ". Error: " + e.getMessage()));
                        }
                    });
        } catch (IOException e) {
            logger.error(AdventureUtility.plain("An error occurred while processing level trial config files: " + e.getMessage()));
        }
    }

    /**
     * Create the {@link YamlConfigurationLoader} for the path provided.
     * @apiNote {@link PlatformUtils#getSerializers()} are included by default.
     * @param path The {@link Path}.
     * @return The {@link YamlConfigurationLoader}.
     */
    protected @NonNull YamlConfigurationLoader createLoader(@NonNull Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(4)
                .defaultOptions(configurationOptions ->
                        configurationOptions.serializers(builder ->
                                builder.registerAll(PlatformUtils.getSerializers())))
                .build();
    }
}