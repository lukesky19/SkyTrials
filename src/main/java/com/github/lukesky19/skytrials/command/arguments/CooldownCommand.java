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

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.time.TimeUtil;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class creates the cooldown command argument to manage player cooldowns for trials.
 */
public class CooldownCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull TrialManager trialManager;
    private final @NotNull CooldownManager cooldownManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param trialManager A {@link TrialManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     */
    public CooldownCommand(@NotNull LocaleManager localeManager, @NotNull TrialManager trialManager, @NotNull CooldownManager cooldownManager) {
        this.localeManager = localeManager;
        this.trialManager = trialManager;
        this.cooldownManager = cooldownManager;
    }

    /**
     * Creates the cooldown command argument to manage player cooldowns for trials.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the cooldown command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("cooldown");

        builder.requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.cooldown") && ctx.getSender() instanceof Player);

        builder.then(Commands.argument("trial_id", StringArgumentType.string())
                .suggests((context, suggestionsBuilder) -> {
                    for(String id : trialManager.getTrialIds()) {
                        suggestionsBuilder.suggest(id);
                    }

                    return suggestionsBuilder.buildFuture();
                })

                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> {
                            Locale locale = localeManager.getLocale();
                            Player senderPlayer = (Player) ctx.getSource().getSender();
                            Player targetPlayer = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                            UUID targetPlayerId = targetPlayer.getUniqueId();

                            String trialId = ctx.getArgument("trial_id", String.class);

                            List< TagResolver.Single> placeholders = new ArrayList<>();
                            placeholders.add(Placeholder.parsed("trial_id", trialId));
                            placeholders.add(Placeholder.parsed("player_name", targetPlayer.getName()));

                            @Nullable Long cooldownTime = cooldownManager.getTrialCooldown(targetPlayerId, trialId);
                            if(cooldownTime != null) {
                                placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(cooldownTime)));

                                senderPlayer.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.playerCooldownTime(), placeholders));
                            } else {
                                senderPlayer.sendMessage(AdventureUtil.serialize(targetPlayer, locale.prefix() + locale.playerNoCooldown(), placeholders));
                            }

                            return 1;
                        }))

                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    String trialId = ctx.getArgument("trial_id", String.class);

                    List< TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("trial_id", trialId));

                    @Nullable Long cooldownTime = cooldownManager.getTrialCooldown(uuid, trialId);
                    if(cooldownTime != null) {
                        placeholders.add(Placeholder.parsed("time", localeManager.getTimeMessage(cooldownTime)));

                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.cooldownTime(), placeholders));
                    } else {
                        player.sendMessage(AdventureUtil.serialize(player, locale.prefix() + locale.noCooldown(), placeholders));
                    }

                    return 1;
                })
        );

        builder.then(Commands.literal("add")
                .requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.cooldown.add"))
                .then(Commands.argument("trial_id", StringArgumentType.string())
                        .suggests((context, builder1) -> {
                            for(String id : trialManager.getTrialIds()) {
                                builder1.suggest(id);
                            }

                            return builder1.buildFuture();
                        })

                        .then(Commands.argument("player", ArgumentTypes.player())
                                .then(Commands.argument("time", StringArgumentType.string())
                                        .executes(ctx -> {
                                            Locale locale =  localeManager.getLocale();
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

                                            target.sendMessage(AdventureUtil.serialize(target, locale.prefix() + locale.cooldownApplied(), placeholders));

                                            if(sender instanceof Player) {
                                                sender.sendMessage(AdventureUtil.serialize(target, locale.prefix() + locale.playerCooldownApplied(), placeholders));
                                            } else {
                                                sender.sendMessage(AdventureUtil.serialize(target, locale.playerCooldownApplied(), placeholders));
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
                        .suggests((context, builder1) -> {
                            for(String id : trialManager.getTrialIds()) {
                                builder1.suggest(id);
                            }

                            return builder1.buildFuture();
                        })

                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> {
                                    Locale locale =  localeManager.getLocale();
                                    CommandSender sender = ctx.getSource().getSender();
                                    Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
                                    UUID uuid = target.getUniqueId();
                                    String trialId = ctx.getArgument("trial_id", String.class);

                                    List<TagResolver.Single> placeholders = List.of(
                                            Placeholder.parsed("trial_id", trialId),
                                            Placeholder.parsed("player_name", target.getName()));

                                    boolean result = cooldownManager.removeCooldown(uuid, trialId);
                                    if(result) {
                                        target.sendMessage(AdventureUtil.serialize(target, locale.prefix() + locale.cooldownRemoved(), placeholders));

                                        if(sender instanceof Player) {
                                            sender.sendMessage(AdventureUtil.serialize(target, locale.prefix() + locale.playerCooldownRemoved(), placeholders));
                                        } else {
                                            sender.sendMessage(AdventureUtil.serialize(target, locale.playerCooldownRemoved(), placeholders));
                                        }

                                        return 1;
                                    } else {
                                        if(sender instanceof Player) {
                                            sender.sendMessage(AdventureUtil.serialize(locale.prefix() + "<red>Unable to remove trial cooldown. Player data may not exist or there is no trial for that id."));
                                        } else {
                                            sender.sendMessage(AdventureUtil.serialize("<red>Unable to remove trial cooldown. Player data may not exist or there is no trial for that id."));
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
