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
import com.github.lukesky19.skytrials.configuration.record.TrialConfig;
import com.github.lukesky19.skytrials.manager.TrialManager;
import com.github.lukesky19.skytrials.util.ConfigurationUtility;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TrialLoader {
    private final SkyTrials skyTrials;
    private final TrialManager trialManager;
    private final ConfigurationUtility configurationUtility;

    public TrialLoader(
            SkyTrials skyTrials,
            TrialManager trialManager,
            ConfigurationUtility configurationUtility)  {
        this.skyTrials = skyTrials;
        this.trialManager = trialManager;
        this.configurationUtility = configurationUtility;
    }

    public void reload() {
        trialManager.clearTrials();

        Path defaultPath = Path.of(skyTrials.getDataFolder() + File.separator + "trials" + File.separator + "example.yml");
        if(!defaultPath.toFile().exists()) {
            skyTrials.saveResource("trials" + File.separator + "example.yml", false);
        }

        try (Stream<Path> paths = Files.walk(Paths.get(skyTrials.getDataFolder() + File.separator + "trials"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        TrialConfig trialConfig;
                        YamlConfigurationLoader  loader = configurationUtility.getYamlConfigurationLoader(path);
                        try {
                            trialConfig = loader.load().get(TrialConfig.class);
                        } catch (ConfigurateException e) {
                            skyTrials.setPluginState(false);
                            throw new RuntimeException(e);
                        }

                        if(trialConfig != null) {
                            trialManager.createTrial(trialConfig);
                        }
                    });
        } catch (IOException e) {
            skyTrials.setPluginState(false);
            throw new RuntimeException(e);
        }
    }
}
