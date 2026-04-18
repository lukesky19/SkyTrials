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
package com.github.lukesky19.skytrials.manager.locale;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.configuration.abstracts.SimpleConfigManager;
import com.github.lukesky19.skylib.common.api.time.Time;
import com.github.lukesky19.skylib.common.api.time.TimeUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.ConfigurationNode;
import com.github.lukesky19.skylib.libs.configurate.serialize.SerializationException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.data.config.settings.Settings;
import com.github.lukesky19.skytrials.manager.settings.SettingsManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale.
 */
public class LocaleManager extends SimpleConfigManager<Locale> {
    private final @NonNull SettingsManager settingsManager;
    private final @NonNull Locale defaultLocale = new Locale(
            3,
            "<gray>[</gray><gold>SkyTrials</gold><gray>]</gray> ",
            "<yellow>The plugin has been reloaded.</yellow>",
            List.of(
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
            "<yellow>You have joined the trial <white><trial_id></white>.</yellow>",
            "<yellow>Player <white><player_name></white> has joined the trial <white><trial_id></white>.</yellow>",
            "<red>You cannot join trial <white><trial_id></white> because you have a cooldown.</red>",
            "<red>You cannot join trial <white><trial_id></white> because it has already started.</red>",
            "<red>You cannot join a trial while in a trial.</red>",
            "<yellow>You are now ready to start the trial. The trial will start once all other players are ready.</yellow>",
            "<yellow>You are no longer ready to start the trial. The trial will start once you and all other players are ready.</yellow>",
            "<yellow>Player <white><player_name></white> is now ready to start the trial.</yellow>",
            "<yellow>Player <white><player_name></white> is no longer ready to start the trial.</yellow>",
            "<yellow>Trial <white><trial_id></white> is now starting.</yellow>",
            "<yellow>Trial <white><trial_id></white> has now started!</yellow>",
            "<red>You cannot start the trial because it has already started.</red>",
            "<red>You cannot start a trial while not in a trial.</red>",
            "<yellow>You have left trial <white><trial_id></white>.</yellow>",
            "<yellow>Player <white><player_name></white> has left the trial <white><trial_id></white>.</yellow>",
            "<red>You cannot leave a trial while not in a trial!</red>",
            "<yellow>The trial <white><trial_id></white> has ended.</yellow>",
            "<yellow>You have been teleported out of the Trial due to a plugin reload! You are free to rejoin the trial with no cooldown!</yellow>",
            "<yellow>The trial you were in ended while you were offline.</yellow>",
            "<yellow>The trial <white><trial_id></white> has ended and is now available to join.</yellow>",
            "<yellow>You died in trial <white><trial_id></white>.</yellow>",
            "<yellow>Player <white><player_name></white> died in trial <white><trial_id></white>.</yellow>",
            "<yellow>You have made it to the next level in the trial. <white><current_level></white>/<white><max_level></white>.</yellow>",
            "<yellow>Trial <white><trial_id></white> is now on cooldown for <white><time></white>.</yellow>",
            "<yellow>Trial <white><trial_id></white> for player <white><player_name></white> is now on cooldown for <white><time></white>.</yellow>",
            "<yellow>Your cooldown for trial <white><trial_id></white> has been removed.</yellow>",
            "<yellow>The cooldown for trial <white><trial_id></white> and player <white><player_name></white> has been removed.</yellow>",
            "<yellow>Your cooldown for trial <white><trial_id></white> has now ended.</yellow>",
            "<yellow>Trial <white><trial_id></white> is still on cooldown for <white><time></white>.</yellow>",
            "<yellow>Player <white><player_name></white> is on cooldown for trial <white><trial_id></white>. Remaining cooldown: <white><time></white>.</yellow>",
            "<yellow>You have no cooldown for trial <white><trial_id></white>.</yellow>",
            "<yellow>Player <white><player_name></white> has no cooldown for trial <white><trial_id></white>.</yellow>",
            new Locale.TimeMessage(
                    "",
                    "<yellow><years></yellow> year(s)",
                    "<yellow><months></yellow> month(s)",
                    "<yellow><weeks></yellow> week(s)",
                    "<yellow><days></yellow> day(s)",
                    "<yellow><hours></yellow> hour(s)",
                    "<yellow><minutes></yellow> minute(s)",
                    "<yellow><seconds></yellow> second(s)",
                    "."));

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param settingsManager A {@link SettingsManager} instance.
     */
    public LocaleManager(
            @NonNull SkyTrials skyTrials,
            @NonNull SettingsManager settingsManager) {
        super(skyTrials, Locale.class);
        this.settingsManager = settingsManager;
    }

    /**
     * Get the plugin's locale or the default locale if invalid.
     * @return The plugin's locale or default locale.
     */
    @Override
    public @NonNull Locale getConfiguration() {
        if(configuration == null) return defaultLocale;
        return configuration;
    }

    @Override
    public void loadConfiguration() {
        configuration = null;

        Settings settings = settingsManager.getConfiguration();
        if(settings == null) {
            logger.error(AdventureUtility.plain("Failed to load plugin's locale due to plugin settings being null."));
            return;
        }
        if(settings.locale() == null) {
            logger.error(AdventureUtility.plain("Failed to load plugin's locale to use in settings.yml is null."));
            return;
        }

        String localeString = settings.locale();
        configurationPath = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + (localeString + ".yml"));

        YamlConfigurationLoader loader = createLoader(configurationPath);
        try {
            ConfigurationNode root = loader.load();
            int version = getVersion(root);

            Locale locale;
            switch(version) {
                case 3 -> {
                    locale = root.get(Locale.class);
                    if(locale == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 3 of locale configuration file " + (localeString + ".yml") + "."));
                        return;
                    }
                }

                case 2 -> {
                    locale = root.get(Locale.class);
                    if(locale == null) {
                        logger.warn(AdventureUtility.plain("Failed to load version 2 of locale configuration file " + (localeString + ".yml") + "."));
                        return;
                    }

                    locale = new Locale(
                            3,
                            locale.prefix(),
                            locale.reload(),
                            locale.help(),
                            locale.joinTrial(),
                            locale.playerJoinedTrial(),
                            locale.trialOnCooldown(),
                            locale.joinTrialActive(),
                            locale.joinTrialInTrial(),
                            locale.ready(),
                            locale.notReady(),
                            locale.playerIsReady(),
                            locale.playerIsNotReady(),
                            locale.startTrial(),
                            locale.broadcastTrialStart(),
                            locale.startTrialActive(),
                            locale.startTrialNotInTrial(),
                            locale.leaveTrial(),
                            locale.playerLeaveTrial(),
                            locale.leaveTrialNotInTrial(),
                            locale.trialEnd(),
                            locale.trialEndReload(),
                            locale.trialEndedWhileOffline(),
                            locale.broadcastTrialEnd(),
                            locale.diedInTrial(),
                            locale.playerDiedInTrial(),
                            locale.levelUp(),
                            locale.cooldownApplied(),
                            locale.playerCooldownApplied(),
                            locale.cooldownRemoved(),
                            locale.playerCooldownRemoved(),
                            locale.cooldownEnded(),
                            locale.cooldownTime(),
                            locale.playerCooldownTime(),
                            locale.noCooldown(),
                            locale.playerNoCooldown(),
                            locale.timeMessage());

                    saveConfiguration(locale);
                }

                case 1 -> {
                    logger.warn(AdventureUtility.plain("Unable to load locale configuration as the locale configuration must be manually migrated for version 1."));
                    return;
                }

                default -> {
                    logger.warn(AdventureUtility.plain("Unable to load locale configuration as the locale configuration has an unrecognized version. Version: " + version));
                    return;
                }
            }

            // Check if the configuration is invalid
            if(!validateConfiguration(locale)) {
                logger.warn(AdventureUtility.plain("Configuration validation failed for locale configuration file " + (localeString + ".yml") + ". Class name: " + this.getClass().getName()));
                return;
            }

            this.configuration = locale;
        } catch (ConfigurateException configurateException) {
            logger.warn(AdventureUtility.plain("Failed to load locale configuration. Error: " + configurateException.getMessage()));
        }
    }

