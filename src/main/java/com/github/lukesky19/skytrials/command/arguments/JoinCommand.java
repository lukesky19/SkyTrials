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
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import com.github.lukesky19.skytrials.trial.AbstractTrial;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class creates the join command argument to join a trial.
 */
public class JoinCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull TrialManager trialManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param trialManager A {@link TrialManager} instance.
     */
    public JoinCommand(@NotNull LocaleManager localeManager, @NotNull TrialManager trialManager) {
        this.localeManager = localeManager;
        this.trialManager = trialManager;
    }

    /**
     * Creates the join command argument to join a trial.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the join command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("join");

        builder.requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.join") && ctx.getSender() instanceof Player);

        builder.then(Commands.argument("trial_id", StringArgumentType.string())
                .suggests((context, builder1) -> {
                    for(String id : trialManager.getTrialIds()) {
                        builder1.suggest(id);
                    }

                    return builder1.buildFuture();
                })

                .executes(ctx -> {
                    Locale locale = localeManager.getLocale();
                    Player player = (Player) ctx.getSource().getSender();
                    UUID uuid = player.getUniqueId();
                    String trialId = ctx.getArgument("trial_id", String.class);

                    if(trialManager.isPlayerInTrial(uuid)) {
                        player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.joinTrialInTrial()));
                        return 0;
                    }

                    AbstractTrial trial = trialManager.getTrialById(trialId);
                    if(trial == null) return 0;

                    trial.join(player, uuid);
                    return 1;
                })
        );

        return builder.build();
    }
}
