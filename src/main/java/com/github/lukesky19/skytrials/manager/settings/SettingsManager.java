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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.settings.Settings;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * This class manages the plugin's settings.
 */
public class SettingsManager {
    private final @NotNull SkyTrials skyTrials;
    private @Nullable Settings settings;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     */
    public SettingsManager(@NotNull SkyTrials skyTrials) {
        this.skyTrials = skyTrials;
    }

    /**
     * Get the plugin's {@link Settings}.
     * @return The plugin's {@link Settings} or null.
     */
    public @Nullable Settings getSettings() {
        return settings;
    }

    /**
     * Reloads the plugin's settings.
     */
    public void reload() {
        settings = null;

        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            skyTrials.saveResource("settings.yml", false);
        }

        @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = loader.load().get(Settings.class);

            validateSettings();
        } catch (ConfigurateException e) {
            skyTrials.getComponentLogger().warn(AdventureUtil.serialize("Failed to load the plugin's settings due to an error. Error: " + e.getMessage()));
        }
    }

    /**
     * Validates the plugin's settings.
     */
    private void validateSettings() {
        if(settings == null) return;
        ComponentLogger logger = skyTrials.getComponentLogger();

        if(settings.configVersion() == null) {
            logger.warn(AdventureUtil.serialize("Your setting's config version is outdated. Either re-generate your settings file or migrate it to version 2.0.0.0."));
            settings = null;
            return;
        }

        if(settings.locale() == null) {
            logger.warn(AdventureUtil.serialize("The locale to use is invalid."));
            settings = null;
        }
    }
}