    /**
     * Validates the plugin's locale.
     */
    @Override
    public boolean validateConfiguration(@Nullable Locale locale) {
        if(locale == null) return false;

        if(locale.version() != 3) {
            logger.warn(AdventureUtility.plain("Your locale version is outdated. Either re-generate your locale file or migrate it to version 3."));
            configuration = null;
            return false;
        }

        if(locale.prefix() == null) {
            logger.warn(AdventureUtility.plain("The prefix message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.reload() == null) {
            logger.warn(AdventureUtility.plain("The reload message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.help().isEmpty()) {
            logger.warn(AdventureUtility.plain("The help messages in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.joinTrial() == null) {
            logger.warn(AdventureUtility.plain("The join trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerJoinedTrial() == null) {
            logger.warn(AdventureUtility.plain("The player joined trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.trialOnCooldown() == null) {
            logger.warn(AdventureUtility.plain("The trial on cooldown message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.joinTrialActive() == null) {
            logger.warn(AdventureUtility.plain("The join trial active message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.joinTrialInTrial() == null) {
            logger.warn(AdventureUtility.plain("The join trial not in trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.ready() == null) {
            logger.warn(AdventureUtility.plain("The ready message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.notReady() == null) {
            logger.warn(AdventureUtility.plain("The not ready message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerIsReady() == null) {
            logger.warn(AdventureUtility.plain("The player is ready message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerIsNotReady() == null) {
            logger.warn(AdventureUtility.plain("The player is not ready message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.startTrial() == null) {
            logger.warn(AdventureUtility.plain("The start trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.broadcastTrialStart() == null) {
            logger.warn(AdventureUtility.plain("The broadcast trial start message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.startTrialActive() == null) {
            logger.warn(AdventureUtility.plain("The start trial active message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.startTrialNotInTrial() == null) {
            logger.warn(AdventureUtility.plain("The start trial not in trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerLeaveTrial() == null) {
            logger.warn(AdventureUtility.plain("The player leave trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.leaveTrialNotInTrial() == null) {
            logger.warn(AdventureUtility.plain("The leave trial not in trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.trialEnd() == null) {
            logger.warn(AdventureUtility.plain("The trial end message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.trialEndReload() == null) {
            logger.warn(AdventureUtility.plain("The trial end reload message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.trialEndedWhileOffline() == null) {
            logger.warn(AdventureUtility.plain("The trial ended while offline message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.broadcastTrialEnd() == null) {
            logger.warn(AdventureUtility.plain("The broadcast trial end message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.diedInTrial() == null) {
            logger.warn(AdventureUtility.plain("The died in trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerDiedInTrial() == null) {
            logger.warn(AdventureUtility.plain("The player died in trial message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.levelUp() == null) {
            logger.warn(AdventureUtility.plain("The level up message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.cooldownApplied() == null) {
            logger.warn(AdventureUtility.plain("The cooldown applied message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerCooldownApplied() == null) {
            logger.warn(AdventureUtility.plain("The player cooldown applied message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.cooldownRemoved() == null) {
            logger.warn(AdventureUtility.plain("The cooldown removed message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerCooldownRemoved() == null) {
            logger.warn(AdventureUtility.plain("The player cooldown removed message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.cooldownEnded() == null) {
            logger.warn(AdventureUtility.plain("The cooldown ended message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.cooldownTime() == null) {
            logger.warn(AdventureUtility.plain("The cooldown time message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerCooldownTime() == null) {
            logger.warn(AdventureUtility.plain("The player cooldown time message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.noCooldown() == null) {
            logger.warn(AdventureUtility.plain("The no cooldown message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        if(locale.playerNoCooldown() == null) {
            logger.warn(AdventureUtility.plain("The player no cooldown message in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        Locale.TimeMessage timeMessage = locale.timeMessage();
        if(timeMessage.prefix() == null
                || timeMessage.years() == null
                || timeMessage.months() == null
                || timeMessage.weeks() == null
                || timeMessage.days() == null
                || timeMessage.hours() == null
                || timeMessage.minutes() == null
                || timeMessage.seconds() == null
                || timeMessage.suffix() == null) {
            logger.warn(AdventureUtility.plain("The time placeholder config in the locale is invalid. The default locale will be used."));
            configuration = null;
            return false;
        }

        return true;
    }

    /**
     * Saves the default locales bundled with the plugin if they don't exist.
     */
    @Override
    public void saveDefaultConfiguration() {
        Path path = Path.of(plugin.getDirectoryFile() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            plugin.saveResource("locale/en_US.yml", false);
        }
    }

    /**
     * Currently no migration is needed so the locale passed is returned.
     * @param locale The {@link Locale} to migrate.
     * @return The passed {@link Locale} as no migration is needed.
     */
    @Override
    public @NonNull Locale migrateConfiguration(@NonNull Locale locale) {
        return locale;
    }

    /**
     * Gets the time message to display in the boss bar.
     * @param timeSeconds The time in seconds.
     * @return A String containing the time message.
     */
    @NonNull
    public String getTimeMessage(long timeSeconds) {
        Locale locale = this.getConfiguration();
        Time timeRecord = TimeUtil.millisToTime(timeSeconds * 1000L);

        List<TagResolver.Single> placeholders = List.of(
                Placeholder.parsed("years", String.valueOf(timeRecord.years())),
                Placeholder.parsed("months", String.valueOf(timeRecord.months())),
                Placeholder.parsed("weeks", String.valueOf(timeRecord.weeks())),
                Placeholder.parsed("days", String.valueOf(timeRecord.days())),
                Placeholder.parsed("hours", String.valueOf(timeRecord.hours())),
                Placeholder.parsed("minutes", String.valueOf(timeRecord.minutes())),
                Placeholder.parsed("seconds", String.valueOf(timeRecord.seconds())));

        StringBuilder stringBuilder = getStringBuilder(locale, timeRecord);

        return AdventureUtility.serialize(AdventureUtility.deserialize(stringBuilder.toString(), placeholders));
    }

    /**
     * Builds the string by populating any non-zero individual time units.
     * @param locale The plugin's locale
     * @param timeRecord The record containing the individual time units to display.
     * @return A populated StringBuilder. May be empty if all time units were 0 and no suffix was configured.
     */
    private @NonNull StringBuilder getStringBuilder(Locale locale, Time timeRecord) {
        Locale.TimeMessage timeMessage = locale.timeMessage();
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(timeMessage.prefix());

        boolean isFirstUnit = true;

        if(timeRecord.years() > 0) {
            stringBuilder.append(timeMessage.years());
            isFirstUnit = false;
        }

        if (timeRecord.months() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.months());
            isFirstUnit = false;
        }

        if (timeRecord.weeks() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.weeks());
            isFirstUnit = false;
        }

        if (timeRecord.days() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.days());
            isFirstUnit = false;
        }

        if (timeRecord.hours() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.hours());
            isFirstUnit = false;
        }

        if (timeRecord.minutes() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.minutes());
            isFirstUnit = false;
        }

        if (timeRecord.seconds() > 0) {
            if (!isFirstUnit) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(timeMessage.seconds());
            isFirstUnit = false;
        }

        if(isFirstUnit) {
            stringBuilder.append(timeMessage.seconds());
        }

        stringBuilder.append(timeMessage.suffix());
        return stringBuilder;
    }

    /**
     * Get the version number.
     * @param root The root {@link ConfigurationNode}.
     * @return The config version.
     */
    private int getVersion(@NonNull ConfigurationNode root) {
        ConfigurationNode versionNode = root.node("version");
        int version = versionNode.getInt();

        com.github.lukesky19.skylib.libs.configurate.ConfigurationNode legacyVersionNode = root.node("config-version");
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