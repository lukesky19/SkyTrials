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
import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.locale.Locale;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jspecify.annotations.NonNull;

/**
 * This class creates the reload command argument to reload the plugin.
 */
public class ReloadCommand {
    private final @NonNull SkyTrials skyTrials;
    private final @NonNull LocaleManager localeManager;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param localeManager A {@link LocaleManager} instance.
     */
    public ReloadCommand(@NonNull SkyTrials skyTrials, @NonNull LocaleManager localeManager) {
        this.skyTrials = skyTrials;
        this.localeManager = localeManager;
    }

    /**
     * Creates the reload command argument to reload the plugin.
     * @return A {@link LiteralCommandNode} of type {@link CommandSourceStack} for the reload command argument.
     */
    public @NonNull LiteralCommandNode<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("reload");

        builder.requires(ctx -> ctx.getSender().hasPermission("skytrials.commands.skytrials.reload"));

        builder.executes(ctx -> {
            Locale locale = localeManager.getConfiguration();

            skyTrials.reload();

            ctx.getSource().getSender().sendMessage(AdventureUtility.deserialize(locale.prefix() + locale.reload()));

            return 1;
        });

        return builder.build();
    }
}
