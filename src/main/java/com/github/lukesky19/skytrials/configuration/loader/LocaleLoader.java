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
import com.github.lukesky19.skytrials.configuration.record.Locale;
import com.github.lukesky19.skytrials.util.ConfigurationUtility;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

public class LocaleLoader {
    private final SkyTrials skyTrials;
    private final ConfigurationUtility configurationUtility;
    private final SettingsLoader settingsLoader;
    private final ComponentLogger logger;
    private Locale locale;
    private final Locale defaultLocale = new Locale(
            "1.0.0",
            "<gray>[</gray><gold>SkyTrials</gold><gray>]</gray> ",
            "<yellow>The plugin has been reloaded.</yellow>",
            "<yellow>You have been teleported out of the Trial due to a plugin reload! You are free to rejoin the trial with no cooldown!</yellow>",
            Arrays.asList(
                    "<aqua>SkyTrials is developed by <white><bold>lukeskywlker19</bold></white>.</aqua>",
                    "<aqua>Source code is released on GitHub: <click:OPEN_URL:https://github.com/lukesky19><yellow><underlined><bold>https://github.com/lukesky19</bold></underlined></yellow></click>",
                    " ",
                    "<aqua><bold>List of Commands:</bold></aqua>",
                    "<white>/</white><aqua>skytrials</aqua> <yellow>help</yellow>",
                    "<white>/</white><aqua>skytrials</aqua> <yellow>reload</yellow>",
                    "<white>/</white><aqua>skytrials</aqua> <yellow>join</yellow> <red><trial_name></red>",
                    "<white>/</white><aqua>skytrials</aqua> <yellow>start</yellow>",
                    "<white>/</white><aqua>skytrials</aqua> <yellow>leave</yellow>",
                    "<white>/</white><aqua>skytrials</aqua> <yellow>cooldown</yellow> <red><trial_name></red>"),
            "<red>You do not have permission for this command or sub-command.</red>",
            "<red>Unknown argument. Double-check your command.</red>",
            "<red>This command is only available in-game.</red>",
            "<red>A world for <trial_id> is invalid.</red>",
            "<red>The region for <trial_id> is invalid.</red>",
            "<red>That is not a valid trial name!</red>",
            "<red>The world for <aqua><block_id></aqua> is invalid for <aqua><trial_id></aqua>.</red>",
            "<red>Player <aqua><player_name></aqua>'s data is invalid.</red>",
            "<red>Player <aqua><uuid></aqua>'s data is invalid.</red>",
            "<red>You cannot join a trial while in a trial!</red>",
            "<red>You are currently on cooldown for this Trial!</red>",
            "<red>This trial is currently active, try again later.</red>",
            "<yellow>You have joined the trial!</yellow>",
            "<yellow><aqua><player_name></aqua> has joined trial <aqua><trial_id></aqua>.</yellow>",
            "<aqua>You are now ready to enter the trial.</aqua>",
            "<aqua>You are now NOT ready to enter the trial.</aqua>",
            "<aqua>Player <yellow><player_name></yellow> is now ready to start the trial.</aqua>",
            "<aqua>Player <yellow><player_name></yellow> is now NOT ready to start the trial.</aqua>",
            "<aqua>You will be teleported once all other players are ready.</aqua>",
            "<red>You will be teleported once you and all other players are ready.</red>",
            "<red>You cannot ready up for a trial while not in a trial!</red>",
            "<red>You cannot start a trial while not in a trial!</red>",
            "<red>The trial is now starting! Have fun!</red>",
            "<yellow>Trial <aqua><trial_id></aqua> has now started.",
            "<red>You cannot leave a trial while not in a trial!</red>",
            "<yellow>You have left the trial.</yellow>",
            "<yellow>The trial you have left is now on cooldown until <time>.</yellow>",
            "<yellow>You have died! The trial you died in is now on cooldown until <time>.</yellow>",
            "<red>Remaining time: <time>.</red>",
            "<aqua>You can now access <trial_id> again.</aqua>",
            "<red>The trial you were in ended while you were offline.</red>",
            "<yellow>The time limit for this trial has been met! Thanks for playing!</yellow>",
            "<red>Trial <aqua><trial_id></aqua> is on cooldown until <aqua><time></aqua>.</red>",
            "<green>Trial <aqua><trial_id></aqua> is not on cooldown!</green>");

    public LocaleLoader(
            SkyTrials skyTrials,
            ConfigurationUtility configurationUtility,
            SettingsLoader settingsLoader) {
        this.skyTrials = skyTrials;
        this.logger = skyTrials.getComponentLogger();
        this.configurationUtility = configurationUtility;
        this.settingsLoader = settingsLoader;
    }

    public Locale getLocale() {
        return locale;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void reload() {
        locale = null;

        if(skyTrials.isPluginDisabled()) {
            logger.error(MiniMessage.miniMessage().deserialize("<red>The locale config cannot be loaded due to a previous plugin error.</red>"));
            logger.error(MiniMessage.miniMessage().deserialize("<red>Please check your server's console.</red>"));
            return;
        }

        String localeName = settingsLoader.getSettings().locale();

        saveDefaultLocales();

        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "locale" + File.separator + (localeName + ".yml"));
        YamlConfigurationLoader loader = configurationUtility.getYamlConfigurationLoader(path);

        try {
            locale = loader.load().get(Locale.class);
        } catch (ConfigurateException e) {
            skyTrials.setPluginState(false);
            throw new RuntimeException(e);
        }
    }

    private void saveDefaultLocales() {
        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            skyTrials.saveResource("locale/en_US.yml", false);
        }
    }
}
