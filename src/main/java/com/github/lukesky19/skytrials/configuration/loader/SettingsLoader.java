/*
    SkyTrials is a mob arena plugin inspired by the Minecraft 1.21 Trial Chambers using Trial Spawners and Vault blocks.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skytrials.configuration.loader;

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.configuration.record.Settings;
import com.github.lukesky19.skytrials.util.ConfigurationUtility;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

public class SettingsLoader {
    private final SkyTrials plugin;
    private final ConfigurationUtility configurationUtility;
    Settings settings;

    public SettingsLoader(SkyTrials plugin, ConfigurationUtility configurationUtility) {
        this.plugin = plugin;
        this.configurationUtility = configurationUtility;
    }

    public Settings getSettings() {
        return settings;
    }

    public void reload() {
        settings = null;

        Path path = Path.of(plugin.getDataFolder() + File.separator + "settings.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("settings.yml", false);
        }

        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);
        try {
            settings = loader.load().get(Settings.class);
        } catch (ConfigurateException e) {
            plugin.setPluginState(false);
            throw new RuntimeException(e);
        }
    }
}

