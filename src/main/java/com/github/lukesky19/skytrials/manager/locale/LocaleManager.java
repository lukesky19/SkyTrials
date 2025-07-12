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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.configurate.ConfigurationUtility;
import com.github.lukesky19.skylib.api.time.Time;
import com.github.lukesky19.skylib.api.time.TimeUtil;
import com.github.lukesky19.skylib.libs.configurate.ConfigurateException;
import com.github.lukesky19.skylib.libs.configurate.yaml.YamlConfigurationLoader;
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.manager.settings.SettingsManager;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.data.config.settings.Settings;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * This class manages the plugin's locale.
 */
public class LocaleManager {
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull SettingsManager settingsManager;
    private @Nullable Locale locale;
    private final @NotNull Locale defaultLocale = new Locale(
            "2.0.0.0",
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
            @NotNull SkyTrials skyTrials,
            @NotNull SettingsManager settingsManager) {
        this.skyTrials = skyTrials;
        this.settingsManager = settingsManager;
    }

    /**
     * Get the plugin's locale or the default locale if invalid.
     * @return The plugin's locale or default locale.
     */
    public @NotNull Locale getLocale() {
        if(locale == null) return defaultLocale;
        return locale;
    }

    /**
     * Reload the plugin's locale.
     */
    public void reload() {
        locale = null;
        Settings settings = settingsManager.getSettings();

        saveDefaultLocales();

        if(settings != null) {
            if(settings.locale() != null) {
                Path path = Path.of(skyTrials.getDataFolder() + File.separator + "locale" + File.separator + (settings.locale() + ".yml"));
                @NotNull YamlConfigurationLoader loader = ConfigurationUtility.getYamlConfigurationLoader(path);

                try {
                    locale = loader.load().get(Locale.class);

                    validateLocale();
                } catch (ConfigurateException e) {
                    skyTrials.getComponentLogger().warn(AdventureUtil.serialize("Failed to load the plugin's locale and the default locale will be used. Error: " + e.getMessage()));
                }
            }
        }
    }

    /**
     * Validates the plugin's locale.
     */
    private void validateLocale() {
        if(locale == null) return;
        ComponentLogger logger = skyTrials.getComponentLogger();

        if(locale.configVersion() == null) {
            logger.warn(AdventureUtil.serialize("Your locale version is outdated. Either re-generate your locale file or migrate it to version 2.0.0.0."));
            locale = null;
            return;
        }

        if(locale.prefix() == null) {
            logger.warn(AdventureUtil.serialize("The prefix message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.reload() == null) {
            logger.warn(AdventureUtil.serialize("The reload message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.help().isEmpty()) {
            logger.warn(AdventureUtil.serialize("The help messages in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.joinTrial() == null) {
            logger.warn(AdventureUtil.serialize("The join trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerJoinedTrial() == null) {
            logger.warn(AdventureUtil.serialize("The player joined trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.trialOnCooldown() == null) {
            logger.warn(AdventureUtil.serialize("The trial on cooldown message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.joinTrialActive() == null) {
            logger.warn(AdventureUtil.serialize("The join trial active message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.joinTrialInTrial() == null) {
            logger.warn(AdventureUtil.serialize("The join trial not in trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.ready() == null) {
            logger.warn(AdventureUtil.serialize("The ready message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.notReady() == null) {
            logger.warn(AdventureUtil.serialize("The not ready message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerIsReady() == null) {
            logger.warn(AdventureUtil.serialize("The player is ready message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerIsNotReady() == null) {
            logger.warn(AdventureUtil.serialize("The player is not ready message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.startTrial() == null) {
            logger.warn(AdventureUtil.serialize("The start trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.broadcastTrialStart() == null) {
            logger.warn(AdventureUtil.serialize("The broadcast trial start message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.startTrialActive() == null) {
            logger.warn(AdventureUtil.serialize("The start trial active message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.startTrialNotInTrial() == null) {
            logger.warn(AdventureUtil.serialize("The start trial not in trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerLeaveTrial() == null) {
            logger.warn(AdventureUtil.serialize("The player leave trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.leaveTrialNotInTrial() == null) {
            logger.warn(AdventureUtil.serialize("The leave trial not in trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.trialEnd() == null) {
            logger.warn(AdventureUtil.serialize("The trial end message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.trialEndReload() == null) {
            logger.warn(AdventureUtil.serialize("The trial end reload message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.trialEndedWhileOffline() == null) {
            logger.warn(AdventureUtil.serialize("The trial ended while offline message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.broadcastTrialEnd() == null) {
            logger.warn(AdventureUtil.serialize("The broadcast trial end message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.diedInTrial() == null) {
            logger.warn(AdventureUtil.serialize("The died in trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerDiedInTrial() == null) {
            logger.warn(AdventureUtil.serialize("The player died in trial message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.levelUp() == null) {
            logger.warn(AdventureUtil.serialize("The level up message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.cooldownApplied() == null) {
            logger.warn(AdventureUtil.serialize("The cooldown applied message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerCooldownApplied() == null) {
            logger.warn(AdventureUtil.serialize("The player cooldown applied message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.cooldownRemoved() == null) {
            logger.warn(AdventureUtil.serialize("The cooldown removed message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerCooldownRemoved() == null) {
            logger.warn(AdventureUtil.serialize("The player cooldown removed message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.cooldownEnded() == null) {
            logger.warn(AdventureUtil.serialize("The cooldown ended message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.cooldownTime() == null) {
            logger.warn(AdventureUtil.serialize("The cooldown time message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerCooldownTime() == null) {
            logger.warn(AdventureUtil.serialize("The player cooldown time message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.noCooldown() == null) {
            logger.warn(AdventureUtil.serialize("The no cooldown message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
        }

        if(locale.playerNoCooldown() == null) {
            logger.warn(AdventureUtil.serialize("The player no cooldown message in the locale is invalid. The default locale will be used."));
            locale = null;
            return;
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
            logger.warn(AdventureUtil.serialize("The time placeholder config in the locale is invalid. The default locale will be used."));
            locale = null;
        }
    }

    /**
     * Saves the default locales bundled with the plugin if they don't exist.
     */
    private void saveDefaultLocales() {
        Path path = Path.of(skyTrials.getDataFolder() + File.separator + "locale" + File.separator + "en_US.yml");
        if(!path.toFile().exists()) {
            skyTrials.saveResource("locale/en_US.yml", false);
        }
    }

    /**
     * Gets the time message to display in the boss bar.
     * @param timeSeconds The time in seconds.
     * @return A String containing the time message.
     */
    @NotNull
    public String getTimeMessage(long timeSeconds) {
        Locale locale = this.getLocale();
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

        return MiniMessage.miniMessage().serialize(AdventureUtil.serialize(stringBuilder.toString(), placeholders));
    }

    /**
     * Builds the string by populating any non-zero individual time units.
     * @param locale The plugin's locale
     * @param timeRecord The record containing the individual time units to display.
     * @return A populated StringBuilder. May be empty if all time units were 0 and no suffix was configured.
     */
    private @NotNull StringBuilder getStringBuilder(Locale locale, Time timeRecord) {
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
}