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
import com.github.lukesky19.skytrials.configuration.record.PlayerData;
import com.github.lukesky19.skytrials.util.ConfigurationUtility;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.CheckForNull;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class PlayerLoader {
    private final SkyTrials skyTrials;
    private final ConfigurationUtility configurationUtility;

    public PlayerLoader(SkyTrials skyTrials, ConfigurationUtility configurationUtility) {
        this.skyTrials = skyTrials;
        this.configurationUtility = configurationUtility;
    }

    @CheckForNull
    public PlayerData loadPlayerData(@NotNull UUID uuid) {
        PlayerData playerData = null;

        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "playerdata" + File.separator + (uuid + ".yml"));
        if (path.toFile().exists()) {
            YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);

            try {
                playerData = loader.load().get(PlayerData.class);
            } catch (ConfigurateException e) {
                throw new RuntimeException(e);
            }
        }

        return playerData;
    }

    public void savePlayerData(@NotNull UUID uuid, @NotNull PlayerData playerData) {
        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "playerdata" + File.separator + (uuid + ".yml"));
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);

        CommentedConfigurationNode playerNode = loader.createNode();
        try {
            playerNode.set(playerData);
            loader.save(playerNode);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPlayerData(@NotNull UUID uuid) {
        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "playerdata" + File.separator + (uuid + ".yml"));
        if(!path.toFile().exists()) {
            YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);

            CommentedConfigurationNode playerNode = loader.createNode();
            PlayerData playerData = new PlayerData("0.1.0", new HashMap<>());
            try {
                playerNode.set(playerData);
                loader.save(playerNode);
            } catch (ConfigurateException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
