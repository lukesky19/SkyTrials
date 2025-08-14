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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This class creates the start command argument to mark a player ready to start a trial.
 */
public class StartCommand {
    private final @NotNull LocaleManager localeManager;
    private final @NotNull TrialManager trialManager;

    /**
     * Constructor
     * @param localeManager A {@link LocaleManager} instance.
     * @param trialManager A {@link TrialManager} instance.
     */
    public StartCommand(@NotNull LocaleManager localeManager, @NotNull TrialManager trialManager) {
        this.localeManager = localeManager;
        this.trialManager = trialManager;
    }

    /**
     * Creates the start command argument to mark a player ready to start a trial.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the start command argument.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("start");

        builder.requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.start") && ctx.getSender() instanceof Player);

        builder.executes(ctx -> {
            Locale locale = localeManager.getLocale();
            Player player = (Player) ctx.getSource().getSender();
            UUID uuid = player.getUniqueId();

            AbstractTrial trial = trialManager.getTrialByPlayerUUID(uuid);
            if(trial == null) {
                player.sendMessage(AdventureUtil.serialize(locale.prefix() + locale.startTrialNotInTrial()));
                return 0;
            }

            trial.togglePlayerStatus(player, uuid);
            return 1;
        });

        return builder.build();
    }
}
