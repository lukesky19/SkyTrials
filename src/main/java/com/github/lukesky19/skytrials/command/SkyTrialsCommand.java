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
package com.github.lukesky19.skytrials.command;

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.configuration.loader.LocaleLoader;
import com.github.lukesky19.skytrials.configuration.record.Locale;
import com.github.lukesky19.skytrials.data.Trial;
import com.github.lukesky19.skytrials.manager.TrialManager;
import com.github.lukesky19.skytrials.util.FormatUtil;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkyTrialsCommand  implements CommandExecutor, TabExecutor {
    private final SkyTrials skyTrials;
    private final ComponentLogger logger;
    private final TrialManager trialManager;
    private final LocaleLoader localeLoader;

    public SkyTrialsCommand(SkyTrials skyTrials, TrialManager trialManager, LocaleLoader localeLoader) {
        this.skyTrials = skyTrials;
        this.logger = skyTrials.getComponentLogger();
        this.trialManager = trialManager;
        this.localeLoader = localeLoader;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Locale locale;
        if (skyTrials.isPluginDisabled()) {
            locale = localeLoader.getDefaultLocale();
        } else {
            locale = localeLoader.getLocale();
        }

        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();

            if (player.hasPermission("skytrials.command.skytrials")) {
                switch (args.length) {
                    case 1 -> {
                        switch (args[0].toLowerCase()) {
                            case "start" -> {
                                if (player.hasPermission("skytrials.command.skytrials.start")) {
                                    trialManager.playerIsReady(uuid, player);
                                    return true;
                                } else {
                                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "leave" -> {
                                if (player.hasPermission("skytrials.command.skytrials.leave")) {
                                    trialManager.playerLeavesTrial(uuid, player);
                                    return true;
                                } else {
                                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "help" -> {
                                if (player.hasPermission("skytrials.command.skytrials.help")) {
                                    for (String msg : locale.help()) {
                                        player.sendMessage(FormatUtil.format(player, msg));
                                    }
                                    return true;
                                } else {
                                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "reload" -> {
                                if (player.hasPermission("skytrials.command.skytrials.reload")) {
                                    skyTrials.reload();
                                    return true;
                                } else {
                                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            default -> {
                                player.sendMessage(FormatUtil.format(locale.prefix() + locale.unknownArgument()));
                                return false;
                            }
                        }
                    }

                    case 2 -> {
                        switch (args[0].toLowerCase()) {
                            case "join" -> {
                                if (player.hasPermission("skytrials.command.skytrials.join")) {
                                    trialManager.playerJoinsTrial(args[1], uuid, player);
                                } else {
                                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.noPermission()));
                                    return false;
                                }
                            }

                            case "cooldown" -> {
                                if (player.hasPermission("skytrials.command.skytrials.cooldown")) {
                                    String cooldownMessage = trialManager.getFormattedCooldownByTrialId(uuid, args[1].toLowerCase());

                                    if (cooldownMessage != null) {
                                        final List<TagResolver.Single> placeholders = new ArrayList<>();
                                        placeholders.add(Placeholder.parsed("trial_id", args[1].toLowerCase()));
                                        placeholders.add(Placeholder.parsed("time", cooldownMessage));
                                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialOnCooldown(), placeholders));
                                        return true;
                                    } else {
                                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidTrialId()));
                                        return false;
                                    }
                                }
                            }

                            default -> {
                                player.sendMessage(FormatUtil.format(locale.prefix() + locale.unknownArgument()));
                                return false;
                            }
                        }
                    }

                    default -> {
                        player.sendMessage(FormatUtil.format(locale.prefix() + locale.unknownArgument()));
                        return false;
                    }
                }
            } else {
                player.sendMessage(FormatUtil.format(player, locale.prefix() + locale.noPermission()));
                return false;
            }
        } else {
            switch(args[0].toLowerCase()) {
                case "help" -> {
                    for (String msg : locale.help()) {
                        logger.info(FormatUtil.format(msg));
                    }
                    return true;
                }

                case "reload" -> {
                    skyTrials.reload();
                    logger.info(FormatUtil.format(locale.reload()));
                    return true;
                }

                default -> {
                    logger.warn(FormatUtil.format(locale.unknownArgument()));
                    return false;
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> arguments = new ArrayList<>();

        if(sender instanceof Player player) {
            if (player.hasPermission("skytrials.command.skytrials")) {
                switch (args.length) {
                    case 1 -> {
                        if (player.hasPermission("skytrials.command.skytrials.join")) {
                            arguments.add("join");
                        }
                        if (player.hasPermission("skytrials.command.skytrials.start")) {
                            arguments.add("start");
                        }
                        if (player.hasPermission("skytrials.command.skytrials.leave")) {
                            arguments.add("leave");
                        }
                        if (player.hasPermission("skytrials.command.skytrials.help")) {
                            arguments.add("help");
                        }
                        if (player.hasPermission("skytrials.command.skytrials.cooldown")) {
                            arguments.add("cooldown");
                        }
                        if (player.hasPermission("skytrials.command.skytrials.reload")) {
                            arguments.add("reload");
                        }
                    }

                    case 2 -> {
                        if (player.hasPermission("skytrials.command.skytrials.join")) {
                            for (Trial trial : trialManager.getTrialsList()) {
                                arguments.add(trial.getTrialId());
                            }
                        }
                        if (player.hasPermission("skytrials.command.skytrials.cooldown")) {
                            for (Trial trial : trialManager.getTrialsList()) {
                                arguments.add(trial.getTrialId());
                            }
                        }
                    }
                }
            }
        } else {
            arguments.add("help");
            arguments.add("reload");
        }

        return arguments;
    }
}
