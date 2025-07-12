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
package com.github.lukesky19.skytrials.listener;

import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import com.github.lukesky19.skytrials.trial.AbstractTrial;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class listens for when a player joins the server and passes the event to any trials their join location is on.
 */
public class LoginListener implements Listener {
    private final @NotNull TrialManager trialManager;

    /**
     * Constructor
     * @param trialManager A {@link TrialManager} instance.
     */
    public LoginListener(@NotNull TrialManager trialManager) {
        this.trialManager = trialManager;
    }

    /**
     * Listen to the join event and pass the event to any trial the player's location is in.
     * @param playerJoinEvent A {@link PlayerJoinEvent}
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent playerJoinEvent) {
        Player player = playerJoinEvent.getPlayer();

        AbstractTrial trial = trialManager.getTrialByLocation(player.getLocation());
        if(trial != null) {
            trial.handlePlayerJoinEvent(playerJoinEvent);
        }
    }
}
