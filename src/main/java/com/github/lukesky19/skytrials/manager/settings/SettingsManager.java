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
package com.github.lukesky19.skytrials.manager.settings;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.settings.Settings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager extends SimpleConfigManager<Settings> {
    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public SettingsManager(@NonNull SkyTrials skyTrials) {
        super(skyTrials, Path.of(skyTrials.getDataFolder() + File.separator + "settings.yml"), Settings.class);
    }

    @Override
    public void loadConfiguration() {
        configuration = null;
        if(configurationPath == null) return;

        saveDefaultConfiguration();

        YamlConfigurationLoader loader = createLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            int version = getVersion(root);

            Settings settings;
            switch(version) {
                case 3 -> {
                    settings = root.get(Settings.class);
                    if(settings == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 3 of plugin's settings configuration."));
                        return;
                    }
                }

                case 2 -> {
                    settings = root.get(Settings.class);
                    if(settings == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 2 of plugin's settings configuration."));
                        return;
                    }

                    settings = new Settings(
                            3,
                            settings.locale());

                    saveConfiguration(settings);
                }

                case 1 -> {
                    logger.warn(AdventureUtility.plain("Unable to load the plugin's settings configuration as the settings must be manually migrated for version 1."));
                    return;
                }

                default -> {
                    logger.warn(AdventureUtility.plain("Unable to load the plugin's settings configuration as the settings has an unrecognized version. Version: " + version));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(settings)) {
                logger.warn(AdventureUtility.plain("Configuration validation failed for plugin settings configuration"));
                return;
            }

            this.configuration = settings;
        } catch (ConfigurateException configurateException) {
            logger.warn(AdventureUtility.plain("Failed to load plugin setting's configuration. Error: " + configurateException.getMessage()));
        }
    }

    @Override
    public void saveDefaultConfiguration() {
        if(configurationPath == null) return;

        if(!configurationPath.toFile().exists()) {
            plugin.saveResource("settings.yml", false);
        }
    }

    /**
     * Currently no migration is needed so the settings passed is returned.
     * @param settings The {@link Settings} to migrate.
     * @return The passed {@link Settings} as no migration is needed.
     */
    @Override
    public @NonNull Settings migrateConfiguration(@NonNull Settings settings) {
        return settings;
    }

    @Override
    public boolean validateConfiguration(@Nullable Settings settings) {
        if(settings == null) return false;

        if(settings.version() != 3) {
            logger.warn(AdventureUtility.plain("Your setting's config version is outdated. Either re-generate your settings file or migrate it to version 3."));
            configuration = null;
            return false;
        }

        if(settings.locale() == null) {
            logger.warn(AdventureUtility.plain("The locale to use is invalid."));
            configuration = null;
            return false;
        }

        return true;
    }

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        ConfigurationNode legacyVersionNode = root.node("config-version");
        String legacyVersion = legacyVersionNode.virtual() ? null : legacyVersionNode.getString();
        if(legacyVersion != null) {
            try {
                switch (legacyVersion) {
                    case "2.0.0.0" -> {
                        versionNode.set(2);
                        version = 2;
                    }

                    case "1.0.0" -> {
                        versionNode.set(1);
                        version = 1;
                    }

                    default -> {
                        versionNode.set(0);
                        version = 0;
                    }
                }
            } catch (SerializationException e) {
                logger.warn(AdventureUtility.plain("Failed to convert String-based version to numeric version"));
                version = 0;
            }
        }

        return version;
    }
}