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
package com.github.lukesky19.skytrials.command.arguments;

import com.github.lukesky19.skylib.common.api.adventure.AdventureUtility;
import com.github.lukesky19.skylib.common.api.time.TimeUtil;
import com.github.lukesky19.skylib.paper.api.adventure.PaperAdventureUtility;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.github.lukesky19.skytrials.manager.player.CooldownManager;
import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class creates the cooldown command argument to manage player cooldowns for trials.
 */
public class CooldownCommand {
    private final @NonNull LocaleManager localeManager;
    private final @NonNull TrialManager trialManager;
    private final @NonNull CooldownManager cooldownManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param trialManager A {@link TrialManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     */
    public CooldownCommand(@NonNull LocaleManager localeManager, @NonNull TrialManager trialManager, @NonNull CooldownManager cooldownManager) {
        this.localeManager = localeManager;
        this.trialManager = trialManager;
        this.cooldownManager = cooldownManager;
    }

    /**
     * Creates the cooldown command argument to manage player cooldowns for trials.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the cooldown command argument.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("cooldown");

        builder.requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.cooldown") && ctx.getSender() instanceof Player);

        builder.then(Commands.argument("trial_id", StringArgumentType.string())
                .suggests((_, suggestionsBuilder) -> {
                    for(String id : trialManager.getTrialIds()) {
                        suggestionsBuilder.suggest(id);
                    }

                    return suggestionsBuilder.buildFuture();
                })

                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            Locale locale = localeManager.getConfiguration();
                            Player senderPlayer = (Player) ctx.getSource().getSender();
                            Player targetPlayer = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            UUID targetPlayerId = targetPlayer.getUniqueId();

                            String trialId = ctx.getArgument("trial_id", String.class);

                            List< TagResolver.Single> placeholders = new ArrayList<>();
                            placeholders.add(Placeholder.parsed("trial_id", trialId));
                            placeholders.add(Placeholder.parsed("player_name", targetPlayer.getName()));

                            Long cooldownTime = cooldownManager.getTrialCooldown(targetPlayerId, trialId);
                            if(cooldownTime != null) {
                                placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(cooldownTime)));

                                senderPlayer.sendMessage(PaperAdventureUtility.deserialize(targetPlayer, locale.prefix() + locale.playerCooldownTime(), placeholders));
                            } else {
                                senderPlayer.sendMessage(PaperAdventureUtility.deserialize(targetPlayer, locale.prefix() + locale.playerNoCooldown(), placeholders));
                            }

                            return 1;
                        }))

                .executes(ctx -> {
                    Locale locale = localeManager.getConfiguration();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    String trialId = ctx.getArgument("trial_id", String.class);

                    List< TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("trial_id", trialId));

                    Long cooldownTime = cooldownManager.getTrialCooldown(uuid, trialId);
                    if(cooldownTime != null) {
                        placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(cooldownTime)));

                        player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.cooldownTime(), placeholders));
                    } else {
                        player.sendMessage(PaperAdventureUtility.deserialize(player, locale.prefix() + locale.noCooldown(), placeholders));
                    }

                    return 1;
                })
        );

        builder.then(Commands.literal("add")
                .requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.cooldown.add"))
                .then(Commands.argument("trial_id", StringArgumentType.string())
                        .suggests((_, builder1) -> {
                            for(String id : trialManager.getTrialIds()) {
                                builder1.suggest(id);
                            }

                            return builder1.buildFuture();
                        })

                        .then(Commands.argument("player", ArgumentTypes.player())
                                .then(Commands.argument("time", StringArgumentType.string())
                                        .executes(ctx -> {
                                            Locale locale =  localeManager.getConfiguration();
                                            CommandSender sender = ctx.getSource().getSender();
                                            Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                            UUID uuid = target.getUniqueId();
                                            String trialId = ctx.getArgument("trial_id", String.class);
                                            String timeInput = ctx.getArgument("time", String.class);
                                            long cooldownMillis = TimeUtil.stringToMillis(timeInput);
                                            long cooldownSeconds = cooldownMillis / 1000;

                                            cooldownManager.addCooldown(uuid, trialId, cooldownSeconds);

                                            List<TagResolver.Single> placeholders = List.of(
                                                    Placeholder.parsed("trial_id", trialId),
                                                    Placeholder.parsed("player_name", target.getName()),
                                                    Placeholder.parsed("time", localeManager.getTimeMessage(cooldownSeconds)));

                                            target.sendMessage(PaperAdventureUtility.deserialize(target, locale.prefix() + locale.cooldownApplied(), placeholders));

                                            if(sender instanceof Player) {
                                                sender.sendMessage(PaperAdventureUtility.deserialize(target, locale.prefix() + locale.playerCooldownApplied(), placeholders));
                                            } else {
                                                sender.sendMessage(PaperAdventureUtility.deserialize(target, locale.playerCooldownApplied(), placeholders));
                                            }

                                            return 1;
                                        })
                                )
                        )
                )
        );

        builder.then(Commands.literal("remove")
                .requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.cooldown.remove"))
                .then(Commands.argument("trial_id", StringArgumentType.string())
                        .suggests((_, builder1) -> {
                            for(String id : trialManager.getTrialIds()) {
                                builder1.suggest(id);
                            }

                            return builder1.buildFuture();
                        })

                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> {
                                    Locale locale =  localeManager.getConfiguration();
                                    CommandSender sender = ctx.getSource().getSender();
                                    Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                    UUID uuid = target.getUniqueId();
                                    String trialId = ctx.getArgument("trial_id", String.class);

                                    List<TagResolver.Single> placeholders = List.of(
                                            Placeholder.parsed("trial_id", trialId),
                                            Placeholder.parsed("player_name", target.getName()));

                                    boolean result = cooldownManager.removeCooldown(uuid, trialId);
                                    if(result) {
                                        target.sendMessage(PaperAdventureUtility.deserialize(target, locale.prefix() + locale.cooldownRemoved(), placeholders));

                                        if(sender instanceof Player) {
                                            sender.sendMessage(PaperAdventureUtility.deserialize(target, locale.prefix() + locale.playerCooldownRemoved(), placeholders));
                                        } else {
                                            sender.sendMessage(PaperAdventureUtility.deserialize(target, locale.playerCooldownRemoved(), placeholders));
                                        }

                                        return 1;
                                    } else {
                                        if(sender instanceof Player) {
                                            sender.sendMessage(AdventureUtility.deserialize(locale.prefix() + "<red>Unable to remove trial cooldown. Player data may not exist or there is no trial for that id."));
                                        } else {
                                            sender.sendMessage(AdventureUtility.deserialize("<red>Unable to remove trial cooldown. Player data may not exist or there is no trial for that id."));
                                        }

                                        return 0;
                                    }
                                })
                        )
                )
        );

        return builder.build();
    }
}