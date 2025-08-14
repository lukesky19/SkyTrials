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
package com.github.lukesky19.skytrials.command;

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.command.arguments.*;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.github.lukesky19.skytrials.manager.player.CooldownManager;
import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;

/**
 * This class creates the SkyTrials command.
 */
public class SkyTrialsCommand{
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull LocaleManager localeManager;
    private final @NotNull TrialManager trialManager;
    private final @NotNull CooldownManager cooldownManager;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param trialManager A {@link TrialManager} instance.
     * @param localeManager A {@link LocaleManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     */
    public SkyTrialsCommand(
            @NotNull SkyTrials skyTrials,
            @NotNull TrialManager trialManager,
            @NotNull LocaleManager localeManager,
            @NotNull CooldownManager cooldownManager) {
        this.skyTrials = skyTrials;
        this.trialManager = trialManager;
        this.localeManager = localeManager;
        this.cooldownManager = cooldownManager;
    }

    /**
     * Creates the /skytrials command.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the /skytrials command.
     */
    public @NotNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skytrials")
                .requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials"));

        JoinCommand joinCommand = new JoinCommand(localeManager, trialManager);
        StartCommand startCommand = new StartCommand(localeManager, trialManager);
        LeaveCommand leaveCommand = new LeaveCommand(localeManager, trialManager);
        CooldownCommand cooldownCommand = new CooldownCommand(localeManager, trialManager, cooldownManager);
        HelpCommand helpCommand = new HelpCommand(localeManager);
        ReloadCommand reloadCommand = new ReloadCommand(skyTrials, localeManager);

        builder.then(joinCommand.createCommand());
        builder.then(startCommand.createCommand());
        builder.then(leaveCommand.createCommand());
        builder.then(cooldownCommand.createCommand());
        builder.then(helpCommand.createCommand());
        builder.then(reloadCommand.createCommand());

        return builder.build();
    }
}
